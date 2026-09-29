package com.inncretech.client.client;

import java.util.List;
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
}
