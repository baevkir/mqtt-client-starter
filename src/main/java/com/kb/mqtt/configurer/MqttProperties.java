package com.kb.mqtt.configurer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {
    private String server;
    private String clientId;
    private String userName;
    private String password;
}
