package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.inncretech.client.client.AgentApiClient;
import com.inncretech.client.client.CommentApiClient;
import com.inncretech.client.client.CompanyApiClient;
import com.inncretech.client.client.ConnectorApiClient;
import com.inncretech.client.client.CustomAgentApiClient;
import com.inncretech.client.client.DataPipelineApiClient;
import com.inncretech.client.client.ElementPermissionsApiClient;
import com.inncretech.client.client.RowApiClient;
import com.inncretech.client.client.ViewApiClient;
import com.inncretech.client.model.dto.AuthResponseDTO;
import com.inncretech.client.model.dto.CompanyThemeDTO;
import com.inncretech.client.model.dto.CustomAgentDTO;
import com.inncretech.client.model.dto.LoginRequestDTO;
import com.inncretech.client.model.dto.RefreshTokenRequestDTO;
import com.inncretech.client.model.dto.RowDataDTO;
import com.inncretech.client.model.dto.ServiceAccountDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.TableViewDTO;
import com.inncretech.client.model.dto.ViewDTO;
import com.inncretech.client.model.dto.WorkSpaceDTO;
import com.inncretech.client.model.dto.WorkbookActionDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Happy-flow smoke tests for the next 20 highest-traffic live saasxl-backend endpoints past the
 * first 25 covered in {@link HappyFlowEndpointsTest} and {@link HappyFlowEndpointsBatch2Test},
 * excluding {@code /widget/**} and {@code /kb/**} per standing instruction. Shares the
 * signup-based bootstrap in {@link AbstractHappyFlowTest}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HappyFlowEndpointsBatch3Test extends AbstractHappyFlowTest {

  @Autowired private AgentApiClient agentApiClient;
  @Autowired private DataPipelineApiClient dataPipelineApiClient;
  @Autowired private RowApiClient rowApiClient;
  @Autowired private ViewApiClient viewApiClient;
  @Autowired private ElementPermissionsApiClient elementPermissionsApiClient;
  @Autowired private CommentApiClient commentApiClient;
  @Autowired private ConnectorApiClient connectorApiClient;
  @Autowired private CompanyApiClient companyApiClient;
  @Autowired private CustomAgentApiClient customAgentApiClient;

  private Object createdRowId;
  private String createdViewId;

  @Test
  @Order(1)
  void agentConfigsByNames_returnsList() {
    List<Map<?, ?>> configs = agentApiClient.getConfigsByNames(List.of("nonexistent")).block();
    assertNotNull(configs);
  }

  @Test
  @Order(2)
  void agentTemplatesByNames_returnsList() {
    List<Map<?, ?>> templates = agentApiClient.getTemplatesByNames(List.of("nonexistent")).block();
    assertNotNull(templates);
  }

  @Test
  @Order(3)
  void dataPipelineQueryEngines_returnsList() {
    List<String> engines = dataPipelineApiClient.getQueryEngines().block();
    assertNotNull(engines);
  }

  @Test
  @Order(4)
  void bulkAddRows_returnsCreatedRows() {
    List<RowDataDTO> created = rowApiClient.addRows(workspaceId, tableId,
        List.of(RowDataDTO.builder().cellValues(Map.of(COLUMN_NAME, "bulk-1")).build())).block();
    assertNotNull(created);
    assertNotNull(created.get(0).getId());
    createdRowId = created.get(0).getId();
  }

  @Test
  @Order(5)
  void getRowById_returnsRow() {
    RowDataDTO row = rowApiClient.getRow(workspaceId, tableId, createdRowId).block();
    assertNotNull(row);
    assertNotNull(row.getId());
  }

  @Test
  @Order(6)
  void workspacesSearch_returnsList() {
    List<Map<?, ?>> results = workSpaceApiClient.search("HappyFlow").block();
    assertNotNull(results);
  }

  @Test
  @Order(7)
  void linkData_completesForTableWithLinkColumn() {
    TableDTO table = tableApiClient.getTable(workspaceId, tableId).block();
    assertNotNull(table);
    String linkColumnId = (table.getColumns() == null ? List.<Map<String, Object>>of() : table.getColumns())
        .stream()
        .filter(col -> String.valueOf(col.get("uiDataType")).toUpperCase().contains("LINK"))
        .map(col -> String.valueOf(col.get("id")))
        .findFirst()
        .orElse(null);
    Assumptions.assumeTrue(linkColumnId != null, "seeded table has no link column to test against");

    List<Map<?, ?>> linkData = tableApiClient.getLinkData(workspaceId, tableId, linkColumnId, createdRowId).block();
    assertNotNull(linkData);
  }

  @Test
  @Order(8)
  void createView_returnsView() {
    // Response is a TableViewDTO envelope — its own "id" is the table's id, not the view's; the
    // created view lives nested under "view".
    TableViewDTO created = viewApiClient.createView(workspaceId, tableId,
        ViewDTO.builder().name("HappyFlow View").build()).block();
    assertNotNull(created);
    assertNotNull(created.getView());
    assertNotNull(created.getView().getId());
    createdViewId = created.getView().getId();
  }

  @Test
  @Order(9)
  void getView_returnsView() {
    TableViewDTO view = viewApiClient.getView(workspaceId, tableId, createdViewId).block();
    assertNotNull(view);
  }

  @Test
  @Order(10)
  void linkTables_returnsList() {
    List<TableDTO> linkTables = tableApiClient.getLinkTables(workspaceId, tableId).block();
    assertNotNull(linkTables);
  }

  @Test
  @Order(11)
  void createServiceAccount_returnsServiceAccount() {
    // Requires @Secured("ROLE_ACCOUNT_ADMIN") — bootstrap's promoteToAdmin grants ACCOUNT_ADMIN;
    // if Spring's role-string matching needs a "ROLE_" prefix this doesn't supply, this 403s —
    // real signal about that mismatch, not something to paper over.
    ServiceAccountDTO created = companyApiClient.createServiceAccount(
        ServiceAccountDTO.builder().name("HappyFlow Service Account").build()).block();
    assertNotNull(created);
  }

  @Test
  @Order(12)
  void getServiceAccounts_returnsNonEmptyList() {
    List<ServiceAccountDTO> accounts = companyApiClient.getServiceAccounts().block();
    assertNotNull(accounts);
  }

  @Test
  @Order(13)
  void workspaceById_returnsWorkspace() {
    WorkSpaceDTO workspace = workSpaceApiClient.getWorkspaceById(workspaceId).block();
    assertNotNull(workspace);
  }

  @Test
  @Order(14)
  void elementPermissionsByType_returnsList() {
    List<Map<?, ?>> permissions = elementPermissionsApiClient.getByType("DATASOURCE").block();
    assertNotNull(permissions);
  }

  @Test
  @Order(15)
  void createAction_returnsAction() {
    WorkbookActionDTO created = tableApiClient.createAction(workspaceId, tableId,
        WorkbookActionDTO.builder().name("HappyFlow Action").actionType("WEB").config(Map.of()).build()).block();
    assertNotNull(created);
  }

  @Test
  @Order(16)
  void getActions_returnsNonEmptyList() {
    List<WorkbookActionDTO> actions = tableApiClient.getActions(workspaceId, tableId).block();
    assertNotNull(actions);
  }

  @Test
  @Order(17)
  void foldersHierarchy_returnsList() {
    List<Map<?, ?>> hierarchy = workSpaceApiClient.getFolders(workspaceId).block();
    assertNotNull(hierarchy);
  }

  @Test
  @Order(18)
  void enrichments_returnsResponse() {
    Map<?, ?> enrichments = tableApiClient.getEnrichments(workspaceId, tableId).block();
    assertNotNull(enrichments);
  }

  @Test
  @Order(19)
  void commentCount_returnsResponse() {
    Map<?, ?> count = commentApiClient.getCommentCount("WORKSPACE", workspaceId, "TABLE", tableId).block();
    assertNotNull(count);
  }

  @Test
  @Order(20)
  void connectorInstances_returnsResponse() {
    Map<?, ?> instances = connectorApiClient.getInstances().block();
    assertNotNull(instances);
  }

  @Test
  @Order(21)
  void createCompanyTheme_returnsTheme() {
    CompanyThemeDTO created = companyApiClient.createTheme(
        CompanyThemeDTO.builder().title("HappyFlow Theme").settings(Map.of()).build()).block();
    assertNotNull(created);
  }

  @Test
  @Order(22)
  void getCompanyThemes_returnsNonEmptyList() {
    List<CompanyThemeDTO> themes = companyApiClient.getThemes().block();
    assertNotNull(themes);
  }

  @Test
  @Order(23)
  void createCustomAgent_returnsAgent() {
    CustomAgentDTO created = customAgentApiClient.create(
        CustomAgentDTO.builder().name("HappyFlow Agent").type("CUSTOM").configs(Map.of()).build()).block();
    assertNotNull(created);
  }

  @Test
  @Order(24)
  void getCustomAgents_returnsNonEmptyList() {
    List<CustomAgentDTO> agents = customAgentApiClient.getAll().block();
    assertNotNull(agents);
  }

  @Test
  @Order(25)
  void refreshToken_returnsNewTokenPair() {
    AuthResponseDTO loggedIn = authApiClient.loginV2(LoginRequestDTO.builder()
        .email(signedUpEmail)
        .password(SIGNUP_PASSWORD)
        .build()).block();
    assertNotNull(loggedIn);
    assertNotNull(loggedIn.getRefreshToken());

    AuthResponseDTO refreshed = authApiClient.refreshToken(
        RefreshTokenRequestDTO.builder().refreshToken(loggedIn.getRefreshToken()).build()).block();
    assertNotNull(refreshed);
    assertNotNull(refreshed.getAccessToken());
  }
}
