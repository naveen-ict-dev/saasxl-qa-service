package com.inncretech.client.client;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class McpConnectorApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /mcpConnectorAuth/api/v1/connectors
   */
  public Mono<List<Map<?, ?>>> listConnectors() {
    return backendWebClient.get()
        .uri("/mcpConnectorAuth/api/v1/connectors")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to list MCP connectors", err));
  }
}
