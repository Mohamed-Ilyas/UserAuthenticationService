package com.example.userauthenticationservice.services;

import com.example.userauthenticationservice.models.User;

public interface IAuthService {
    User signup(String name, String email, String password, String phoneNumber);
}
