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
public class CompanyApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /idp/api/v1/company
   */
  public Mono<Map<?, ?>> getCompany() {
    return backendWebClient.get()
        .uri("/idp/api/v1/company")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch company", err));
  }

  /**
   * Calls GET /idp/api/v1/company/llm/models
   */
  public Mono<List<Map<?, ?>>> getLlmModels() {
    return backendWebClient.get()
        .uri("/idp/api/v1/company/llm/models")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch LLM models", err));
  }
}
