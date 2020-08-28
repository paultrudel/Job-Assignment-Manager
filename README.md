# Job Assignment Manager
Many companies, especially those in the service sector, have a set of jobs to be completed each day which must be assigned to their employees in some manner. This program automates the process of job assignments to employees. Simulated annealing is used to maximize a profit function **P(x)** where **'x'** is a specific set of job assignments to employees. The profit function is composed of other functions such that

**P(x) = R(x) - D(x) - S(x) - E(x)** where<br>
**R(x)** is the revenue generated from the given set of assignments<br>
**D(x)** is the cost incurred from the employees travelling to job locations<br>
**S(x)** is the amount to be paid to the employees for their work<br>
**E(x)** is the penalty caused by assigning an employee to an inappropriate job type, e.g. a job that is not suitable for their skill set<br><br>

The program will seek to maxmimize the functions **P(x)** and **R(x)** while minimizing the functions **D(x)**, **S(x)**, and **E(x)**. Values produced by these functions can be modified by adjusting values within the program such as employee hourly pay and the cost per kilometer travelled by an employee. Some contstraints that have been taken into account include paying employees overtime if they are assigned to more than 8 hours of work in a single day and capping the number of hours an employee may work in a day to 12.
