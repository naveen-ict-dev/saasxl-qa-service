package com.inncretech.client.client;

import com.inncretech.client.model.dto.QueryOptionDTO;
import com.inncretech.client.model.dto.SaasxlResponseDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.WorkSpaceDTO;
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
public class WorkSpaceApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /noCo/api/v2/workspaces
   * Backend returns a bare List<WorkSpaceDTO>, not wrapped in SaasxlResponseDTO.
   */
  public Mono<List<WorkSpaceDTO>> getAllWorkspaces() {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<WorkSpaceDTO>>() {})
        .doOnSuccess(resp -> log.info("Fetched {} workspaces", resp != null ? resp.size() : 0))
        .doOnError(err -> log.error("Failed to fetch workspaces", err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}
   */
  public Mono<SaasxlResponseDTO<WorkSpaceDTO>> getWorkspaceById(String workspaceId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}", workspaceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<SaasxlResponseDTO<WorkSpaceDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch workspace {}", workspaceId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables
   */
  public Mono<SaasxlResponseDTO<List<TableDTO>>> getTables(String workspaceId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables", workspaceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<SaasxlResponseDTO<List<TableDTO>>>() {})
        .doOnError(err -> log.error("Failed to fetch tables for workspace {}", workspaceId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/cursor
   */
  public Mono<Map<?, ?>> getCursorData(String workspaceId, String tableId, QueryOptionDTO options) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/cursor", workspaceId, tableId)
        .bodyValue(options != null ? options : new QueryOptionDTO())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch cursor data for table {}", tableId, err));
  }
}
