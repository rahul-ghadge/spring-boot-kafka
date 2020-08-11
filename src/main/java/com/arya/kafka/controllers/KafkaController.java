package com.arya.kafka.controllers;

import com.arya.kafka.model.SuperHero;
import com.arya.kafka.service.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/kafka")
public class KafkaController {

    @Autowired
    private Producer<SuperHero> producer;


    @GetMapping(value = "/publish")
    public String sendMessageToKafkaTopic(@RequestParam("message") String message) {
        producer.sendMessage(message);
        return "Successfully publisher message..!";
    }


    @PostMapping(value = "/publish")
    public String sendObjectToKafkaTopic(@RequestBody SuperHero superHero) {
        producer.sendSuperHeroMessage(superHero);
        return "Successfully publisher Super Hero..!";
    }
}
