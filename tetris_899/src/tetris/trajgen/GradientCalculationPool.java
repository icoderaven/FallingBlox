package tetris.trajgen;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tetris.agent.GradientPolicy;
import tetris.agent.Trajectory;

public class GradientCalculationPool {

	final protected ExecutorService _pool;
	
	/**
	 * Create the pooled generator with a set number of worker threads.
	 * @param numThreads
	 */
	public GradientCalculationPool(int numThreads) {
		_pool = Executors.newFixedThreadPool(numThreads);
	}
	
	/**
	 * Generates a number of trajectories in parallel by using the thread pool.
	 * @param trajGen - A base trajectory generator to copy into tasks.
	 * @param numTrajectories - The number of trajectories to return
	 * @return A Vector of Trajectories generated.
	 */
	public GradientResult[] calculate_gradients (GradientPolicy policy, Trajectory[] trajectories) {
		
		int numTrajectories = trajectories.length;
		GradientResult[] results = new GradientResult[numTrajectories];
		
		// This lightweight object allows us to asynchronously collect results
		CompletionService<GradientResult> taskService = new ExecutorCompletionService<GradientResult>(_pool);
		for(int i = 0; i < numTrajectories; i++) {
			GradientGenerator gen = new GradientGenerator(policy, trajectories[i]);
			taskService.submit(gen);
		}
		
		// Record the results as they are done
//		double startTime = System.currentTimeMillis();
//		double rewardSum = 0;
//		int rowsSum = 0;
//		int lengthSum = 0;
		for(int i = 0; i < numTrajectories; i++) {
			try {
				GradientResult result = taskService.take().get();
//				rewardSum += result.reward;
//				lengthSum += result.numSteps;
//				rowsSum += result.numRows;
//				System.out.format("Completed task %d with reward %f and length %d%n", i, traj.sum_rewards_tail(0,  1.0), traj.tuples.size());
				results[i] = result;

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
		
//		double tick = System.currentTimeMillis();
//		System.out.format( "Completed all calculations (%d/%d) at rate %f Hz.%n", 
//				numTrajectories, numTrajectories, 1000*(numTrajectories)/(tick - startTime) );
		return results;
		
	}
	
	/**
	 * Terminate this resource.
	 */
	public void close() {
		_pool.shutdown();
	}
	
	
}
