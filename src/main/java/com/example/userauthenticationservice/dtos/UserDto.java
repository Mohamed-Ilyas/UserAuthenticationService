package com.example.userauthenticationservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDto {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private List<String> roles;
}
