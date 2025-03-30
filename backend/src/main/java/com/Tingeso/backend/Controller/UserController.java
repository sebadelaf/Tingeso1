package com.Tingeso.backend.Controller;

import com.Tingeso.backend.Entity.UserEntity;
import com.Tingeso.backend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/user")
public class UserController {
    @Autowired
    public UserService userService;

    @PostMapping("/crear")
    public UserEntity createUser(@RequestBody UserEntity user) {
        String nombre = user.getNombre();
        String apellidopaterno = user.getApellidopaterno();
        String apellidomaterno = user.getApellidomaterno();
        String email = user.getEmail();
        String fechanacimiento = user.getFechanacimiento();
        UserEntity user1 = new UserEntity(nombre, apellidopaterno, apellidomaterno, email, fechanacimiento);
        return userService.createUser(user1);
    }
}
