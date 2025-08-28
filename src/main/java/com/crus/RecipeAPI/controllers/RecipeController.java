package com.crus.RecipeAPI.controllers;

import com.crus.RecipeAPI.exceptions.NoSuchRecipeException;
import com.crus.RecipeAPI.models.*;
import com.crus.RecipeAPI.repos.UserRepo;
import com.crus.RecipeAPI.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RecipeController is a REST controller that manages endpoints for handling
 * recipes. It provides functionality to create, retrieve, update, search for,
 * and delete recipes. This controller interacts with the RecipeService layer
 * to perform business logic and communicates responses to API clients using
 * ResponseEntity.
 */
@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    RecipeService recipeService;
    @Autowired
    private UserRepo userRepo;

    /**
     * Creates a new recipe by validating, saving it to the database, generating a location URI,
     * and returning it in the response. If validation fails, it returns a bad request response with an error message.
     *
     * @param recipe the Recipe object to be created and saved; must not be null and must pass validation rules such as having at least one ingredient and step
     * @return a ResponseEntity containing the created Recipe object and a location URI if successful,
     *         or an error message if validation fails or another error occurs
     */
    @PostMapping
    public ResponseEntity<?> createNewRecipe(@RequestBody Recipe recipe, Authentication authentication) {

        String username = authentication.getName();
        CustomUserDetails user = userRepo.findByUsername(username);
        recipe.setUser(user);

        if (recipe.getReviews() != null) {
            for (Review review : recipe.getReviews()) {
                review.setUser(user);
            }
        }

        Recipe savedRecipe = recipeService.createNewRecipe(recipe);
        return ResponseEntity.created(savedRecipe.getLocationURI()).body(savedRecipe);
    }
    /**
     * Retrieves a recipe by its unique ID. If the recipe is found, it returns the recipe
     * with an HTTP 200 (OK) status. If no recipe is found with the given ID, it returns
     * a 404 (Not Found) response with an error message.
     *
     * @param id the unique identifier of the recipe to be retrieved; cannot be null
     * @return a ResponseEntity containing the recipe object if found, or an error message
     *         if no recipe is found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?>  getRecipeById(@PathVariable("id") Long id) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            return ResponseEntity.ok(recipe);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Retrieves all recipes available in the system. If recipes are found, it returns
     * a list of recipes with an HTTP 200 (OK) status. If no recipes are available, it
     * returns a 404 (Not Found) status with an appropriate error message.
     *
     * @return a ResponseEntity containing a list of Recipe objects if available,
     *         or an error message if no recipes are found in the system
     */
    @GetMapping
    public ResponseEntity<?> getAllRecipes() {
        try {
            List<Recipe> allRecipes = recipeService.getAllRecipes();
            return ResponseEntity.ok(recipeService.getAllRecipes());
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Searches for recipes by their name. If recipes containing the specified name
     * are found, it returns a list of matching recipes with an HTTP 200 (OK) status.
     * If no recipes match the search criteria, it returns a 404 (Not Found) response
     * with an error message.
     *
     * @param name the keyword to search for in recipe names; must not be null
     * @return a ResponseEntity containing a list of Recipe objects if matches are found,
     *         or an error message if no recipes match the search criteria
     */
    @GetMapping("/search/{name}")
    ResponseEntity<?> getRecipesByName(@PathVariable("name") String name) {
        try {
            List<Recipe> matchingRecipes = recipeService.getRecipesByName(name);

            return ResponseEntity.ok(matchingRecipes);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/search/{name}/minRating/{minRating}")
    public ResponseEntity<?> getRecipesByNameAndMinRating(@PathVariable("name") String name,
                                                          @PathVariable("minRating") Double minRating) {
        try {
            if (minRating < 0 || minRating > 10) {
                return ResponseEntity.badRequest()
                        .body("Minimum rating must be between 0 and 10.");
            }
            List<Recipe> recipes = recipeService.getRecipesByNameAndMinRating(name, minRating);
            return ResponseEntity.ok(recipes);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/user/{username}")
    ResponseEntity<?> getRecipesByUser(@PathVariable("username") String username) {

        try {
            List<Recipe> userRecipes = recipeService.getRecipesByUser(username);
            return ResponseEntity.ok(userRecipes);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/user/{username}/search/{name}")
    public ResponseEntity<?> getRecipesByNameAndUser(@PathVariable("username") String username,
                                                     @PathVariable("name") String name) {
        try {
            List<Recipe> recipes = recipeService.getRecipesByNameAndUser(name, username);
            return ResponseEntity.ok(recipes);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Deletes a recipe identified by its unique ID. If the recipe is successfully deleted,
     * it returns a success message with an HTTP 200 (OK) status. If the recipe is not found,
     * it returns a 400 (Bad Request) response with an error message.
     *
     * @param id the unique identifier of the*/
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'Recipe', 'delete')")
    public ResponseEntity<?> deleteRecipeById(@PathVariable("id") Long id) {
        try {
            Recipe deletedRecipe = recipeService.deleteRecipeById(id);
            String successMessage = "The recipe with ID " + deletedRecipe.getId() +
                    " and name " + deletedRecipe.getName() +
                    " was deleted.";
            return ResponseEntity.ok(successMessage);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates an existing recipe with the provided details. If the specified recipe
     * does not exist or the update fails due to validation errors, an appropriate
     * error response is returned.
     *
     * @param updatedRecipe the Recipe object containing updated details; must not be null
     * @return a ResponseEntity containing the updated Recipe object if successful,
     *         or an error message if the recipe is not found or validation fails
     */
    @PatchMapping
    @PreAuthorize("hasPermission(#updatedRecipe.id, 'Recipe', 'edit')")
    public ResponseEntity<?> updateRecipe(@RequestBody Recipe updatedRecipe) {
        try {
            Recipe returnedUpdatedRecipe = recipeService.updateRecipe(updatedRecipe, true);
            return ResponseEntity.ok(returnedUpdatedRecipe);
        } catch (NoSuchRecipeException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/difficulty")
    @PreAuthorize("hasPermission(#id, 'Recipe', 'edit')")
    public ResponseEntity<?> updateRecipeDifficulty(@PathVariable("id") Long id, @RequestParam("rating") int difficultyRating) {

        try {
            Recipe updatedRecipe = recipeService.updateRecipeDifficulty(id, difficultyRating);
            return ResponseEntity.ok(updatedRecipe);
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
