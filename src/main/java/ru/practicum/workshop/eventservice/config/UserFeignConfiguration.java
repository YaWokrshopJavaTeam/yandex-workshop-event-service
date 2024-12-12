package ru.practicum.workshop.eventservice.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class UserFeignConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignCustomErrorDecoder();
    }
}
