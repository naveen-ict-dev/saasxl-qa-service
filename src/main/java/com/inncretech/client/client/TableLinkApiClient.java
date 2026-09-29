package com.inncretech.client.client;

import com.inncretech.client.model.dto.LinkRecordDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class TableLinkApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/linkRecordV2
   */
  public Mono<Void> linkRecord(String workspaceId, String tableId, List<LinkRecordDTO> linkRecords) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/linkRecordV2", workspaceId, tableId)
        .bodyValue(linkRecords)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to link records on table {}", tableId, err));
  }
}
