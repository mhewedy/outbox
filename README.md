## Outbox

A simple library to implement Outbox microservice pattern in Java using DB Polling.

Read this medium Post to know about what problem Outbox Pattern solves https://link.medium.com/p1FnyAMjWxb
### Usage:

first, make sure to reate table `outbox_messages` from the file `schema.sql`

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
    
        @Outbox  // <---- You put the annotation on the method that communicate with external system (queue, another service, etc.)
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
   
        @Transactional // <---- Now this can run in a single Transaction (hence both invoked methods will write to the database)
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

> See the `UserController` in the example project.

### Handling Failures:

Sometimes, the Scheduler failed to successfully invoke the target method, maybe due to some network issue etc.
So to overcome such issues, You can call a method in `OutboxService` called `setPending`, which will make sure
that the status of the failed message is eligibility to be rescheduled by the scheduler.

> See the `AdminController` in the example project.

