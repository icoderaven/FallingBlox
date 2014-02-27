package tetris.agent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Vector;

import org.ejml.simple.SimpleMatrix;

import tetris.simulator.State;
import tetris.trajgen.FixedLengthTrajectoryGenerator;
import tetris.trajgen.FixedStateGenerator;
import tetris.trajgen.PolicyStateGenerator;
import tetris.trajgen.StateGenerator;
import tetris.trajgen.TrajectoryGenerationPool;
import tetris.trajgen.TrajectoryGenerator;

public class Trainer {

	public static void main(String[] args) {

		int trajectoryBatchSize = 64; // 2 x num parameters
		int updateBatchSize = 1; // # steps to run before decreasing step size,
									// temp, gamma, etc.
		int updateIterationCounter = 0;
		int maxTrajectoryLength = 1000;
		int trainerSteps = 1;

		// File to store to
		String logname = "params.txt";

		GradientPolicy pi;
		try {
			SimpleMatrix paramMatrix = SimpleMatrix.loadCSV(logname);
			Feature feat = new BoardFeature();
			pi = new GradientPolicy(feat, paramMatrix);
			System.out.println("Param log loaded.");
		} catch (Exception e) {
			System.out.println("No param log found. Creating new policy.");
			pi = new GradientPolicy();
		}

		SimpleMatrix pars = pi.get_params();
		pars.transpose().print();

		State startState = new State();

		TrajectoryGenerationPool trajMachine = new TrajectoryGenerationPool(8); // #
																				// threads

		// StateGenerator stateGen = new FixedStateGenerator(startState);
		Policy trainerPi = new RandomPolicy();
		StateGenerator stateGen = new PolicyStateGenerator(trainerPi,
				startState, trainerSteps);
		RewardFunction func1 = new LinesClearedReward(10.0);
		RewardFunction func2 = new TurnsAliveReward(1);
		RewardFunction func3 = new DeathReward(-100); // Penalty of -100 for
														// dieing
		RewardFunction comp1 = new CompositeReward(func1, func2);
		RewardFunction rewardFunc = new CompositeReward(comp1, func3);

		double startTemp = 1.0;
		pi.set_temperature(startTemp);

		double startStepSize = 1.0;

		double startGamma = 0.99;
		double gammaConstant = -0.01; // 30 iterations to decay to gamma = 0.95
		pi.set_gamma(startGamma);
		Vector<Double> last_reward = new Vector<Double>();
		Vector<Trajectory> good_trajs = new Vector<Trajectory>();
		double good_reward_avg = 0;
		try {
			while (true) {
				for (int i = 0; i < updateBatchSize; i++) {

					TrajectoryGenerator trajGen = new FixedLengthTrajectoryGenerator(
							stateGen, pi, rewardFunc, maxTrajectoryLength);
					Trajectory[] trajectories = trajMachine
							.generate_trajectories(trajGen, trajectoryBatchSize);

					// pi.fit_policy(trajectories,
					// startStepSize/(updateIterationCounter+1));
					double max = 0.0;
					int index = 0;
					double avg_rewards = 0.0;
					for (int k = 0; k < trajectories.length; k++) {
						double temp = trajectories[k].sum_rewards_tail(0, 1.0);
						avg_rewards += temp;
						if (temp > max) {
							index = k;
							max = temp;
						}
					}
					avg_rewards = avg_rewards / trajectories.length;

					if (max > 2 * avg_rewards) {
						good_trajs.add(trajectories[index]);
						System.out.format("%nAdding traj with reward %f", max);
						good_reward_avg = (good_reward_avg
								* (last_reward.size()) + max)
								/ (last_reward.size() + 1);
						last_reward.add(max);

						// prune
						double min = last_reward.get(0);
						int min_index = 0;
						for (int q = 0; q < last_reward.size(); q++) {
							if (last_reward.get(q) < 2*avg_rewards) {
								good_trajs.remove(q);
								last_reward.remove(q);
								good_reward_avg = 0;
								for (int p = 0; p < last_reward.size(); p++) {
									good_reward_avg += last_reward.get(p);
								}
								good_reward_avg /= last_reward.size();
								System.out.println("Popped!");
							}
						}

					}
					Vector<Trajectory> to_fit_trajs = new Vector<Trajectory>(
							good_trajs.size() + trajectories.length);
					for (int k = 0; k < trajectories.length; k++) {
						to_fit_trajs.addElement(trajectories[k]);
					}
//					to_fit_trajs.addAll(good_trajs);
					System.out.println(to_fit_trajs.size());
					pi.fit_policy((Trajectory[]) to_fit_trajs
							.toArray(new Trajectory[to_fit_trajs.size()]), 1.0);
					stateGen = new PolicyStateGenerator(pi, startState,
							(int) Math.random() * trainerSteps);
				}

				SimpleMatrix parameters = pi.get_params();

				parameters.transpose().print();
				parameters.saveToFileCSV(logname);

				updateIterationCounter++;

				// Reduce temperature at each step
				// pi.set_temperature(startTemp/updateIterationCounter);
				// double nextGamma = (1.0 -
				// startGamma*Math.exp(updateIterationCounter*gammaConstant));
				// pi.set_gamma( nextGamma );

				// System.out.format("Ran %d iterations so far. Gamma: %f%n",
				// updateIterationCounter, nextGamma);
				System.out.format("Ran %d iterations so far.%n",
						updateIterationCounter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		trajMachine.close();

	}

}
