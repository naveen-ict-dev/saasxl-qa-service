package com.inncretech.client.client;

import com.inncretech.client.model.dto.DagDTO;
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
public class DagApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /orchestration/api/v1/dags
   */
  public Mono<DagDTO> create(DagDTO dag) {
    return backendWebClient.post()
        .uri("/orchestration/api/v1/dags")
        .bodyValue(dag)
        .retrieve()
        .bodyToMono(DagDTO.class)
        .doOnError(err -> log.error("Failed to create DAG", err));
  }

  /**
   * Calls GET /orchestration/api/v1/dags
   */
  public Mono<Map<?, ?>> getAll() {
    return backendWebClient.get()
        .uri("/orchestration/api/v1/dags")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch DAGs", err));
  }

  /**
   * Calls GET /orchestration/api/v1/dags/{dagId}
   */
  public Mono<DagDTO> getById(Long dagId) {
    return backendWebClient.get()
        .uri("/orchestration/api/v1/dags/{dagId}", dagId)
        .retrieve()
        .bodyToMono(DagDTO.class)
        .doOnError(err -> log.error("Failed to fetch DAG {}", dagId, err));
  }

  /**
   * Calls PUT /orchestration/api/v1/dags/{dagId}
   */
  public Mono<DagDTO> update(Long dagId, DagDTO dag) {
    return backendWebClient.put()
        .uri("/orchestration/api/v1/dags/{dagId}", dagId)
        .bodyValue(dag)
        .retrieve()
        .bodyToMono(DagDTO.class)
        .doOnError(err -> log.error("Failed to update DAG {}", dagId, err));
  }

  /**
   * Calls DELETE /orchestration/api/v1/dags/{dagId}
   */
  public Mono<DagDTO> delete(Long dagId) {
    return backendWebClient.delete()
        .uri("/orchestration/api/v1/dags/{dagId}", dagId)
        .retrieve()
        .bodyToMono(DagDTO.class)
        .doOnError(err -> log.error("Failed to delete DAG {}", dagId, err));
  }

  /**
   * Calls GET /orchestration/api/v1/dags/{dagId}/pipelinesV2
   */
  public Mono<Map<?, ?>> getPipelinesV2(Long dagId) {
    return backendWebClient.get()
        .uri("/orchestration/api/v1/dags/{dagId}/pipelinesV2", dagId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch pipelines for DAG {}", dagId, err));
  }

  /**
   * Calls GET /orchestration/api/v1/dags/{dagId}/runs
   */
  public Mono<Map<?, ?>> getRuns(Long dagId) {
    return backendWebClient.get()
        .uri("/orchestration/api/v1/dags/{dagId}/runs", dagId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch runs for DAG {}", dagId, err));
  }

  /**
   * Calls POST /orchestration/api/v1/dags/{dagId}/nodes
   */
  public Mono<Map<?, ?>> createNode(Long dagId, Map<String, Object> body) {
    return backendWebClient.post()
        .uri("/orchestration/api/v1/dags/{dagId}/nodes", dagId)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to create node on DAG {}", dagId, err));
  }

  /**
   * Calls POST /orchestration/api/v1/dags/{dagId}/runs. Body must be sent non-null (even {}) —
   * DagController.createDagRun dereferences the raw @RequestBody(required=false) parameter
   * unconditionally and NPEs if it's omitted (confirmed via backend source).
   */
  public Mono<Map<?, ?>> createRun(Long dagId, Map<String, Object> body) {
    return backendWebClient.post()
        .uri("/orchestration/api/v1/dags/{dagId}/runs", dagId)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to create run for DAG {}", dagId, err));
  }

  /**
   * Calls GET /orchestration/api/v1/dags/{dagId}/runs/{runId}
   */
  public Mono<Map<?, ?>> getRunById(Long dagId, Object runId) {
    return backendWebClient.get()
        .uri("/orchestration/api/v1/dags/{dagId}/runs/{runId}", dagId, runId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch run {} for DAG {}", runId, dagId, err));
  }

  /**
   * Calls GET /orchestration/api/v1/dags/{dagId}/snapshots
   */
  public Mono<Map<?, ?>> getSnapshots(Long dagId) {
    return backendWebClient.get()
        .uri("/orchestration/api/v1/dags/{dagId}/snapshots", dagId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch snapshots for DAG {}", dagId, err));
  }
}
