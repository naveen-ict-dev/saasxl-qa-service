package com.inncretech.client.client;

import com.inncretech.client.model.dto.AttachmentDTO;
import java.util.List;
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
public class FileApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /noCo/api/v1/file/upload with isPublicStorage=true. The response's `key` (not
   * `fileName`) is what /public/download looks the object up by.
   */
  public Mono<List<AttachmentDTO>> uploadPublicFile(byte[] content, String filename) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("files", new ByteArrayResource(content) {
      @Override
      public String getFilename() {
        return filename;
      }
    });

    return backendWebClient.post()
        .uri(uriBuilder -> uriBuilder
            .path("/noCo/api/v1/file/upload")
            .queryParam("isPublicStorage", true)
            .build())
        .body(BodyInserters.fromMultipartData(builder.build()))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<AttachmentDTO>>() {})
        .doOnError(err -> log.error("Failed to upload public file {}", filename, err));
  }

  /**
   * Calls GET /noCo/api/v1/file/public/download
   */
  public Mono<byte[]> downloadPublicFile(Long companyId, String fileName) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/noCo/api/v1/file/public/download")
            .queryParam("companyId", companyId)
            .queryParam("fileName", fileName)
            .build())
        .retrieve()
        .bodyToMono(byte[].class)
        .doOnError(err -> log.error("Failed to download public file {}", fileName, err));
  }
}
