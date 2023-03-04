package com.github.mhewedy.outbox;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static com.github.mhewedy.outbox.OutboxEntity.Status;

public class OutboxDto {

    public static final RowMapper<OutboxDto> ROW_MAPPER = new OutboxMapper();

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

    private static class OutboxMapper implements RowMapper<OutboxDto> {

        @Override
        public OutboxDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            var entity = new OutboxDto();
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
