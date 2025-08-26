package com.crus.RecipeAPI.controllers;

import com.crus.RecipeAPI.models.CustomUserDetails;
import com.crus.RecipeAPI.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.crus.RecipeAPI.models.UserMeta;

@RestController
public class UserController {

    @Autowired
    CustomUserDetailsService userDetailsService;

    @GetMapping("/user")
    public CustomUserDetails getUser(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @PostMapping("/user")
    public ResponseEntity<?> createNewUser(
            @RequestBody CustomUserDetails userDetails) {
        try {
            return ResponseEntity.ok(
                    userDetailsService.createNewUser(userDetails));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/user/quick")
    public ResponseEntity<?> createQuickTestUser(@RequestParam String username) {
        try {
            CustomUserDetails testUser = CustomUserDetails.builder()
                    .username(username)
                    .password("password123")
                    .userMeta(UserMeta.builder()
                            .email(username + "@test.com")
                            .name("Test " + username)
                            .build())
                    .build();
            return ResponseEntity.ok(userDetailsService.createNewUser(testUser));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}

