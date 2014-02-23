package tetris.trajgen;

import tetris.agent.Trajectory;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Generates trajectories using a pool of threads.
 * @author Humhu
 *
 */
public class TrajectoryGenerationPool {

	final protected ExecutorService _pool;
	protected TrajectoryGenerator _gen;
	
	/**
	 * Create the pooled generator with a set number of worker threads.
	 * @param numThreads
	 */
	public TrajectoryGenerationPool(int numThreads) {
		_pool = Executors.newFixedThreadPool(numThreads);
	}
	
	/**
	 * Generates a number of trajectories in parallel by using the thread pool.
	 * @param trajGen - A base trajectory generator to copy into tasks.
	 * @param numTrajectories - The number of trajectories to return
	 * @return A Vector of Trajectories generated.
	 */
	public Trajectory[] generate_trajectories(TrajectoryGenerator trajGen,
			int numTrajectories) {
		
		// Prepare for computation
		Trajectory trajectories[] = new Trajectory[numTrajectories];
		
		// This lightweight object allows us to asynchronously collect results
		CompletionService<Trajectory> taskService = new ExecutorCompletionService<Trajectory>(_pool);
		for(int i = 0; i < numTrajectories; i++) {
			TrajectoryGenerator gen = trajGen.copy();
			taskService.submit(gen);
		}
		
		// Record the results as they are done
		double startTime = System.currentTimeMillis();
		double rewardSum = 0;
		int lengthSum = 0;
		for(int i = 0; i < numTrajectories; i++) {
			try {
				Trajectory traj = taskService.take().get();
				rewardSum += traj.sum_rewards();
				lengthSum += traj.tuples.size();
				trajectories[i] = traj;
				//double tick = System.currentTimeMillis();
				//System.out.format("Completed task %d/%d, Average rate %f%n", 
				//		i, numTrajectories, 1000*(i+1)/(tick - startTime));
			} catch (InterruptedException e) {
				// Interrupted before all tasks could finish
				System.out.println("Task interrupted.");
				e.printStackTrace();
			} catch (ExecutionException e) {
				// A task threw an exception
				System.out.println("Execution exception!");
				e.printStackTrace();
			}
		}
		
		double tick = System.currentTimeMillis();
		System.out.format("Completed all task (%d/%d) at rate %f Hz with average reward %f"
				+ " and average length %f.%n", 
				numTrajectories, numTrajectories, 1000*(numTrajectories)/(tick - startTime),
				rewardSum/numTrajectories, 1.0*lengthSum/numTrajectories);
		return trajectories;
		
	}
	
	/**
	 * Terminate this resource.
	 */
	public void close() {
		_pool.shutdown();
	}
	
}
