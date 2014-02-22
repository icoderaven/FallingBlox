package tetris.agent;
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
	protected Vector<Trajectory> _trajectories;
	
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
	public Vector<Trajectory> generate_trajectories(TrajectoryGenerator trajGen,
			int numTrajectories) {
		
		// Prepare for computation
		_trajectories.clear();
		
		// This lightweight object allows us to asynchronously collect results
		CompletionService<Trajectory> taskService = new ExecutorCompletionService<Trajectory>(_pool);
		for(int i = 0; i < numTrajectories; i++) {
			TrajectoryGenerator gen = trajGen.copy();
			taskService.submit(gen);
		}
		
		// Record the results as they are done
		double startTime = System.currentTimeMillis();
		for(int i = 0; i < numTrajectories; i++) {
			try {
				_trajectories.add(taskService.take().get());
				double tick = System.currentTimeMillis();
				System.out.format("Completed task %d/%d, Average rate %f", 
						i, numTrajectories, 1000*(i+1)/(tick - startTime));
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
		
		return new Vector<Trajectory>(_trajectories);
		
	}
	
}
