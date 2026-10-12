package com.smarthospital.controller;

import com.smarthospital.ai.AiAssistantService;
import com.smarthospital.common.Result;
import com.smarthospital.common.UserContext;
import com.smarthospital.dto.Dtos.ChatReq;
import com.smarthospital.dto.Dtos.ChatResult;
import com.smarthospital.dto.Dtos.SummaryReq;
import com.smarthospital.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService aiAssistantService;

    /** AI 导诊流式对话（SSE） */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody @Valid ChatReq req) {
        UserContext.get(); // 需登录
        return aiAssistantService.chatStream(req.sessionId(), req.message());
    }

    /** AI 导诊非流式对话（返回回复 + 推荐科室） */
    @PostMapping("/chat")
    public Result<ChatResult> chat(@RequestBody @Valid ChatReq req) {
        UserContext.get();
        return Result.ok(aiAssistantService.chat(req.sessionId(), req.message()));
    }

    /** 病历摘要（医生） */
    @PostMapping("/record-summary")
    public Result<String> recordSummary(@RequestBody @Valid SummaryReq req) {
        UserContext.requireRole(User.Role.DOCTOR);
        String summary = aiAssistantService.summarizeRecord(req.chiefComplaint(), req.diagnosis(), req.advice());
        return Result.ok(summary);
    }
}
