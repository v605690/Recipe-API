package com.crus.RecipeAPI;

import com.crus.RecipeAPI.repos.RecipeRepo;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @Order(1)
    public void testGetRecipeByIdSuccessBehavior() throws Exception {
        final long recipeId = 4;

        mockMvc.perform(get("/recipes/" + recipeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                .andExpect(jsonPath("id").value(recipeId))
                .andExpect(jsonPath("minutesToMake").value(1))
                .andExpect(jsonPath("reviews", hasSize(1)))
                .andExpect(jsonPath("ingredients", hasSize(2)))
                .andExpect(jsonPath("steps", hasSize(1)));
    }
    @Test
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

}
