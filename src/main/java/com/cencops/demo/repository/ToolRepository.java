package com.cencops.demo.repository;

import com.cencops.demo.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Long> {

    Optional<Tool> findByNameIgnoreCase(String name);

}
