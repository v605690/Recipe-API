package com.crus.RecipeAPI.services;

import com.crus.RecipeAPI.models.*;
import com.crus.RecipeAPI.repos.RecipeRepo;
import com.crus.RecipeAPI.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@SpringBootApplication
@Profile("test")
public class RecipeDataLoader implements CommandLineRunner {

    @Autowired
    RecipeRepo recipeRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {

        System.out.println("STARTING WITH TEST DATABASE SETUP");

        UserMeta userMeta =
                UserMeta.builder().name("test user").email("testuser1@gmail.com").build();
        CustomUserDetails userDetails = new CustomUserDetails(
                "testuser1", encoder.encode("password"), Collections.singletonList(new Role(Role.Roles.ROLE_USER)), userMeta);
        if (userRepo.findByUsername("testuser1") != null) {
            return;
        }

        userRepo.save(userDetails);

        if (recipeRepo.findAll().isEmpty()) {

            Ingredient ingredient = Ingredient.builder()
                    .name("flour")
                    .state("dry")
                    .amount("2 cups")
                    .build();

            Step step1 = Step.builder()
                    .description("put flour in bowl")
                    .stepNumber(1)
                    .build();
            Step step2 = Step.builder()
                    .description("eat it?")
                    .stepNumber(2)
                    .build();

            Review review = Review.builder()
                    .description("tasted pretty bad")
                    .rating(3)
                    .username("idfk")
                    .user(userDetails)
                    .build();

            Recipe recipe1 = Recipe.builder()
                    .user(userDetails)
                    .name("test recipe")
                    .difficultyRating(10)
                    .minutesToMake(2)
                    .ingredients(Set.of(ingredient))
                    .steps(Set.of(step1, step2))
                    .reviews(Set.of(review))
                    .submittedBy("bob")
                    .build();

            recipeRepo.save(recipe1);

            ingredient.setId(null);

            Review review2 = Review.builder()
                    .user(userDetails)
                    .description("tasted pretty bad")
                    .rating(2)
                    .username("idfk")
                    .build();

            Recipe recipe2 = Recipe.builder()
                    .user(userDetails)
                    .steps(Set.of(Step.builder()
                            .description("test")
                            .build()))
                    .ingredients(Set.of(Ingredient.builder()
                            .name("test ing")
                            .amount("1")
                            .state("dry")
                            .build()))
                    .name("another test recipe")
                    .difficultyRating(10)
                    .minutesToMake(2)
                    .submittedBy("Sally")
                    .build();
            recipeRepo.save(recipe2);

            Recipe recipe3 = Recipe.builder()
                    .user(userDetails)
                    .steps(Set.of(Step.builder()
                            .description("test 2")
                            .build()))
                    .ingredients(Set.of(Ingredient.builder()
                            .name("test ing 2")
                            .amount("2")
                            .state("wet")
                            .build()))
                    .name("another another test recipe")
                    .difficultyRating(5)
                    .minutesToMake(2)
                    .submittedBy("Mark")
                    .build();

            recipeRepo.save(recipe3);

            Recipe recipe4 = Recipe.builder()
                    .user(userDetails)
                    .name("chocolate and potato chips")
                    .difficultyRating(10)
                    .minutesToMake(1)
                    .ingredients(Set.of(
                            Ingredient.builder()
                                    .name("potato chips")
                                    .amount("1 bag")
                                    .build(),
                            Ingredient.builder()
                                    .name("chocolate")
                                    .amount("1 bar")
                                    .build()))
                    .steps(Set.of(Step.builder()
                            .stepNumber(1)
                            .description("eat both items together")
                            .build()))
                    .reviews(Set.of(Review.builder()
                            .user(userDetails)
                            .username("ben")
                            .rating(10)
                            .description("this stuff is so good")
                            .build()))
                    .submittedBy("Billy")
                    .build();

            recipeRepo.save(recipe4);
            System.out.println("FINISHED TEST DATABASE SETUP");
        }
    }
}
