package com.hosting.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerOrderDto {
    // 웹 서버 정보
    private String webName;
    private String webDesc;
    private int webCpu;
    private int webRam;
    private int webStorage;

    // 데이터베이스 정보 (dbPassword 필드가 누락되면 다음 단계에서 유실됩니다!)
    private String dbName;
    private String dbSchemaName;
    private String dbUser;
    private String dbPassword; // 🛠️ 추가 확인
    private int dbCpu;
    private int dbRam;
    private int dbStorage;

    // 상품 패키지 타입 식별용
    private String productPackage; // 🛠️ 추가 확인
}