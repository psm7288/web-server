package com.hosting.repository;

import com.hosting.entity.DbServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DbServerInfoRepository extends JpaRepository<DbServerInfo, Long> {

    /**
     * 서버 ID와 회원 ID를 이용해 해당 사용자의 DB 정보를 안전하게 조회합니다.
     */
    Optional<DbServerInfo> findByServer_ServerIdAndMemberId(Long serverId, Long memberId);
}