package com.hosting.service;

import com.hosting.dto.DbConsoleInfoDto;
import com.hosting.entity.DbServerInfo;
import com.hosting.entity.Server;
import com.hosting.repository.DbServerInfoRepository;
import com.hosting.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final DbServerInfoRepository dbServerInfoRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getServerDetail(Long requestId) {
        Map<String, Object> detail = new HashMap<>();
// ServerService.java 수정
        List<Server> servers = serverRepository.findByServerRequest_RequestId(requestId);

        for (Server server : servers) {
            // Web 서버 카드 데이터
            if ("web".equals(server.getServerType())) {
                detail.put("webServer", server);
            }
            // DB 서버 카드 데이터 및 접속 정보
            else if ("db".equals(server.getServerType())) {
                detail.put("dbServer", server);

                // DB 상세 정보(db_server_info) 조회
                DbServerInfo info = dbServerInfoRepository.findByRequestId(requestId)
                        .orElseThrow(() -> new RuntimeException("DB 상세정보를 찾을 수 없습니다."));

                // DTO 빌드
                DbConsoleInfoDto dbInfo = DbConsoleInfoDto.builder()
                        .dbConsoleUrl(info.getDbConsoleUrl())
                        .dbLoginServer("localhost") // Adminer 접속용 고정값
                        .dbName(info.getDbName())
                        .dbUsername(info.getDbUsername())
                        .dbPassword(info.getDbPassword())
                        .dbHost(info.getDbHost())
                        .dbPort(info.getDbPort())
                        .build();

                detail.put("dbConsoleInfo", dbInfo);
            }
        }
        return detail;
    }
}