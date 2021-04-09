package com.kb.mqtt.configurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.mqtt.service.MqttMessageConverter;
import com.kb.mqtt.service.MqttService;
import com.kb.mqtt.service.MqttServiceImpl;
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
    public MqttMessageConverter mqttMessageConverter(ObjectMapper objectMapper) {
        return new MqttMessageConverter(objectMapper);
    }

    @Bean
    public MqttService mqttService(MqttProperties properties, MqttMessageConverter mqttMessageConverter) {
        return new MqttServiceImpl(properties, mqttMessageConverter);
    }
}
