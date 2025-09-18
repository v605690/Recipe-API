package com.crus.RecipeAPI;

import org.ehcache.core.internal.statistics.DefaultStatisticsService;
import org.ehcache.core.spi.service.StatisticsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;
import static org.hibernate.boot.model.process.spi.MetadataBuildingProcess.build;

@Configuration
@SpringBootApplication
public class RecipeApiApplication {

    @Bean
    public StatisticsService statisticsService() {
        return new DefaultStatisticsService();
    }

    @Bean
    public org.ehcache.CacheManager cacheManager(StatisticsService statisticsService) {
        return newCacheManagerBuilder()
                .using(statisticsService)
                .withCache("ownersSearch", newCacheConfigurationBuilder(String.class, Long.class, heap(10)))
                .withCache("allRecipesCache", newCacheConfigurationBuilder(String.class, List.class, heap(100)))
                .withCache("reviewSearch", newCacheConfigurationBuilder(String.class, Long.class, heap(10)))
                .withCache("allReviewsCache", newCacheConfigurationBuilder(String.class, List.class, heap(100)))
                .build(true);
    }

	public static void main(String[] args) {
		SpringApplication.run(RecipeApiApplication.class, args);
	}

}
