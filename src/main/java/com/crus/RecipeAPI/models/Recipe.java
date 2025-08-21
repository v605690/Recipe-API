package com.crus.RecipeAPI.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer minutesToMake;

    @Column(nullable = false)
    private Integer difficultyRating;

    @Column(nullable = false)
    private String submittedBy;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Collection<Ingredient> ingredients = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Collection<Step> steps = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Collection<Review> reviews;

    @ManyToOne(optional = false)
    @JoinColumn
    @JsonIgnore
    private CustomUserDetails user;

    @Transient
    @JsonIgnore
    private URI locationURI;

    @Transient
    private Double averageRating;

    public String getAuthor() {
        return user.getUsername();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setSubmitted(String submittedBy) {
        if (submittedBy == null || submittedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Submitted by cannot be null or empty");
        }
        this.submittedBy = submittedBy.trim();
        }

    public void setDifficultyRating(Integer difficultyRating) {
        if (difficultyRating == null) {
            throw new IllegalArgumentException("Difficulty rating cannot be null");
        }
        if (difficultyRating < 0 || difficultyRating > 10) {
            throw new IllegalArgumentException("Difficulty rating must be between 0 and 10");
        }
        this.difficultyRating = difficultyRating;
    }

    public void validate() throws IllegalStateException {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalStateException("You need at least one ingredient for your recipe!");
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalStateException("You need at least one step for your recipe!");
        }
    }

    public void generateLocationURI() {
        try {
            locationURI = new URI(
                    ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/recipes/")
                            .path(String.valueOf(id))
                            .toUriString());
        } catch (URISyntaxException e) {
            e.getMessage();
        }
    }
    public double getAverageRating(Long id) {

        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        long sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return (double) sum / reviews.size();
    }

    public Recipe recipeWithAverageRating(Recipe recipe) {
        double avgRating = getAverageRating(recipe.getId());
        recipe.setAverageRating(avgRating);
        return recipe;
    }
}
