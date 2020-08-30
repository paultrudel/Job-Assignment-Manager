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
		double utilSolution = computeUtility(solution.getWorkersToJobs()); // Compute the utility value of the initial solution
		// Run the simulated annealing algorithm for the specified number of iterations
		for(int n = 1; n <= MAX_ITERATIONS; n++) {
			nextState = generateNextState(solution); // Move to the next state in the search space
			double utilNext = computeUtility(nextState.getWorkersToJobs()); // Compute the utility of this new state
			double delta = utilNext - utilSolution; // Difference between the new and old utilities
			// If the new state has a better utility make it the new solution
			if(delta > 0) {
				solution = cloneState(nextState);
				utilSolution = utilNext;
			}
			/** 
			If the new state has the same or worse utility move to the new state with some probability.
			This is done to avoid getting stuck in local maxima in the hope of finding the gloabl maximum.
			The probability of moving to the new state is based on difference in the utility values. The bigger
			the difference the lower the probability of moving.
			**/
			else {
				double lambda = Math.log(1 + n);
				double p = Math.exp(delta / lambda); // Probability of moving to the new state
				double r = rand.nextDouble(); // Random uniform value
				// Move to the new state by making it the new solution if the random value is less than the probability
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
	
	/** 
	Uses the current state to identify a neighbouring state in the search space. This is done by taking a job assignment
	from one worker at random and giving it to another random worker.
	**/
	private State generateNextState(State currentState) {
		// Copy job assignments from the current state
		HashMap<String, String> jobsToWorkers = new HashMap<>(currentState.getJobsToWorkers());
		HashMap<String, List<String>> workersToJobs = new HashMap<>(currentState.getWorkersToJobs());
		
		List<String> jobIds = new ArrayList<>(jobsToWorkers.keySet()); // List of jobs IDs that have been assigned
		String jobId = jobIds.get(rand.nextInt(jobIds.size())); // Pick a job at random
		String prevWorkerId = jobsToWorkers.get(jobId); // Identify the worker that the job is being taken from
		List<String> workerIds = company.getWorkerIds(); // List of worker IDs
		String nextWorkerId = workerIds.get(rand.nextInt(workerIds.size())); // Pick a worker at random
		
		// Ensure that the new worker is not the same as the previous one
		while(prevWorkerId.equals(nextWorkerId))
			nextWorkerId = workerIds.get(rand.nextInt(workerIds.size()));
		
		List<String> prevWorkerJobs = new ArrayList<>(workersToJobs.get(prevWorkerId)); // Get the list of assigned jobs for the previous worker
		List<String> nextWorkerJobs = new ArrayList<>(workersToJobs.get(nextWorkerId)); // Get the list of assigned jobs for the next worker
		
		prevWorkerJobs.remove(jobId); // Take the job from the previous worker
		nextWorkerJobs.add(jobId); // Give the job to the new worker
		
		// Create the next state using the new job assignements
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
	
	/** Computes the utility value of the given job assignments **/
	public double computeUtility(HashMap<String, List<String>> workersToJobs) {
		double utility = 0; // Overall utility value of the assignments
		double revenue = 0; // Revenue generated from the assignments
		double distanceTravelled = 0; // Distance employees must travel to complete the jobs
		double employeePay = 0; // Amount the employees must be paid for their work
		int numMismatched = 0; // The number of jobs that have been inappropriately assigned
		
		// Loop through all of the job assignments to compute their contribution to the overall utility
		for(Map.Entry<String, List<String>> entry: workersToJobs.entrySet()) {
			revenue += computeRevenue(entry.getValue()); // Compute revenue from the job
			distanceTravelled += computeDistanceTravelled(entry.getValue()); // Compute distance to be travelled for the job
			employeePay += computeEmployeePay(workers.get(entry.getKey()), entry.getValue()); // Compute the amount to pay worker for job
			numMismatched += computeNumMismatched(workers.get(entry.getKey()), entry.getValue()); // Check if the job is properly assigned
		}
		
		// Compute the utility using company specific values for employee pay and travel cost
		utility = revenue - (Company.DISTANCE_COST * distanceTravelled) - employeePay - 
				(Company.MISMATCH_PENALTY * numMismatched);
		return utility;
	}
	
	/** Checks if it is possible for the desired worker to take on the given job **/
	private boolean canTakeJob(List<String> jobAssignments, Job job, Worker worker) {
		int[] skillSet = worker.getSkillSet(); // Get the workers skill set
		// Check if the worker has the skill to complete the job and if assignment of the job will not put them over the work limit
		for(int skill : skillSet) {
			if(skill == job.getType() && (getHoursToWork(jobAssignments) + job.getDuration()) < 
					Company.MAX_TIME)
				return true;
		}
		return false;
	}
	
	/** Compute the number of hours an employee must work from their job assignments **/
	private int getHoursToWork(List<String> jobAssignments) {
		int hoursToWork = 0;
		for(String jobId: jobAssignments)
			hoursToWork += jobs.get(jobId).getDuration();
		return hoursToWork;
	}
	
	/** Compute the revenue generated from completing the assigned jobs **/
	private double computeRevenue(List<String> jobAssignments) {
		double revenue = 0;
		for(String jobId: jobAssignments)
			revenue += jobs.get(jobId).getPayment();
		return revenue;
	}
	
	/** Compute the distance the worker must travel to complete the jobs **/
	private double computeDistanceTravelled(List<String> jobAssignments) {
		double distanceTravelled = 0;
		Point2D currLocation = Company.LOCATION;
		for(String jobId: jobAssignments) {
			distanceTravelled += currLocation.distance(jobs.get(jobId).getLocation());
			currLocation = jobs.get(jobId).getLocation();
		}
		return (2 * distanceTravelled);
	}
	
	/** Compute the amount the employee must be paid for their work **/
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
	
	/** Identify the number of jobs improperly assigned to the worker **/
	private int computeNumMismatched(Worker worker, List<String> jobAssignments) {
		int numMismatched = 0;
		for(String jobId: jobAssignments) {
			if(!isMatch(jobs.get(jobId), worker))
				numMismatched++;
		}
		return numMismatched;
	}
	
	/** Given a job and worker see if the worker can do the job **/
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
	
	/** State representation of a set of job assignments **/
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
