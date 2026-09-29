package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inncretech.client.client.DynamicApiClient;
import com.inncretech.client.model.dto.ApiExecutionResult;
import com.inncretech.client.model.dto.EndpointDefinition;
import java.io.IOException;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class DynamicApiClientTest {

  private MockWebServer mockWebServer;
  private DynamicApiClient dynamicApiClient;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    WebClient webClient = WebClient.builder()
        .baseUrl(mockWebServer.url("/").toString())
        .build();

    dynamicApiClient = new DynamicApiClient(webClient);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void testResolvePath() {
    String template = "/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}";
    Map<String, String> context = Map.of(
        "workspaceId", "ws-123",
        "tableId", "tbl-456"
    );

    String resolved = dynamicApiClient.resolvePath(template, context);
    assertEquals("/noCo/api/v2/workspaces/ws-123/tables/tbl-456", resolved);
  }

  @Test
  void testExecuteGetEndpoint() throws InterruptedException {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("{\"success\":true,\"data\":{\"id\":1}}"));

    EndpointDefinition endpoint = EndpointDefinition.builder()
        .method("GET")
        .route("/idp/api/v1/user")
        .build();

    ApiExecutionResult result = dynamicApiClient.executeEndpoint(endpoint, Map.of()).block();

    assertEquals(200, result.getStatusCode());
    assertTrue(result.isSuccess());

    RecordedRequest recorded = mockWebServer.takeRequest();
    assertEquals("GET", recorded.getMethod());
    assertEquals("/idp/api/v1/user", recorded.getPath());
  }
}
