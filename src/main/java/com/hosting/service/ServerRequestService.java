package com.hosting.service;

import com.hosting.dto.ServerRequestDto;
import com.hosting.entity.Member;
import com.hosting.entity.ServerRequest;
import com.hosting.repository.MemberRepository;
import com.hosting.repository.ServerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServerRequestService {

    private final ServerRequestRepository serverRequestRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createServerRequest(ServerRequestDto dto, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ServerRequest request = new ServerRequest();

        // 필수 값 세팅
        request.setMember(member);
        request.setRequestType("hosting");
        request.setServerName(dto.getServerName() + "-hosting");
        request.setWebServerName(dto.getWebServerName());
        request.setDbServerName(dto.getDbServerName());

        // 기본값 하드코딩 (필요시 dto에서 받도록 수정)
        request.setImage("Ubuntu-24.04");
        request.setFlavor("c2.r4.d50");
        request.setNetworkName("selfservice");
        request.setKeyName("mykey");
        request.setTargetHost("compute-PowerEdge-T360");
        request.setNeedDb(true);
        request.setStatus("PENDING");

        // DB에 저장하고 생성된 ID 반환 (여기서 메서드가 종료되며 Commit 됨)
        ServerRequest saved = serverRequestRepository.save(request);
        return saved.getRequestId();
    }
}