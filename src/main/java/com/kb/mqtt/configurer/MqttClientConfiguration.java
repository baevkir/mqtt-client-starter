package com.kb.mqtt.configurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.mqtt.service.MqttMessageConverter;
import com.kb.mqtt.service.MqttService;
import com.kb.mqtt.service.MqttServiceImpl;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = {"server"}, prefix = "mqtt")
@ConditionalOnBean(ObjectMapper.class)
@EnableConfigurationProperties(MqttProperties.class)
public class MqttClientConfiguration {

    @Bean
    public IMqttClient mqttClient(MqttProperties properties) throws MqttException {
        var mqttClient = new MqttClient(properties.getServer(), properties.getClientId() + "_" + System.nanoTime(), new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setUserName(properties.getUserName());
        if (properties.getPassword() != null) {
            options.setPassword(properties.getPassword().toCharArray());
        }
        options.setConnectionTimeout(10);
        mqttClient.connect(options);

        return mqttClient;
    }

    @Bean
    public MqttMessageConverter mqttMessageConverter(ObjectMapper objectMapper) {
        return new MqttMessageConverter(objectMapper);
    }

    @Bean
    public MqttService mqttService(IMqttClient mqttClient, MqttMessageConverter mqttMessageConverter) {
        return new MqttServiceImpl(mqttClient, mqttMessageConverter);
    }
}
