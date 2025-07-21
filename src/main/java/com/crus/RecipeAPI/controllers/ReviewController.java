package com.crus.RecipeAPI.controllers;

import com.crus.RecipeAPI.exceptions.NoSuchRecipeException;
import com.crus.RecipeAPI.exceptions.NoSuchReviewException;
import com.crus.RecipeAPI.models.Recipe;
import com.crus.RecipeAPI.models.Review;
import com.crus.RecipeAPI.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable("id") Long id) {
        try {
            Review retrievedReview = reviewService.getReviewById(id);
            return ResponseEntity.ok(retrievedReview);
        } catch (IllegalStateException | NoSuchReviewException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recipeRating/{recipeId}")
    public ResponseEntity<?> getAverageReviewRating(@PathVariable("recipeId") Long recipeId) {
        try {
            double retrievedRating = reviewService.getAverageRating(recipeId);
            return ResponseEntity.ok(retrievedRating);
        } catch (IllegalStateException | NoSuchRecipeException | NoSuchReviewException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<?> getReviewByRecipeId(
            @PathVariable("recipeId") Long recipeId) {
        try {
            double reviews =
                    reviewService.getReviewByRecipeId(recipeId);
            return ResponseEntity.ok(reviews);
        } catch (NoSuchRecipeException | NoSuchReviewException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getReviewByUsername(
            @PathVariable("username") String username) throws NoSuchReviewException {
        try {
        List<Review> reviews =
                reviewService.getReviewByUsername(username);
        return ResponseEntity.ok(reviews);
    } catch (NoSuchReviewException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{recipeId}")
    public ResponseEntity<?> postNewReview(
            @RequestBody Review review,
            @PathVariable("recipeId") Long recipeId) {
        try {
            Recipe insertedRecipe =
                    reviewService.postNewReview(review, recipeId);
            return ResponseEntity.created(
                    insertedRecipe.getLocationURI()).body(insertedRecipe);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReviewById(
            @PathVariable("id") Long id) {
        try {
            Review review = reviewService.deleteReviewById(id);
            return ResponseEntity.ok(review);
        } catch (NoSuchReviewException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping
    public ResponseEntity<?> updateReviewById(
            @RequestBody Review reviewToUpdate) {
        try {
            Review review =
                    reviewService.updateReviewById(reviewToUpdate);
            return ResponseEntity.ok(review);
        } catch (NoSuchReviewException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

