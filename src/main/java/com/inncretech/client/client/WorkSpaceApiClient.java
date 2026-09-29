package com.inncretech.client.client;

import com.inncretech.client.model.dto.QueryOptionDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.WidgetParameterDTO;
import com.inncretech.client.model.dto.WorkSpaceDTO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
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
   * Backend returns a bare BaseWorkSpaceDTO, not wrapped in SaasxlResponseDTO.
   */
  public Mono<WorkSpaceDTO> getWorkspaceById(String workspaceId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}", workspaceId)
        .retrieve()
        .bodyToMono(WorkSpaceDTO.class)
        .doOnError(err -> log.error("Failed to fetch workspace {}", workspaceId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/search
   */
  public Mono<List<Map<?, ?>>> search(String query) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/noCo/api/v2/workspaces/search")
            .queryParam("query", query)
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to search workspaces for {}", query, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables
   * Backend returns a bare List<TableDTO>, not wrapped in SaasxlResponseDTO.
   */
  public Mono<List<TableDTO>> getTables(String workspaceId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables", workspaceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<TableDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch tables for workspace {}", workspaceId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/folder
   */
  public Mono<List<Map<?, ?>>> getFolders(String workspaceId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/folder", workspaceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch folders for workspace {}", workspaceId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/folder/externalTempToken
   */
  public Mono<Map<?, ?>> getFolderExternalTempToken(String workspaceId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/folder/externalTempToken", workspaceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch folder temp token for workspace {}", workspaceId, err));
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

  /**
   * Calls GET /noCo/api/v2/workspaces/getRequestedWorkspaces
   */
  public Mono<List<Map<?, ?>>> getRequestedWorkspaces() {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/getRequestedWorkspaces")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch requested workspaces", err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/getAllPublicWorkspaces
   */
  public Mono<List<Map<?, ?>>> getAllPublicWorkspaces() {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/getAllPublicWorkspaces")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch public workspaces", err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/invitedWorkspaces
   */
  public Mono<List<Map<?, ?>>> getInvitedWorkspaces() {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/invitedWorkspaces")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch invited workspaces", err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/parameters
   */
  public Mono<WidgetParameterDTO> createWidgetParameter(String workspaceId, WidgetParameterDTO parameter) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/parameters", workspaceId)
        .bodyValue(parameter)
        .retrieve()
        .bodyToMono(WidgetParameterDTO.class)
        .doOnError(err -> log.error("Failed to create widget parameter for workspace {}", workspaceId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/parameters
   */
  public Mono<Map<?, ?>> getWidgetParameters(String workspaceId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/parameters", workspaceId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch widget parameters for workspace {}", workspaceId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/folder/upload
   */
  public Mono<List<Map<?, ?>>> uploadWorkspaceFile(String workspaceId, byte[] content, String filename) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("files", new ByteArrayResource(content) {
      @Override
      public String getFilename() {
        return filename;
      }
    });

    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/folder/upload", workspaceId)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to upload workspace file for workspace {}", workspaceId, err));
  }
}
