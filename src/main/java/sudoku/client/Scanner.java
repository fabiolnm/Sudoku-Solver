package sudoku.client;

public class Scanner {
	public interface SudokuView {
		void showCandidates(int i, int j, String candidates);

		void setValue(int i, int j, String value);

		void cpuGuess(int x, int y, String value, String type);
	}
	
	private final SudokuView view;
	
	private int x, y;
	private State[][] states = new State[9][9]; 

	Scanner(SudokuView view) {
		this.view = view;
		for (int i=0; i<9; i++)
			for (int j=0; j<9; j++)
				states[i][j] = new State(i,j);
		
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				State s = states[i][j];
				s.setNeighborhood(horizontalNeighboors(s), verticalNeighboors(s), sectorNeighboors(s));
			}
		}
	}
	
	public boolean isEmpty(int i, int j) {
		return states[i][j].getValue().isEmpty();
	}

	public void init(String[][] values) {
		for (int i=0; i<9; i++)
			for (int j=0; j<9; j++)
				update(i, j, values[i][j]);
	}
	
	public void update(int i, int j, String value) {
		State state = states[i][j];
		state.setValue(value);
		view.setValue(state.getX(), state.getY(), value);
		
		for (State s : state.getHorizontalNeighboors())
			view.showCandidates(s.getX(), s.getY(), s.candidatesString());

		for (State s : state.getVerticalNeighboors())
			view.showCandidates(s.getX(), s.getY(), s.candidatesString());

		for (State s : state.getSectorNeighboors())
			view.showCandidates(s.getX(), s.getY(), s.candidatesString());
	}
	
	private State[] horizontalNeighboors(State s) {
		State[] res = new State[8];
		int x = s.getX(), y = s.getY();
		for (int n=0, k=0; k<9; k++)
			if (k!=y)
				res[n++] = states[x][k];
		return res;
	}

	private State[] verticalNeighboors(State s) {
		State[] res = new State[8];
		int x = s.getX(), y = s.getY();
		for (int n=0, k=0; k<9; k++)
			if (k!=x)
				res[n++] = states[k][y];
		return res;
	}

	private State[] sectorNeighboors(State s) {
		State[] res = new State[8];
		int x = s.getX(), y = s.getY();
		int h = x/3, v = y/3, c = 0;
		int h1 = h*3, h2 = h1+3, v1 = v*3, v2 = v1+3;
		for (int i=h1; i<h2; i++)
			for (int j=v1; j<v2; j++)
				if (x!=i || y!=j)
					res[c++] = states[i][j];
		return res;
	}
	
	protected void next() {
		if (resolveSingleState())
			next();
		else if (resolveSubtractionState())
			next();
	}

	protected boolean resolveSingleState() {
		int startX = x, startY = y;
		do {
			State s = states[x][y];
			if (s.resolveSingleState()) {
				view.cpuGuess(x, y, s.getValue(), "single");
				return true;
			}
		} while(continueScan(startX, startY));
		return false;
	}

	private boolean resolveSubtractionState() {
		int startX = x, startY = y;
		do {
			State s = states[x][y];
			if (s.resolveSubtractingState()) {
				view.cpuGuess(x, y, s.getValue(), "subtract");
				return true;
			}
		} while(continueScan(startX, startY));
		return false;
	}
	
	private boolean continueScan(int startX, int startY) {
		y = ++y%9;
		if (y==0)
			x = ++x%9;
		return x!=startX || y!=startY;
	}
}