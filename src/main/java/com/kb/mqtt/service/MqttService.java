package com.kb.mqtt.service;

import org.eclipse.paho.client.mqttv3.MqttException;
import reactor.core.publisher.Flux;

public interface MqttService {
    <M> void publish(M measure);
    <C> Flux<C> subscribe(Class<C> type);
}
