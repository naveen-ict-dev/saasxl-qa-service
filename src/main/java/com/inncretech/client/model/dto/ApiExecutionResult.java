package com.inncretech.client.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionResult {

  private String method;
  private String path;
  private int statusCode;
  private boolean success;
  private long durationMs;
  private String responseSnippet;
  private String errorMessage;
}
