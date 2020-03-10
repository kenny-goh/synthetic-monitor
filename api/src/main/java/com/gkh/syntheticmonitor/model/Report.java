package com.gkh.syntheticmonitor.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.cache.annotation.Cacheable;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Access(AccessType.FIELD)
public class ReportTest {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;
	
	private transient int numberOfTests;
	private transient boolean passed;
	private transient long sumResponseTime;
	private transient long minResponseTime;
	private transient long maxResponseTime;
	private transient long averageResponseTime;
	private transient int numberOfActions;
	private transient long sumOptimalTimeThreshold;
	private transient long sumMaxTimeThreshold;
	private transient boolean allResponseTimeOptimal;
	private transient boolean allResponseTimeUnderMax;
	private transient boolean allStatusCodeMatching;

	@CreationTimestamp
	private Timestamp timestamp;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "parent_id")
	@Fetch(FetchMode.JOIN)
	private List<ReportTestAction> transactionReports = new ArrayList<>();

	@Column
	@Access(AccessType.PROPERTY)
	public int getNumberOfTests() {
		return this.transactionReports.size();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public boolean isPassed() {
		return this.transactionReports
				.stream()
				.filter(each->each.isStatusSuccess())
				.collect(Collectors.toList()).size() == this.getNumberOfTests();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public long getMinResponseTime() {
		return this.transactionReports
				.stream()
				.mapToLong(ReportTestAction::getResponseTime)
				.summaryStatistics().getMin();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public long getMaxResponseTime() {
		return this.transactionReports
				.stream()
				.mapToLong(ReportTestAction::getResponseTime)
				.summaryStatistics().getMax();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public long getAverageResponseTime() {
		return (long) this.transactionReports
				.stream()
				.mapToLong(ReportTestAction::getResponseTime)
				.summaryStatistics().getAverage();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public long getSumResponseTime() {
		return this.transactionReports
				.stream()
				.mapToLong(ReportTestAction::getResponseTime)
				.summaryStatistics().getSum();
	}

	@JsonGetter
	public String getType() {
		if (this.transactionReports.size() == 1) {
			return this.transactionReports.get(0).getType();
		} else {
			return "Transactions";
		}
	}

	@Column
	@Access(AccessType.PROPERTY)
	public int getNumberOfActions() {
		return this.transactionReports.size();
	}


	@Column
	@Access(AccessType.PROPERTY)
	public long getSumOptimalTimeThreshold() {
		return this.transactionReports.stream().mapToLong(o->o.getOptimalResponseThreshold()).summaryStatistics().getSum();
	}


	@Column
	@Access(AccessType.PROPERTY)
	public long getSumMaxTimeThreshold() {
		return this.transactionReports.stream().mapToLong(o->o.getMaximumResponseThreshold()).summaryStatistics().getSum();
	}


	@Column
	@Access(AccessType.PROPERTY)
	public boolean isAllResponseTimeOptimal() {
		return this.transactionReports.stream().allMatch(p->p.isResponseTimeOptimal());
	}


	@Column
	@Access(AccessType.PROPERTY)
	public boolean isAllResponseTimeUnderMax() {
		return this.transactionReports.stream().allMatch(p->p.isResponseTimeUnderMax());
	}

	@Column
	@Access(AccessType.PROPERTY)
	public boolean isAllStatusCodeMatching() {
		return this.transactionReports.stream().allMatch(p->p.isStatusCodeMatching());
	}




}
