package com.example.Backend.controller;

import com.example.Backend.dto.request.AiChatRequest;
import com.example.Backend.dto.response.AiChatResponse;
import com.example.Backend.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        String reply = aiChatService.chat(request.getMessage());
        return ResponseEntity.ok(new AiChatResponse(reply));
    }
}
