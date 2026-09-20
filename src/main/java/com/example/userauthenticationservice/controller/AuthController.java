package com.example.userauthenticationservice.controller;

// ✅ ADD THIS LINE
import org.antlr.v4.runtime.misc.Pair;
import com.example.userauthenticationservice.dtos.LoginRequestDto;
import com.example.userauthenticationservice.dtos.SignUpRequestDto;
import com.example.userauthenticationservice.dtos.UserDto;
import com.example.userauthenticationservice.models.Role;
import com.example.userauthenticationservice.models.User;
import com.example.userauthenticationservice.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/signup")
    private ResponseEntity<UserDto> signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        User user = authService.signup(
                signUpRequestDto.getName(),
                signUpRequestDto.getEmail(),
                signUpRequestDto.getPassword(),
                signUpRequestDto.getPhoneNumber()
                );
        UserDto userDto = from(user);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/login")
    private ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        Pair<User, String> response = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        User user = response.a;
        String token = response.b;
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.SET_COOKIE, "auth_session_id="+token);
        UserDto userDto = from(user);
        return new ResponseEntity<>(userDto, headers, HttpStatus.OK);
    }

    private UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        userDto.setPassword(user.getPassword());
        userDto.setEmail(user.getEmail());
        List<String> roleValues = new ArrayList<>();
        for(Role role : user.getRoles()) {
            roleValues.add(role.getValue());
        }
        userDto.setRoles(roleValues);
        return userDto;
    }
}
