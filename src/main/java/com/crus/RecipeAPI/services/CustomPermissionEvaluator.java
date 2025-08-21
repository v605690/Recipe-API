package com.crus.RecipeAPI.services;

import com.crus.RecipeAPI.models.CustomUserDetails;
import com.crus.RecipeAPI.models.Recipe;
import com.crus.RecipeAPI.models.Review;
import com.crus.RecipeAPI.models.Role;
import com.crus.RecipeAPI.repos.RecipeRepo;
import com.crus.RecipeAPI.repos.ReviewRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    RecipeRepo recipeRepo;

    @Autowired
    ReviewRepo reviewRepo;

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Object targetDomainObject,
            Object permission) {
        // this method will not be used - but if used by accident,
        // should always block access for good measure.
        return false;
    }

    @Override
    public boolean hasPermission(
            Authentication authentication,
            Serializable targetId,
            String targetType,
            Object permission) {

        if (!permission.getClass().equals("".getClass())) {
            throw new SecurityException(
                    "Cannot execute hasPermission() calls where " +
                            "permission is not in String form");
        }

        // if the user is an admin they should be allowed to proceed
        if (userIsAdmin(authentication)) {
            return true;
        } else {
            // the user must be the owner of the object to edit it.
            CustomUserDetails userDetails =
                    (CustomUserDetails) authentication.getPrincipal();

            if (targetType.equalsIgnoreCase("recipe")) {
                Optional<Recipe> recipe =
                        recipeRepo.findById(
                                Long.parseLong(targetId.toString()));
                if (recipe.isEmpty()) {
                    // no recipe with id exists, return true so the method
                    // can continue ultimately throwing an exception
                    return true;
                }

                // if the author of the entity matches the current user
                // they are the owner of the recipe and allowed access
                return recipe
                        .get()
                        .getAuthor()
                        .equals(userDetails.getUsername());

            } else if (targetType.equalsIgnoreCase("review")) {
                Optional<Review> review =
                        reviewRepo.findById(
                                Long.parseLong(
                                        targetId.toString()));
                if (review.isEmpty()) {
                    throw new EntityNotFoundException(
                            "The review you are trying to " +
                                    "access does not exist");
                }

                // if the author of the entity matches the current user
                // they are the owner of the review and allowed access
                return review
                        .get()
                        .getAuthor()
                        .equals(userDetails.getUsername());
            }
        }
        return true;
    }

    public boolean userIsAdmin(Authentication authentication) {
        Collection<Role> grantedAuthorities =
                (Collection<Role>) authentication.getAuthorities();

        for (Role r : grantedAuthorities) {
            if (r.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }
}

