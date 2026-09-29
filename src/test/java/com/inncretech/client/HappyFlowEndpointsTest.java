package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inncretech.client.auth.TokenManager;
import com.inncretech.client.client.AuthApiClient;
import com.inncretech.client.client.ChartApiClient;
import com.inncretech.client.client.ConfigApiClient;
import com.inncretech.client.client.RowApiClient;
import com.inncretech.client.client.TableApiClient;
import com.inncretech.client.client.TableLinkApiClient;
import com.inncretech.client.client.UserApiClient;
import com.inncretech.client.client.WorkSpaceApiClient;
import com.inncretech.client.model.dto.ChartDataAbstractionRequestDTO;
import com.inncretech.client.model.dto.ColumnDTO;
import com.inncretech.client.model.dto.ConfigPropertyDTO;
import com.inncretech.client.model.dto.LinkRecordDTO;
import com.inncretech.client.model.dto.LoginRequestDTO;
import com.inncretech.client.model.dto.NoCoQueryDTO;
import com.inncretech.client.model.dto.QueryOptionDTO;
import com.inncretech.client.model.dto.RequestPageDetailDTO;
import com.inncretech.client.model.dto.RowDataDTO;
import com.inncretech.client.model.dto.SignUpRequestDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.UserDTO;
import com.inncretech.client.model.dto.WorkSpaceDTO;
import com.inncretech.client.util.TestRetry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Happy-flow smoke tests for the 10 highest-traffic live saasxl-backend endpoints, derived from
 * a production API-hit log plus a scan of the current controllers. Each test makes a real HTTP
 * call against the backend at {@code backend.base-url}, which must be running locally — the
 * bootstrap below signs up its own company/user, so no pre-seeded data is required. Do not point
 * this suite at a shared/staging/prod backend: the bootstrap depends on two unguarded
 * automation-only endpoints that bypass real email verification and grant admin.
 */
// runner.enabled=false: ApiExecutionRunner is a CommandLineRunner and would otherwise fire on
// context startup, logging in with the static admin creds (unrelated to this class's own
// signup-based bootstrap) and firing CSV-driven traffic alongside these tests.
@SpringBootTest(classes = ApiClientApplication.class, properties = "runner.enabled=false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HappyFlowEndpointsTest {

  private static final String SIGNUP_PASSWORD = "Demo12#$";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired private TokenManager tokenManager;
  @Autowired private AuthApiClient authApiClient;
  @Autowired private UserApiClient userApiClient;
  @Autowired private WorkSpaceApiClient workSpaceApiClient;
  @Autowired private RowApiClient rowApiClient;
  @Autowired private TableApiClient tableApiClient;
  @Autowired private TableLinkApiClient tableLinkApiClient;
  @Autowired private ChartApiClient chartApiClient;
  @Autowired private ConfigApiClient configApiClient;

  private static final String COLUMN_NAME = "col1";

  private String signedUpEmail;
  private String workspaceId;
  private String tableId;
  private Object createdRowId;

  @Test
  @Order(1)
  void bootstrap_signsUpVerifiesAndSeedsWorkspaceAndTable() {
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
    // produces intermittent "table not found" errors below. Same wait DataSourceConnection.setup()
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

  @Test
  @Order(2)
  void cursor_returnsPageOfRows() {
    Map<?, ?> page = workSpaceApiClient.getCursorData(workspaceId, tableId,
        QueryOptionDTO.builder().requestPageDetails(RequestPageDetailDTO.builder().build()).build()).block();
    assertNotNull(page);
  }

  @Test
  @Order(3)
  void addRow_createsRowWithId() {
    RowDataDTO created = rowApiClient.addRow(
        workspaceId, tableId, RowDataDTO.builder().cellValues(Map.of(COLUMN_NAME, "hello")).build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    createdRowId = created.getId();
  }

  @Test
  @Order(4)
  void getCurrentUser_returnsAuthenticatedUser() {
    UserDTO user = userApiClient.getCurrentUser().block();
    assertNotNull(user);
    assertNotNull(user.getId());
  }

  @Test
  @Order(5)
  void linkRecord_completesForTableWithLinkColumn() {
    TableDTO table = tableApiClient.getTable(workspaceId, tableId).block();
    assertNotNull(table);
    // Relation/link columns are identified by uiDataType on the raw column map; skip when the
    // seeded table has none, since a happy-path link requires a real link column to target.
    String linkColumnId = (table.getColumns() == null ? List.<Map<String, Object>>of() : table.getColumns())
        .stream()
        .filter(col -> String.valueOf(col.get("uiDataType")).toUpperCase().contains("LINK"))
        .map(col -> String.valueOf(col.get("id")))
        .findFirst()
        .orElse(null);
    Assumptions.assumeTrue(linkColumnId != null, "seeded table has no link column to test against");

    tableLinkApiClient.linkRecord(workspaceId, tableId, List.of(LinkRecordDTO.builder()
        .sourceRecordId(createdRowId)
        .destinationRecordId(createdRowId)
        .linkColumnId(linkColumnId)
        .tableId(tableId)
        .build())).block();
  }

  @Test
  @Order(6)
  void getTable_returnsTableDefinition() {
    TableDTO table = tableApiClient.getTable(workspaceId, tableId).block();
    assertNotNull(table);
    assertNotNull(table.getId());
  }

  @Test
  @Order(7)
  void chartAbstract_returnsAbstractedData() {
    Map<?, ?> chart = chartApiClient.getChartAbstract(ChartDataAbstractionRequestDTO.builder()
        .tableId(tableId)
        .seriesType("TABLE")
        .columnY(List.of())
        .build()).block();
    assertNotNull(chart);
  }

  @Test
  @Order(8)
  void login_returnsAuthenticatedUser() {
    UserDTO user = authApiClient.login(LoginRequestDTO.builder()
        .email(signedUpEmail)
        .password(SIGNUP_PASSWORD)
        .build()).block();
    assertNotNull(user);
    assertNotNull(user.getId());
  }

  @Test
  @Order(9)
  void externalData_returnsPage() {
    // The table's underlying Postgres storage is provisioned asynchronously and can flip
    // between queryable and not between calls; retry the real assertion, not just a one-time
    // setup probe, since the race isn't confined to the table's first use.
    TestRetry.untilSucceeds(() -> {
      Map<?, ?> page = tableApiClient.getExternalData(workspaceId, tableId,
          QueryOptionDTO.builder().requestPageDetails(RequestPageDetailDTO.builder().build()).build()).block();
      assertNotNull(page);
    });
  }

  @Test
  @Order(10)
  void executeQuery_returnsResult() {
    TestRetry.untilSucceeds(() -> {
      Map<?, ?> result = tableApiClient.executeQuery(
          workspaceId, tableId, NoCoQueryDTO.builder().value("SELECT 1").build()).block();
      assertNotNull(result);
    });
  }

  @Test
  @Order(11)
  void getAllWorkspaces_returnsNonEmptyList() {
    List<WorkSpaceDTO> workspaces = workSpaceApiClient.getAllWorkspaces().block();
    assertNotNull(workspaces);
    assertTrue(!workspaces.isEmpty());
  }

  @Test
  @Order(12)
  void cleanup_deletesRowCreatedByThisTest() {
    if (createdRowId != null) {
      rowApiClient.deleteRow(workspaceId, tableId, createdRowId).block();
    }
  }
}
