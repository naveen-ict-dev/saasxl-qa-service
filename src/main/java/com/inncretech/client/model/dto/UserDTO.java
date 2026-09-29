package com.inncretech.client.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

  private Long id;
  private String firstname;
  private String lastname;
  private String email;
  private String companyName;
  private Long companyId;
  private String status;
  private List<String> roles;
  private Set<String> permissions;
  private Boolean mfaEnabled;
  private String role;
}
