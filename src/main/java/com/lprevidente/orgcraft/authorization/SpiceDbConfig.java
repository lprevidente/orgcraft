package com.lprevidente.orgcraft.authorization;

import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc.SchemaServiceBlockingStub;
import com.authzed.grpcutil.BearerToken;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "orgcraft.spicedb.enabled", havingValue = "true")
@EnableConfigurationProperties(SpiceDbProperties.class)
class SpiceDbConfig {

  private final ManagedChannel channel;

  SpiceDbConfig(SpiceDbProperties properties) {
    final var builder =
        NettyChannelBuilder.forTarget(properties.endpoint())
            // Keep the connection warm so gRPC processes the server's GOAWAY(max_age)
            // between calls instead of racing an idle, draining connection.
            .keepAliveTime(60, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            // Recover transient UNAVAILABLE (GOAWAY connection recycling) automatically.
            .enableRetry();
    if (properties.plaintext()) {
      builder.usePlaintext();
    }
    this.channel = builder.build();
  }

  @Bean
  ManagedChannel spiceDbChannel() {
    return channel;
  }

  @Bean
  PermissionsServiceBlockingStub permissionsService(SpiceDbProperties properties) {
    return PermissionsServiceGrpc.newBlockingStub(channel)
        .withCallCredentials(new BearerToken(properties.presharedKey()));
  }

  @Bean
  SchemaServiceBlockingStub schemaService(SpiceDbProperties properties) {
    return SchemaServiceGrpc.newBlockingStub(channel)
        .withCallCredentials(new BearerToken(properties.presharedKey()));
  }

  @PreDestroy
  void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }
}
