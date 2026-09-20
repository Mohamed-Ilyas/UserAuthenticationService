package com.example.userauthenticationservice.services;

import com.example.userauthenticationservice.exceptions.PasswordMismatchException;
import com.example.userauthenticationservice.exceptions.UserAlreadyExistException;
import com.example.userauthenticationservice.exceptions.UserNotRegisteredException;
import com.example.userauthenticationservice.models.Role;
import com.example.userauthenticationservice.models.User;
import com.example.userauthenticationservice.models.UserSession;
import com.example.userauthenticationservice.repos.RoleRepo;
import com.example.userauthenticationservice.repos.UserRepo;
import com.example.userauthenticationservice.repos.UserSessionRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import org.antlr.v4.runtime.misc.Pair;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserSessionRepo userSessionRepo;

    @Autowired
    private SecretKey secretKey;

    @Override
    public User signup(String name, String email, String password, String phoneNumber) {
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if(optionalUser.isPresent()) {
            throw new UserAlreadyExistException("User already present");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);

        Optional<Role> optionalRole = roleRepo.findByValue("NON_ADMIN");
        Role role;
        if(optionalRole.isEmpty()) {
            role = new Role();
            role.setValue("NON_ADMIN");
            roleRepo.save(role);
        }
        else {
            role = optionalRole.get();
        }
        List<Role> roleList = new ArrayList<>();
        roleList.add(role);
        user.setRoles(roleList);

        return userRepo.save(user);
    }

    @Override
    public Pair<User, String> login(String email, String password) {
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if(optionalUser.isEmpty()) {
            throw new UserNotRegisteredException("User is not registered. Please signup");
        }
        User user = optionalUser.get();
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new PasswordMismatchException("Incorrect password entered");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("issuer", "scaler");
        Long currentTime = System.currentTimeMillis();
        claims.put("iat",currentTime);
        claims.put("exp",currentTime+100000);
        List<String> roles = new ArrayList<>();
        for(Role role : user.getRoles()) {
            roles.add(role.getValue());
        }
        claims.put("access", roles);
        String token = Jwts.builder().claims(claims).signWith(secretKey).compact();

        UserSession userSession = new UserSession();
        userSession.setToken(token);
        userSession.setUser(user);
        userSessionRepo.save(userSession);

        return new Pair<>(user, token);
    }


    public Boolean validateToken(String token ) {
        Optional<UserSession> optionalUserSession = userSessionRepo.findByToken(token);

        if(optionalUserSession.isEmpty()) return false;

        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        Long expiry = (Long)claims.get("exp");
        Long currentTime = System.currentTimeMillis();
        if(currentTime > expiry) {
            UserSession userSession = optionalUserSession.get();
            userSessionRepo.deleteById(optionalUserSession.get().getId());
            return false;
        }

        return true;
    }
}
