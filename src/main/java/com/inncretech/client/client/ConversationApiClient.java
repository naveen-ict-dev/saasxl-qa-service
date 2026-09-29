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
public class ConversationApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /ai/api/v2/conversations
   */
  public Mono<Map<?, ?>> createConversation(Map<String, Object> body) {
    return backendWebClient.post()
        .uri("/ai/api/v2/conversations")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to create conversation", err));
  }

  /**
   * Calls GET /ai/api/v2/conversations
   */
  public Mono<List<Map<?, ?>>> getConversations() {
    return backendWebClient.get()
        .uri("/ai/api/v2/conversations")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch conversations", err));
  }
}
