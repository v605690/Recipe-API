package com.crus.RecipeAPI.repos;

import com.crus.RecipeAPI.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe, Long> {

    List<Recipe> findByNameContaining(String name);

    List<Recipe> findBySubmittedBy(String username);

    List<Recipe> findByNameContainingIgnoreCaseAndSubmittedByIgnoreCase(String name, String username);
}
