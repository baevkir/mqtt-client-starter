package com.kb.mqtt.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import reactor.core.publisher.Flux;

@Slf4j
public class MqttServiceImpl implements MqttService {
    private final IMqttClient mqttClient;
    private final MqttMessageConverter mqttMessageConverter;

    public MqttServiceImpl(IMqttClient mqttClient, MqttMessageConverter mqttMessageConverter) {
        this.mqttClient = mqttClient;
        this.mqttMessageConverter = mqttMessageConverter;
    }

    public <M> void publish(M measure) {
        var mqttMessageModel = mqttMessageConverter.convert(measure);
        try {
            mqttClient.publish(mqttMessageModel.getTopic(), mqttMessageModel.getMessage());
        } catch (MqttException e) {
            throw new RuntimeException("Cannot publish message " + mqttMessageModel.getTopic(), e);
        }
    }

    public <C> Flux<C> subscribe(Class<C> type) {
        var config = mqttMessageConverter.getConfig(type);
        return Flux.create(sink -> {
            try {
                mqttClient.subscribe(config.getTopic(), config.getQos(), (topic, message) -> {
                    C result = mqttMessageConverter.convert(
                            MqttMessageModel.builder()
                                    .topic(topic)
                                    .message(message)
                                    .build(),
                            type
                    );
                    sink.next(result);
                });
            } catch (Exception exception) {
                sink.error(exception);
            }
        });
    }
}
