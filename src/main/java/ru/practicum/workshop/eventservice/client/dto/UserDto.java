package ru.practicum.workshop.eventservice.client.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String aboutMe;
}