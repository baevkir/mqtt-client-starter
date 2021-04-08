package com.kb.mqtt.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MqttMessageConfig {
    private String topic;
    private int qos;
    private boolean retained;
}
