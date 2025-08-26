package com.crus.RecipeAPI;

import com.crus.RecipeAPI.models.*;
import com.crus.RecipeAPI.repos.RecipeRepo;
import com.crus.RecipeAPI.repos.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        // Check if user already exists
        if (!userRepo.existsByUsername("testuser1")) {

        // Create and save user
        UserMeta userMeta = UserMeta.builder()
                .email("testuser1@gmail.com")
                .name("Test User 1")
                .build();

        Role userRole = Role.builder()
                .role(Role.Roles.ROLE_USER)
                .build();

        CustomUserDetails user = CustomUserDetails.builder()
                .username("testuser1")
                .password(passwordEncoder.encode("password")) // Always encode!
                .userMeta(userMeta)
                .authorities(Collections.singletonList(userRole))
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();

        userRepo.save(user);
        }
    }

    @WithMockUser(username = "testuser1", roles = {"USER"})
    @Test
    @Order(1)
    public void testGetRecipeByIdSuccessBehavior() throws Exception {
        //final long recipeId = recipeRepo.findAll().get(0).getId();
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
    @WithMockUser(username = "testuser1", roles = {"USER"})
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
    @WithMockUser(username = "testuser1", roles = {"USER"})
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

    @WithUserDetails(value = "testuser1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @Order(4)
    public void testCreateNewRecipeSuccessBehavior() throws Exception {

        // Create test users
        CustomUserDetails recipeAuthor = TestUtil.createTestUser("testuser1");
        CustomUserDetails reviewer = TestUtil.createTestUser("reviewer");
        userRepo.save(recipeAuthor);
        userRepo.save(reviewer);

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
                .username("reviewer")
                .user(reviewer)
                .build();

        Recipe recipe = Recipe.builder()
                .name("caramel in a pan")
                .difficultyRating(10)
                .minutesToMake(2)
                .ingredients(Set.of(ingredient))
                .steps(Set.of(step1, step2))
                .reviews(Set.of(review))
                .submittedBy("chef123")
                .user(recipeAuthor)
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
                        .value("reviewer"))
                .andReturn()
                .getResponse();
    }
    @WithMockUser(username = "testuser1", roles = {"USER"})
    @Test
    @Order(5)
    public void testCreateNewRecipeFailureBehavior() throws Exception {

        // Create test user for the recipe
        CustomUserDetails testUser = TestUtil.createTestUser("testuser1");
        userRepo.save(testUser);

        Recipe recipe = new Recipe();

        recipe.setName("Test Recipe");
        recipe.setMinutesToMake(10);
        recipe.setDifficultyRating(5);
        recipe.setSubmittedBy("testuser1");
        recipe.setUser(testUser);

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

    @WithMockUser(username = "testUser1", roles = {"USER"})
    @Test
    @Order(6)
    public void testGetRecipesByNameSuccessBehavior() throws Exception {

        // get request to search for recipes with names including "recipe"
        MvcResult mvcResult =
                mockMvc.perform(get("/recipes/search/recipe"))
                        // expect 200 OK
                        .andExpect(status().isOk())
                        // expect JSON in return
                        .andExpect(content().contentType("application/json"))
                        // return the MvcResult
                        .andReturn();

        // pull json byte array from the result
        byte[] jsonByteArray =
                mvcResult.getResponse().getContentAsByteArray();
        // convert the json bytes to an array of Recipe objects
        Recipe[] returnedRecipes = TestUtil.convertJsonBytesToObject(
                jsonByteArray, Recipe[].class);

        // confirm 3 recipes were returned
        assertThat(returnedRecipes.length).isEqualTo(2);

        for (Recipe r : returnedRecipes) {
            // confirm none of the recipes are null
            assertThat(r).isNotNull();
            // confirm they all have IDs
            assertThat(r.getId()).isNotNull();
            // confirm they all contain recipe in the name
            assertThat(r.getName()).contains("recipe");
        }

        // get request to search for recipes with names containing potato
        byte[] jsonBytes = mockMvc.perform(get("/recipes/search/potato"))
                // expect 200 OK
                .andExpect(status().isOk())
                // expect json
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                // return response byte array
                .andReturn().getResponse().getContentAsByteArray();

        // get recipes as a java array
        returnedRecipes =
                TestUtil.convertJsonBytesToObject(jsonBytes, Recipe[].class);

        // confirm only one recipe was returned
        assertThat(returnedRecipes.length).isEqualTo(1);

        // make sure the recipe isn't null
        assertThat(returnedRecipes[0]).isNotNull();

        // expect that the name should contain potato
        assertThat(returnedRecipes[0].getName()).contains("potato");
    }
    @WithMockUser(username = "testUser1", roles = {"USER"})
    @Test
    @Order(7)
    public void testGetRecipeByNameFailureBehavior() throws Exception {

        byte[] contentAsByteArray = mockMvc.perform(
                        get("/recipes/search/should not exist"))
                // expect 404 NOT FOUND
                .andExpect(status().isNotFound())
                // expect only a String in the body
                .andExpect(content().contentType(
                        MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                // retrieve content byte array
                .andReturn().getResponse().getContentAsByteArray();

        // convert JSON to String
        String message = new String(contentAsByteArray);

        // confirm error message is correct
        assertThat(message).isEqualTo(
                "No recipes could be found with that name.");
    }
    @WithMockUser(username = "testUser1", roles = {"USER"})
    @Test
    @Order(8)
    public void testDeleteRecipeByIdSuccessBehavior() throws Exception {
        final long recipeId = 16;
        // get the recipe with ID 3 for future error message confirmation
        byte[] responseByteArr =
                mockMvc.perform(get("/recipes/" + recipeId))
                        .andExpect(status().isOk())
                        // confirm correct recipe was returned
                        .andExpect(jsonPath("id").value(recipeId))
                        .andReturn().getResponse().getContentAsByteArray();

        Recipe recipe3 = TestUtil.convertJsonBytesToObject(
                responseByteArr, Recipe.class);

        // set up delete request
        byte[] deleteResponseByteArr =
                mockMvc.perform(delete("/recipes/" + recipeId))
                        // confirm 200 OK was returned
                        .andExpect(status().isOk())
                        // confirm a String was returned
                        .andExpect(content().contentType(
                                MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                        .andReturn().getResponse().getContentAsByteArray();

        // pull delete message from byte[]
        String returnedDeleteConfirmationMessage =
                new String(deleteResponseByteArr);

        // confirm the message is as expected
        // using the previously acquired Recipe object
        assertThat(returnedDeleteConfirmationMessage)
                .isEqualTo("The recipe with ID " +
                        recipe3.getId() + " and name " +
                        recipe3.getName() + " was deleted.");
    }
    @WithMockUser(username = "testUser1", roles = {"USER"})
    @Test
    @Order(9)
    public void testDeleteRecipeByIdFailureBehavior() throws Exception {
        // force error with invalid ID
        byte[] responseContent =
                mockMvc.perform(delete("/recipes/-1"))
                        // expect 400 BAD REQUEST
                        .andExpect(status().isBadRequest())
                        // expect plain text aka a String
                        .andExpect(content().contentType(
                                MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                        .andReturn().getResponse().getContentAsByteArray();

        String errorMessage = new String(responseContent);

        // confirm correct error message
        assertThat(errorMessage).isEqualTo(
                "No recipe with ID -1 could be found. Could not delete.");
    }


    @WithMockUser(username = "testUser1", roles = {"USER"})
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