package sudoku.client;



public class Scanner {
	public interface SudokuView {
		void clearValue(int i, int j);

		void showCandidates(int i, int j, String candidates);

		void setValue(int i, int j, String value);

		void resetState(int i, int j);

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
	
	public void init(String[][] values) {
		for (int i=0; i<9; i++)
			for (int j=0; j<9; j++)
				update(i, j, values[i][j]);
	}
	
	public boolean isEmpty(int i, int j) {
		return states[i][j].getValue().isEmpty();
	}

	protected void update(int i, int j, String value) {
		update(states[i][j], value);
	}
	
	protected void update(State s, String value) {
		if (value.isEmpty()) 
			restoreCandidates(s);
		else setState(s,value);
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
	
	private void setState(State s, String value) {
		String msg = "Movimento Inválido. Causa: ";
		boolean valid = s.isValid(value);
		if (!valid)
			msg += "Valor inválido para a célula.";
		else {
			State conflict = s.horizontalConflict(value);
			if (conflict!=null)
				msg += "Não pode remover valor " + value + " do vizinho horizontal " + conflict;
			else {
				conflict = s.verticalConflict(value);
				if (conflict!=null)
					msg += "Não pode remover valor " + value + " do vizinho vertical " + conflict;
				else {
					conflict = s.sectorialConflict(value);
					if (conflict!=null)
						msg += "Não pode remover valor " + value + " do vizinho setorial " + conflict;
				}
			}
			valid = conflict==null;
		}
		if (!valid) {
			view.clearValue(s.getX(), s.getY());
			throw new Error(msg);
		}
		
		for (State h : s.getHorizontalNeighboors())
			view.showCandidates(h.getX(), h.getY(), h.removeCandidate(value));

		for (State v : s.getVerticalNeighboors())
			view.showCandidates(v.getX(), v.getY(), v.removeCandidate(value));

		for (State ss : s.getSectorNeighboors())
			view.showCandidates(ss.getX(), ss.getY(), ss.removeCandidate(value));

		view.setValue(s.getX(), s.getY(), value);
		s.setValue(value);
	}

	private void restoreCandidates(State s) {
		view.resetState(s.getX(), s.getY());
		String valueToRestore = s.getValue();
		s.setValue(""); // update itself

		// update neighbors
		for (State h : horizontalNeighboors(s)) {
			if (!inNeighborhood(h, valueToRestore))
				h.addCandidate(valueToRestore);
			view.showCandidates(h.getX(), h.getY(), h.candidatesString());
		}
		for (State v : verticalNeighboors(s)) {
			if (!inNeighborhood(v, valueToRestore))
				v.addCandidate(valueToRestore);
			view.showCandidates(v.getX(), v.getY(), v.candidatesString());
		}
		for (State ss : sectorNeighboors(s)) {
			if (!inNeighborhood(ss, valueToRestore))
				ss.addCandidate(valueToRestore);
			view.showCandidates(ss.getX(), ss.getY(), ss.candidatesString());
		}
	}

	private boolean inNeighborhood(State s, String value) {
		if (!value.isEmpty()) {
			for (State h : horizontalNeighboors(s))
				if (h.getValue().equals(value))
					return true;
			
			for (State v : verticalNeighboors(s))
				if (v.getValue().equals(value))
					return true;
	
			for (State ss : sectorNeighboors(s))
				if (ss.getValue().equals(value))
					return true;
		}
		return false;
	}
	
	protected void next() {
		int startX = x, startY = y;
		while(true) {
			State s = states[x][y];
			if (s.resolveSingleState()) {
				view.cpuGuess(x, y, s.getValue(), "single");
				break;
			}
			
			y = ++y%9;
			if (y==0)
				x = ++x%9;
			
			if (x==startX && y==startY)
				break;
		}
	}
}