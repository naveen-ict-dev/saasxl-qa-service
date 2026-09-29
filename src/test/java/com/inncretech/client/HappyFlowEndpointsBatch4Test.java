package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inncretech.client.client.CustomAgentApiClient;
import com.inncretech.client.client.DataSourceApiClient;
import com.inncretech.client.client.ElementPermissionsApiClient;
import com.inncretech.client.client.RowApiClient;
import com.inncretech.client.client.TableApiClient;
import com.inncretech.client.model.dto.CustomAgentDTO;
import com.inncretech.client.model.dto.RearrangePositionDTO;
import com.inncretech.client.model.dto.RowDataDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.util.TestRetry;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Happy-flow smoke tests for the custom-agent lifecycle/extras, element-permission sharing, table
 * column/cache extras, and datasource schema/cron extras — the remaining endpoints on controllers
 * already touched by earlier batches. Shares the signup-based bootstrap in
 * {@link AbstractHappyFlowTest}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HappyFlowEndpointsBatch4Test extends AbstractHappyFlowTest {

  @Autowired private CustomAgentApiClient customAgentApiClient;
  @Autowired private ElementPermissionsApiClient elementPermissionsApiClient;
  @Autowired private DataSourceApiClient dataSourceApiClient;
  @Autowired private RowApiClient rowApiClient;

  private String customAgentId;
  private String lifecycleAgentId;
  private String bulkAgentId;
  private String bulkSharedEmail;
  private String permissionId;
  private String columnId;
  private Object seededRowId;
  private Long dataSourceId;

  @Test
  @Order(1)
  void createCustomAgentFixture_returnsAgent() {
    CustomAgentDTO created = customAgentApiClient.create(CustomAgentDTO.builder()
        .name("HappyFlow Fixture Agent " + UUID.randomUUID())
        .type("CUSTOM")
        .configs(Map.of())
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    customAgentId = created.getId();
  }

  @Test
  @Order(2)
  void createCustomAgentLifecycle_returnsAgent() {
    CustomAgentDTO created = customAgentApiClient.create(CustomAgentDTO.builder()
        .name("HappyFlow Lifecycle Agent " + UUID.randomUUID())
        .type("CUSTOM")
        .configs(Map.of())
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    lifecycleAgentId = created.getId();
  }

  @Test
  @Order(3)
  void getCustomAgentById_returnsAgent() {
    CustomAgentDTO agent = customAgentApiClient.getById(lifecycleAgentId).block();
    assertNotNull(agent);
    assertEquals(lifecycleAgentId, agent.getId());
  }

  @Test
  @Order(4)
  void updateCustomAgent_returnsUpdatedAgent() {
    // CustomAgentService.updateCustomAgent maps the request DTO straight onto the managed entity
    // (customAgentMapper.updateEntity) — omitting id here 500s with a Hibernate
    // "identifier ... was altered" error, so id must be echoed back explicitly on every update.
    CustomAgentDTO updated = customAgentApiClient.update(lifecycleAgentId, CustomAgentDTO.builder()
        .id(lifecycleAgentId)
        .name("HappyFlow Lifecycle Agent Updated")
        .type("CUSTOM")
        .configs(Map.of())
        .build()).block();
    assertNotNull(updated);
    assertEquals("HappyFlow Lifecycle Agent Updated", updated.getName());
  }

  @Test
  @Order(5)
  void getActivePublicCustomAgentsByIds_returnsList() {
    List<CustomAgentDTO> agents = customAgentApiClient.getActivePublicByIds(List.of(customAgentId)).block();
    assertNotNull(agents);
  }

  @Test
  @Order(6)
  void getCustomAgentWorkbooks_returnsResponse() {
    Map<?, ?> workbooks = customAgentApiClient.getWorkbooks(customAgentId).block();
    assertNotNull(workbooks);
  }

  @Test
  @Order(7)
  void getCustomAgentRuns_returnsResponse() {
    Map<?, ?> runs = customAgentApiClient.getRuns(customAgentId).block();
    assertNotNull(runs);
  }

  @Test
  @Order(8)
  void deleteCustomAgentLifecycle_succeeds() {
    customAgentApiClient.delete(lifecycleAgentId).block();
  }

  @Test
  @Order(9)
  void createElementPermission_returnsPermission() {
    // CUSTOM_AGENT is a registered ElementPermissionStrategy (NoCoDbConstants.CUSTOM_AGENT) —
    // confirmed via CustomAgentPermissionStrategy.getElementType(), unlike a bare "TABLE" type
    // which has no strategy bean at all and would 400 "Invalid element type". Sharing with the
    // signed-up user's own email 400s "User already has access to this element" — creating the
    // agent already grants its creator an implicit CREATOR permission, confirmed via backend log
    // (ElementPermissionServiceRefactored.validateExistingPermissions) — so this shares with a
    // second, never-before-granted email instead, the same as inviting a teammate.
    Map<?, ?> created = elementPermissionsApiClient.create(Map.of(
        "elementType", "CUSTOM_AGENT",
        "elementId", customAgentId,
        "email", "shared-" + UUID.randomUUID() + "@example.com",
        "permission", "EDITOR")).block();
    assertNotNull(created);
    assertNotNull(created.get("id"));
    permissionId = String.valueOf(created.get("id"));
  }

  @Test
  @Order(10)
  void getElementPermissions_returnsList() {
    List<Map<?, ?>> permissions = elementPermissionsApiClient.getForElement("CUSTOM_AGENT", customAgentId).block();
    assertNotNull(permissions);
    assertTrue(!permissions.isEmpty());
  }

  @Test
  @Order(11)
  void getMatchedPermission_returnsPermission() {
    Map<?, ?> matched = elementPermissionsApiClient.getMatchedPermission("CUSTOM_AGENT", customAgentId).block();
    assertNotNull(matched);
  }

  @Test
  @Order(12)
  void getSharedUsers_returnsList() {
    List<Map<?, ?>> sharedUsers = elementPermissionsApiClient.getSharedUsers("CUSTOM_AGENT").block();
    assertNotNull(sharedUsers);
  }

  @Test
  @Order(13)
  void getLinkAccess_returnsAccess() {
    Map<?, ?> access = elementPermissionsApiClient.getLinkAccess("CUSTOM_AGENT", customAgentId).block();
    assertNotNull(access);
    assertNotNull(access.get("scope"));
  }

  @Test
  @Order(14)
  void setLinkAccess_returnsUpdatedAccess() {
    Map<?, ?> access = elementPermissionsApiClient.setLinkAccess("CUSTOM_AGENT", customAgentId,
        Map.of("scope", "COMPANY")).block();
    assertNotNull(access);
    assertEquals("COMPANY", access.get("scope"));
  }

  @Test
  @Order(15)
  void createBulkTargetAgent_returnsAgent() {
    CustomAgentDTO created = customAgentApiClient.create(CustomAgentDTO.builder()
        .name("HappyFlow Bulk Agent " + UUID.randomUUID())
        .type("CUSTOM")
        .configs(Map.of())
        .build()).block();
    assertNotNull(created);
    bulkAgentId = created.getId();
  }

  @Test
  @Order(16)
  void bulkCreateElementPermissions_returnsSuccessfulOperations() {
    // Same "already has access" constraint as createElementPermission_returnsPermission above —
    // share with a fresh email, not the agent's own creator.
    bulkSharedEmail = "shared-" + UUID.randomUUID() + "@example.com";
    Map<?, ?> response = elementPermissionsApiClient.bulkCreate(Map.of(
        "permissions", List.of(Map.of(
            "elementType", "CUSTOM_AGENT",
            "elementId", bulkAgentId,
            "email", bulkSharedEmail,
            "permission", "VIEWER")))).block();
    assertNotNull(response);
    List<?> successful = (List<?>) response.get("successfulOperations");
    assertNotNull(successful);
    assertFalse(successful.isEmpty());
  }

  @Test
  @Order(17)
  void bulkDeleteElementPermissions_returnsSuccessfulOperations() {
    // The element now carries two permissions — the creator's own implicit one and the shared
    // one just created — so the target has to be matched by email, not assumed to be index 0.
    List<Map<?, ?>> permissions = elementPermissionsApiClient.getForElement("CUSTOM_AGENT", bulkAgentId).block();
    assertNotNull(permissions);
    String bulkPermissionId = permissions.stream()
        .filter(p -> bulkSharedEmail.equals(p.get("email")))
        .map(p -> String.valueOf(p.get("id")))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("bulk-created permission not found"));

    Map<?, ?> response = elementPermissionsApiClient.bulkDelete(
        Map.of("ids", List.of(bulkPermissionId))).block();
    assertNotNull(response);
    List<?> successful = (List<?>) response.get("successfulOperations");
    assertNotNull(successful);
    assertFalse(successful.isEmpty());
  }

  @Test
  @Order(18)
  void deleteElementPermission_succeeds() {
    elementPermissionsApiClient.delete(permissionId).block();
  }

  @Test
  @Order(19)
  void columnCatalog_returnsCatalog() {
    Map<?, ?> catalog = tableApiClient.getColumnCatalog(workspaceId, tableId).block();
    assertNotNull(catalog);
  }

  @Test
  @Order(20)
  void getColumn_returnsColumn() {
    TableDTO table = tableApiClient.getTable(workspaceId, tableId).block();
    assertNotNull(table);
    columnId = table.getColumns().stream()
        .filter(col -> COLUMN_NAME.equals(col.get("name")))
        .map(col -> String.valueOf(col.get("id")))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("fixture column not found on table"));

    var column = tableApiClient.getColumn(workspaceId, tableId, columnId).block();
    assertNotNull(column);
    assertEquals(columnId, column.getId());
  }

  @Test
  @Order(21)
  void searchColumnValues_returnsList() {
    List<Map<?, ?>> values = tableApiClient.searchColumnValues(workspaceId, tableId, columnId, "bulk").block();
    assertNotNull(values);
  }

  @Test
  @Order(22)
  void refreshCache_succeeds() {
    tableApiClient.refreshCache(workspaceId, tableId).block();
  }

  @Test
  @Order(23)
  void rearrangePosition_returnsCreatedRow() {
    // getPositionsQuery requires a beforeId or afterId to compute the new position — with neither
    // set it 400s "Invalid position movement" (confirmed via backend log/source,
    // TableServiceUtils.getPositionsQuery) — so a reference row has to exist first.
    RowDataDTO seedRow = rowApiClient.addRow(workspaceId, tableId,
        RowDataDTO.builder().cellValues(Map.of(COLUMN_NAME, "seed-for-rearrange")).build()).block();
    assertNotNull(seedRow);
    seededRowId = seedRow.getId();

    RowDataDTO row = tableApiClient.rearrangePosition(workspaceId, tableId, RearrangePositionDTO.builder()
        .isItNewRow(true)
        .afterId(seededRowId)
        .row(RowDataDTO.builder().cellValues(Map.of(COLUMN_NAME, "rearranged")).build())
        .build()).block();
    assertNotNull(row);
    assertNotNull(row.getId());
  }

  @Test
  @Order(24)
  void addDatasourceFixture_returnsCreatedDatasource() {
    Map<?, ?> created = dataSourceApiClient.addDatasource(Map.of(
        "name", "happyflow4_" + UUID.randomUUID().toString().replace("-", ""),
        "type", "SQL",
        "driverName", "org.postgresql.Driver",
        "username", "postgres",
        "password", "Demo12#$",
        "properties", Map.of(
            "dataSourceProvider", "postgresql",
            "host", "localhost",
            "port", "5432",
            "dbName", "saasxl"))).block();
    assertNotNull(created);
    dataSourceId = Long.valueOf(String.valueOf(created.get("id")));

    TestRetry.untilSucceeds(Duration.ofMinutes(2), Duration.ofSeconds(5), () -> {
      Map<?, ?> status = dataSourceApiClient.getSyncSchemaStatus(dataSourceId).block();
      assertNotNull(status);
      assertEquals("COMPLETED", status.get("status"));
    });
  }

  @Test
  @Order(25)
  void getDataSourceById_returnsDatasource() {
    Map<?, ?> datasource = dataSourceApiClient.getDataSourceById(dataSourceId).block();
    assertNotNull(datasource);
    assertEquals(dataSourceId, Long.valueOf(String.valueOf(datasource.get("id"))));
  }

  @Test
  @Order(26)
  void getAllSchemaChanges_returnsResponse() {
    Map<?, ?> response = dataSourceApiClient.getAllSchemaChanges().block();
    assertNotNull(response);
  }

  @Test
  @Order(27)
  void getSchemaChangesForDataSource_returnsResponse() {
    Map<?, ?> response = dataSourceApiClient.getSchemaChangesForDataSource(dataSourceId).block();
    assertNotNull(response);
  }

  @Test
  @Order(28)
  void getSchema_returnsSchema() {
    Map<?, ?> schema = dataSourceApiClient.getSchema(dataSourceId).block();
    assertNotNull(schema);
  }

  @Test
  @Order(29)
  void getDescriptions_returnsResponse() {
    Map<?, ?> descriptions = dataSourceApiClient.getDescriptions(dataSourceId).block();
    assertNotNull(descriptions);
  }

  @Test
  @Order(30)
  void setupSchemaSyncCron_returnsCron() {
    // Validated with org.quartz.CronExpression.isValidExpression (confirmed via backend source,
    // DataSourceSchemaSyncCronServiceImpl) — needs Quartz's 6-field seconds-first form AND exactly
    // one of day-of-month/day-of-week as "?"; a standard 5-field crontab expression, and even
    // "* * * * * *" with both date fields as "*", both 400 "Invalid cron expression".
    Map<?, ?> cron = dataSourceApiClient.setupSchemaSyncCron(dataSourceId, "0 0 0 * * ?").block();
    assertNotNull(cron);
  }

  @Test
  @Order(31)
  void getSchemaSyncCron_returnsCron() {
    Map<?, ?> cron = dataSourceApiClient.getSchemaSyncCron(dataSourceId).block();
    assertNotNull(cron);
  }

  @Test
  @Order(32)
  void deleteSchemaSyncCron_succeeds() {
    dataSourceApiClient.deleteSchemaSyncCron(dataSourceId).block();
  }

  @Test
  @Order(33)
  void deleteDataSource_succeeds() {
    dataSourceApiClient.deleteDataSource(dataSourceId).block();
  }
}
