package com.hosting.repository;

import com.hosting.entity.ServerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServerRequestRepository extends JpaRepository<ServerRequest, Long> {
    List<ServerRequest> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);
}