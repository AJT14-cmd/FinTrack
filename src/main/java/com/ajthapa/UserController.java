package com.ajthapa;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @GetMapping
    public List<User> getUsers() {
        return List.of(
            new User((long) 1, "Anuj Jung Thapa", "anujcaeli@gmail.com")
        );
    }

}
