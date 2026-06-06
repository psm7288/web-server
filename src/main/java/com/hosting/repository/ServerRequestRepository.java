package com.hosting.repository;

import com.hosting.entity.ServerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ServerRequestRepository extends JpaRepository<ServerRequest, Long> {

    // 1. 중복 확인용
    boolean existsByDbServerName(String dbServerName);

    // 2. Fetch Join을 사용하여 서버 목록까지 한 번에 로딩 (성능 최적화 및 N+1 문제 해결)
    @Query("SELECT DISTINCT sr FROM ServerRequest sr LEFT JOIN FETCH sr.servers WHERE sr.member.memberId = :memberId ORDER BY sr.createdAt DESC")
    List<ServerRequest> findAllByMemberIdWithServers(@Param("memberId") Long memberId);

    // 3. 에러가 발생한 메서드 추가 (규칙: findBy + 필드명 + 정렬조건)
    // Server 엔티티의 member 필드 내 memberId를 기준으로 정렬
    List<ServerRequest> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);
}