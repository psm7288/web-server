package com.hosting.repository;

import com.hosting.entity.ServerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServerOrderRepository extends JpaRepository<ServerOrder, Long> {

    // ✨ [신규 추가] 특정 사용자 이름(username)을 가진 가상 서버 패키지 목록만 조회하는 쿼리 메소드
    List<ServerOrder> findByUsername(String username);
}