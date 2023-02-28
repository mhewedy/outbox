package com.github.mhewedy.outboxexample;

import com.github.mhewedy.outbox.Outbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UseService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }

    @Outbox
    public void syncUser(UserEntity user) {
        Map<?, ?> map = restTemplate.postForObject("https://gorest.co.in/public/v2/users",
                user, Map.class);

        log.info("response from api: {}", map);
    }
}
