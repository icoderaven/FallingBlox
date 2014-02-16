package tetris.simulator;

public class SimState extends State {

	//Copy constructor
	public SimState(State s)
	{
		super(s);
	}
	
	public void set_next_piece(int piece)
	{
		//Sanity Check
		if (piece > -1 && piece< N_PIECES)
		nextPiece = piece;
	}
	
	//Makes a move and sets the next piece to sent piece 
	public void makeMove(int orient, int slot, int piece)
	{
		makeMove(orient, slot);
		set_next_piece(piece);
	}
	
}
