package com.hosting.repository;

import com.hosting.entity.DbServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface DbServerInfoRepository extends JpaRepository<DbServerInfo, Long> {

    // 1. 기존에 작성하신 복잡한 쿼리
    @Query("SELECT d FROM DbServerInfo d " +
            "WHERE d.server.serverId = :serverId " +
            "AND d.serverRequest.member.memberId = :memberId")
    Optional<DbServerInfo> findByServerIdAndMemberId(
            @Param("serverId") Long serverId,
            @Param("memberId") Long memberId);

    // 2. 서비스에서 사용할 메서드 (이 부분이 꼭 있어야 합니다!)
    // 만약 에러가 계속된다면, DbServerInfo 엔티티 안의 필드명에 맞춰 아래를 조절해야 합니다.
    Optional<DbServerInfo> findByServerRequest_RequestId(Long requestId);

    // JPQL을 사용하여 명시적으로 requestId를 통해 조회합니다.
    // d.serverRequest.requestId : DbServerInfo 엔티티 -> ServerRequest 엔티티 -> requestId 필드 순서
    @Query("SELECT d FROM DbServerInfo d WHERE d.serverRequest.requestId = :requestId")
    Optional<DbServerInfo> findByRequestId(@Param("requestId") Long requestId);
}