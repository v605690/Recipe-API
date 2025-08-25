package com.crus.RecipeAPI;

import com.crus.RecipeAPI.controllers.RecipeController;
import com.crus.RecipeAPI.exceptions.NoSuchRecipeException;
import com.crus.RecipeAPI.models.*;

import com.crus.RecipeAPI.repos.RecipeRepo;
import com.crus.RecipeAPI.services.RecipeService;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
@ContextConfiguration(classes = RecipeApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecipeControllerUnitTest {

    @MockitoBean
    RecipeService recipeService;

    @MockitoBean
    RecipeRepo recipeRepo;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    public void testGetRecipeByIdSuccessBehavior() throws Exception {

        // Create mock user for the recipe
        CustomUserDetails mockUser = TestUtil.createTestUser("testUser1");

        Recipe mockRecipe = new Recipe();
        mockRecipe.setId(16          L);
        mockRecipe.setMinutesToMake(2);
        mockRecipe.setUser(mockUser);
        mockRecipe.setSubmittedBy("testUser1");
        mockRecipe.setReviews(Collections.nCopies(1, mock(Review.class)));
        mockRecipe.setIngredients(Collections.nCopies(1, mock(Ingredient.class)));
        mockRecipe.setSteps(Collections.nCopies(2, mock(Step.class)));

        when(recipeService.getRecipeById(anyLong()))
                .thenReturn(mockRecipe);

        final long recipeId = 90;

        mockMvc.perform(get("/recipes/" + recipeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                .andExpect(jsonPath("id").value(recipeId))
                .andExpect(jsonPath("minutesToMake").value(2))
                .andExpect(jsonPath("reviews", hasSize(1)))
                .andExpect(jsonPath("ingredients", hasSize(1)))
                .andExpect(jsonPath("steps", hasSize(2)));
    }
    @Test
    @Order(2)
    public void testGetRecipeByIdFailureBehavior() throws Exception {

        when(recipeService.getRecipeById((long) anyLong()))
                .thenThrow(new NoSuchRecipeException("No recipe with ID 5000 could be found."));

        final long recipeId = 5000;

        // set up guaranteed to fail in testing environment request
        mockMvc.perform(get("/recipes/" + recipeId))

                //print response
                .andDo(print())
                //expect status 404 NOT FOUND
                .andExpect(status().isNotFound())
                //confirm that HTTP body contains correct error message
                .andExpect(content().string(containsString(
                        "No recipe with ID " + recipeId +
                                " could be found.")));
    }

    @Test
    @Order(3)
    public void testGetAllRecipesSuccessBehavior() throws Exception {

        // Create mock users for the recipes
        CustomUserDetails user1 = TestUtil.createTestUser("user1");
        CustomUserDetails user2 = TestUtil.createTestUser("user2");
        CustomUserDetails user3 = TestUtil.createTestUser("user3");
        CustomUserDetails user4 = TestUtil.createTestUser("user4");
        CustomUserDetails user5 = TestUtil.createTestUser("user5");

        List<Recipe> mockRecipes = Arrays.asList(
                Recipe.builder().id(1L).name("test recipe").minutesToMake(2).difficultyRating(5).user(user1).submittedBy("user1").build(),
                Recipe.builder().id(2L).name("recipe 2").minutesToMake(2).difficultyRating(4).user(user2).submittedBy("user2").build(),
                Recipe.builder().id(3L).name("recipe 3").minutesToMake(45).difficultyRating(5).user(user3).submittedBy("user3").build(),
                Recipe.builder().id(4L).name("recipe 4").minutesToMake(30).difficultyRating(2).user(user4).submittedBy("user4").build(),
                Recipe.builder().id(5L).name("recipe 5").minutesToMake(25).difficultyRating(6).user(user5).submittedBy("user5").build()
        );

        when(recipeService.getAllRecipes()).thenReturn(mockRecipes);

        mockMvc
                .perform(get("/recipes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].id").value(mockRecipes.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value("test recipe"))
                .andExpect(jsonPath("$[1].id").value(mockRecipes.get(1).getId()))
                .andExpect(jsonPath("$[1].minutesToMake").value(2))
                .andExpect(jsonPath("$[2].id").value(mockRecipes.get(2).getId()))
                .andExpect(jsonPath("$[2].difficultyRating").value(5));
    }
}