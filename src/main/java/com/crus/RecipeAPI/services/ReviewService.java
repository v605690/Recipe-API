package com.crus.RecipeAPI.services;

import com.crus.RecipeAPI.exceptions.NoSuchRecipeException;
import com.crus.RecipeAPI.exceptions.NoSuchReviewException;
import com.crus.RecipeAPI.models.Recipe;
import com.crus.RecipeAPI.models.Review;
import com.crus.RecipeAPI.repos.ReviewRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private RecipeService recipeService;


    /**
     1. **Method Signature**:
     - Takes a parameter representing the review's ID `Long id`
     - Returns a object `Review`
     - Can throw if the review isn't found `NoSuchReviewException`

     2. **Database Query**:
     - Uses which returns an `reviewRepo.findById(id)``Optional<Review>`
     - The repository method is provided by Spring Data JPA
     - Returns because the review might not exist `Optional`

     3. **Error Handling**:
     - Checks if the is empty using `Optional``review.isEmpty()`
     - If empty, throws with a descriptive message `NoSuchReviewException`
     - This ensures the calling code knows when a review doesn't exist

     4. **Return Value**:
     - Uses to extract the actual object from the `review.get()``Review``Optional`
     - This is safe because we already checked that the Optional isn't empty

     This method follows good practices by:
     - Using to handle potential null cases `Optional`
     - Providing clear error messages
     - Throwing specific exceptions for error cases
     - Following a clean and straightforward flow

     The code interacts with the entity which has fields for , , , and as defined in the class. `Review``id``username``rating``description``Review.java`

     */
    public Review getReviewById(Long id) throws NoSuchReviewException {
        Optional<Review> review = reviewRepo.findById(id);

        if (review.isEmpty()) {
            throw new NoSuchReviewException(
                    "The review with ID " + id + " could not be found.");
        }
        return review.get();
    }


    public double getAverageRating(Long recipeId) throws NoSuchRecipeException, NoSuchReviewException{
        List<Review> reviews = reviewRepo.findReviewById(recipeId);

        int sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return (double) sum / reviews.size();
    }

    /**
     * 1. **Method Signature**:
     * - Returns: - a collection of Review objects `Collection<Review>`
     * - Parameter: - the ID of the recipe whose reviews we want to fetch `Long recipeId`
     * - Throws:
     * - - if the recipe doesn't exist `NoSuchRecipeException`
     * - - if there are no reviews for the recipe `NoSuchReviewException`
     * <p>
     * 2. **Method Flow**:
     * - First, it attempts to get the recipe using
     * - If the recipe doesn't exist, will be thrown `NoSuchRecipeException`
     * <p>
     * `recipeService.getRecipeById(recipeId)`
     * - Then retrieves the reviews collection from the recipe using `recipe.getReviews()`
     * - Checks if the reviews collection is empty
     * - If empty, throws `NoSuchReviewException`
     * <p>
     * - If reviews exist, returns the collection of reviews
     * <p>
     * 3. **JPA Relationship Context**:
     * - The reviews are fetched through the relationship defined in the Recipe class `@OneToMany`
     * - The relationship is managed by JPA with a foreign key in the reviews table `recipe_id`
     * <p>
     * This method is part of the service layer and provides a way to access all reviews associated with a
     * particular recipe, with appropriate error handling for cases where either the recipe or reviews
     * don't exist.
     */

    public double getReviewByRecipeId(Long recipeId) throws NoSuchRecipeException, NoSuchReviewException {
        List<Review> reviews = reviewRepo.findReviewById(recipeId);

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public List<Review> getReviewByUsername(String username)
            throws NoSuchReviewException {
        List<Review> reviews = reviewRepo.findByUsername(username);

        if (reviews.isEmpty()) {
            throw new NoSuchReviewException(
                    "No reviews could be found for username " + username);
        }
        return reviews;
    }

    /**
     1. **Method Parameters**:
     - : A Review object containing the review details (username, rating, description) `review`
     - : The ID of the recipe to which the review will be added `recipeId`
     - Returns: The updated Recipe object
     - Throws: if the recipe isn't found `NoSuchRecipeException`

     2. **Method Flow**:
     - First, it retrieves the recipe using `recipeService.getRecipeById(recipeId)`
     - Then adds the new review to the recipe's collection of reviews using `recipe.getReviews().add(review)`
     - Updates the recipe in the database using `recipeService.updateRecipe(recipe, false)`
     - Returns the updated recipe

     3. **Important Details**:
     - The method leverages JPA's cascading ( on the Recipe's reviews field) `@OneToMany(cascade = CascadeType.ALL)`
     - The `false` parameter in means it won't force an ID check before updating `updateRecipe`
     - The review will be automatically persisted due to the cascade settings
     - The relationship is managed through the foreign key as specified in the `recipe_id``@JoinColumn`

     */
    public Recipe postNewReview(Review review, Long recipeId) throws NoSuchRecipeException {
        Recipe recipe = recipeService.getRecipeById(recipeId);
        recipe.getReviews().add(review);
        recipeService.updateRecipe(recipe, false);
        return recipe;
    }

    /**
     1. **Method Signature**:
     - Takes a parameter representing the review's ID `Long id`
     - Returns a object (the deleted review) `Review`
     - Can throw if the review isn't found `NoSuchReviewException`

     2. **Review Retrieval**:
     - Uses to fetch the review from the database `getReviewById(id)`
     - Note: There's actually a logic issue here because already throws if the review isn't found `getReviewById()``NoSuchReviewException`

     3. **Null Check** (redundant):
     - Checks if the review is null
     - This check is actually unnecessary because would have already thrown an exception if the review wasn't found `getReviewById()`

     4. **Deletion**:
     - Uses Spring Data JPA's to remove the review from the database `reviewRepo.deleteById(id)`

     5. **Return**:
     - Returns the deleted review object (which was fetched before deletion)

     */
    public Review deleteReviewById(Long id) throws NoSuchReviewException {
        Review review = getReviewById(id);

        if (null == review) {
            throw new NoSuchReviewException(
                    "The review you are trying to delete does not exist.");
        }
        reviewRepo.deleteById(id);
        return review;
    }

    /**
     1. **Input Parameter**:
     - Takes a object () that contains the updated review data `Review``reviewToUpdate`

     2. **Validation Check**:
     - First tries to find the existing review using `getReviewById()`
     - If the review doesn't exist, it catches the and throws a new one with a more specific message `NoSuchReviewException`
     - This check ensures we're not trying to update a non-existent review

     3. **Database Operation**:
     - If the review exists, it uses Spring Data JPA's `save()` method to update the review in the database
     - The `reviewRepo.save()` method will update the existing record since the review has an existing ID

     4. **Return Value**:
     - Returns the updated review object

     */
    public Review updateReviewById(Review reviewToUpdate) throws NoSuchReviewException {

        try {
            Review review = getReviewById(reviewToUpdate.getId());
        } catch (NoSuchReviewException e) {
            throw new NoSuchReviewException(
                    "The review you are trying to update. " +
                            "Maybe you meant to create one? If not," +
                            "please double-check the ID you passed in.");
        }
        reviewRepo.save(reviewToUpdate);
        return reviewToUpdate;
    }
}
