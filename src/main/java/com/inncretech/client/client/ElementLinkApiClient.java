package com.inncretech.client.client;

import com.inncretech.client.model.dto.ElementLinkDTO;
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
public class ElementLinkApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v1/elementLinks/attach
   */
  public Mono<ElementLinkDTO> attach(ElementLinkDTO link) {
    return backendWebClient.post()
        .uri("/noCo/api/v1/elementLinks/attach")
        .bodyValue(link)
        .retrieve()
        .bodyToMono(ElementLinkDTO.class)
        .doOnError(err -> log.error("Failed to attach element link", err));
  }

  /**
   * Calls GET /noCo/api/v1/elementLinks/{elementType}/{elementId}
   */
  public Mono<Map<?, ?>> getByElement(String elementType, String elementId) {
    return backendWebClient.get()
        .uri("/noCo/api/v1/elementLinks/{elementType}/{elementId}", elementType, elementId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch element link for {}:{}", elementType, elementId, err));
  }
}
