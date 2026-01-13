package com.cencops.demo.dto.request;

import com.cencops.demo.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Status is required")
    private User.Status status;

    @NotNull(message = "Role is required")
    private User.Role role;
}