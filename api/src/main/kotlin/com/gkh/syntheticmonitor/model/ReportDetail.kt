package com.gkh.syntheticmonitor.model

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import javax.persistence.*

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Access(AccessType.FIELD)
class ReportDetail(@Id @GeneratedValue(strategy = GenerationType.AUTO)  val id: Long? = null,
                   var name: String? = null,
                   var details: String? = null,
                   var type: String? = null,
                   var status: String? = null,
                   var responseTime: Long = 0,
                   var maximumResponseThreshold: Long = 0,
                   var expectedStatus: String? = null,
                   @Transient val content: String? = null,
                   @ManyToOne @JsonIgnore val parent: Report? = null) {

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var isResponseTimeUnderMax = false
        get() = responseTime <= maximumResponseThreshold

    @get:Access(AccessType.PROPERTY) @get:Column @Transient
    var isStatusSuccess = false
        get() = isStatusCodeMatching and this.isResponseTimeUnderMax

    @get:JsonGetter
    val isStatusCodeMatching: Boolean
        get() = status == expectedStatus
}