package com.inncretech.client.client;

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
public class CommentApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /noCo/api/v1/{authElementType}/{authElementId}/comments/{primaryElementType}/{primaryElementId}/commentCount
   */
  public Mono<Map<?, ?>> getCommentCount(
      String authElementType, String authElementId, String primaryElementType, String primaryElementId) {
    return backendWebClient.get()
        .uri("/noCo/api/v1/{authElementType}/{authElementId}/comments/{primaryElementType}/{primaryElementId}/commentCount",
            authElementType, authElementId, primaryElementType, primaryElementId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch comment count for {}/{}", primaryElementType, primaryElementId, err));
  }
}
