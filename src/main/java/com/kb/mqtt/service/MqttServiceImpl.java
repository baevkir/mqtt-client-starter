package com.kb.mqtt.service;

import com.kb.mqtt.configurer.MqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;

@Slf4j
public class MqttServiceImpl implements MqttService, AutoCloseable {
    private IMqttClient mqttClient;
    private final MqttProperties properties;
    private final MqttMessageConverter mqttMessageConverter;

    public MqttServiceImpl(MqttProperties properties, MqttMessageConverter mqttMessageConverter) {
        this.properties = properties;
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

    @PostConstruct
    public void init() throws Exception {
        mqttClient = new MqttClient(properties.getServer(), properties.getClientId() + "_" + System.nanoTime(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setUserName(properties.getUserName());
        if (properties.getPassword() != null) {
            options.setPassword(properties.getPassword().toCharArray());
        }
        options.setConnectionTimeout(10);
        mqttClient.connect(options);
    }

    @Override
    public void close() throws Exception {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
        mqttClient.close();
    }
}
