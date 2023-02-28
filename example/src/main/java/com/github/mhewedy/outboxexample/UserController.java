package com.github.mhewedy.outboxexample;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UseService useService;

    @Transactional
    @PostMapping("/users")
    public void send(@RequestBody UserEntity user) {
        useService.saveUser(user);
        useService.syncUser(user);
    }
}
