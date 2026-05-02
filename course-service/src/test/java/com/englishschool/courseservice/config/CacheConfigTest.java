package com.englishschool.courseservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void cacheManager_createsCaffeineManagerWithNamedCaches() {
        CacheConfig config = new CacheConfig();

        CacheManager manager = config.cacheManager();

        assertThat(manager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(manager.getCache(CacheConfig.CACHE_COURSES)).isNotNull();
        assertThat(manager.getCache(CacheConfig.CACHE_COURSE_BY_ID)).isNotNull();
    }
}

