package com.example.userauthenticationservice.services;

import com.example.userauthenticationservice.exceptions.UserAlreadyExistException;
import com.example.userauthenticationservice.models.Role;
import com.example.userauthenticationservice.models.User;
import com.example.userauthenticationservice.repos.RoleRepo;
import com.example.userauthenticationservice.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Override
    public User signup(String name, String email, String password, String phoneNumber) {
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if(optionalUser.isPresent()) {
            throw new UserAlreadyExistException("User already present");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
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
}
