package com.gkh.syntheticmonitor.model

import com.fasterxml.jackson.annotation.JsonGetter
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.sql.Timestamp
import java.util.stream.Collectors
import javax.persistence.*

@Entity
@Access(AccessType.FIELD)
class Report(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) var id: Long? = null,
        var name: String? = null
) {

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var numberOfTests = 0
        get() = reportDetails.size

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var isPassed = false
        get() = reportDetails
                .stream()
                .filter { each: ReportDetail -> each.isStatusSuccess }
                .collect(Collectors.toList()).size == numberOfTests

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var sumResponseTime: Long = 0
        get() = reportDetails
                .stream()
                .mapToLong { obj: ReportDetail -> obj.responseTime }
                .summaryStatistics().sum

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var minResponseTime: Long = 0
        get() = reportDetails
                .stream()
                .mapToLong { obj: ReportDetail -> obj.responseTime }
                .summaryStatistics().min

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var maxResponseTime: Long = 0
        get() = reportDetails
                .stream()
                .mapToLong { obj: ReportDetail -> obj.responseTime }
                .summaryStatistics().max
    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var averageResponseTime: Long = 0
        get() = reportDetails
                .stream()
                .mapToLong { obj: ReportDetail -> obj.responseTime }
                .summaryStatistics().average.toLong()

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var numberOfActions = 0
        get() = reportDetails.size

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var sumMaxTimeThreshold: Long = 0
        get() = reportDetails.stream().mapToLong { o: ReportDetail -> o.maximumResponseThreshold }.summaryStatistics().sum

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var isAllResponseTimeUnderMax = false
        get() = reportDetails.stream().allMatch { p: ReportDetail -> p.isResponseTimeUnderMax }

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var isAllStatusCodeMatching = false
        get() = reportDetails.stream().allMatch { p: ReportDetail -> p.isStatusCodeMatching }

    @CreationTimestamp
    var timestamp: Timestamp? = null

    @OneToMany(cascade = [CascadeType.ALL],
            fetch = FetchType.EAGER,
            orphanRemoval = true)
    @JoinColumn(name = "parent_id")
    @Fetch(FetchMode.JOIN)
    val reportDetails: MutableList<ReportDetail> = arrayListOf()

    @get:JsonGetter
    val type: String?
        get() = if (reportDetails.size == 1) {
            reportDetails[0].type
        }
        else {
            "Transactions"
        }

}