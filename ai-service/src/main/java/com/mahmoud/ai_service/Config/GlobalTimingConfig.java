package com.mahmoud.ai_service.Config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalTimingConfig {
    
    @Bean
    public ZoneId buisnessZoneId() {
        return ZoneId.of("Africa/Tunis");
    }

    @Bean
    public Clock buisnessClock(ZoneId buisnessZoneId) {
        return Clock.system(buisnessZoneId);
    }

}
