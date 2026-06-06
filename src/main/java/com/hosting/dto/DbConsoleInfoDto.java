package com.hosting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DbConsoleInfoDto {
    private String dbConsoleUrl;
    private String dbLoginServer; // 항상 "localhost"
    private String dbName;
    private String dbUsername;
    private String dbPassword;
    private String dbHost;
    private Integer dbPort;
}