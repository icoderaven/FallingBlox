package tetris.trajgen;

import tetris.agent.CETrajectory;
import org.ejml.simple.SimpleMatrix;

import java.util.Vector;
import java.util.concurrent.*;

/**
 * Generates trajectories using a pool of threads.  Modified to also save params used for Cross-Entropy.
 * @author Humhu
 *
 */
public class CETrajectoryGenerationPool {

	final protected ExecutorService _pool;
	public CETrajectoryGenerator _gen;
	
	/**
	 * Create the pooled generator with a set number of worker threads.
	 * @param numThreads
	 */
	public CETrajectoryGenerationPool(int numThreads) {
		_pool = Executors.newFixedThreadPool(numThreads);
	}
	
	/**
	 * Generates a number of trajectories in parallel by using the thread pool.
	 * @param trajGen - A base trajectory generator to copy into tasks.
	 * @param numTrajectories - The number of trajectories to return
	 * @return A Vector of Trajectories generated.
	 */
	public CETrajectory[] generate_trajectories(CETrajectoryGenerator trajGen,
			int numTrajectories) {
		
		// Prepare for computation
		CETrajectory trajectories[] = new CETrajectory[numTrajectories];
		
		// This lightweight object allows us to asynchronously collect results
		CompletionService<CETrajectory> taskService = new ExecutorCompletionService<CETrajectory>(_pool);
		for(int i = 0; i < numTrajectories; i++) {
			CETrajectoryGenerator gen = trajGen.copy();
			taskService.submit(gen);
		}
		
		// Record the results as they are done
		double startTime = System.currentTimeMillis();
		double rewardSum = 0;
		int rowsSum = 0;
		int lengthSum = 0;
		for(int i = 0; i < numTrajectories; i++) {
			try {
				CETrajectory traj = taskService.take().get();
				rewardSum += traj.sum_rewards_tail(0, 1.0);
				lengthSum += traj.tuples.size();
				rowsSum += traj.tuples.get(traj.tuples.size() - 1).state.getRowsCleared();
//				System.out.format("Completed task %d with reward %f and length %d%n", i, traj.sum_rewards_tail(0,  1.0), traj.tuples.size());
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
		System.out.format( "Completed all task (%d/%d) at rate %f Hz with average reward %f"
				+ " average length %f and average rows %f.%n", 
				numTrajectories, numTrajectories, 1000*(numTrajectories)/(tick - startTime),
				rewardSum/numTrajectories, 1.0*lengthSum/numTrajectories, rowsSum/(1.0*numTrajectories) );
		return trajectories;
		
	}
	
	/**
	 * Terminate this resource.
	 */
	public void close() {
		_pool.shutdown();
	}
	
}
