package com.inncretech.client.runner;

import com.inncretech.client.auth.AuthService;
import com.inncretech.client.client.DynamicApiClient;
import com.inncretech.client.client.UserApiClient;
import com.inncretech.client.client.WorkSpaceApiClient;
import com.inncretech.client.model.dto.ApiExecutionResult;
import com.inncretech.client.model.dto.EndpointDefinition;
import com.inncretech.client.model.dto.UserDTO;
import com.inncretech.client.model.dto.WorkSpaceDTO;
import com.inncretech.client.reader.SheetEndpointReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiExecutionRunner implements CommandLineRunner {

  private final AuthService authService;
  private final UserApiClient userApiClient;
  private final WorkSpaceApiClient workSpaceApiClient;
  private final DynamicApiClient dynamicApiClient;
  private final SheetEndpointReader sheetEndpointReader;

  @Value("${runner.enabled:true}")
  private boolean runnerEnabled;

  @Value("${runner.max-endpoints-to-test:10}")
  private int maxEndpointsToTest;

  @Override
  public void run(String... args) {
    if (!runnerEnabled) {
      log.info("ApiExecutionRunner is disabled via configuration.");
      return;
    }

    log.info("======================================================================");
    log.info("Starting SaasXL WebClient API Execution Runner");
    log.info("======================================================================");

    Map<String, String> runtimeContext = new HashMap<>();

    try {
      // Step 1: Perform login and obtain Bearer token
      log.info("Step 1: Logging in and obtaining Authorization Token...");
      String token = authService.getOrRefreshAccessToken();
      log.info("Obtained Authorization Token (Bearer prefix will be attached automatically)");

      // Step 2: Fetch Current User Profile
      log.info("Step 2: Calling GET /idp/api/v1/user using Bearer token...");
      try {
        UserDTO user = userApiClient.getCurrentUser().block();
        if (user != null) {
          log.info("Logged in User: ID={}, Email={}, CompanyID={}, Roles={}",
              user.getId(), user.getEmail(), user.getCompanyId(), user.getRoles());
          runtimeContext.put("userId", String.valueOf(user.getId()));
          runtimeContext.put("companyId", String.valueOf(user.getCompanyId()));
        }
      } catch (Exception ex) {
        log.warn("Could not retrieve current user profile (Backend might not be running locally): {}",
            ex.getMessage(), ex);
      }

      // Step 3: Fetch Workspaces to seed runtime parameters
      log.info("Step 3: Fetching workspaces to seed runtime context...");
      try {
        List<WorkSpaceDTO> workspaces = workSpaceApiClient.getAllWorkspaces().block();
        if (workspaces != null && !workspaces.isEmpty()) {
          WorkSpaceDTO firstWs = workspaces.get(0);
          runtimeContext.put("workspaceId", firstWs.getId());
          runtimeContext.put("id", firstWs.getId());
          log.info("Seeded context with workspaceId: {}", firstWs.getId());

          if (firstWs.getTables() != null && !firstWs.getTables().isEmpty()) {
            runtimeContext.put("tableId", firstWs.getTables().get(0).getId());
            log.info("Seeded context with tableId: {}", firstWs.getTables().get(0).getId());
          }
        }
      } catch (Exception ex) {
        log.warn("Could not fetch workspaces: {}", ex.getMessage(), ex);
      }

      // Step 4: Read endpoints from the Spreadsheet / CSV
      log.info("Step 4: Reading endpoints from Google Sheet / CSV...");
      List<EndpointDefinition> endpoints = sheetEndpointReader.readEndpoints();
      log.info("Loaded {} endpoint definitions to process", endpoints.size());

      // Step 5: Test endpoints using WebClient
      log.info("Step 5: Invoking up to {} endpoints...", Math.min(endpoints.size(), maxEndpointsToTest));
      int count = 0;
      for (EndpointDefinition endpoint : endpoints) {
        if (count >= maxEndpointsToTest) {
          break;
        }

        // Avoid destructive operations during automated test run
        if ("DELETE".equalsIgnoreCase(endpoint.getMethod())) {
          log.debug("Skipping DELETE endpoint for safety: {}", endpoint.getRoute());
          continue;
        }

        log.info("Executing [{}]: {} {}", count + 1, endpoint.getMethod(), endpoint.getRoute());
        ApiExecutionResult result = dynamicApiClient.executeEndpoint(endpoint, runtimeContext).block();

        if (result != null) {
          log.info(" -> Result: HTTP {} | Duration: {}ms | Success: {} | Snippet: {}",
              result.getStatusCode(),
              result.getDurationMs(),
              result.isSuccess(),
              result.getResponseSnippet());
        }
        count++;
      }

      log.info("======================================================================");
      log.info("API Execution Runner Completed Successfully");
      log.info("======================================================================");

    } catch (Exception ex) {
      log.error("Execution failed: {}", ex.getMessage(), ex);
    }
  }
}
