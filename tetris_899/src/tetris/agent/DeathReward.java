package tetris.agent;

import tetris.simulator.State;

public class DeathReward extends RewardFunction {
	
	public boolean isDead;
//	private State preDeadState;
//	private State deadState;
//	private int fatalAction;
//	private int numCalls;
//	
	public DeathReward(DeathReward other) {
		super(other);
		isDead = other.isDead;
//		numCalls = 0;
	}
	
	public DeathReward(double d) {
		super(d);
		isDead = false;
//		numCalls = 0;
	}

	@Override
	public RewardFunction copy() {
		// TODO Auto-generated method stub
		return new DeathReward(this);
	}

	@Override
	protected double calculate_reward(State state, int action) {
		State copyState = new State(state);
		copyState.makeMove(action);
//		numCalls++;
//		long id = Thread.currentThread().getId();
//		
//		if (isDead) {
//			System.out.format("Dead men play no tetris in thread %d! fatal action: %d piece: %d%n", id, fatalAction, preDeadState.getNextPiece());
//			
//			System.out.println("Before death:");
//			System.out.println( preDeadState.toString() );
//			System.out.println("After death:");
//			System.out.println( deadState.toString() );
//			System.out.format("Applying fatal move %d:%n", fatalAction);
//			preDeadState.makeMove(action);
//			System.out.println( preDeadState.toString() );
//		}
//		
//		if (copyState.hasLost()) {
//			System.out.format("State died in thread %d with action %d and piece%d%n", id, action, state.getNextPiece());
//			System.out.format("To die with action %d:%n", action);
//			System.out.println(state.toString());
//			System.out.println("Dead copy state:");
//			System.out.println(copyState.toString());
//			
//			preDeadState = new State(state);
//			isDead = true;
//			deadState = new State(copyState);
//			fatalAction = action;
//		}
		return (copyState.hasLost()) ? 1.0 : 0.0;
	}

}
