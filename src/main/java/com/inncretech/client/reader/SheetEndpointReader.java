package com.inncretech.client.reader;

import com.inncretech.client.model.dto.EndpointDefinition;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class SheetEndpointReader {

  @Value("${sheet.google-sheet-url}")
  private String googleSheetUrl;

  @Value("${sheet.local-csv-path:#{null}}")
  private String localCsvPath;

  @Value("${sheet.fallback-resource-csv:classpath:sample-endpoints.csv}")
  private String fallbackResourceCsv;

  private final ResourceLoader resourceLoader;
  private final WebClient webClient;

  public SheetEndpointReader(ResourceLoader resourceLoader, WebClient.Builder builder) {
    this.resourceLoader = resourceLoader;
    this.webClient = builder.build();
  }

  /**
   * Reads endpoints prioritizing:
   * 1. Google Sheets CSV URL (if accessible)
   * 2. Local CSV export file path
   * 3. Classpath bundled sample CSV
   */
  public List<EndpointDefinition> readEndpoints() {
    // 1. Try reading directly from Google Sheet export URL
    if (StringUtils.isNotBlank(googleSheetUrl)) {
      try {
        log.info("Attempting to fetch endpoints from Google Sheet URL: {}", googleSheetUrl);
        String csvContent = webClient.get()
            .uri(googleSheetUrl)
            .retrieve()
            .bodyToMono(String.class)
            .block();

        if (StringUtils.isNotBlank(csvContent) && csvContent.contains("Route")) {
          log.info("Successfully fetched sheet from Google Sheets URL");
          return parseCsv(new StringReader(csvContent));
        }
      } catch (Exception ex) {
        log.warn("Could not fetch remote Google Sheet directly (it may require Google Auth)", ex);
      }
    }

    // 2. Try reading local CSV file (e.g. downloaded file in ~/Downloads)
    if (StringUtils.isNotBlank(localCsvPath)) {
      File file = new File(localCsvPath);
      if (file.exists() && file.canRead()) {
        try (InputStream in = new FileInputStream(file);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
          log.info("Reading endpoints from local CSV file: {}", localCsvPath);
          return parseCsv(reader);
        } catch (Exception ex) {
          log.warn("Failed to read local CSV file at {}: {}", localCsvPath, ex.getMessage(), ex);
        }
      } else {
        log.info("Local CSV file not found at: {}", localCsvPath);
      }
    }

    // 3. Fallback to bundled classpath CSV
    try {
      log.info("Falling back to bundled resource CSV: {}", fallbackResourceCsv);
      Resource resource = resourceLoader.getResource(fallbackResourceCsv);
      try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
        return parseCsv(reader);
      }
    } catch (Exception ex) {
      log.error("Failed to read fallback CSV", ex);
      return List.of();
    }
  }

  /**
   * Parses CSV records into EndpointDefinition list.
   */
  public List<EndpointDefinition> parseCsv(Reader reader) {
    List<EndpointDefinition> endpoints = new ArrayList<>();
    try {
      CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
          .setHeader()
          .setSkipHeaderRecord(true)
          .setIgnoreHeaderCase(true)
          .setTrim(true)
          .build();

      try (CSVParser parser = new CSVParser(reader, csvFormat)) {
        for (CSVRecord record : parser) {
          String method = getRecordValue(record, "Method");
          String route = getRecordValue(record, "Route", "API Endpoint (URI)");
          if (StringUtils.isBlank(method) || StringUtils.isBlank(route)) {
            continue;
          }

          endpoints.add(EndpointDefinition.builder()
              .method(method.toUpperCase().trim())
              .route(route.trim())
              .pkg(getRecordValue(record, "Package", "Category"))
              .controllerMethod(getRecordValue(record, "Controller.method"))
              .fileAndLine(getRecordValue(record, "file:line"))
              .action(getRecordValue(record, "Action"))
              .build());
        }
      }
    } catch (Exception ex) {
      log.error("Error parsing CSV data", ex);
    }
    log.info("Parsed {} endpoints from CSV source", endpoints.size());
    return endpoints;
  }

  private String getRecordValue(CSVRecord record, String... headerNames) {
    for (String header : headerNames) {
      if (record.isMapped(header)) {
        return record.get(header);
      }
    }
    return "";
  }
}
