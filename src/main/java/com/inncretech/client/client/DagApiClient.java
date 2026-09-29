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
}
