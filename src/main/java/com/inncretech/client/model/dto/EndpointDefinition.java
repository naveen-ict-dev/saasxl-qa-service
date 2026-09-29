package com.inncretech.client.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointDefinition {

  private String method;
  private String route;
  private String pkg;
  private String controllerMethod;
  private String fileAndLine;
  private String action;
}
