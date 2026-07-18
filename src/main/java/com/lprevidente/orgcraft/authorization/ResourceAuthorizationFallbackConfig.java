package com.lprevidente.orgcraft.authorization;

import com.lprevidente.orgcraft.common.authorization.ResourceAuthorization;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ResourceAuthorizationFallbackConfig {

  /**
   * When SpiceDB is disabled (e.g. the test profile), authorization is a no-op: list endpoints
   * return everything ({@link Optional#empty()} = unfiltered) and single-resource checks permit
   * all.
   */
  @Bean
  @ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "false", matchIfMissing = true)
  ResourceAuthorization permitAllResourceAuthorization() {
    return new ResourceAuthorization() {
      @Override
      public Optional<Set<UUID>> accessibleResourceIds(
          String resourceType, String permission, UUID subjectId) {
        return Optional.empty();
      }

      @Override
      public boolean hasPermission(
          String resourceType, String resourceId, String permission, UUID subjectId) {
        return true;
      }
    };
  }
}
