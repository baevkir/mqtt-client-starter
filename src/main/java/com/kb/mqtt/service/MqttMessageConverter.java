package com.kb.mqtt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MqttMessageConverter {
    private final ObjectMapper objectMapper;

    public MqttMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <S> MqttMessageConfig getConfig(Class<S> sourceType) {
        Objects.requireNonNull(sourceType);
        throwExceptionIfMissingAnnotation(sourceType, MqttMessage.class);
        var annotation = sourceType.getAnnotation(MqttMessage.class);
        return MqttMessageConfig.builder()
                .topic(annotation.topic())
                .qos(annotation.qos())
                .retained(annotation.retained())
                .build();
    }

    public <S> MqttMessageModel convert(S source) {
        Objects.requireNonNull(source);
        throwExceptionIfMissingAnnotation(source.getClass(), MqttMessage.class);
        var config = getConfig(source.getClass());
        return MqttMessageModel.builder()
                .topic(config.getTopic())
                .message(buildMessage(source, config))
                .build();
    }

    public <S> S convert(MqttMessageModel source, Class<S> type) {
        Objects.requireNonNull(source, "Source is null");
        Objects.requireNonNull(type, "Type is null");
        throwExceptionIfMissingAnnotation(type, MqttMessage.class);
        var config = getConfig(type);
        Assert.isTrue(source.getTopic().equals(config.getTopic()), "Source and config has different topics.");

        try {
            return objectMapper.readValue(source.getMessage().getPayload(), type);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read mqtt message.", e);
        }
    }

    private void throwExceptionIfMissingAnnotation(Class<?> clazz, Class<? extends Annotation> expectedClass) {
        if (!clazz.isAnnotationPresent(expectedClass)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @" + MqttMessage.class.getSimpleName());
        }
    }

    private <S> org.eclipse.paho.client.mqttv3.MqttMessage buildMessage(S source, MqttMessageConfig config) {
        var message = new org.eclipse.paho.client.mqttv3.MqttMessage(findPayload(source));
        message.setQos(config.getQos());
        message.setRetained(config.isRetained());
        return message;
    }

    private <S> byte[] findPayload(S source) {
        Class<? extends Object> clazz = source.getClass();
        var payload = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(MqttField.class) != null)
                .collect(Collectors.toMap(
                        field -> field.getAnnotation(MqttField.class).value(),
                        field -> {
                            field.setAccessible(true);
                            return Optional.ofNullable(ReflectionUtils.getField(field, source)).orElse("");
                        }
                ));

        if (payload.isEmpty()) {
            return serializeValue(source);
        }
        return serializeValue(payload);
    }

    private byte[] serializeValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize value", e);
        }
    }
}
