package qinghuan.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求 DTO
 */
@Data
public class UserRegisterRequest {
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Email(message = "Email must be valid")
    private String email;
}