package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inncretech.client.auth.TokenManager;
import com.inncretech.client.client.AuthApiClient;
import com.inncretech.client.client.ConfigApiClient;
import com.inncretech.client.client.TableApiClient;
import com.inncretech.client.client.WorkSpaceApiClient;
import com.inncretech.client.model.dto.ColumnDTO;
import com.inncretech.client.model.dto.ConfigPropertyDTO;
import com.inncretech.client.model.dto.LoginRequestDTO;
import com.inncretech.client.model.dto.SignUpRequestDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.WorkSpaceDTO;
import com.inncretech.client.util.TestRetry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Shared signup-based bootstrap for happy-flow test classes: signs up a fresh company/user, waits
 * for its data warehouse to be provisioned, and creates a table with one writable column — all
 * self-contained, no pre-seeded backend state required. Subclasses add their own endpoint tests
 * on top of the {@code workspaceId}/{@code tableId} this sets up. Do not point this at a
 * shared/staging/prod backend: the bootstrap depends on two unguarded automation-only endpoints
 * that bypass real email verification and grant admin.
 */
// runner.enabled=false: ApiExecutionRunner is a CommandLineRunner and would otherwise fire on
// context startup, logging in with the static admin creds (unrelated to this class's own
// signup-based bootstrap) and firing CSV-driven traffic alongside these tests.
@SpringBootTest(classes = ApiClientApplication.class, properties = "runner.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractHappyFlowTest {

  protected static final String SIGNUP_PASSWORD = "Demo12#$";
  protected static final String COLUMN_NAME = "col1";
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired protected TokenManager tokenManager;
  @Autowired protected AuthApiClient authApiClient;
  @Autowired protected WorkSpaceApiClient workSpaceApiClient;
  @Autowired protected TableApiClient tableApiClient;
  @Autowired protected ConfigApiClient configApiClient;

  protected String signedUpEmail;
  protected String workspaceId;
  protected String tableId;

  @BeforeAll
  void bootstrap() {
    String username = UUID.randomUUID().toString().replace("-", "");
    signedUpEmail = username + "@" + username + ".com";

    authApiClient.signUp(SignUpRequestDTO.builder()
        .firstname(username)
        .lastname(username)
        .email(signedUpEmail)
        .companyName(username)
        .password(SIGNUP_PASSWORD)
        .build()).block();

    String verificationHash = authApiClient.getVerificationHash(signedUpEmail).block();
    assertNotNull(verificationHash, "signup did not produce a verification hash");
    authApiClient.verifyEmail(verificationHash).block();
    authApiClient.promoteToAdmin(signedUpEmail).block();

    String token = authApiClient.loginAndCaptureToken(LoginRequestDTO.builder()
        .email(signedUpEmail)
        .password(SIGNUP_PASSWORD)
        .build()).block();
    assertNotNull(token, "login did not return a Bearer token");
    tokenManager.updateTokens(token, null, 3600L);

    List<WorkSpaceDTO> workspaces = workSpaceApiClient.getAllWorkspaces().block();
    assertNotNull(workspaces, "expected the signup-provisioned Default workspace");
    WorkSpaceDTO defaultWorkspace = workspaces.stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("signup did not provision a workspace"));
    workspaceId = defaultWorkspace.getId();

    // The company's data warehouse (the read/write Postgres connections queries actually run
    // against) is provisioned asynchronously after signup — querying before it's ready is what
    // produces intermittent "table not found" errors. Same wait DataSourceConnection.setup()
    // uses in the QA repo, just via this client instead of the generated Feign one.
    waitForWarehouseConfig("DEFAULT_DB_WAREHOUSE");
    waitForWarehouseConfig("DEFAULT_WAREHOUSE");

    TableDTO created = tableApiClient.createTable(
        workspaceId, TableDTO.builder().title("HappyFlow_" + username).build()).block();
    assertNotNull(created, "failed to create a table to seed test context");
    tableId = created.getId();

    // A fresh table has only auto-generated system columns; addRow needs a real writable one.
    // Creating the table with columns inline 500s server-side today, so this is added separately.
    tableApiClient.addColumn(workspaceId, tableId, ColumnDTO.builder()
        .name(COLUMN_NAME)
        .uiDataType("SINGLE_LINE_TEXT")
        .uiMetadata(Map.of("title", COLUMN_NAME))
        .build()).block();
  }

  private void waitForWarehouseConfig(String configKey) {
    TestRetry.untilSucceeds(Duration.ofMinutes(5), Duration.ofSeconds(5), () -> {
      ConfigPropertyDTO config = configApiClient.getConfigProperty(configKey).block();
      assertNotNull(config, configKey + " config not available yet");
      assertNotNull(config.getConfigValue(), configKey + " config value not available yet");
      Map<?, ?> configMap = OBJECT_MAPPER.readValue(config.getConfigValue(), Map.class);
      if ("DEFAULT_DB_WAREHOUSE".equalsIgnoreCase(configKey)) {
        assertTrue(configMap.containsKey("defaultDbWareHouseId"));
        assertTrue(configMap.containsKey("defaultReadDbWareHouseId"));
      } else {
        assertTrue(configMap.containsKey("defaultWarehouseId"));
      }
    });
  }
}
