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
public class CatalogSearchApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /catalog/api/v1/search/dataSources
   */
  public Mono<List<Map<?, ?>>> searchDataSources(String query) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/catalog/api/v1/search/dataSources")
            .queryParam("query", query)
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to search datasources for {}", query, err));
  }
}
