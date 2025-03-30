package com.Tingeso.backend.Service;

import com.Tingeso.backend.Entity.UserEntity;
import com.Tingeso.backend.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    public UserRepository userRepository;

    @Transactional
    public UserEntity createUser(UserEntity user){
        return userRepository.save(user);
    }
}
