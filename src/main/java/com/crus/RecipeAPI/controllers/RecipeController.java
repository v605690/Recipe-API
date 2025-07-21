package com.crus.RecipeAPI.controllers;

import com.crus.RecipeAPI.exceptions.NoSuchRecipeException;
import com.crus.RecipeAPI.models.Recipe;
import com.crus.RecipeAPI.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     1. **Annotations**:
     - - Handles HTTP POST requests to `/recipes/{id}` `@PostMapping("/{id}")`
     - - Deserializes the HTTP request body into a object `@RequestBody``Recipe`

     2. **Processing Flow**:
     - Takes a object as input from the request body `Recipe`
     - Calls `recipeService.createNewRecipe()` which:
     - Validates the recipe (must have ingredients and steps)
     - Saves it to the database
     - Generates a location URI for the new recipe

     3. **Response Handling**:
     - On success:
     - Returns HTTP 201 (Created) status
     - Includes the location URI in the response header
     - Returns the created recipe in the response body

     - On validation failure:
     - Catches `IllegalStateException`
     - Returns HTTP 400 (Bad Request)
     - Returns the error message in the response body

     4. **Uses ResponseEntity**:
     - Provides fine-grained control over the HTTP response
     - Allows setting status code, headers, and body

     */
    @PostMapping("/{id}")
    public ResponseEntity<?> createNewRecipe(@RequestBody Recipe recipe) {
        try {
             Recipe insertedRecipe = recipeService.createNewRecipe(recipe);
             return ResponseEntity.created(
                     insertedRecipe.getLocationURI()).body(insertedRecipe);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
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

    @GetMapping
    public ResponseEntity<?> getAllRecipes() {
        try {
            return ResponseEntity.ok(recipeService.getAllRecipes());
        } catch (NoSuchRecipeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

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

    @DeleteMapping("/{id}")
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

    @PatchMapping
    public ResponseEntity<?> updateRecipe(@RequestBody Recipe updatedRecipe) {
        try {
            Recipe returnedUpdatedRecipe = recipeService.updateRecipe(updatedRecipe, true);
            return ResponseEntity.ok(returnedUpdatedRecipe);
        } catch (NoSuchRecipeException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
