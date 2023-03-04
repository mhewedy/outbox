package com.github.mhewedy.outbox;

import java.time.Instant;

public class OutboxDto {

    public String id;
    public String serviceClass;
    public String methodName;
    public String paramTypes;
    public String paramValues;
    public String lockId;
    public Status status;
    public String errorMessage;
    public Instant createdDate;
    public Instant modifiedDate;

    public static OutboxDto fromEntity(OutboxEntity entity) {
        var dto = new OutboxDto();
        dto.id = entity.id;
        dto.serviceClass = entity.serviceClass;
        dto.methodName = entity.methodName;
        dto.paramTypes = entity.paramTypes;
        dto.paramValues = entity.paramValues;
        dto.lockId = entity.lockId;
        dto.status = entity.status;
        dto.errorMessage = entity.errorMessage;
        dto.createdDate = entity.createdDate;
        dto.modifiedDate = entity.modifiedDate;
        return dto;
    }
}
