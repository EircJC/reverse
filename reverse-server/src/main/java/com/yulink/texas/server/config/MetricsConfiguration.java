package com.yulink.texas.server.config;

import com.yulink.texas.server.constants.MetricsName;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.pause.ClockDriftPauseDetector;
import java.time.Duration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            MetricsName.METRICS_TAG_APP_NAME, MetricsName.APP_NAME
        ).pauseDetector(new ClockDriftPauseDetector(Duration.ofSeconds(1), Duration.ofSeconds(1)));
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
