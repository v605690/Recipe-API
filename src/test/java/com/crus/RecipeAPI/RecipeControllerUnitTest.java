package com.crus.RecipeAPI;

import com.crus.RecipeAPI.controllers.RecipeController;
import com.crus.RecipeAPI.models.Ingredient;
import com.crus.RecipeAPI.models.Recipe;
import com.crus.RecipeAPI.models.Review;
import com.crus.RecipeAPI.models.Step;

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

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
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

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    public void testGetRecipeByIdSuccessBehavior() throws Exception {

        Recipe mockRecipe = new Recipe();
        mockRecipe.setId(90L);
        mockRecipe.setMinutesToMake(2);
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
}
