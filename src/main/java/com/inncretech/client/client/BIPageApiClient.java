package com.inncretech.client.client;

import com.inncretech.client.model.dto.BIPageDTO;
import com.inncretech.client.model.dto.WidgetDTO;
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
public class BIPageApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/biPage
   */
  public Mono<BIPageDTO> createPage(String workspaceId, BIPageDTO page) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/biPage", workspaceId)
        .bodyValue(page)
        .retrieve()
        .bodyToMono(BIPageDTO.class)
        .doOnError(err -> log.error("Failed to create BI page for workspace {}", workspaceId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}
   */
  public Mono<BIPageDTO> getPageById(String workspaceId, String pageId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}", workspaceId, pageId)
        .retrieve()
        .bodyToMono(BIPageDTO.class)
        .doOnError(err -> log.error("Failed to fetch BI page {}", pageId, err));
  }

  /**
   * Calls PUT /noCo/api/v2/workspaces/{workspaceId}/biPage/{id}
   */
  public Mono<BIPageDTO> modifyPage(String workspaceId, String pageId, BIPageDTO page) {
    return backendWebClient.put()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/biPage/{id}", workspaceId, pageId)
        .bodyValue(page)
        .retrieve()
        .bodyToMono(BIPageDTO.class)
        .doOnError(err -> log.error("Failed to modify BI page {}", pageId, err));
  }

  /**
   * Calls DELETE /noCo/api/v2/workspaces/{workspaceId}/biPage/{id}
   */
  public Mono<Void> deletePage(String workspaceId, String pageId) {
    return backendWebClient.delete()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/biPage/{id}", workspaceId, pageId)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to delete BI page {}", pageId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}/widgets
   */
  public Mono<List<WidgetDTO>> getWidgets(String workspaceId, String pageId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}/widgets", workspaceId, pageId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<WidgetDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch widgets for BI page {}", pageId, err));
  }

  /**
   * Calls PUT /noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}/widgets
   */
  public Mono<List<WidgetDTO>> updateWidgets(String workspaceId, String pageId, List<WidgetDTO> widgets) {
    return backendWebClient.put()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}/widgets", workspaceId, pageId)
        .bodyValue(widgets)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<WidgetDTO>>() {})
        .doOnError(err -> log.error("Failed to update widgets for BI page {}", pageId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}/widgets/{widgetId}/attach
   */
  public Mono<WidgetDTO> attachWidgetToPage(String workspaceId, String pageId, String widgetId) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/biPage/{pageId}/widgets/{widgetId}/attach",
            workspaceId, pageId, widgetId)
        .retrieve()
        .bodyToMono(WidgetDTO.class)
        .doOnError(err -> log.error("Failed to attach widget {} to BI page {}", widgetId, pageId, err));
  }
}
