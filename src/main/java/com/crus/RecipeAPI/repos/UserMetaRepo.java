package com.crus.RecipeAPI.repos;

import com.crus.RecipeAPI.models.UserMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMetaRepo extends JpaRepository<UserMeta, Long> {
    UserMeta findByEmail(String mail);
}
