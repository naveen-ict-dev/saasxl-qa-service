package com.inncretech.client.client;

import com.inncretech.client.model.dto.CompanyThemeDTO;
import com.inncretech.client.model.dto.ServiceAccountDTO;
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
public class CompanyApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /idp/api/v1/company
   */
  public Mono<Map<?, ?>> getCompany() {
    return backendWebClient.get()
        .uri("/idp/api/v1/company")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch company", err));
  }

  /**
   * Calls GET /idp/api/v1/company/llm/models
   */
  public Mono<List<Map<?, ?>>> getLlmModels() {
    return backendWebClient.get()
        .uri("/idp/api/v1/company/llm/models")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch LLM models", err));
  }

  /**
   * Calls POST /idp/api/v1/company/theme
   */
  public Mono<CompanyThemeDTO> createTheme(CompanyThemeDTO theme) {
    return backendWebClient.post()
        .uri("/idp/api/v1/company/theme")
        .bodyValue(theme)
        .retrieve()
        .bodyToMono(CompanyThemeDTO.class)
        .doOnError(err -> log.error("Failed to create company theme", err));
  }

  /**
   * Calls GET /idp/api/v1/company/theme
   */
  public Mono<List<CompanyThemeDTO>> getThemes() {
    return backendWebClient.get()
        .uri("/idp/api/v1/company/theme")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<CompanyThemeDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch company themes", err));
  }

  /**
   * Calls POST /idp/api/v1/company/serviceAccount (requires ROLE_ACCOUNT_ADMIN)
   */
  public Mono<ServiceAccountDTO> createServiceAccount(ServiceAccountDTO serviceAccount) {
    return backendWebClient.post()
        .uri("/idp/api/v1/company/serviceAccount")
        .bodyValue(serviceAccount)
        .retrieve()
        .bodyToMono(ServiceAccountDTO.class)
        .doOnError(err -> log.error("Failed to create service account", err));
  }

  /**
   * Calls GET /idp/api/v1/company/serviceAccount (requires ROLE_ACCOUNT_ADMIN)
   */
  public Mono<List<ServiceAccountDTO>> getServiceAccounts() {
    return backendWebClient.get()
        .uri("/idp/api/v1/company/serviceAccount")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<ServiceAccountDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch service accounts", err));
  }
}
