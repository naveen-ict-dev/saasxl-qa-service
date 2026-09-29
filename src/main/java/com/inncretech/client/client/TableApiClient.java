package com.inncretech.client.client;

import com.inncretech.client.model.dto.ColumnDTO;
import com.inncretech.client.model.dto.NoCoQueryDTO;
import com.inncretech.client.model.dto.QueryOptionDTO;
import com.inncretech.client.model.dto.TableDTO;
import com.inncretech.client.model.dto.WorkbookActionDTO;
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
public class TableApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables to create a table.
   */
  public Mono<TableDTO> createTable(String workspaceId, TableDTO table) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables", workspaceId)
        .bodyValue(table)
        .retrieve()
        .bodyToMono(TableDTO.class)
        .doOnError(err -> log.error("Failed to create table in workspace {}", workspaceId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/column to add a user column.
   * Creating a table with columns inline (in the addTable body) 500s server-side today, so a
   * fixture table needing a writable column must add it via this separate call instead.
   */
  public Mono<ColumnDTO> addColumn(String workspaceId, String tableId, ColumnDTO column) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/column", workspaceId, tableId)
        .bodyValue(column)
        .retrieve()
        .bodyToMono(ColumnDTO.class)
        .doOnError(err -> log.error("Failed to add column to table {}", tableId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}
   */
  public Mono<TableDTO> getTable(String workspaceId, String tableId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}", workspaceId, tableId)
        .retrieve()
        .bodyToMono(TableDTO.class)
        .doOnError(err -> log.error("Failed to fetch table {}", tableId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/data/external
   */
  public Mono<Map<?, ?>> getExternalData(String workspaceId, String tableId, QueryOptionDTO options) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/data/external", workspaceId, tableId)
        .bodyValue(options != null ? options : new QueryOptionDTO())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch external data for table {}", tableId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/executeQuery
   */
  public Mono<Map<?, ?>> executeQuery(String workspaceId, String tableId, NoCoQueryDTO query) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/executeQuery", workspaceId, tableId)
        .bodyValue(query)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to execute query on table {}", tableId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/{columnId}/linkData
   */
  public Mono<List<Map<?, ?>>> getLinkData(String workspaceId, String tableId, String columnId, Object rowId) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/{columnId}/linkData")
            .queryParam("id", rowId)
            .build(workspaceId, tableId, columnId))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch link data for column {}", columnId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/linkTables
   */
  public Mono<List<TableDTO>> getLinkTables(String workspaceId, String tableId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/linkTables", workspaceId, tableId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<TableDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch link tables for table {}", tableId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/enrichments
   */
  public Mono<Map<?, ?>> getEnrichments(String workspaceId, String tableId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/enrichments", workspaceId, tableId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch enrichments for table {}", tableId, err));
  }

  /**
   * Calls POST /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/actions
   */
  public Mono<WorkbookActionDTO> createAction(String workspaceId, String tableId, WorkbookActionDTO action) {
    return backendWebClient.post()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/actions", workspaceId, tableId)
        .bodyValue(action)
        .retrieve()
        .bodyToMono(WorkbookActionDTO.class)
        .doOnError(err -> log.error("Failed to create action on table {}", tableId, err));
  }

  /**
   * Calls GET /noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/actions
   */
  public Mono<List<WorkbookActionDTO>> getActions(String workspaceId, String tableId) {
    return backendWebClient.get()
        .uri("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/actions", workspaceId, tableId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<WorkbookActionDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch actions for table {}", tableId, err));
  }
}
