package model;

import java.util.ArrayList;
import java.util.UUID;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Worker {

	private StringProperty id;
	public StringProperty idProperty() {
		if(id == null)
			id = new SimpleStringProperty(this, "id");
		return id;
	}
	
	private StringProperty number;
	public StringProperty numberProperty() {
		if(number == null)
			number = new SimpleStringProperty(this, "num");
		return number;
	}
	
	private int[] skillSet;
	private StringProperty skillsProperty;
	public StringProperty skillsProperty() {
		if(skillsProperty == null)
			skillsProperty = new SimpleStringProperty(this, "skills");
		return skillsProperty;
	}
	
	private double hourlyPay;
	
	private ArrayList<Job> jobs;
	private StringProperty jobsProperty;
	public StringProperty jobsProperty() {
		if(jobsProperty == null)
			jobsProperty = new SimpleStringProperty(this, "jobs");
		return jobsProperty;
	}
	
	public Worker() {
		idProperty().set(UUID.randomUUID().toString());
	}
	
	public Worker(int[] skillSet) {
		idProperty().set(UUID.randomUUID().toString());
		this.skillSet = skillSet;
		setSkillsProperty();
		computeHourlyPay();
	}
	
	public void setNumberProperty(String number) { numberProperty().set(number); }
	
	public void setJobs(ArrayList<Job> jobs) { 
		this.jobs = jobs;
		setJobsProperty();
	}
	
	private void setJobsProperty() {
		StringBuilder sb = new StringBuilder();
		for(Job job: jobs) {
			sb.append(job.getNumber());
			if(jobs.indexOf(job) != jobs.size() - 1)
				sb.append(", ");
		}
		jobsProperty().set(sb.toString());
	}
	
	public void setSkillSet(int[] skillSet) {
		this.skillSet = skillSet;
		setSkillsProperty();
	}
	
	private void setSkillsProperty() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < skillSet.length; i++) {
			sb.append(skillSet[i]);
			if(i != skillSet.length - 1)
				sb.append(", ");
		}
		skillsProperty().set(sb.toString());
	}
	
	public String getId() { return idProperty().get(); }
	public String getNumber() { return numberProperty().get(); }
	public int[] getSkillSet() { return skillSet; }
	public String getSkillsProperty() { return skillsProperty().get(); }
	public double getHourlyPay() { return hourlyPay; }
	public ArrayList<Job> getJobs() { return jobs; }
	public String getJobsProperty() { return jobsProperty().get(); }
	
	public void computeHourlyPay() {
		double hourlyPay = 0;
		double bonus = 0;
		for(int i: skillSet)
			bonus += (i / 10.0);
		hourlyPay = Company.BASE_WORKER_PAY * (1 + bonus);
		this.hourlyPay = hourlyPay;
	}
}
