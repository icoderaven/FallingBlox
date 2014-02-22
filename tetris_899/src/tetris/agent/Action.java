package tetris.agent;

import tetris.simulator.State;

/** A simple immutable class representing an action comprised of a move index. 
 * The fields are public gettable for convenience but not settable.
 * @author Humhu
 */
public class Action {

		final public int index;
		
		public Action(int ind)
		{
			index = ind;
		}
		
		/** 
		 * Apply this action to a state. This changes the state!
		 * @param state - The state to apply the action to.
		 */
		public void apply(State state) {
			state.makeMove(index);
		}

}
