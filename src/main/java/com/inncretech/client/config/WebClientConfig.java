package com.inncretech.client.config;

import com.inncretech.client.auth.AuthService;
import com.inncretech.client.auth.TokenManager;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@Slf4j
public class WebClientConfig {

  @Value("${backend.base-url}")
  private String baseUrl;

  @Value("${webclient.connect-timeout-ms:10000}")
  private int connectTimeoutMs;

  @Value("${webclient.read-timeout-ms:30000}")
  private int readTimeoutMs;

  @Value("${webclient.max-in-memory-size-mb:16}")
  private int maxInMemorySizeMb;

  @Bean
  public WebClient backendWebClient(
      WebClient.Builder builder,
      AuthService authService,
      TokenManager tokenManager) {

    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
        .responseTimeout(Duration.ofMillis(readTimeoutMs))
        .doOnConnected(conn -> conn
            .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
            .addHandlerLast(new WriteTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS)));

    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxInMemorySizeMb * 1024 * 1024))
        .build();

    return builder
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(strategies)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .filter(authHeaderFilter(authService))
        .filter(autoRefreshOnUnauthorizedFilter(authService, tokenManager))
        .filter(logRequestResponseFilter())
        .build();
  }

  /**
   * Filter that automatically adds 'Authorization: Bearer <token>' to every request.
   */
  private ExchangeFilterFunction authHeaderFilter(AuthService authService) {
    return (ClientRequest request, ExchangeFunction next) -> {
      String path = request.url().getPath();
      // Whitelist login and auth endpoints from having bearer token attached
      if (path.contains("/idp/api/v1/user/login")
          || path.contains("/idp/api/v1/user/token")
          || path.contains("/idp/api/v1/user/v2/login")
          || path.contains("/idp/api/v1/user/v2/refresh")
          || path.contains("/idp/api/v1/user/sign-up")
          || path.contains("/idp/api/v1/user/verify")
          || path.contains("/idp/api/v1/user/automation/")) {
        return next.exchange(request);
      }

      String token = authService.getOrRefreshAccessToken();
      ClientRequest authorizedRequest = ClientRequest.from(request)
          .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token))
          .build();

      return next.exchange(authorizedRequest);
    };
  }

  /**
   * Filter that intercepts 401 Unauthorized, forces a token refresh, and retries once.
   */
  private ExchangeFilterFunction autoRefreshOnUnauthorizedFilter(
      AuthService authService, TokenManager tokenManager) {
    return (ClientRequest request, ExchangeFunction next) ->
        next.exchange(request).flatMap(response -> {
          if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
            log.warn("Received 401 Unauthorized for {}. Refreshing token and retrying...", request.url());
            tokenManager.clear();
            String newToken = authService.getOrRefreshAccessToken();
            ClientRequest retryRequest = ClientRequest.from(request)
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + newToken))
                .build();
            return next.exchange(retryRequest);
          }
          return Mono.just(response);
        });
  }

  /**
   * Filter for logging request method/URI and response status.
   */
  private ExchangeFilterFunction logRequestResponseFilter() {
    return ExchangeFilterFunction.ofRequestProcessor(request -> {
      log.debug("HTTP {} {}", request.method(), request.url());
      return Mono.just(request);
    }).andThen(ExchangeFilterFunction.ofResponseProcessor(response -> {
      log.debug("HTTP Status: {}", response.statusCode());
      return Mono.just(response);
    }));
  }
}
