package com.crus.RecipeAPI.services;

import com.crus.RecipeAPI.exceptions.NoSuchRecipeException;
import com.crus.RecipeAPI.models.Recipe;
import com.crus.RecipeAPI.repos.RecipeRepo;
import com.crus.RecipeAPI.repos.ReviewRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    @Autowired
    RecipeRepo recipeRepo;
    @Autowired
    private ReviewRepo reviewRepo;

    /**
     * Creates and saves a new recipe to the repository after performing validation
     * and generating a location URI for it.
     *
     * @param recipe the recipe object to be created and saved; must not be null and must
     *               pass validation rules, such as having at least one ingredient and one step
     * @return the saved recipe object with a unique ID and location URI populated
     * @throws IllegalStateException if the recipe does not pass validation rules
     */
    @Transactional
    public Recipe createNewRecipe(Recipe recipe)
        throws IllegalStateException {
        recipe.validate();
        recipe = recipeRepo.save(recipe);
        recipe.generateLocationURI();
        return recipe;
    }

    /**
     * Retrieves a recipe by its unique ID from the repository and ensures its location URI
     * is generated before returning it.
     *
     * @param id the unique identifier of the recipe to retrieve; must not be null
     * @return the recipe object corresponding to the provided ID, with its location URI populated
     * @throws NoSuchRecipeException if no recipe is found with the given ID
     */
    public Recipe getRecipeById(Long id) throws NoSuchRecipeException {
        Optional<Recipe> recipeOptional = recipeRepo.findById(id);

        if (recipeOptional.isEmpty()) {
            throw new NoSuchRecipeException(
                    "No recipe with ID " + id + " could be found."
            );
        }
        Recipe recipe = recipeOptional.get();
        recipe.generateLocationURI();
        return recipe.recipeWithAverageRating(recipe);
    }

    // get recipes by user
    public List<Recipe> getRecipesByUser(String username) throws NoSuchRecipeException {
        List<Recipe> userRecipes = recipeRepo.findBySubmittedBy(username);

        if (userRecipes.isEmpty()) {
            throw new NoSuchRecipeException("No recipes found for user: " + username);
        }
        return userRecipes.stream()
                .map(recipe -> {
                   recipe.generateLocationURI();
                   return recipe.recipeWithAverageRating(recipe);
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of recipes whose names contain the specified keyword.
     *
     * @param name the keyword to search for in recipe names; must not be null
     * @return a list of Recipe objects that match the search criteria
     * @throws NoSuchRecipeException if no recipes are found in the repository
     */
    public List<Recipe> getRecipesByName(String name) throws NoSuchRecipeException {
        List<Recipe> matchingRecipes = recipeRepo.findByNameContaining(name);

        if (matchingRecipes.isEmpty()) {
            throw new NoSuchRecipeException("No recipes could be found with that name.");
        }
        return matchingRecipes.stream()
                .map(recipe -> {
                    recipe.generateLocationURI();
                    return recipe.recipeWithAverageRating(recipe);
                })
                .collect(Collectors.toList());
    }

    public List<Recipe> getAllRecipes() throws NoSuchRecipeException {
        List<Recipe> recipes = recipeRepo.findAll();

        if (recipes.isEmpty()) {
            throw new NoSuchRecipeException("There are no recipes yet :( feel free to add one.");
        }
        return recipes.stream()
                .map(recipe -> {
                    recipe.generateLocationURI();
                    return recipe.recipeWithAverageRating(recipe);
                })
                .collect(Collectors.toList());
    }

    // get recipes by name and minimal rating
    public List<Recipe> getRecipesByNameAndMinRating(String name, Double minAverageRating) throws NoSuchRecipeException {
        List<Recipe> matchingRecipes = recipeRepo.findByNameContaining(name);

        List<Recipe> filteredRecipes = matchingRecipes.stream()
                .map(this::processRecipeWithRatingAndUri)
                .filter(recipe -> recipe.getAverageRating(recipe.getId()) >= minAverageRating)
                .toList();

        if (filteredRecipes.isEmpty()) {
            throw new NoSuchRecipeException("No recipes found with name containing " + name +
                    " and average rating of " + minAverageRating + " or higher.");
        }
        return filteredRecipes;
    }

    private Recipe processRecipeWithRatingAndUri(Recipe recipe) {
        recipe.generateLocationURI();
        return recipe.recipeWithAverageRating(recipe);
    }

    // get recipes by name and by the user
    public List<Recipe> getRecipesByNameAndUser(String name, String username) throws NoSuchRecipeException {
        List<Recipe> matchingRecipes = recipeRepo.findByNameContainingIgnoreCaseAndSubmittedByIgnoreCase(name, username);

        if (matchingRecipes.isEmpty()) {
            throw new NoSuchRecipeException("No recipes found with name containing " + name +
                    "submitted by user " + username);
    }
            return matchingRecipes.stream()
                    .map(recipe -> {
                        recipe.generateLocationURI();
                        return recipe.recipeWithAverageRating(recipe);
                    })
                    .collect(Collectors.toList());
        }

    /**
     * Deletes a recipe from the repository based on its unique ID.
     *
     * @param id the unique identifier of the recipe to delete; must not be null
     * @return the recipe object that was deleted
     * @throws NoSuchRecipeException if no recipe is found with the given ID or if deletion fails
     */
    @Transactional
    public Recipe deleteRecipeById(Long id) throws NoSuchRecipeException {
        try {
            Recipe recipe = getRecipeById(id);
            recipeRepo.deleteById(id);
            return recipe;
        } catch (NoSuchRecipeException e) {
            throw new NoSuchRecipeException(
                    e.getMessage() + " Could not delete.");
        }
    }

    /**
     * Updates an existing recipe in the repository, performing validation and ensuring
     * the recipe's location URI is generated. An optional ID check can be enforced
     * to verify the existence of the recipe prior to updating.
     *
     * @param recipe the Recipe object to update; must not be null and must pass validation
     *               (e.g., must have at least one ingredient and one step)
     * @param forceIdCheck a boolean flag indicating whether the recipe's ID should be checked
     *                     against the repository to confirm existence
     * @return the updated Recipe object with its details modified and location URI populated
     * @throws NoSuchRecipeException if the recipe does not have a valid ID in the repository
     *                               or is missing when the forceIdCheck flag is enabled
     */
    @Transactional
    public Recipe updateRecipe(Recipe recipe, boolean forceIdCheck) throws NoSuchRecipeException {
        try {
            if (forceIdCheck) {
                getRecipeById(recipe.getId());
            }
            recipe.validate();
            Recipe savedRecipe = recipeRepo.save(recipe);
            savedRecipe.generateLocationURI();
            return savedRecipe;
        } catch (NoSuchRecipeException e) {
            throw new NoSuchRecipeException("The recipe you passed in did not have an ID found " +
                    "in the database. Double check that it is correct. " +
                    "Or maybe you meant to POST a recipe not PATCH one.");
        }
    }

    // update recipe difficulty rating
    @Transactional
    public Recipe updateRecipeDifficulty(Long recipeId, int newDifficultyRating) throws NoSuchRecipeException {
        Recipe recipe = getRecipeById(recipeId);
        recipe.setDifficultyRating(newDifficultyRating);
        Recipe updateRecipe = recipeRepo.save(recipe);
        updateRecipe.generateLocationURI();
        return updateRecipe;
    }
}
