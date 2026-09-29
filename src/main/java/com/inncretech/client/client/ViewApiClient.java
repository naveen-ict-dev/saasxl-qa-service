package com.inncretech.client.client;

import com.inncretech.client.model.dto.TableViewDTO;
import com.inncretech.client.model.dto.ViewDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/views. Response is a table
   * envelope wrapping the created view under "view" — TableViewDTO.getId() is the table's id, the
   * view's own id is TableViewDTO.getView().getId().
   */
  public Mono<TableViewDTO> createView(String workspaceId, String tableId, ViewDTO view) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/views", workspaceId, tableId)
        .bodyValue(view)
        .retrieve()
        .bodyToMono(TableViewDTO.class)
        .doOnError(err -> log.error("Failed to create view on table {}", tableId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/views/{viewId}
   */
  public Mono<TableViewDTO> getView(String workspaceId, String tableId, String viewId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/views/{viewId}",
            workspaceId, tableId, viewId)
        .retrieve()
        .bodyToMono(TableViewDTO.class)
        .doOnError(err -> log.error("Failed to fetch view {}", viewId, err));
  }
}
