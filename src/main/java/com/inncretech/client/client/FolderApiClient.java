package com.inncretech.client.client;

import com.inncretech.client.model.dto.ElementLinkDTO;
import com.inncretech.client.model.dto.FolderDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class FolderApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/folders
   */
  public Mono<FolderDTO> create(String workspaceId, FolderDTO folder) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/folders", workspaceId)
        .bodyValue(folder)
        .retrieve()
        .bodyToMono(FolderDTO.class)
        .doOnError(err -> log.error("Failed to create folder in workspace {}", workspaceId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/folders/{folderId}/elements
   */
  public Mono<Void> attachElements(String workspaceId, String folderId, List<ElementLinkDTO> elements) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/folders/{folderId}/elements", workspaceId, folderId)
        .bodyValue(elements)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to attach elements to folder {}", folderId, err));
  }
}
