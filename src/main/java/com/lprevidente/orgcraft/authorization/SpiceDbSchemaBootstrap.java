package com.lprevidente.orgcraft.authorization;

import com.authzed.api.v1.SchemaServiceGrpc.SchemaServiceBlockingStub;
import com.authzed.api.v1.WriteSchemaRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
class SpiceDbSchemaBootstrap implements ApplicationRunner {

  private static final String SCHEMA_LOCATION = "spicedb/schema.zed";

  private final SchemaServiceBlockingStub schemaService;

  @Override
  public void run(ApplicationArguments args) throws IOException {
    final var schema = loadSchema();
    schemaService.writeSchema(WriteSchemaRequest.newBuilder().setSchema(schema).build());
    log.info("SpiceDB schema written from classpath:{}", SCHEMA_LOCATION);
  }

  private String loadSchema() throws IOException {
    try (var input = new ClassPathResource(SCHEMA_LOCATION).getInputStream()) {
      return StreamUtils.copyToString(input, StandardCharsets.UTF_8);
    }
  }
}
