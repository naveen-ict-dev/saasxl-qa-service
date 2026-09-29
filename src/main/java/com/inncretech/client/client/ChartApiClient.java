package com.inncretech.client.client;

import com.inncretech.client.model.dto.ChartDataAbstractionRequestDTO;
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
public class ChartApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /widget/api/v1/chart/abstract
   */
  public Mono<Map<?, ?>> getChartAbstract(ChartDataAbstractionRequestDTO request) {
    return backendWebClient.post()
        .uri("/widget/api/v1/chart/abstract")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch chart abstraction for table {}", request.getTableId(), err));
  }
}
