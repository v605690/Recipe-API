package com.crus.RecipeAPI;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ehcache.Cache;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.springframework.context.annotation.Configuration;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Configuration
public class CacheManager {

    private org.ehcache.CacheManager cacheManager;
    private Cache<Long, String> preConfigured;
    private Cache<Long, String> myCache;

    public void initializeCache() {
        cacheManager = newCacheManagerBuilder()
                .withCache("preConfigured", newCacheConfigurationBuilder(Long.class, String.class, heap(100)))
                .build();

            preConfigured =
                    cacheManager.getCache("preConfigured", Long.class, String.class);

            myCache = cacheManager.createCache("myCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, heap(10)));

            myCache.put(1L, "da one!");
            String value = myCache.get(1L);

            cacheManager.removeCache("preConfigured");

            cacheManager.close();
    }
}
