# Job Assignment Manager
Many companies, especially those in the service sector, have a set of jobs to be completed each day which must be assigned to their employees in some manner. This program automates the process of job assignments to employees. Simulated annealing is used to maximize a profit function **P(x)** where **'x'** is a specific set of job assignments to employees. The profit function is composed of other functions such that

**P(x) = R(x) - D(x) - S(x) - E(x)** where<br>
**R(x)** is the revenue generated from the given set of assignments<br>
**D(x)** is the cost incurred from the workers travelling to job locations<br>
**S(x)** is the amount to be paid to the workers for their work<br>
**E(x)** is the penalty caused by assigning a worker to an inappropriate job type, e.g. a job that is not suitable for their skill set<br><br>

The program will seek to maxmimize the functions **P(x)** and **R(x)** while minimizing the functions **D(x)**, **S(x)**, and **E(x)**. Values produced by these functions can be modified by adjusting values within the program such as employee hourly pay and the cost per kilometer travelled by an employee. Some contstraints that have been taken into account include paying workers overtime if they are assigned to more than 8 hours of work in a single day and capping the number of hours they may work in a day to 12.

<h2>Functionality</h2>

<h3>Creating Jobs</h3>
Jobs can either be created individually or can be created as a set of random jobs. Each job has three properties: a location, duration, and type. The location is an (x,y) coordinate where x can vary between 0 and 1200, and y between 0 and 845. The duration of the job can be one of 30, 60, 90, or 120 minutes, but this can be changed to add different lengths of time. The type of job corresponds to the worker skill set required to complete the job, by default there are three different job types. The revenue generated from the job is determined by its duration and the type.<br>

![Image of Job Creation](https://github.com/paultrudel/Job-Assignment-Manager/blob/master/Job-Assignment-Manager/images/create%20jobs.PNG)

<h3>Creating Workers</h3>
Like jobs workers can either be created individually or a specified number randomly generated at once. Each worker has only one property; their skill set. The skill can contain one or more skills which match the job types in the system.<br>

![Image of Worker Creation](https://github.com/paultrudel/Job-Assignment-Manager/blob/master/Job-Assignment-Manager/images/create%20workers.PNG)

<h3>Iterations</h3>
The user can specify the number of iterations (or epochs) they wish the algorithm to run for, by default this is 100,000 iterations. More iterations increases the probability that the optimal assignment solution will be found. The number of iterations chosen should be based on the size of the problem. Since simulated annealing is a form of stochastistic search it is always possible that one run with 100,000 iterations may produce a better solution than a run with 1,000,000 iterations for example. Therefore, it is important to run the algorithm multiple time on the same set of jobs in order to obtain the best results. The increment size in the iterations window is used for graphing the solution utility values over the epochs; 5000 iterations means that a utility value will plotted every 5000 iterations.<br>

![Image of Iteration Options](https://github.com/paultrudel/Job-Assignment-Manager/blob/master/Job-Assignment-Manager/images/set%20iterations.PNG)

<h3>Results</h3>
After the algorithm has finished running the user can view the final job assignments as well the plot of the job assignment utility values over the iterations.

![Image of Job Assignments](https://github.com/paultrudel/Job-Assignment-Manager/blob/master/Job-Assignment-Manager/images/1200%20jobs%20300%20workers%20assignments.PNG)

![Image of Utilities](https://github.com/paultrudel/Job-Assignment-Manager/blob/master/Job-Assignment-Manager/images/1200Jobs%20300Workers100000Iterations.png)
