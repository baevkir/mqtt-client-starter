package com.kb.mqtt.service;

import lombok.Builder;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Data
@Builder
public class MqttMessageModel {
    private String topic;
    private MqttMessage message;
}
