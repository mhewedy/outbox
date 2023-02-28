package com.github.mhewedy.outbox;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    public int prefetchCount = 1;
    public int schedulerFixedRate = 1000;
}
