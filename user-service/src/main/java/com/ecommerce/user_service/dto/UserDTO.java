package com.ecommerce.user_service.dto;

import com.ecommerce.user_service.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {

    private String userId;

    @NotBlank(message = "Please provide Email ID.")
    @Email(message = "Please provide Email ID in correct format.")
    private String emailId;

    @NotBlank(message = "Please provide password.")
    @Pattern(regexp="((?=.*\\d)(?=.*[a-z])(?=.*[#@$*]).{5,20})",
            message = "Password does not match with the pattern.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "Please confirm password.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String confirmPassword;

    @NotNull(message = "Please provide Role.")
    private UserRole userRole;

    @AssertTrue(message = "The password fields must match!")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

}
