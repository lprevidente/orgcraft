package com.lprevidente.orgcraft;

import com.lprevidente.orgcraft.authorization.AuthorizationTestConfig;
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
  @Autowired protected MockMvcTester mockMvcTester;
  @Autowired protected JsonMapper jsonMapper;
}
