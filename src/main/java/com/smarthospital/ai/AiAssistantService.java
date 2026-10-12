package com.smarthospital.ai;

import com.smarthospital.dto.Dtos.ChatResult;
import com.smarthospital.dto.Dtos.DeptSuggest;
import com.smarthospital.entity.Department;
import com.smarthospital.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** AI 助手服务：多轮记忆的流式导诊对话 + 病历摘要生成 */
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private static final int MAX_HISTORY = 20;

    private final ChatClient chatClient;
    private final HospitalTools hospitalTools;
    private final DepartmentRepository departmentRepository;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    private final Map<String, List<Message>> sessions = new ConcurrentHashMap<>();

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.contains("your-deepseek");
    }

    /** 非流式导诊对话：返回回复文本 + 推荐科室（未配置 AI 时走关键词兜底） */
    public ChatResult chat(String sessionId, String userMessage) {
        List<DeptSuggest> suggested = suggestDepartments(userMessage);
        if (!isConfigured()) {
            String reply = buildFallbackReply(userMessage, suggested);
            return new ChatResult(reply, suggested);
        }
        List<Message> history = sessions.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()));
        List<Message> snapshot;
        synchronized (history) {
            snapshot = new ArrayList<>(history);
        }
        try {
            String content = chatClient.prompt()
                    .messages(snapshot)
                    .user(userMessage)
                    .tools(hospitalTools)
                    .call()
                    .content();
            String reply = (content == null || content.isBlank()) ? "抱歉，我暂时无法回答，请稍后再试。" : content.trim();
            synchronized (history) {
                history.add(new UserMessage(userMessage));
                history.add(new AssistantMessage(reply));
                while (history.size() > MAX_HISTORY) history.remove(0);
            }
            return new ChatResult(reply, suggested);
        } catch (Exception e) {
            return new ChatResult(buildFallbackReply(userMessage, suggested), suggested);
        }
    }

    /** 基于症状关键词推荐科室 */
    private List<DeptSuggest> suggestDepartments(String message) {
        String msg = (message == null ? "" : message).toLowerCase();
        List<Department> all = departmentRepository.findAllByOrderByIdAsc();
        List<DeptSuggest> result = new ArrayList<>();
        String[][] rules = {
            {"内", "内科", "呼吸", "咳嗽", "咳痰", "胸闷", "气喘", "感冒", "发烧", "发热", "肺炎", "哮喘"},
            {"心", "心内科", "心脏", "胸痛", "心悸", "心慌", "高血压", "血压", "冠心病", "心律"},
            {"神", "神经内科", "神经", "头痛", "头晕", "眩晕", "失眠", "癫痫", "中风", "脑梗"},
            {"消化", "消化内科", "胃痛", "腹痛", "腹泻", "便秘", "恶心", "呕吐", "反酸", "胃炎", "肝"},
            {"儿", "儿科", "小孩", "宝宝", "儿童", "婴儿"},
            {"妇", "妇产科", "妇科", "产科", "月经", "怀孕", "妊娠"},
            {"眼", "眼科", "眼睛", "视力", "近视", "红眼"},
            {"口腔", "口腔科", "牙齿", "牙疼", "牙龈"},
            {"皮肤", "皮肤科", "皮疹", "瘙痒", "过敏", "湿疹", "痘痘"},
            {"骨", "骨科", "关节", "腰痛", "腿疼", "骨折", "颈椎"},
            {"泌尿", "泌尿外科", "泌尿", "肾", "尿频", "尿急"},
            {"内分泌", "内分泌", "糖尿病", "甲状腺", "甲亢"},
        };
        for (String[] rule : rules) {
            for (int i = 1; i < rule.length; i++) {
                if (msg.contains(rule[i])) {
                    String target = rule[0];
                    all.stream()
                            .filter(d -> d.getName().contains(target) || target.contains(d.getName()))
                            .findFirst()
                            .ifPresent(d -> {
                                if (result.stream().noneMatch(s -> s.id().equals(d.getId()))) {
                                    result.add(new DeptSuggest(d.getId(), d.getName()));
                                }
                            });
                    break;
                }
            }
            if (result.size() >= 3) break;
        }
        return result;
    }

    private String buildFallbackReply(String message, List<DeptSuggest> suggested) {
        StringBuilder sb = new StringBuilder("根据您描述的症状，");
        if (suggested.isEmpty()) {
            sb.append("建议您前往内科或全科门诊就诊，医生会为您做进一步检查。");
        } else {
            sb.append("推荐您前往以下科室就诊：");
            for (int i = 0; i < suggested.size(); i++) {
                if (i > 0) sb.append("、");
                sb.append(suggested.get(i).name());
            }
            sb.append("。");
        }
        sb.append("\n\n温馨提示：以上仅为初步建议，具体诊断请以医生面诊为准。如症状紧急，请立即拨打 120。");
        return sb.toString();
    }

    /** 流式导诊对话（携带多轮上下文 + 工具调用） */
    public Flux<String> chatStream(String sessionId, String userMessage) {
        if (!isConfigured()) {
            return Flux.just("【系统提示】AI 服务尚未配置，请在 application.yml 中填入 DeepSeek API Key"
                    + "（或设置环境变量 DEEPSEEK_API_KEY）后重启服务。系统其余功能不受影响。");
        }
        List<Message> history = sessions.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()));
        List<Message> snapshot;
        synchronized (history) {
            snapshot = new ArrayList<>(history);
        }
        StringBuilder reply = new StringBuilder();
        return chatClient.prompt()
                .messages(snapshot)
                .user(userMessage)
                .tools(hospitalTools)
                .stream()
                .content()
                .doOnNext(reply::append)
                .doOnComplete(() -> {
                    synchronized (history) {
                        history.add(new UserMessage(userMessage));
                        history.add(new AssistantMessage(reply.toString()));
                        while (history.size() > MAX_HISTORY) {
                            history.remove(0);
                        }
                    }
                })
                .onErrorResume(e -> Flux.just("【系统提示】AI 服务暂时不可用，请检查 DeepSeek API Key 配置后重试。"));
    }

    /** 为病历生成面向患者的通俗摘要，AI 不可用时返回 null */
    public String summarizeRecord(String chiefComplaint, String diagnosis, String advice) {
        if (!isConfigured()) {
            return null;
        }
        String prompt = """
                请根据以下门诊病历信息，为患者生成一段100字以内的通俗就诊摘要，内容涵盖：本次就诊结论、注意事项、复诊建议。直接输出摘要正文，不要多余客套。
                主诉：%s
                诊断：%s
                医嘱：%s
                """.formatted(chiefComplaint, diagnosis, advice == null || advice.isBlank() ? "无" : advice);
        try {
            String content = chatClient.prompt().user(prompt).call().content();
            return content == null || content.isBlank() ? null : content.trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
