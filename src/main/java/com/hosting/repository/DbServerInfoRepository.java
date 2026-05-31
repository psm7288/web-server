package com.hosting.repository;

import com.hosting.entity.DbServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DbServerInfoRepository extends JpaRepository<DbServerInfo, Long> {

    /**
     * [수정 완료]
     * 엔티티 관계(DbServerInfo -> ServerRequest -> Member)를 탐색하여
     * 해당 사용자가 소유한 서버의 DB 정보만 안전하게 조회합니다.
     */
    @Query("SELECT d FROM DbServerInfo d " +
            "WHERE d.server.serverId = :serverId " +
            "AND d.serverRequest.member.memberId = :memberId")
    Optional<DbServerInfo> findByServerIdAndMemberId(
            @Param("serverId") Long serverId,
            @Param("memberId") Long memberId);
}