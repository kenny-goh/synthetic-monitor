package com.gkh.syntheticmonitor.repository;
import com.gkh.syntheticmonitor.model.ReportTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportTest, Long> {
}
