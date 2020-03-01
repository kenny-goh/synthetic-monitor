package com.gkh.syntheticmonitor.model;


import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.stream.Collectors;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Access(AccessType.FIELD)
public class ReportTestAction {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	private String type;
	private String status;

	private long responseTime;
	private long optimalResponseThreshold;
	private long maximumResponseThreshold;

	private transient boolean responseTimeOptimal;
	private transient boolean responseTimeUnderMax;
	private transient boolean statusSuccess;

	private String expectedStatus;

	@Lob
	private String content;

	@ManyToOne
	@JsonIgnore
	private ReportTest parent;

	@Column
	@Access(AccessType.PROPERTY)
	public boolean isStatusSuccess() {
		return this.status.equals(this.expectedStatus);
	}

	@Column
	@Access(AccessType.PROPERTY)
	public boolean isResponseTimeOptimal() {
		return this.responseTime <= this.optimalResponseThreshold;
	}

	@Column
	@Access(AccessType.PROPERTY)
	public boolean isResponseTimeUnderMax() {
		return this.responseTime <= this.maximumResponseThreshold;
	}



}
