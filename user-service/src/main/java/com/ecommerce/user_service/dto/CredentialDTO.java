package com.ecommerce.user_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CredentialDTO {

    @NotBlank(message = "Please provide Email ID.")
    @Email(message = "Please provide Email ID in correct format.")
    private String emailId;

    @NotBlank(message = "Please provide password.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
