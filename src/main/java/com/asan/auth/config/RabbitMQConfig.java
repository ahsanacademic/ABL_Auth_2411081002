package com.asan.auth.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String USER_REGISTERED_QUEUE = "user_registered_queue";
  public static final String USER_REGISTERED_DLQ = "user_registered_dlq";

  @Bean
  public Queue userRegisteredQueue() {
    return QueueBuilder.durable(USER_REGISTERED_QUEUE)
        .deadLetterExchange("")
        .deadLetterRoutingKey(USER_REGISTERED_DLQ)
        .build();
  }

  @Bean
  public Queue userRegisteredDeadLetterQueue() {
    return QueueBuilder.durable(USER_REGISTERED_DLQ).build();
  }
}
