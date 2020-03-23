package com.gkh.syntheticmonitor.model;


import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Access(AccessType.FIELD)
public class ReportDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	private String details;
	private String type;
	private String status;

	private long responseTime;
	private long maximumResponseThreshold;

	private transient boolean responseTimeUnderMax;
	private transient boolean statusSuccess;

	private String expectedStatus;

	private transient String content;

	@ManyToOne
	@JsonIgnore
	private Report parent;

	@Column
	@Access(AccessType.PROPERTY)
	public boolean isStatusSuccess() {
		return this.isStatusCodeMatching() & this.isResponseTimeUnderMax();
	}


	@Column
	@Access(AccessType.PROPERTY)
	public boolean isResponseTimeUnderMax() {
		return this.responseTime <= this.maximumResponseThreshold;
	}

	@JsonGetter
	public boolean isStatusCodeMatching() {
		return this.status.equals(this.expectedStatus);
	}

}
