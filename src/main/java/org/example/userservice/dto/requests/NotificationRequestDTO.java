package org.example.userservice.dto.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.userservice.enums.ChannelTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class NotificationRequestDTO implements Serializable {
    String userId;
    Map<String, Object> message;
    ChannelTypeEnum channel;
    LocalDateTime createdAt;
}
