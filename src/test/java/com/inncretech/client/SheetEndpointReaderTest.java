package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.inncretech.client.model.dto.EndpointDefinition;
import com.inncretech.client.reader.SheetEndpointReader;
import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.web.reactive.function.client.WebClient;

class SheetEndpointReaderTest {

  @Test
  void testParseCsv() {
    SheetEndpointReader reader = new SheetEndpointReader(
        new DefaultResourceLoader(), WebClient.builder());

    String sampleCsv = """
        Method,Route,Package,Controller.method,file:line,Action
        GET,/idp/api/v1/user,controller/idp,UserController.getCurrentUser,UserController.java:25,Get User
        POST,/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/cursor,controller/noco,TableV2Controller.getCursorData,TableV2Controller.java:1285,Fetch Rows
        """;

    List<EndpointDefinition> endpoints = reader.parseCsv(new StringReader(sampleCsv));

    assertEquals(2, endpoints.size());

    EndpointDefinition first = endpoints.get(0);
    assertEquals("GET", first.getMethod());
    assertEquals("/idp/api/v1/user", first.getRoute());
    assertEquals("controller/idp", first.getPkg());

    EndpointDefinition second = endpoints.get(1);
    assertEquals("POST", second.getMethod());
    assertEquals("/noCo/api/v2/workspaces/{workspaceId}/tables/{tableId}/cursor", second.getRoute());
  }
}
