package com.crus.RecipeAPI.repos;

import com.crus.RecipeAPI.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {

    List<Review> findByUsername(String username);

    List<Review> findReviewById(Long recipeId);

}
