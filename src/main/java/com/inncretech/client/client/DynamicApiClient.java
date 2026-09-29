package com.inncretech.client.client;

import com.inncretech.client.model.dto.ApiExecutionResult;
import com.inncretech.client.model.dto.EndpointDefinition;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicApiClient {

  private final WebClient backendWebClient;

  /**
   * Executes an endpoint definition using WebClient.
   * Path parameters in `{paramName}` format are replaced from the provided context.
   */
  public Mono<ApiExecutionResult> executeEndpoint(
      EndpointDefinition endpoint, Map<String, String> contextParams) {
    long startTime = System.currentTimeMillis();
    String resolvedPath = resolvePath(endpoint.getRoute(), contextParams);
    HttpMethod httpMethod = HttpMethod.valueOf(endpoint.getMethod().toUpperCase());

    WebClient.RequestBodyUriSpec uriSpec = backendWebClient.method(httpMethod);
    WebClient.RequestHeadersSpec<?> headersSpec;

    // Attach empty JSON body for POST/PUT if needed
    if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH) {
      headersSpec = uriSpec.uri(resolvedPath).bodyValue(Map.of());
    } else {
      headersSpec = uriSpec.uri(resolvedPath);
    }

    return headersSpec.exchangeToMono(response -> handleResponse(response, endpoint.getMethod(), resolvedPath, startTime))
        .onErrorResume(ex -> {
          long duration = System.currentTimeMillis() - startTime;
          log.warn("Error calling {} {}: {}", endpoint.getMethod(), resolvedPath, ex.getMessage(), ex);
          return Mono.just(ApiExecutionResult.builder()
              .method(endpoint.getMethod())
              .path(resolvedPath)
              .statusCode(500)
              .success(false)
              .durationMs(duration)
              .errorMessage(ex.getMessage())
              .build());
        });
  }

  private Mono<ApiExecutionResult> handleResponse(
      ClientResponse response, String method, String path, long startTime) {
    long duration = System.currentTimeMillis() - startTime;
    int statusCode = response.statusCode().value();
    boolean isSuccess = response.statusCode().is2xxSuccessful();

    return response.bodyToMono(String.class)
        .defaultIfEmpty("")
        .map(body -> {
          String snippet = StringUtils.abbreviate(body, 200);
          return ApiExecutionResult.builder()
              .method(method)
              .path(path)
              .statusCode(statusCode)
              .success(isSuccess)
              .durationMs(duration)
              .responseSnippet(snippet)
              .build();
        });
  }

  /**
   * Replaces placeholders like {workspaceId}, {tableId}, {profileId} with actual runtime values.
   */
  public String resolvePath(String route, Map<String, String> context) {
    String resolved = route;
    if (context != null) {
      for (Map.Entry<String, String> entry : context.entrySet()) {
        resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
      }
    }
    // Replace any remaining unresolved {param} with a mock uuid/id so it does not fail syntax
    return resolved.replaceAll("\\{[^}]+\\}", "test-id");
  }
}
