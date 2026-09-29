package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inncretech.client.client.AgentSkillApiClient;
import com.inncretech.client.client.BIDashboardApiClient;
import com.inncretech.client.client.BIPageApiClient;
import com.inncretech.client.client.CatalogSearchApiClient;
import com.inncretech.client.client.ClassificationApiClient;
import com.inncretech.client.client.DagApiClient;
import com.inncretech.client.client.ElementLinkApiClient;
import com.inncretech.client.client.FolderApiClient;
import com.inncretech.client.client.WidgetApiClient;
import com.inncretech.client.model.dto.AgentSkillDTO;
import com.inncretech.client.model.dto.AnnotationDTO;
import com.inncretech.client.model.dto.BIDashboardDTO;
import com.inncretech.client.model.dto.BIPageDTO;
import com.inncretech.client.model.dto.DagDTO;
import com.inncretech.client.model.dto.ElementLinkDTO;
import com.inncretech.client.model.dto.FolderDTO;
import com.inncretech.client.model.dto.WidgetDTO;
import com.inncretech.client.model.dto.WidgetParameterDTO;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Happy-flow smoke tests for workspace-list extras, widget parameters, workspace file upload, BI
 * pages/dashboards, orchestration DAGs, agent skills, classifications, catalog search, LLM key,
 * element links, and folders — the remainder of the sheet's live traffic not covered by batches
 * 1-4. Shares the signup-based bootstrap in {@link AbstractHappyFlowTest}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HappyFlowEndpointsBatch5Test extends AbstractHappyFlowTest {

  @Autowired private WidgetApiClient widgetApiClient;
  @Autowired private BIPageApiClient biPageApiClient;
  @Autowired private BIDashboardApiClient biDashboardApiClient;
  @Autowired private DagApiClient dagApiClient;
  @Autowired private AgentSkillApiClient agentSkillApiClient;
  @Autowired private ClassificationApiClient classificationApiClient;
  @Autowired private CatalogSearchApiClient catalogSearchApiClient;
  @Autowired private ElementLinkApiClient elementLinkApiClient;
  @Autowired private FolderApiClient folderApiClient;

  private String widgetId;
  private String biPageId;
  private String dashboardId;
  private Long dagId;
  private Long skillId;
  private String folderId;

  @Test
  @Order(1)
  void getRequestedWorkspaces_returnsList() {
    List<Map<?, ?>> workspaces = workSpaceApiClient.getRequestedWorkspaces().block();
    assertNotNull(workspaces);
  }

  @Test
  @Order(2)
  void getAllPublicWorkspaces_returnsList() {
    List<Map<?, ?>> workspaces = workSpaceApiClient.getAllPublicWorkspaces().block();
    assertNotNull(workspaces);
  }

  @Test
  @Order(3)
  void getInvitedWorkspaces_returnsList() {
    List<Map<?, ?>> workspaces = workSpaceApiClient.getInvitedWorkspaces().block();
    assertNotNull(workspaces);
  }

  @Test
  @Order(4)
  void createWidgetParameter_returnsParameter() {
    // WidgetParameterService.validateRequest requires a non-null defaultValue regardless of
    // type — omitting it 400s "defaultValue not provided." (confirmed via backend source).
    WidgetParameterDTO created = workSpaceApiClient.createWidgetParameter(workspaceId, WidgetParameterDTO.builder()
        .name("HappyFlow Param")
        .key("happyflow_param")
        .type("TEXT")
        .defaultValue("default")
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
  }

  @Test
  @Order(5)
  void getWidgetParameters_returnsResponse() {
    Map<?, ?> parameters = workSpaceApiClient.getWidgetParameters(workspaceId).block();
    assertNotNull(parameters);
  }

  @Test
  @Order(6)
  void uploadWorkspaceFile_returnsUploadedFiles() {
    List<Map<?, ?>> files = workSpaceApiClient.uploadWorkspaceFile(
        workspaceId, "happy-flow-workspace-file".getBytes(StandardCharsets.UTF_8), "happyflow-ws.txt").block();
    assertNotNull(files);
    assertFalse(files.isEmpty());
  }

  @Test
  @Order(7)
  void createWidget_returnsWidget() {
    WidgetDTO created = widgetApiClient.create(WidgetDTO.builder()
        .title("HappyFlow Widget")
        .type("TABLE")
        .tableId(tableId)
        .config(Map.of())
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    widgetId = created.getId();
  }

  @Test
  @Order(8)
  void createBIPage_returnsPage() {
    BIPageDTO created = biPageApiClient.createPage(workspaceId, BIPageDTO.builder()
        .title("HappyFlow BI Page")
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    biPageId = created.getId();
  }

  @Test
  @Order(9)
  void attachWidgetToPage_returnsWidget() {
    WidgetDTO attached = biPageApiClient.attachWidgetToPage(workspaceId, biPageId, widgetId).block();
    assertNotNull(attached);
  }

  @Test
  @Order(10)
  void getBIPageWidgets_returnsNonEmptyList() {
    List<WidgetDTO> widgets = biPageApiClient.getWidgets(workspaceId, biPageId).block();
    assertNotNull(widgets);
    assertFalse(widgets.isEmpty());
  }

  @Test
  @Order(11)
  void updateBIPageWidgets_returnsList() {
    List<WidgetDTO> widgets = biPageApiClient.getWidgets(workspaceId, biPageId).block();
    assertNotNull(widgets);
    List<WidgetDTO> updated = biPageApiClient.updateWidgets(workspaceId, biPageId, widgets).block();
    assertNotNull(updated);
  }

  @Test
  @Order(12)
  void getBIPageById_returnsPage() {
    BIPageDTO page = biPageApiClient.getPageById(workspaceId, biPageId).block();
    assertNotNull(page);
    assertEquals(biPageId, page.getId());
  }

  @Test
  @Order(13)
  void modifyBIPage_returnsUpdatedPage() {
    BIPageDTO updated = biPageApiClient.modifyPage(workspaceId, biPageId, BIPageDTO.builder()
        .title("HappyFlow BI Page Updated")
        .build()).block();
    assertNotNull(updated);
    assertEquals("HappyFlow BI Page Updated", updated.getTitle());
  }

  @Test
  @Order(14)
  void deleteBIPage_succeeds() {
    biPageApiClient.deletePage(workspaceId, biPageId).block();
  }

  @Test
  @Order(15)
  void createDashboard_returnsDashboard() {
    BIDashboardDTO created = biDashboardApiClient.create(BIDashboardDTO.builder()
        .title("HappyFlow Dashboard")
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    dashboardId = created.getId();
  }

  @Test
  @Order(16)
  void getDashboardById_returnsDashboard() {
    BIDashboardDTO dashboard = biDashboardApiClient.getById(dashboardId).block();
    assertNotNull(dashboard);
    assertEquals(dashboardId, dashboard.getId());
  }

  @Test
  @Order(17)
  void editDashboard_returnsUpdatedDashboard() {
    BIDashboardDTO updated = biDashboardApiClient.edit(dashboardId, BIDashboardDTO.builder()
        .title("HappyFlow Dashboard Updated")
        .build()).block();
    assertNotNull(updated);
    assertEquals("HappyFlow Dashboard Updated", updated.getTitle());
  }

  @Test
  @Order(18)
  void getDashboards_returnsNonEmptyList() {
    List<BIDashboardDTO> dashboards = biDashboardApiClient.getAll().block();
    assertNotNull(dashboards);
    assertTrue(dashboards.stream().anyMatch(d -> dashboardId.equals(d.getId())));
  }

  @Test
  @Order(19)
  void deleteDashboard_succeeds() {
    biDashboardApiClient.delete(dashboardId).block();
  }

  @Test
  @Order(20)
  void createDag_returnsDag() {
    DagDTO created = dagApiClient.create(DagDTO.builder()
        .name("HappyFlow DAG " + UUID.randomUUID())
        .nodes(List.of())
        .edges(List.of())
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    dagId = created.getId();
  }

  @Test
  @Order(21)
  void getAllDags_returnsResponse() {
    Map<?, ?> response = dagApiClient.getAll().block();
    assertNotNull(response);
    List<?> results = (List<?>) response.get("results");
    assertNotNull(results);
    assertFalse(results.isEmpty());
  }

  @Test
  @Order(22)
  void getDagById_returnsDag() {
    DagDTO dag = dagApiClient.getById(dagId).block();
    assertNotNull(dag);
    assertEquals(dagId, dag.getId());
  }

  @Test
  @Order(23)
  void updateDag_returnsUpdatedDag() {
    DagDTO updated = dagApiClient.update(dagId, DagDTO.builder()
        .name("HappyFlow DAG Updated")
        .nodes(List.of())
        .edges(List.of())
        .build()).block();
    assertNotNull(updated);
    assertEquals("HappyFlow DAG Updated", updated.getName());
  }

  @Test
  @Order(24)
  void getPipelinesV2ByDagId_returnsResponse() {
    Map<?, ?> response = dagApiClient.getPipelinesV2(dagId).block();
    assertNotNull(response);
  }

  @Test
  @Order(25)
  void getDagRuns_returnsResponse() {
    Map<?, ?> response = dagApiClient.getRuns(dagId).block();
    assertNotNull(response);
  }

  @Test
  @Order(26)
  void deleteDag_succeeds() {
    dagApiClient.delete(dagId).block();
  }

  @Test
  @Order(27)
  void createSkill_returnsSkill() {
    // Skill names must be a lowercase-hyphen slug — "Use only lowercase letters (a-z), numbers
    // (0-9), and hyphens (-)" (confirmed via backend error message); a UUID is already exactly
    // that shape once lowercased.
    AgentSkillDTO created = agentSkillApiClient.create(AgentSkillDTO.builder()
        .name("happyflow-skill-" + UUID.randomUUID())
        .description("happy flow test skill")
        .s3Url("skills/happyflow-test.md")
        .enabled(true)
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    skillId = created.getId();
  }

  @Test
  @Order(28)
  void listSkills_returnsNonEmptyList() {
    List<AgentSkillDTO> skills = agentSkillApiClient.list().block();
    assertNotNull(skills);
    assertTrue(skills.stream().anyMatch(s -> skillId.equals(s.getId())));
  }

  @Test
  @Order(29)
  void getSkill_returnsSkill() {
    AgentSkillDTO skill = agentSkillApiClient.getById(skillId).block();
    assertNotNull(skill);
    assertEquals(skillId, skill.getId());
  }

  @Test
  @Order(30)
  void getSkillVersions_returnsList() {
    List<?> versions = agentSkillApiClient.getVersions(skillId).block();
    assertNotNull(versions);
  }

  @Test
  @Order(31)
  void createClassification_returnsClassification() {
    AnnotationDTO created = classificationApiClient.create(AnnotationDTO.builder()
        .name("HappyFlow Classification " + UUID.randomUUID())
        .description("happy flow test classification")
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
  }

  @Test
  @Order(32)
  void getClassifications_returnsResponse() {
    Map<?, ?> response = classificationApiClient.getAll().block();
    assertNotNull(response);
  }

  @Test
  @Order(33)
  void searchDataSources_returnsList() {
    List<Map<?, ?>> results = catalogSearchApiClient.searchDataSources("happyflow").block();
    assertNotNull(results);
  }

  // getLlmKey (GET /idp/api/v1/company/llm/key) omitted: LiteLLmService.createOrGetTeamAndVirtual
  // KeyIfNotExists calls out to a real external LiteLLM proxy team/key API — confirmed by reading
  // the service (no local/mock path) and by this call hanging until the 30s client read-timeout
  // in this environment. Same class of external-dependency limitation as the DataPipelineV2/
  // MaterializedViews exclusions in the batch-4 plan notes — not automatable without a reachable
  // LiteLLM instance.

  @Test
  @Order(35)
  void attachElementLink_returnsLink() {
    ElementLinkDTO attached = elementLinkApiClient.attach(ElementLinkDTO.builder()
        .elementType("WORKSPACE")
        .elementId(workspaceId)
        .linkedElementType("TABLE")
        .linkedId(tableId)
        .build()).block();
    assertNotNull(attached);
  }

  @Test
  @Order(36)
  void getElementLink_returnsResponse() {
    Map<?, ?> link = elementLinkApiClient.getByElement("WORKSPACE", workspaceId).block();
    assertNotNull(link);
  }

  @Test
  @Order(37)
  void createFolder_returnsFolder() {
    FolderDTO created = folderApiClient.create(workspaceId, FolderDTO.builder()
        .name("HappyFlow Folder")
        .build()).block();
    assertNotNull(created);
    assertNotNull(created.getId());
    folderId = created.getId();
  }

  @Test
  @Order(38)
  void attachElementsToFolder_succeeds() {
    // FolderServiceImpl.attachElementsToFolder reads linkedElementType/linkedId off each
    // ElementLinkDTO (elementType/elementId are set internally to FOLDER/folderId) — sending
    // elementType/elementId instead NPEs server-side on a null elementType (confirmed via
    // backend log/source, FolderServiceImpl.verifyLinkedElementExists).
    folderApiClient.attachElements(workspaceId, folderId, List.of(ElementLinkDTO.builder()
        .linkedElementType("TABLE")
        .linkedId(tableId)
        .build())).block();
  }
}
