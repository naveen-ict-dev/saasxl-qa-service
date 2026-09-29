package com.inncretech.client.client;

import com.inncretech.client.model.dto.AnnotationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClassificationApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /dataSources/api/v1/classifications
   */
  public Mono<AnnotationDTO> create(AnnotationDTO classification) {
    return backendWebClient.post()
        .uri("/dataSources/api/v1/classifications")
        .bodyValue(classification)
        .retrieve()
        .bodyToMono(AnnotationDTO.class)
        .doOnError(err -> log.error("Failed to create classification", err));
  }

  /**
   * Calls GET /dataSources/api/v1/classifications
   */
  public Mono<Map<?, ?>> getAll() {
    return backendWebClient.get()
        .uri("/dataSources/api/v1/classifications")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch classifications", err));
  }
}
