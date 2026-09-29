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

  /**
   * Calls GET /dataSources/api/v1/dataSources/{dataSourceId}
   */
  public Mono<Map<?, ?>> getDataSourceById(Object dataSourceId) {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}", dataSourceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch datasource {}", dataSourceId, err));
  }

  /**
   * Calls DELETE /dataSources/api/v1/dataSources/{dataSourceId}
   */
  public Mono<Void> deleteDataSource(Object dataSourceId) {
    return backendWebClient.delete()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}", dataSourceId)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to delete datasource {}", dataSourceId, err));
  }

  /**
   * Calls GET /dataSources/api/v1/dataSources/schemaChanges (no dataSourceId — company-wide)
   */
  public Mono<Map<?, ?>> getAllSchemaChanges() {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources/schemaChanges")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch schema changes", err));
  }

  /**
   * Calls GET /dataSources/api/v1/dataSources/{dataSourceId}/schemaChanges
   */
  public Mono<Map<?, ?>> getSchemaChangesForDataSource(Object dataSourceId) {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}/schemaChanges", dataSourceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch schema changes for datasource {}", dataSourceId, err));
  }

  /**
   * Calls GET /dataSources/api/v1/dataSources/{dataSourceId}/schemas (latest synced schema)
   */
  public Mono<Map<?, ?>> getSchema(Object dataSourceId) {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}/schemas", dataSourceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch schema for datasource {}", dataSourceId, err));
  }

  /**
   * Calls GET /dataSources/api/v1/dataSources/{dataSourceId}/descriptions
   */
  public Mono<Map<?, ?>> getDescriptions(Object dataSourceId) {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}/descriptions", dataSourceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch descriptions for datasource {}", dataSourceId, err));
  }

  /**
   * Calls PUT /dataSources/api/v1/dataSources/{dataSourceId}/schemaSyncCron
   */
  public Mono<Map<?, ?>> setupSchemaSyncCron(Object dataSourceId, String cronExpression) {
    return backendWebClient.put()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}/schemaSyncCron", dataSourceId)
        .bodyValue(Map.of("cronExpression", cronExpression))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to set up schema sync cron for datasource {}", dataSourceId, err));
  }

  /**
   * Calls GET /dataSources/api/v1/dataSources/{dataSourceId}/schemaSyncCron
   */
  public Mono<Map<?, ?>> getSchemaSyncCron(Object dataSourceId) {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}/schemaSyncCron", dataSourceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch schema sync cron for datasource {}", dataSourceId, err));
  }

  /**
   * Calls DELETE /dataSources/api/v1/dataSources/{dataSourceId}/schemaSyncCron
   */
  public Mono<Void> deleteSchemaSyncCron(Object dataSourceId) {
    return backendWebClient.delete()
        .uri("/dataSources/api/v1/dataSources/{dataSourceId}/schemaSyncCron", dataSourceId)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to delete schema sync cron for datasource {}", dataSourceId, err));
  }
}
