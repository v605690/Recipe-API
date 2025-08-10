package com.crus.RecipeAPI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;

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
}

