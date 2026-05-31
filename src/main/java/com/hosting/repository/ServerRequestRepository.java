package com.hosting.repository;

import com.hosting.entity.ServerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServerRequestRepository extends JpaRepository<ServerRequest, Long> {
    // Member 엔티티 내부의 memberId 필드를 기준으로 조회
    List<ServerRequest> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);
}