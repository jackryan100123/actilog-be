package com.cencops.demo.repository;

import com.cencops.demo.entity.AppManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppManualRepository extends JpaRepository<AppManual, Long> {

}
