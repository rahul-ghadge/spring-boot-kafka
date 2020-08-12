package com.arya.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerService<T> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.kafka.topic}")
    private String topic;

    @Value("${spring.kafka.superhero-topic}")
    private String superHeroTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, T> kafkaTemplateSuperHero;


    public void sendMessage(String message) {
        logger.info("#### -> Publishing message -> {}", message);
        kafkaTemplate.send(topic, message);
    }


    public void sendSuperHeroMessage(T superHero) {
        logger.info("#### -> Publishing SuperHero :: {}", superHero);
        kafkaTemplateSuperHero.send(superHeroTopic, superHero);
    }
}
