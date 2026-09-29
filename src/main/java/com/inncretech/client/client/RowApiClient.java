package com.inncretech.client.client;

import com.inncretech.client.model.dto.RowDataDTO;
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
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows/bulk
   */
  public Mono<List<RowDataDTO>> addRows(String workspaceId, String tableId, List<RowDataDTO> rows) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows/bulk", workspaceId, tableId)
        .bodyValue(rows)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<RowDataDTO>>() {})
        .doOnError(err -> log.error("Failed to bulk-add rows to table {}", tableId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows/{rowId}
   */
  public Mono<RowDataDTO> getRow(String workspaceId, String tableId, Object rowId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/rows/{rowId}",
            workspaceId, tableId, rowId)
        .retrieve()
        .bodyToMono(RowDataDTO.class)
        .doOnError(err -> log.error("Failed to fetch row {} from table {}", rowId, tableId, err));
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
