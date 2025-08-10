package com.crus.RecipeAPI;

import com.crus.RecipeAPI.models.Ingredient;
import com.crus.RecipeAPI.models.Recipe;
import com.crus.RecipeAPI.models.Review;
import com.crus.RecipeAPI.models.Step;
import com.crus.RecipeAPI.repos.RecipeRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = RecipeApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RecipeControllerEndPointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepo recipeRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    public void testGetRecipeByIdSuccessBehavior() throws Exception {
        //final long recipeId = recipeRepo.findAll().get(0).getId();
        final long recipeId = 64;

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

        List<Recipe> recipes = recipeRepo.findAll();

        mockMvc
                .perform(get("/recipes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].id").value(recipes.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value("test recipe"))
                .andExpect(jsonPath("$[1].id").value(recipes.get(1).getId()))
                .andExpect(jsonPath("$[1].minutesToMake").value(2))
                .andExpect(jsonPath("$[2].id").value(recipes.get(2).getId()))
                .andExpect(jsonPath("$[2].difficultyRating").value(5));
    }

    @Test
    @Order(4)
    public void testCreateNewRecipeSuccessBehavior() throws Exception {

        Ingredient ingredient = Ingredient.builder()
                .name("brown sugar")
                .state("dry")
                .amount("1 cup")
                .build();

        Step step1 = Step.builder()
                .description("heat pan")
                .stepNumber(1)
                .build();
        Step step2 = Step.builder().
                description("add sugar")
                .stepNumber(2)
                .build();

        Review review = Review.builder()
                .description("was just caramel")
                .rating(3)
                .username("idk")
                .build();

        Recipe recipe = Recipe.builder()
                .name("caramel in a pan")
                .difficultyRating(10)
                .minutesToMake(2)
                .ingredients(Set.of(ingredient))
                .steps(Set.of(step1, step2))
                .reviews(Set.of(review))
                .build();

        MockHttpServletResponse response = mockMvc
                .perform(post("/recipes")
                        // set request Content-Type header
                        .contentType("application/json")
                        // set HTTP body equal to JSON based on recipe object
                        .content(objectMapper.writeValueAsString(recipe)))

                // confirm HTTP response meta
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                // confirm Location header with new location of
                // object matches the correct URL structure
                .andExpect(header().string(
                        "Location",
                        containsString("http://localhost/recipes/")))

                // confirm some recipe data
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("name")
                        .value("caramel in a pan"))

                // confirm ingredient data
                .andExpect(jsonPath("ingredients", hasSize(1)))
                .andExpect(jsonPath("ingredients[0].name")
                        .value("brown sugar"))
                .andExpect(jsonPath("ingredients[0].amount")
                        .value("1 cup"))

                // confirm step data
                .andExpect(jsonPath("steps", hasSize(2)))
                .andExpect(jsonPath("steps[0]").isNotEmpty())
                .andExpect(jsonPath("steps[1]").isNotEmpty())

                // confirm review data
                .andExpect(jsonPath("reviews", hasSize(1)))
                .andExpect(jsonPath("reviews[0].username")
                        .value("idk"))
                .andReturn()
                .getResponse();
    }

    @Test
    @Order(5)
    public void testCreateNewRecipeFailureBehavior() throws Exception {

        Recipe recipe = new Recipe();

        recipe.setName("Test Recipe");
        recipe.setMinutesToMake(10);
        recipe.setDifficultyRating(5);
        recipe.setSubmittedBy("tester");

        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());

        // force failure with empty User object
        mockMvc.perform(post("/recipes")
                        // set body equal to empty recipe object
                        .content(objectMapper.writeValueAsString(recipe))
                        // set Content-Type header
                        .contentType("application/json"))
                // confirm status code 400 BAD REQUEST
                .andDo(print())
                .andExpect(status().isBadRequest())
              //  .andExpect(status().isBadRequest())
                // confirm the body only contains a String
                .andExpect(content().string("You need at least one ingredient for your recipe!"));
    }


    @Test
// make sure this test runs last
    @Order(11)
    public void testGetAllRecipesFailureBehavior() throws Exception {

        // delete all entries to force error
        recipeRepo.deleteAll();

        // perform GET all recipes
        this.mockMvc.perform(get("/recipes"))

                .andDo(print())

                // expect 404 NOT FOUND
                .andExpect(status().isNotFound())

                // expect error message defined in RecipeService class
                .andExpect(jsonPath("$").value(
                        "There are no recipes yet :( " +
                                "feel free to add one."));
    }
}
