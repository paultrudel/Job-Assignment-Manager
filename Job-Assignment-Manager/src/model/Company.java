package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jam.JobAssignmentManager;
import javafx.geometry.Point2D;
import view.Interface;

public class Company {

	public static final int[] JOB_TYPES = {1, 2, 3}; // Types of jobs the company can do
	public static final int[] JOB_DURATIONS = {30, 60, 90, 120}; // Possible job lengths 
	public static final Point2D LOCATION = new Point2D((Interface.MAP_WIDTH / 2), 
			(Interface.MAP_HEIGHT / 2)); // Location of the company on the map
	public static final double BASE_JOB_PAY = 150.00; // Base amount the company is paid for a job
	public static final double BASE_WORKER_PAY = 20.00; // Base hourly worker pay
	public static final int OVERTIME_THRESHOLD = 480; // Threshold, in minutes, before overtime starts
	public static final double OVERTIME_BONUS = 0.5; // Worker overtime bonus
	public static final int MAX_TIME = 720; // Maximum amount of time an employee may work in a day
	public static final double OVERWORK_PENALTY = 10000.0; // Penalty incurred for assigning more than maximum amount of work to an employee
	public static final double DISTANCE_COST = 0.5; // Cost for travelling one unit of distance
	public static final double MISMATCH_PENALTY = 1000.00; // Penalty incurred for assigning a worker to a job they cannot complete
	
	private JobAssignmentManager JAM;
	private HashMap<String, List<String>> jobAssignments; // Job assignments for workers
	private HashMap<String, Job> jobs; // Jobs to be completed
	private ArrayList<String> jobIds; // IDs of the jobs
	private HashMap<String, Worker> workers; // Workers who can complete jobs
	private ArrayList<String> workerIds; // IDs of the workers
	private Interface view;
	
	public Company(Interface view) {
		JAM = new JobAssignmentManager();
		jobs = new HashMap<>();
		jobIds = new ArrayList<>();
		workers = new HashMap<>();
		workerIds = new ArrayList<>();
		this.view = view;
	}
	
	/** Adds a job in the system to be compelted **/
	public void addJob(Job job) {
		jobs.put(job.getId(), job);
		jobIds.add(job.getId());
		job.setNumberProperty(String.valueOf(jobIds.indexOf(job.getId())));
	}
	
	/** Adds a worker to the company **/
	public void addWorker(Worker worker) { 
		workers.put(worker.getId(), worker);
		workerIds.add(worker.getId());
		worker.setNumberProperty(String.valueOf(workerIds.indexOf(worker.getId())));
	}
	
	public int getNumJobs() { return jobs.size(); }
	public int getNumWorkers() { return workers.size(); }
	public HashMap<String, Job> getJobs() { return jobs; }
	public List<String> getJobIds() { return jobIds; }
	public int getJobNumber(String jobId) { return jobIds.indexOf(jobId); }
	public HashMap<String, Worker> getWorkers() { return workers; }
	public List<String> getWorkerIds() { return workerIds; }
	public int getWorkerNumber(String workerId) { return workerIds.indexOf(workerId); }
	public HashMap<String, List<String>> getJobAssignments() { return jobAssignments; }
	
	/** Resets all jobs and workers **/
	public void reset() {
		resetJobs();
		resetWorkers();
	}
	
	/** Resets all jobs **/
	public void resetJobs() { 
		jobs = new HashMap<>();
		jobIds = new ArrayList<>();
	}
	
	/** Restes all workers **/
	public void resetWorkers() { 
		workers = new HashMap<>(); 
		workerIds = new ArrayList<>();
	}
	
	/** Create a specified number of jobs **/
	public void createJobs(int numJobs) {
		resetJobs();
		Random r = new Random();
		for(int i = 0; i < numJobs; i++) {
			int type = JOB_TYPES[r.nextInt(JOB_TYPES.length)];
			int duration = JOB_DURATIONS[r.nextInt(JOB_DURATIONS.length)];
			Point2D location = new Point2D((double) r.nextInt(Interface.MAP_WIDTH), 
					(double) r.nextInt(Interface.MAP_HEIGHT));
			addJob(new Job(type, duration, location));
		}
		view.showAlert("Jobs Created", "Finished creating " + numJobs + " jobs");
	}
	
	/** Create a specified number of workers **/
	public void createWorkers(int numWorkers) {
		resetWorkers();
		Random r = new Random();
		for(int i = 0; i < numWorkers; i++) {
			int numSkills = r.nextInt(JOB_TYPES.length) + 1;
			int[] skillSet = sample(JOB_TYPES, numSkills);
			addWorker(new Worker(skillSet));
		}
		view.showAlert("Workers Created", "Finished creating " + numWorkers + " workers");
	}
	
	/** Use the Job Assignment Manager to generate job assignments **/
	public void generateJobAssignments() {
		jobAssignments = JAM.generateJobAssignments(this, jobs, workers);
		assignJobs();
		printUtilities();
		view.showAlert("Job Allocation Complete", "Program is finished allocating jobs to employees");
	}
	
	/** Assign the finalized job assignments to the workers **/
	private void assignJobs() {
		for(Map.Entry<String, List<String>> entry: jobAssignments.entrySet()) {
			Worker worker = workers.get(entry.getKey());
			ArrayList<Job> j = new ArrayList<Job>();
			for(String jobId: entry.getValue())
				j.add(jobs.get(jobId));
			worker.setJobs(j);
		}
	}
	
	private void printUtilities() {
		HashMap<Integer, Double> utilities = getUtilities();
		for(Map.Entry<Integer, Double> entry: utilities.entrySet()) {
			System.out.println(entry.getKey() + ", " + entry.getValue());
		}
	}
	
	public HashMap<Integer, Double> getUtilities() {
		return JAM.getUtilities();
	}
	
	public double getProfit() {
		return JAM.computeUtility(jobAssignments);
	}
	
	/** Randomly samples a specified number of items from a list **/
	private int[] sample(int[] samples, int numToSample) {
		int[] sample = new int[numToSample];
		List<Integer> indices = new ArrayList<>();
		Random r = new Random();
		int index;
		for(int i = 0; i < numToSample; i++) {
			do {
				index = r.nextInt(samples.length);
			}while(indices.contains(index));
			sample[i] = samples[index];
			indices.add(index);
		}
		return sample;
	}
}
