package com.lprevidente.orgcraft;

import com.lprevidente.orgcraft.authorization.AuthorizationTestConfig;
import com.lprevidente.orgcraft.tenancy.api.TenantContext;
import com.lprevidente.orgcraft.tenancy.api.TenantId;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthorizationTestConfig.class)
public abstract class BaseIntegrationTest {

  protected static final String TEST_TENANT_SLUG = "test-tenant";
  protected static final String TEST_TENANT = "00000000-0000-0000-0000-000000000001";
  protected static final TenantId TEST_TENANT_ID = TenantId.of(UUID.fromString(TEST_TENANT));

  @Autowired protected MockMvcTester mockMvcTester;
  @Autowired protected JsonMapper jsonMapper;

  @BeforeEach
  void setTenantContext() {
    TenantContext.set(TEST_TENANT_ID);
  }

  @AfterEach
  void clearTenantContext() {
    TenantContext.clear();
  }
}
