package com.gkh.syntheticmonitor.repository

import com.gkh.syntheticmonitor.model.Report
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportRepository : JpaRepository<Report, Long?>