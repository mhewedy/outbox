package com.github.mhewedy.outboxexample;

import com.github.mhewedy.outbox.Outbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
        var headers = new HttpHeaders();
        headers.setBearerAuth("0380d38aded226492239036105b0bbe5e0c6b21d71e521151e301065b0abcb59");
        var httpEntity = new HttpEntity<>(user, headers);

        ResponseEntity<?> response = restTemplate.exchange("https://gorest.co.in/public/v2/users",
                HttpMethod.POST, httpEntity, Map.class);

        log.info("response from api: {}", response);
    }
}
