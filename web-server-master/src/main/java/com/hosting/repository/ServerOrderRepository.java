package com.hosting.repository;

import com.hosting.entity.ServerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerOrderRepository extends JpaRepository<ServerOrder, Long> {
    // 기본 CRUD 기능 자동 제공 (save, findAll 등)
}