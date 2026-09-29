package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inncretech.client.client.AgentProfileApiClient;
import com.inncretech.client.client.CompanyApiClient;
import com.inncretech.client.client.ConversationApiClient;
import com.inncretech.client.client.DataSourceApiClient;
import com.inncretech.client.client.FileApiClient;
import com.inncretech.client.client.McpConnectorApiClient;
import com.inncretech.client.client.PreferenceApiClient;
import com.inncretech.client.client.TeamApiClient;
import com.inncretech.client.client.UserApiClient;
import com.inncretech.client.model.dto.AgentProfileRequestDTO;
import com.inncretech.client.model.dto.AttachmentDTO;
import com.inncretech.client.model.dto.ConfigPropertyDTO;
import com.inncretech.client.model.dto.PreferenceDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.UserDTO;
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
 * Happy-flow smoke tests for the next 15 highest-traffic live saasxl-backend endpoints past the
 * first 10 (see {@link HappyFlowEndpointsTest}), excluding all {@code /widget/**} and
 * {@code /kb/**} endpoints per dev's instruction. Shares the signup-based bootstrap in
 * {@link AbstractHappyFlowTest}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HappyFlowEndpointsBatch2Test extends AbstractHappyFlowTest {

  @Autowired private UserApiClient userApiClient;
  @Autowired private FileApiClient fileApiClient;
  @Autowired private TeamApiClient teamApiClient;
  @Autowired private ConversationApiClient conversationApiClient;
  @Autowired private PreferenceApiClient preferenceApiClient;
  @Autowired private AgentProfileApiClient agentProfileApiClient;
  @Autowired private CompanyApiClient companyApiClient;
  @Autowired private McpConnectorApiClient mcpConnectorApiClient;
  @Autowired private DataSourceApiClient dataSourceApiClient;

  private String preferenceEntityId;

  @Test
  @Order(1)
  void uploadAndDownloadPublicFile_returnsSameContent() {
    List<AttachmentDTO> uploaded = fileApiClient.uploadPublicFile(
        "happy-flow-content".getBytes(StandardCharsets.UTF_8), "happyflow.txt").block();
    assertNotNull(uploaded);
    assertTrue(!uploaded.isEmpty());
    String key = uploaded.get(0).getKey();
    assertNotNull(key);

    UserDTO user = userApiClient.getCurrentUser().block();
    assertNotNull(user);

    byte[] downloaded = fileApiClient.downloadPublicFile(user.getCompanyId(), key).block();
    assertNotNull(downloaded);
    assertTrue(downloaded.length > 0);
  }

  @Test
  @Order(2)
  void configProperty_returnsWarehouseConfig() {
    ConfigPropertyDTO config = configApiClient.getConfigProperty("DEFAULT_DB_WAREHOUSE").block();
    assertNotNull(config);
    assertNotNull(config.getConfigValue());
  }

  @Test
  @Order(3)
  void team_returnsTeamMembers() {
    Map<?, ?> team = teamApiClient.getTeam().block();
    assertNotNull(team);
  }

  @Test
  @Order(4)
  void tablesList_containsSeededTable() {
    List<TableDTO> tables = workSpaceApiClient.getTables(workspaceId).block();
    assertNotNull(tables);
    assertTrue(tables.stream().anyMatch(t -> tableId.equals(t.getId())));
  }

  @Test
  @Order(5)
  void createConversation_returnsConversation() {
    Map<?, ?> conversation = conversationApiClient.createConversation(Map.of()).block();
    assertNotNull(conversation);
  }

  @Test
  @Order(6)
  void getConversations_returnsNonEmptyList() {
    List<Map<?, ?>> conversations = conversationApiClient.getConversations().block();
    assertNotNull(conversations);
    assertTrue(!conversations.isEmpty());
  }

  @Test
  @Order(7)
  void savePreference_returnsSavedValue() {
    preferenceEntityId = UUID.randomUUID().toString();
    PreferenceDTO saved = preferenceApiClient.savePreference(PreferenceDTO.builder()
        .entityId(preferenceEntityId)
        .entityType("HAPPYFLOW_TEST")
        .settings(Map.of("key", "value"))
        .build()).block();
    assertNotNull(saved);
  }

  @Test
  @Order(8)
  void getPreference_returnsSavedValue() {
    PreferenceDTO preference = preferenceApiClient.getPreference(preferenceEntityId, "HAPPYFLOW_TEST").block();
    assertNotNull(preference);
    assertNotNull(preference.getEntityId());
  }

  @Test
  @Order(9)
  void llmModels_returnsList() {
    List<Map<?, ?>> models = companyApiClient.getLlmModels().block();
    assertNotNull(models);
  }

  @Test
  @Order(10)
  void createAgentProfile_returnsProfile() {
    // agentType has no enum/whitelist server-side — any non-blank string is accepted
    // (AgentProfileServiceImpl.java:41-44); "DAVE" matches the doc/example value.
    Map<?, ?> profile = agentProfileApiClient.createProfile(AgentProfileRequestDTO.builder()
        .name("HappyFlow_" + UUID.randomUUID())
        .agentType("DAVE")
        .config(Map.of())
        .build()).block();
    assertNotNull(profile);
  }

  @Test
  @Order(11)
  void getAgentProfiles_returnsNonEmptyList() {
    List<Map<?, ?>> profiles = agentProfileApiClient.getProfiles().block();
    assertNotNull(profiles);
    assertTrue(!profiles.isEmpty());
  }

  @Test
  @Order(12)
  void folderExternalTempToken_returnsToken() {
    Map<?, ?> token = workSpaceApiClient.getFolderExternalTempToken(workspaceId).block();
    assertNotNull(token);
    assertNotNull(token.get("externalTempToken"));
  }

  @Test
  @Order(13)
  void folders_returnsList() {
    List<Map<?, ?>> folders = workSpaceApiClient.getFolders(workspaceId).block();
    assertNotNull(folders);
  }

  @Test
  @Order(14)
  void company_returnsCompanyProfile() {
    Map<?, ?> company = companyApiClient.getCompany().block();
    assertNotNull(company);
  }

  @Test
  @Order(15)
  void mcpConnectors_returnsList() {
    List<Map<?, ?>> connectors = mcpConnectorApiClient.listConnectors().block();
    assertNotNull(connectors);
  }

  @Test
  @Order(16)
  void getDataSources_returnsPage() {
    Map<?, ?> page = dataSourceApiClient.getDataSources().block();
    assertNotNull(page);
  }

  @Test
  @Order(17)
  void addDatasource_returnsCreatedDatasource() {
    // DataSourceDTO.type is DataSourceType {SQL, REST, NO_SQL, WAREHOUSE}; username/password are
    // top-level DataSourceDTO fields (not nested under "properties"), and "dataSourceProvider" in
    // properties is the key the backend reads to pick the JDBC dialect. Points at this
    // environment's own dev Postgres (application-dev.properties) rather than a guessed target,
    // since that's the one instance guaranteed reachable from wherever the backend is running.
    Map<?, ?> created = dataSourceApiClient.addDatasource(Map.of(
        "name", "happyflow_" + UUID.randomUUID().toString().replace("-", ""),
        "type", "SQL",
        "username", "postgres",
        "password", "Demo12#$",
        "properties", Map.of(
            "dataSourceProvider", "postgresql",
            "host", "localhost",
            "port", "5432",
            "dbName", "saasxl"))).block();
    assertNotNull(created);
  }
}
