package com.hosting.repository;

import com.hosting.entity.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServerRepository extends JpaRepository<Server, Long> {
    List<Server> findByServerRequest_RequestId(Long requestId);
}