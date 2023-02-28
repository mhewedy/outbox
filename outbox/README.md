## Outbox

A simple library to implement Outbox microservice pattern in Java using DB Polling.

### Usage:

1. Annotate your method that contains code that contains network calls with `@Outbox`:
    ```java
    @Slf4j
    @Service
    public class CalculatorService {
    
        @Outbox
        public void add(Integer a1, Point p) {
            // code that contains network 
            // (call other microservice, send a message to queue, etc ...)
            // ...
            log.info("result of add function: {}", a1 + p.x + p.y);
        }
        
        // .....
    }
    ```
2. Call this method regulary as if you call any regular spring service:
    ```java 
    calculatorService.add(1, new CalculatorService.Point(2, 3));
    ```
   The call will return immediately, saved to `outbox_messages` table, and then a background process will make sure
   that method is being called (will try to execute the method and log the exception to the database in case of error
   happens).