package com.kb.mqtt.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MqttMessage {
    String topic();
    int qos() default  0;
    boolean retained() default true;
}
