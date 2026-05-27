package com.hosting.repository;

import com.hosting.entity.ServerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional; // 💡 Optional 임포트 추가

@Repository
public interface ServerOrderRepository extends JpaRepository<ServerOrder, Long> {
    List<ServerOrder> findByUsername(String username);

    // 💡 [여기에 이 한 줄을 추가해 주세요!]
    Optional<ServerOrder> findByDbName(String dbName);
}