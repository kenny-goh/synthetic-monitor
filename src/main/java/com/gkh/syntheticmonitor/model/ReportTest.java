package com.gkh.syntheticmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
	private transient int numberOfTestsMatchingExpectedStatus;
	private transient int numberOfTestsWithinMaximumThresholdTime;
	private transient int numberOfTestsWithinOptimalThresholdTime;

	@CreationTimestamp
	private Timestamp timestamp;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "parent_id")
	private List<ReportTestAction> transactionReports = new ArrayList<>();

	@Column
	@Access(AccessType.PROPERTY)
	public int getNumberOfTests() {
		return this.transactionReports.size();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public int getNumberOfTestsMatchingExpectedStatus() {
		return this.transactionReports.stream().filter(each->each.isStatusSuccess()).collect(Collectors.toList()).size();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public int getNumberOfTestsWithinMaximumThresholdTime() {
		return this.transactionReports.stream().filter(each->each.isResponseTimeUnderMax()).collect(Collectors.toList()).size();
	}

	@Column
	@Access(AccessType.PROPERTY)
	public int getNumberOfTestsWithinOptimalThresholdTime() {
		return this.transactionReports.stream().filter(each->each.isResponseTimeOptimal()).collect(Collectors.toList()).size();
	}

}
