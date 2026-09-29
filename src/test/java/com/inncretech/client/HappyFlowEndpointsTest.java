package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inncretech.client.client.ChartApiClient;
import com.inncretech.client.client.RowApiClient;
import com.inncretech.client.client.TableLinkApiClient;
import com.inncretech.client.client.UserApiClient;
import com.inncretech.client.model.dto.ChartDataAbstractionRequestDTO;
import com.inncretech.client.model.dto.LinkRecordDTO;
import com.inncretech.client.model.dto.LoginRequestDTO;
import com.inncretech.client.model.dto.NoCoQueryDTO;
import com.inncretech.client.model.dto.QueryOptionDTO;
import com.inncretech.client.model.dto.RequestPageDetailDTO;
import com.inncretech.client.model.dto.RowDataDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.UserDTO;
import com.inncretech.client.model.dto.WorkSpaceDTO;
import com.inncretech.client.util.TestRetry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Happy-flow smoke tests for the first 10 highest-traffic live saasxl-backend endpoints, derived
 * from a production API-hit log plus a scan of the current controllers. Each test makes a real
 * HTTP call against the backend at {@code backend.base-url}, which must be running locally — the
 * inherited bootstrap signs up its own company/user, so no pre-seeded data is required.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HappyFlowEndpointsTest extends AbstractHappyFlowTest {

  @Autowired private UserApiClient userApiClient;
  @Autowired private RowApiClient rowApiClient;
  @Autowired private TableLinkApiClient tableLinkApiClient;
  @Autowired private ChartApiClient chartApiClient;

  private Object createdRowId;

  @Test
  @Order(1)
  void cursor_returnsPageOfRows() {
    Map<?, ?> page = workSpaceApiClient.getCursorData(workspaceId, tableId,
        QueryOptionDTO.builder().requestPageDetails(RequestPageDetailDTO.builder().build()).build()).block();
    assertNotNull(page);
  }

  @Test
  @Order(2)
  void addRow_createsRowWithId() {
    RowDataDTO created = rowApiClient.addRow(
        workspaceId, tableId, RowDataDTO.builder().cellValues(Map.of(COLUMN_NAME, "hello")).build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    createdRowId = created.getId();
  }

  @Test
  @Order(3)
  void getCurrentUser_returnsAuthenticatedUser() {
    UserDTO user = userApiClient.getCurrentUser().block();
    assertNotNull(user);
    assertNotNull(user.getId());
  }

  @Test
  @Order(4)
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
  @Order(5)
  void getTable_returnsTableDefinition() {
    TableDTO table = tableApiClient.getTable(workspaceId, tableId).block();
    assertNotNull(table);
    assertNotNull(table.getId());
  }

  @Test
  @Order(6)
  void chartAbstract_returnsAbstractedData() {
    Map<?, ?> chart = chartApiClient.getChartAbstract(ChartDataAbstractionRequestDTO.builder()
        .tableId(tableId)
        .seriesType("TABLE")
        .columnY(List.of())
        .build()).block();
    assertNotNull(chart);
  }

  @Test
  @Order(7)
  void login_returnsAuthenticatedUser() {
    UserDTO user = authApiClient.login(LoginRequestDTO.builder()
        .email(signedUpEmail)
        .password(SIGNUP_PASSWORD)
        .build()).block();
    assertNotNull(user);
    assertNotNull(user.getId());
  }

  @Test
  @Order(8)
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
  @Order(9)
  void executeQuery_returnsResult() {
    TestRetry.untilSucceeds(() -> {
      Map<?, ?> result = tableApiClient.executeQuery(
          workspaceId, tableId, NoCoQueryDTO.builder().value("SELECT 1").build()).block();
      assertNotNull(result);
    });
  }

  @Test
  @Order(10)
  void getAllWorkspaces_returnsNonEmptyList() {
    List<WorkSpaceDTO> workspaces = workSpaceApiClient.getAllWorkspaces().block();
    assertNotNull(workspaces);
    assertTrue(!workspaces.isEmpty());
  }

  @Test
  @Order(11)
  void cleanup_deletesRowCreatedByThisTest() {
    if (createdRowId != null) {
      rowApiClient.deleteRow(workspaceId, tableId, createdRowId).block();
    }
  }
}
