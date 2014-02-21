package tetris.agent;

public class Action {

		private int _orientation, _position;
		
		public Action(int[] move)
		{
			this(move[0], move[1]);
		}
		public Action(int orientation, int position)
		{
			_orientation = orientation;
			_position = position;
		}
		public int getOrientation()
		{
			return _orientation;
		}
		
		public int getPosition()
		{
			return _position;
		}
}
