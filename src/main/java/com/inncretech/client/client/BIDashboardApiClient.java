package com.inncretech.client.client;

import com.inncretech.client.model.dto.BIDashboardDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class BIDashboardApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v1/dashboards
   */
  public Mono<BIDashboardDTO> create(BIDashboardDTO dashboard) {
    return backendWebClient.post()
        .uri("/noCo/api/v1/dashboards")
        .bodyValue(dashboard)
        .retrieve()
        .bodyToMono(BIDashboardDTO.class)
        .doOnError(err -> log.error("Failed to create BI dashboard", err));
  }

  /**
   * Calls GET /noCo/api/v1/dashboards/{id}
   */
  public Mono<BIDashboardDTO> getById(String id) {
    return backendWebClient.get()
        .uri("/noCo/api/v1/dashboards/{id}", id)
        .retrieve()
        .bodyToMono(BIDashboardDTO.class)
        .doOnError(err -> log.error("Failed to fetch BI dashboard {}", id, err));
  }

  /**
   * Calls PUT /noCo/api/v1/dashboards/{id}
   */
  public Mono<BIDashboardDTO> edit(String id, BIDashboardDTO dashboard) {
    return backendWebClient.put()
        .uri("/noCo/api/v1/dashboards/{id}", id)
        .bodyValue(dashboard)
        .retrieve()
        .bodyToMono(BIDashboardDTO.class)
        .doOnError(err -> log.error("Failed to edit BI dashboard {}", id, err));
  }

  /**
   * Calls GET /noCo/api/v1/dashboards
   */
  public Mono<List<BIDashboardDTO>> getAll() {
    return backendWebClient.get()
        .uri("/noCo/api/v1/dashboards")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<BIDashboardDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch BI dashboards", err));
  }

  /**
   * Calls DELETE /noCo/api/v1/dashboards/{id}
   */
  public Mono<Void> delete(String id) {
    return backendWebClient.delete()
        .uri("/noCo/api/v1/dashboards/{id}", id)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to delete BI dashboard {}", id, err));
  }
}
