## Outbox

A simple library to implement Outbox microservice pattern in Java using DB Polling.

### Usage:

0. Create table `outbox_messages` from the file `schema.sql`

1. Annotate your method that contains code that contains network calls with `@Outbox`:
    ```java
   
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
            Map<?, ?> map = restTemplate.postForObject("https://gorest.co.in/public/v2/users", user, Map.class);
            log.info("response from api: {}", map);
        }
    }
    ```
2. Call this method regularly as if you call any regular spring service:
    ```java 
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
    ```
   The call to `useService.syncUser()` method will return immediately, saved to `outbox_messages` table, 
   and then a background process will make sure that method is being called (will try to execute the method and log the 
   exception to the database in case of error happens).