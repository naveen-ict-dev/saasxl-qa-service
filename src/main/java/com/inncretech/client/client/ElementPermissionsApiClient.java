package com.inncretech.client.client;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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

  /**
   * Calls POST /noCo/api/v2/elementPermissions
   */
  public Mono<Map<?, ?>> create(Map<String, Object> body) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/elementPermissions")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to create element permission", err));
  }

  /**
   * Calls GET /noCo/api/v2/elementPermissions/{elementType}/{elementId}
   */
  public Mono<List<Map<?, ?>>> getForElement(String elementType, String elementId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/elementPermissions/{elementType}/{elementId}", elementType, elementId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch permissions for {}:{}", elementType, elementId, err));
  }

  /**
   * Calls GET /noCo/api/v2/elementPermissions/matchedPermission/{elementType}/{elementId}
   */
  public Mono<Map<?, ?>> getMatchedPermission(String elementType, String elementId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/elementPermissions/matchedPermission/{elementType}/{elementId}", elementType, elementId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch matched permission for {}:{}", elementType, elementId, err));
  }

  /**
   * Calls GET /noCo/api/v2/elementPermissions/{elementType}/sharedUsers
   */
  public Mono<List<Map<?, ?>>> getSharedUsers(String elementType) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/elementPermissions/{elementType}/sharedUsers", elementType)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch shared users for {}", elementType, err));
  }

  /**
   * Calls GET /noCo/api/v2/elementPermissions/link/{elementType}/{elementId}
   */
  public Mono<Map<?, ?>> getLinkAccess(String elementType, String elementId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/elementPermissions/link/{elementType}/{elementId}", elementType, elementId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch link access for {}:{}", elementType, elementId, err));
  }

  /**
   * Calls PUT /noCo/api/v2/elementPermissions/link/{elementType}/{elementId}
   */
  public Mono<Map<?, ?>> setLinkAccess(String elementType, String elementId, Map<String, Object> body) {
    return backendWebClient.put()
        .uri("/noCo/api/v2/elementPermissions/link/{elementType}/{elementId}", elementType, elementId)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to set link access for {}:{}", elementType, elementId, err));
  }

  /**
   * Calls POST /noCo/api/v2/elementPermissions/bulk
   */
  public Mono<Map<?, ?>> bulkCreate(Map<String, Object> body) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/elementPermissions/bulk")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to bulk create element permissions", err));
  }

  /**
   * Calls DELETE /noCo/api/v2/elementPermissions/bulk
   */
  public Mono<Map<?, ?>> bulkDelete(Map<String, Object> body) {
    return backendWebClient.method(HttpMethod.DELETE)
        .uri("/noCo/api/v2/elementPermissions/bulk")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to bulk delete element permissions", err));
  }

  /**
   * Calls DELETE /noCo/api/v2/elementPermissions/{id}
   */
  public Mono<Void> delete(String id) {
    return backendWebClient.delete()
        .uri("/noCo/api/v2/elementPermissions/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to delete element permission {}", id, err));
  }
}
