package com.gkh.syntheticmonitor.repository;
import com.gkh.syntheticmonitor.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
