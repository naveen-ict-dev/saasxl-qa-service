package com.inncretech.client.client;

import com.inncretech.client.model.dto.ConfigPropertyDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /config/api/v2/configProperty/{configKey}
   */
  public Mono<ConfigPropertyDTO> getConfigProperty(String configKey) {
    return backendWebClient.get()
        .uri("/config/api/v2/configProperty/{configKey}", configKey)
        .retrieve()
        .bodyToMono(ConfigPropertyDTO.class)
        .doOnError(err -> log.error("Failed to fetch config property {}", configKey, err));
  }
}
