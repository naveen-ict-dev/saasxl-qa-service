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
public class ElementPermissionsApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /noCo/api/v2/elementPermissions/{elementType}
   */
  public Mono<List<Map<?, ?>>> getByType(String elementType) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/elementPermissions/{elementType}", elementType)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch element permissions for {}", elementType, err));
  }
}
