package com.github.mhewedy.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OutboxEntity {

    private static final String PARM_TYPES_SEP = ",";
    private static final String PARAM_VALUES_SEP = "__,,__";

    public static final RowMapper<OutboxEntity> ROW_MAPPER = new OutboxMapper();

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

    public enum Status {PENDING, LOCKED, SUCCESS, FAIL}

    public static OutboxEntity create(ObjectMapper objectMapper, Method method, List<Object> args) {
        var entity = new OutboxEntity();
        entity.serviceClass = method.getDeclaringClass().getName();
        entity.methodName = method.getName();
        entity.paramTypes = Arrays.stream(method.getParameterTypes())
                .map(Class::getName).collect(Collectors.joining(PARM_TYPES_SEP));
        entity.paramValues = args.stream()
                .map(it -> writeValueAsString(objectMapper, it)).collect(Collectors.joining(PARAM_VALUES_SEP));
        entity.createdDate = Instant.now();
        entity.status = Status.PENDING;
        return entity;
    }

    public Class<?> getServiceClass() {
        return forName(this.serviceClass);
    }

    @SneakyThrows
    public Method getMethod() {
        Class<?>[] paramTypes = this.parseParamTypes();
        return getServiceClass().getDeclaredMethod(this.methodName, paramTypes);
    }

    @SneakyThrows
    public Object[] parseParamValues(ObjectMapper objectMapper) {
        Class<?>[] paramTypes = this.parseParamTypes();
        String[] paramValues = this.paramValues.split(PARAM_VALUES_SEP);
        Object[] objects = new Object[paramValues.length];

        for (int i = 0; i < paramValues.length; i++) {
            objects[i] = objectMapper.readValue(paramValues[i], paramTypes[i]);
        }
        return objects;
    }

    @SneakyThrows
    private static String writeValueAsString(ObjectMapper objectMapper, Object o) {
        return objectMapper.writeValueAsString(o);
    }

    private Class<?>[] parseParamTypes() {
        return Arrays.stream(this.paramTypes.split(PARM_TYPES_SEP))
                .map(this::forName)
                .toArray(Class<?>[]::new);
    }

    @SneakyThrows
    private Class<?> forName(String className) {
        return Class.forName(className);
    }

    private static class OutboxMapper implements RowMapper<OutboxEntity> {

        @Override
        public OutboxEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            var entity = new OutboxEntity();
            entity.id = rs.getString("id");
            entity.serviceClass = rs.getString("service_class");
            entity.methodName = rs.getString("method_name");
            entity.paramTypes = rs.getString("param_types");
            entity.paramValues = rs.getString("param_values");
            entity.lockId = rs.getString("lock_id");
            entity.status = Status.values()[rs.getInt("status")];
            entity.errorMessage = rs.getString("error_message");
            entity.createdDate = rs.getTimestamp("created_date").toInstant();
            entity.modifiedDate = Optional.ofNullable(rs.getTimestamp("modified_date")).map(Timestamp::toInstant).orElse(null);
            return entity;
        }
    }
}
