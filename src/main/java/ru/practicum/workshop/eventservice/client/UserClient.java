package ru.practicum.workshop.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.workshop.eventservice.config.UserFeignConfiguration;
import ru.practicum.workshop.eventservice.dto.UserDto;

@FeignClient(value = "userClient", url = "${userservice.url}", path = "/users", configuration = UserFeignConfiguration.class)
public interface UserClient {
    @GetMapping("/{userId}")
    UserDto getUserById(@PathVariable(value = "userId") long userId);
}
