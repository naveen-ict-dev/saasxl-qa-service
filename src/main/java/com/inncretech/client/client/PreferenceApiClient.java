package com.inncretech.client.client;

import com.inncretech.client.model.dto.PreferenceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreferenceApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /idp/api/v1/preference (permitAll)
   */
  public Mono<PreferenceDTO> savePreference(PreferenceDTO preference) {
    return backendWebClient.post()
        .uri("/idp/api/v1/preference")
        .bodyValue(preference)
        .retrieve()
        .bodyToMono(PreferenceDTO.class)
        .doOnError(err -> log.error("Failed to save preference {}", preference.getEntityId(), err));
  }

  /**
   * Calls GET /idp/api/v1/preference/{entityId}/{entityType} (permitAll)
   */
  public Mono<PreferenceDTO> getPreference(String entityId, String entityType) {
    return backendWebClient.get()
        .uri("/idp/api/v1/preference/{entityId}/{entityType}", entityId, entityType)
        .retrieve()
        .bodyToMono(PreferenceDTO.class)
        .doOnError(err -> log.error("Failed to fetch preference {}/{}", entityId, entityType, err));
  }
}
