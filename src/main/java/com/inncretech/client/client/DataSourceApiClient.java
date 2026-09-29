package com.inncretech.client.client;

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
public class DataSourceApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /dataSources/api/v1/dataSources
   */
  public Mono<Map<?, ?>> addDatasource(Map<String, Object> body) {
    return backendWebClient.post()
        .uri("/dataSources/api/v1/dataSources")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to add datasource", err));
  }

  /**
   * Calls GET /dataSources/api/v1/dataSources
   */
  public Mono<Map<?, ?>> getDataSources() {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch datasources", err));
  }

  /**
   * Calls GET /dataSources/api/v1/{dataSourceId}/getSyncSchemaStatus. Response is
   * {"status": "RUNNING"|"COMPLETED"|"FAILED"|"NOT_AVAILABLE"} — reads the latest async
   * schema-sync job's outcome, since a 200 from addDatasource only proves the synchronous
   * connection check passed, not that table discovery finished.
   */
  public Mono<Map<?, ?>> getSyncSchemaStatus(Object dataSourceId) {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/{dataSourceId}/getSyncSchemaStatus", dataSourceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch sync schema status for datasource {}", dataSourceId, err));
  }

  /**
   * Calls GET /dataSources/api/v1/dataSources/{dataSourceId}/tables
   */
  public Mono<Map<?, ?>> getDataSourceTables(Object dataSourceId) {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}/tables", dataSourceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch tables for datasource {}", dataSourceId, err));
  }
}
