package com.inncretech.client.client;

import com.inncretech.client.model.dto.WidgetDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class WidgetApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v1/widgets
   */
  public Mono<WidgetDTO> create(WidgetDTO widget) {
    return backendWebClient.post()
        .uri("/noCo/api/v1/widgets")
        .bodyValue(widget)
        .retrieve()
        .bodyToMono(WidgetDTO.class)
        .doOnError(err -> log.error("Failed to create widget", err));
  }
}
