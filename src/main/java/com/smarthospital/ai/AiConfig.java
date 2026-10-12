package com.smarthospital.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    public static final String SYSTEM_PROMPT = """
            你是"智慧医院智能导诊助手"，名字叫小慧。职责与要求：
            1. 通过友好的对话收集患者症状、持续时间、严重程度等关键信息；
            2. 根据症状判断并推荐最合适的科室，简要说明理由；
            3. 你可以调用工具查询医院真实的科室与医生排班数据，主动告知患者可预约的医生和余号；
            4. 出现胸痛、呼吸困难、意识障碍、大出血等急危重症时，立即建议拨打120或前往急诊科；
            5. 不做确诊、不推荐具体药物和剂量，仅提供就医建议；
            6. 全程使用简体中文，语气亲切专业，回答简洁（200字以内）；
            7. 当给出科室建议时，必须在最后单独一行输出：【推荐科室】科室名
            """;

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
}
