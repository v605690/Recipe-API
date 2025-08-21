package com.crus.RecipeAPI.repos;

import com.crus.RecipeAPI.models.CustomUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo
        extends JpaRepository<CustomUserDetails, Long> {

    CustomUserDetails findByUsername(String username);
}
