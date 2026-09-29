package com.inncretech.client.client;

import com.inncretech.client.model.dto.RowDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RowApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows
   */
  public Mono<RowDataDTO> addRow(String workspaceId, String tableId, RowDataDTO row) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows", workspaceId, tableId)
        .bodyValue(row)
        .retrieve()
        .bodyToMono(RowDataDTO.class)
        .doOnError(err -> log.error("Failed to add row to table {}", tableId, err));
  }

  /**
   * Calls DELETE /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows/{rowId}
   */
  public Mono<Void> deleteRow(String workspaceId, String tableId, Object rowId) {
    return backendWebClient.delete()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows/{rowId}",
            workspaceId, tableId, rowId)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to delete row {} from table {}", rowId, tableId, err));
  }
}
