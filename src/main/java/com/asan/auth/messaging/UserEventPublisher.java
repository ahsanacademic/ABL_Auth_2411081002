package com.asan.auth.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.asan.auth.config.RabbitMQConfig;
import com.asan.auth.dto.UserRegisteredEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserEventPublisher {

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public UserEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
    this.rabbitTemplate = rabbitTemplate;
    this.objectMapper = objectMapper;
  }

  public void publishUserRegistered(UserRegisteredEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      rabbitTemplate.convertAndSend(RabbitMQConfig.USER_REGISTERED_QUEUE, payload);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Could not serialize user registration event", e);
    }
  }
}
