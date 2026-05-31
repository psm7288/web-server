package com.hosting.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class OpenStackService {

    private final String OPENSTACK_IP = "172.141.0.216";

    // 1. Keystone에서 토큰 가져오는 함수
    public String getToken() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://" + OPENSTACK_IP + ":5000/v3/auth/tokens";

        // JSON 문서를 만드는 부분 (텍스트 블록 사용)
        String body = """
            {
            
                "auth": {
                    "identity": {
                        "methods": ["password"],
                        "password": {
                            "user": {
                                "name": "admin",
                                "domain": { "id": "default" },
                                "password": "openstack"
                            }
                        }
                    },
                    "scope": {
                        "project": {
                            "name": "admin",
                            "domain": { "id": "default" }
                        }
                    }
                }
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // 실제 요청 보내기
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 응답 헤더에서 토큰(X-Subject-Token) 꺼내기
        return response.getHeaders().getFirst("X-Subject-Token");
    }

    // 2. Nova에서 콘솔 URL 가져오는 함수
    public String getConsoleUrl(String serverId) {
        String token = getToken(); // 위에서 만든 토큰을 먼저 가져옴
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://" + OPENSTACK_IP + ":8774/v2.1/servers/" + serverId + "/action";

        // VNC 콘솔 주소를 요청하는 데이터
        String body = "{\"os-getVNCConsole\": {\"type\": \"novnc\"}}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token); // 발급받은 토큰을 신분증으로 제시
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        // OpenStack에 요청 보내기
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        // 응답 결과에서 URL만 추출해서 리턴
        Map<String, String> console = (Map<String, String>) response.get("console");
        return console.get("url");
    }
}