package com.hosting.repository;

import com.hosting.entity.ServerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ServerRequestRepository extends JpaRepository<ServerRequest, Long> {

    // 사용자의 신청 목록 조회 (기존 유지)
    List<ServerRequest> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);

    // [수정] DB 서버 이름 중복 체크 (직접 쿼리 방식)
    @Query("SELECT COUNT(s) > 0 FROM ServerRequest s WHERE s.dbServerName = :dbServerName")
    boolean existsByDbServerName(@Param("dbServerName") String dbServerName);
}