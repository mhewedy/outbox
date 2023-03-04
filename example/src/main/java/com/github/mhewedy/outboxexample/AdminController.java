package com.github.mhewedy.outboxexample;

import com.github.mhewedy.outbox.OutboxDto;
import com.github.mhewedy.outbox.OutboxService;
import com.github.mhewedy.outbox.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OutboxService outboxService;

    @GetMapping("/failed-messages")
    public List<OutboxDto> listFailedMessages() {
        return outboxService.listByStatus(Status.FAIL);
    }

    @PostMapping("/messages/{id}/pending")
    public Map<String, Object> setPending(@PathVariable("id") String id) {
        boolean success = outboxService.setPending(id);
        return Map.of("success", success);
    }
}
