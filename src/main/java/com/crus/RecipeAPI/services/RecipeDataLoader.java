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
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("STARTING WITH TEST DATABASE SETUP");
        if (recipeRepo.findAll().isEmpty()) {

            // Create test users first
            CustomUserDetails tjUser = createTestUser("TJ", "tj@test.com", "TJ");
            CustomUserDetails donUser = createTestUser("Don", "don@test.com", "Donny Long");
            CustomUserDetails davidUser = createTestUser("David", "mark@test.com", "David Wilson");
            CustomUserDetails jackUser = createTestUser("Jack", "billy@test.com", "Jack Brown");
            CustomUserDetails janeUser = createTestUser("Jane", "ben@test.com", "Jane Davis");
            CustomUserDetails reviewerUser = createTestUser("jimmy", "reviewer@test.com", "Anonymous Reviewer");

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
                    .rating(2)
                    .username("idfk")
                    .user(reviewerUser)
                    .build();

            Recipe recipe1 = Recipe.builder()
                    .name("test recipe")
                    .difficultyRating(10)
                    .minutesToMake(2)
                    .ingredients(Set.of(ingredient))
                    .steps(Set.of(step1, step2))
                    .reviews(Set.of(review))
                    .submittedBy("bob")
                    .user(tjUser)
                    .build();

            recipeRepo.save(recipe1);

            ingredient.setId(null);
            Recipe recipe2 = Recipe.builder()
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
                    .user(donUser)
                    .build();
            recipeRepo.save(recipe2);

            Recipe recipe3 = Recipe.builder()
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
                    .user(davidUser)
                    .build();

            recipeRepo.save(recipe3);

            Recipe recipe4 = Recipe.builder()
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
                            .username("ben")
                            .rating(10)
                            .description("this stuff is so good")
                            .user(janeUser)
                            .build()))
                    .submittedBy("Billy")
                    .user(jackUser)
                    .build();

            recipeRepo.save(recipe4);
            System.out.println("FINISHED TEST DATABASE SETUP");
        }
    }

    private CustomUserDetails createTestUser(String username, String email, String name) {
        UserMeta userMeta = UserMeta.builder()
                .email(email)
                .name(name)
                .build();

        Role userRole = Role.builder()
                .role(Role.Roles.ROLE_USER)
                .build();

        CustomUserDetails user = CustomUserDetails.builder()
                .username(username)
                .password(passwordEncoder.encode("testPassword123"))
                .userMeta(userMeta)
                .authorities(Collections.singletonList(userRole))
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();

        return userRepo.save(user);
    }
}
