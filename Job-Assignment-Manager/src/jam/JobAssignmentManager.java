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

public class JobAssignmentManager {
	
	private static int MAX_ITERATIONS = 100000;
	private static  int INCREMENTS = MAX_ITERATIONS / 20;

	private HashMap<String, Job> jobs;
	private HashMap<String, Worker> workers;
	private HashMap<Integer, Double> utilities;
	private Random rand;
	private Company company;
	
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
	
	private State generateInitialState() {
		HashMap<String, String> jobsToWorkers = new HashMap<>();
		HashMap<String, List<String>> workersToJobs = new HashMap<>();
		
		for(Worker worker: workers.values())
			workersToJobs.put(worker.getId(), new ArrayList<>());
		
		for(Job job: jobs.values()) {
			boolean canAssign = false;
			for(Worker worker: workers.values()) {
				canAssign = canTakeJob(workersToJobs.get(worker.getId()), job, worker);
				if(canAssign) {
					jobsToWorkers.put(job.getId(), worker.getId());
					List<String> jobs = workersToJobs.get(worker.getId());
					jobs.add(job.getId());
					workersToJobs.put(worker.getId(), jobs);
					break;
				}
			}
		}
		
		State initialState = new State(jobsToWorkers, workersToJobs);
		utilities.put(0, computeUtility(initialState.getWorkersToJobs()));
		return initialState;
	}
	
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
