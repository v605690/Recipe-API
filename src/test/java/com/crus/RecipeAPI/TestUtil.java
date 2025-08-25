package com.crus.RecipeAPI;

import com.crus.RecipeAPI.models.CustomUserDetails;
import com.crus.RecipeAPI.models.Role;
import com.crus.RecipeAPI.models.UserMeta;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.Collections;

public class TestUtil {

    public static byte[] convertObjectToJsonBytes(Object object) throws JsonProcessingException {
        // ObjectMapper is used to translate object into JSON
        ObjectMapper mapper = new ObjectMapper();
        // take the object and return the JSON as a byte[]
        return mapper.writeValueAsBytes(object);
    }

    public static String convertObjectToJsonString(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // write the JSON in the form of a String and return
        return mapper.writeValueAsString(object);
    }

    public static <T> T convertJsonBytesToObject(
            byte[] bytes, Class<T> clazz) throws IOException {
        // ObjectReader is used to translate JSON to a Java object
        ObjectReader reader = new ObjectMapper()
                // indicate which class the reader maps to
                .readerFor(clazz);

        // read the JSON byte array and translate it into an object.
        return reader.readValue(bytes);
    }

    /**
     * Creates a test user with the given username and default settings
     */
    public static CustomUserDetails createTestUser(String username) {
        return createTestUser(username, username + "@gmail.com", "test " + username);
    }

    /**
     * Creates a test user with custom details
     */
    public static CustomUserDetails createTestUser(String username, String email, String name) {
        UserMeta userMeta = UserMeta.builder()
                .email(email)
                .name(name)
                .build();

        Role userRole = Role.builder()
                .role(Role.Roles.ROLE_USER)
                .build();

        return CustomUserDetails.builder()
                .username(username)
                .password("password")
                .userMeta(userMeta)
                .authorities(Collections.singletonList(userRole))
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();
    }

    /**
     * Creates a test admin user
     */
    public static CustomUserDetails createTestAdminUser(String username) {
        UserMeta userMeta = UserMeta.builder()
                .email(username + "@gmail.com")
                .name("Admin " + username)
                .build();

        Role adminRole = Role.builder()
                .role(Role.Roles.ROLE_ADMIN)
                .build();

        return CustomUserDetails.builder()
                .username(username)
                .password("password")
                .userMeta(userMeta)
                .authorities(Collections.singletonList(adminRole))
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();
    }
}

