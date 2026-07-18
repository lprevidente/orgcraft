package com.lprevidente.orgcraft.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventPublication.Status;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class EventPublicationRetryConfig {

  private final FailedEventPublications failedEventPublications;
  private final EventPublicationRegistry registry;

  /**
   * Maximum number of resubmissions before an event is dead-lettered.
   */
  private final int maxAttempts;

  EventPublicationRetryConfig(
      FailedEventPublications failedEventPublications,
      EventPublicationRegistry registry,
      @Value("${orgcraft.spicedb.retry.max-attempts:10}") int maxAttempts) {
    this.failedEventPublications = failedEventPublications;
    this.registry = registry;
    this.maxAttempts = maxAttempts;
  }

  /**
   * Resubmits failed authorization event publications that are still under the attempt cap. {@code
   * minAge} gives a transient SpiceDB blip a moment to clear before we retry, and {@code maxInFlight}
   * throttles how many resubmissions run in parallel to avoid overwhelming SpiceDB during recovery.
   */
  @Scheduled(fixedDelayString = "${orgcraft.spicedb.retry.interval:PT1M}")
  void resubmitFailedPublications() {
    failedEventPublications.resubmit(
        ResubmissionOptions.defaults()
            .withMinAge(Duration.ofSeconds(30))
            .withMaxInFlight(10)
            .withFilter(pub -> pub.getCompletionAttempts() < maxAttempts));
  }

  /**
   * Dead-letters publications that have exhausted the attempt cap: logs them for manual
   * reconciliation and marks them completed so they are no longer retried nor re-scanned.
   */
  @Scheduled(fixedDelayString = "${orgcraft.spicedb.dead-letter.interval:PT5M}")
  void deadLetterExhaustedPublications() {
    registry.findIncompletePublications().stream()
        .filter(pub -> pub.getStatus() == Status.FAILED)
        .filter(pub -> pub.getCompletionAttempts() >= maxAttempts)
        .forEach(
            pub -> {
              log.error(
                  "DEAD-LETTER: giving up on event publication {} after {} attempts "
                      + "(event={}, listener={}, firstPublished={}). "
                      + "SpiceDB state may be inconsistent and needs manual reconciliation.",
                  pub.getIdentifier(),
                  pub.getCompletionAttempts(),
                  pub.getEvent(),
                  pub.getTargetIdentifier().getValue(),
                  pub.getPublicationDate());
              registry.markCompleted(pub.getEvent(), pub.getTargetIdentifier());
            });
  }
}
