package jam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.geometry.Point2D;
import model.Company;
import model.Job;
import model.Worker;

/**
Takes as input a set of jobs and a set of workers and outputs a set of job assignments to the workers.
The program will attempt to output a set of job assignments that maximize the profit function define by
the company through the use of simulated annealing.
**/
public class JobAssignmentManager {
	
	// Number of iterations (or epochs) the algorithm should run for
	private static int MAX_ITERATIONS = 100000;
	
	// After how many epochs a plot point should be created. Used for plotting the progress of the algorithm
	private static  int INCREMENTS = MAX_ITERATIONS / 20;

	// Set of jobs where each job is mapped to a unique id
	private HashMap<String, Job> jobs;
	
	// Set of workers where each worker is mapped to a unique id 
	private HashMap<String, Worker> workers;
	
	// Plot points of the algorithms progress. A utility value is mapped to an epoch
	private HashMap<Integer, Double> utilities;
	
	private Random rand;
	private Company company;
	
	/** Recieves the sets of jobs and workers and returns job assignments **/
	public HashMap<String, List<String>> generateJobAssignments(Company company, HashMap<String, Job> jobs, 
			HashMap<String, Worker> workers) {
		this.jobs = new HashMap<String, Job>(jobs);
		this.workers = new HashMap<String, Worker>(workers);
		rand = new Random();
		this.company = company;
		utilities = new HashMap<Integer, Double>();
		State initialState = generateInitialState();
		return generateOptimalSolution(initialState).getWorkersToJobs();
	}
	
	public HashMap<Integer, Double> getUtilities() { return utilities; }
	public static int getMaxIterations() { return MAX_ITERATIONS; }
	public static void setMaxIterations(int maxIterations) { 
		MAX_ITERATIONS = maxIterations; 
		INCREMENTS = MAX_ITERATIONS / 20;
	}
	public static int getIncrements() { return INCREMENTS; }
	
	/** Create the initial state from which to start the search for an optimal solution **/
	private State generateInitialState() {
		// Maps jobs to workers in a one-to-one manner. A job can only have one worker.
		HashMap<String, String> jobsToWorkers = new HashMap<>();
		
		// Maps workers to jobs in a one-to-many manner. A worker may have many jobs.
		HashMap<String, List<String>> workersToJobs = new HashMap<>();
		
		// Initially assign all workers to an empty set of jobs
		for(Worker worker: workers.values())
			workersToJobs.put(worker.getId(), new ArrayList<>());
		
		/** 
		Loop through the jobs and assign the job to a worker. Jobs are initially assigned to the first
		available worker. Workers are considered to be available if they can do the job, i.e. their
		skill set matches the one required for the job, and they can take the job without exceeding the
		12 hours of work in a day limit.
		**/
		for(Job job: jobs.values()) {
			boolean canAssign = false;
			// Loop through the workers to identify the first available worker
			for(Worker worker: workers.values()) {
				// Check if the worker can take the job, i.e. is available
				canAssign = canTakeJob(workersToJobs.get(worker.getId()), job, worker);
				// Assign the worker to the job if they are available
				if(canAssign) {
					jobsToWorkers.put(job.getId(), worker.getId());
					List<String> jobs = workersToJobs.get(worker.getId());
					jobs.add(job.getId());
					workersToJobs.put(worker.getId(), jobs);
					break;
				}
			}
		}
		
		// Create the initial state using the job assignments
		State initialState = new State(jobsToWorkers, workersToJobs);
		
		// Enter the initial state utility, the utility value at epoch 0, into the map
		utilities.put(0, computeUtility(initialState.getWorkersToJobs()));
		return initialState;
	}
	
	/** The simulated annealing algorithm used to identify the job assignment which maximize the companies profit function **/
	private State generateOptimalSolution(State initialState) {
		State nextState = cloneState(initialState);
		State solution = cloneState(initialState);
		double utilSolution = computeUtility(solution.getWorkersToJobs());
		for(int n = 1; n <= MAX_ITERATIONS; n++) {
			nextState = generateNextState(solution);
			double utilNext = computeUtility(nextState.getWorkersToJobs());
			double delta = utilNext - utilSolution;
			if(delta > 0) {
				solution = cloneState(nextState);
				utilSolution = utilNext;
			}
			else {
				double lambda = Math.log(1 + n);
				double p = Math.exp(delta / lambda);
				double r = rand.nextDouble();
				if(r < p) {
					solution = cloneState(nextState);
					utilSolution = utilNext;
				}
			}
			if(n % INCREMENTS == 0)
				utilities.put((n / INCREMENTS), computeUtility(solution.getWorkersToJobs()));
		}
		return solution;
	}
	
	private State generateNextState(State currentState) {
		HashMap<String, String> jobsToWorkers = new HashMap<>(currentState.getJobsToWorkers());
		HashMap<String, List<String>> workersToJobs = new HashMap<>(currentState.getWorkersToJobs());
		List<String> jobIds = new ArrayList<>(jobsToWorkers.keySet());
		String jobId = jobIds.get(rand.nextInt(jobIds.size()));
		String prevWorkerId = jobsToWorkers.get(jobId);
		List<String> workerIds = company.getWorkerIds();
		String nextWorkerId = workerIds.get(rand.nextInt(workerIds.size()));
		while(prevWorkerId.equals(nextWorkerId))
			nextWorkerId = workerIds.get(rand.nextInt(workerIds.size()));
		List<String> prevWorkerJobs = new ArrayList<>(workersToJobs.get(prevWorkerId));
		List<String> nextWorkerJobs = new ArrayList<>(workersToJobs.get(nextWorkerId));
		prevWorkerJobs.remove(jobId);
		nextWorkerJobs.add(jobId);
		jobsToWorkers.put(jobId, nextWorkerId);
		workersToJobs.put(prevWorkerId, prevWorkerJobs);
		workersToJobs.put(nextWorkerId, nextWorkerJobs);
		State nextState = new State(jobsToWorkers, workersToJobs);
		return nextState;
	}
	
	private State cloneState(State stateToClone) {
		HashMap<String, String> jobsToWorkers = new HashMap<>(stateToClone.getJobsToWorkers());
		HashMap<String, List<String>> workersToJobs = new HashMap<>(stateToClone.getWorkersToJobs());
		State clonedState = new State(jobsToWorkers, workersToJobs);
		return clonedState;
	}
	
	public double computeUtility(HashMap<String, List<String>> workersToJobs) {
		double utility = 0;
		double revenue = 0;
		double distanceTravelled = 0;
		double employeePay = 0;
		int numMismatched = 0;
		
		for(Map.Entry<String, List<String>> entry: workersToJobs.entrySet()) {
			revenue += computeRevenue(entry.getValue());
			distanceTravelled += computeDistanceTravelled(entry.getValue());
			employeePay += computeEmployeePay(workers.get(entry.getKey()), entry.getValue());
			numMismatched += computeNumMismatched(workers.get(entry.getKey()), entry.getValue());
		}
		
		utility = revenue - (Company.DISTANCE_COST * distanceTravelled) - employeePay - 
				(Company.MISMATCH_PENALTY * numMismatched);
		return utility;
	}
	
	private boolean canTakeJob(List<String> jobAssignments, Job job, Worker worker) {
		int[] skillSet = worker.getSkillSet();
		for(int skill : skillSet) {
			if(skill == job.getType() && (getHoursToWork(jobAssignments) + job.getDuration()) < 
					Company.MAX_TIME)
				return true;
		}
		return false;
	}
	
	private int getHoursToWork(List<String> jobAssignments) {
		int hoursToWork = 0;
		for(String jobId: jobAssignments)
			hoursToWork += jobs.get(jobId).getDuration();
		return hoursToWork;
	}
	
	private double computeRevenue(List<String> jobAssignments) {
		double revenue = 0;
		for(String jobId: jobAssignments)
			revenue += jobs.get(jobId).getPayment();
		return revenue;
	}
	
	private double computeDistanceTravelled(List<String> jobAssignments) {
		double distanceTravelled = 0;
		Point2D currLocation = Company.LOCATION;
		for(String jobId: jobAssignments) {
			distanceTravelled += currLocation.distance(jobs.get(jobId).getLocation());
			currLocation = jobs.get(jobId).getLocation();
		}
		return (2 * distanceTravelled);
	}
	
	private double computeEmployeePay(Worker worker, List<String> jobAssignments) {
		int timeWorked = 0;
		double payment = 0;
		double hourlyPay = worker.getHourlyPay();
		for(String jobId: jobAssignments)
			timeWorked += jobs.get(jobId).getDuration();
		if(timeWorked > Company.MAX_TIME)
			return Company.OVERWORK_PENALTY;
		int overtime = timeWorked - Company.OVERTIME_THRESHOLD;
		if(overtime < 0)
			overtime = 0;
		payment = (hourlyPay * (timeWorked / 60.0)) + ((hourlyPay * Company.OVERTIME_BONUS) * (overtime / 60.0));
		return payment;
	}
	
	private int computeNumMismatched(Worker worker, List<String> jobAssignments) {
		int numMismatched = 0;
		for(String jobId: jobAssignments) {
			if(!isMatch(jobs.get(jobId), worker))
				numMismatched++;
		}
		return numMismatched;
	}
	
	private boolean isMatch(Job job, Worker worker) {
		for(int skill: worker.getSkillSet()) {
			if(skill == job.getType())
				return true;
		}
		return false;
	}
	
	public void printJobAssignments(HashMap<String, List<String>> workersToJobs) {
		for(Map.Entry<String, List<String>> entry: workersToJobs.entrySet()) {
			System.out.println("Worker " + workers.get(entry.getKey()).getNumber() + ": ");
			for(String jobId: entry.getValue()) {
				System.out.print(jobs.get(jobId).getNumber() + " ");
			}
			System.out.println("");
		}
	}
	
	public class State {
		
		private HashMap<String, String> jobsToWorkers;
		private HashMap<String, List<String>> workersToJobs;
		
		public State(HashMap<String, String> jobsToWorkers, HashMap<String, List<String>> workersToJobs) {
			this.jobsToWorkers = jobsToWorkers;
			this.workersToJobs = workersToJobs;
		}
		
		public HashMap<String, String> getJobsToWorkers() { return jobsToWorkers; }
		public HashMap<String, List<String>> getWorkersToJobs() { return workersToJobs; }
	}
}
