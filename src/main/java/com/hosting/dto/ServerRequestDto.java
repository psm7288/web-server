package com.hosting.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerRequestDto {
    private String serverName;
    private String webServerName;
    private String dbServerName;
}