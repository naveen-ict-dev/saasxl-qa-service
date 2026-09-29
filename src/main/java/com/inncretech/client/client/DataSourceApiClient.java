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
}
