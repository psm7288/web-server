package com.hosting.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequestDto {
    private String username;
    private String password;
    private String passwordConfirm;
    private String name;
    private String email;
    private String phone;
}
