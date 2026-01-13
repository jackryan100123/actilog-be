package com.cencops.demo.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String status;
    private String name;
    private String designation;
    private String username;
    private String role;
}
