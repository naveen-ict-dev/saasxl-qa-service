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
public class DataPipelineApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /dataPipeline/api/v2/queryEngines
   */
  public Mono<List<String>> getQueryEngines() {
    return backendWebClient.get()
        .uri("/dataPipeline/api/v2/queryEngines")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
        .doOnError(err -> log.error("Failed to fetch query engines", err));
  }

  /**
   * Calls POST /dataPipeline/api/v2
   */
  public Mono<Map<?, ?>> create(Map<String, Object> body) {
    return backendWebClient.post()
        .uri("/dataPipeline/api/v2")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to create data pipeline", err));
  }

  /**
   * Calls GET /dataPipeline/api/v2/{pipelineId}
   */
  public Mono<Map<?, ?>> getById(Object pipelineId) {
    return backendWebClient.get()
        .uri("/dataPipeline/api/v2/{pipelineId}", pipelineId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch data pipeline {}", pipelineId, err));
  }

  /**
   * Calls PUT /dataPipeline/api/v2/{dataPipelineId}
   */
  public Mono<Map<?, ?>> update(Object pipelineId, Map<String, Object> body) {
    return backendWebClient.put()
        .uri("/dataPipeline/api/v2/{pipelineId}", pipelineId)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to update data pipeline {}", pipelineId, err));
  }

  /**
   * Calls POST /dataPipeline/api/v2/getAllPaginated
   */
  public Mono<Map<?, ?>> getAllPaginated(Map<String, Object> searchBody) {
    return backendWebClient.post()
        .uri("/dataPipeline/api/v2/getAllPaginated")
        .bodyValue(searchBody)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch paginated data pipelines", err));
  }

  /**
   * Calls GET /dataPipeline/api/v2/{pipelineId}/jobs?page=&size=. page is 1-based — the controller
   * throws InvalidArgumentException for page&lt;=0 (confirmed via backend source).
   */
  public Mono<Map<?, ?>> getJobs(Object pipelineId, int page, int size) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/dataPipeline/api/v2/{pipelineId}/jobs")
            .queryParam("page", page)
            .queryParam("size", size)
            .build(pipelineId))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch jobs for pipeline {}", pipelineId, err));
  }

  /**
   * Calls GET /dataPipeline/api/v2/{pipelineId}/job/{jobId}
   */
  public Mono<Map<?, ?>> getJob(Object pipelineId, Object jobId) {
    return backendWebClient.get()
        .uri("/dataPipeline/api/v2/{pipelineId}/job/{jobId}", pipelineId, jobId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch job {} for pipeline {}", jobId, pipelineId, err));
  }
}
