package sudoku.client;

import com.google.gwt.core.client.GWT;

public class Scanner {
	public interface SudokuView {
		void clearValue(int i, int j);

		void showCandidates(int i, int j, String candidates);

		void setValue(int i, int j, String value);

		void clearState(int i, int j);

		void cpuGuess(int x, int y, String value);
	}
	
	private final SudokuView view;
	
	private int x, y;
	private State[][] states = new State[9][9]; 

	Scanner(SudokuView view) {
		this.view = view;
		for (int i=0; i<9; i++)
			for (int j=0; j<9; j++)
				states[i][j] = new State();
	}
	
	public void init(String[][] values) {
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				String v = values[i][j];
				if (!v.isEmpty())
					update(i, j, v);
			}
		}
	}
	
	public String valueOf(int i, int j) {
		return states[i][j].getValue();
	}

	public boolean isEmpty(int i, int j) {
		return states[i][j].getValue().isEmpty();
	}

	protected void update(int i, int j, String value) {
		if (value.isEmpty()) 
			restoreCandidates(i,j);
		else setState(i,j,value);
	}

	private void setState(int i, int j, String value) {
		int h = i/3, v = j/3;
		boolean valid = states[i][j].isValid(value);
		if (valid) {
			for (int k=0; valid && k<9; k++) {
				if (j!=k) {
					State s = states[i][k];
					GWT.log(i + "," + k + ": " + s.getValue() + ", " + s);
					valid = states[i][k].canRemoveCandidate(value);
				}
				if (valid && i!=k) {
					State s = states[k][j];
					GWT.log(k + "," + j + ": " + s.getValue() + ", " + s);
					valid = states[k][j].canRemoveCandidate(value);
				}
			}
			if (valid) {
				for (int m=h*3; valid && m<3*(h+1); m++)
					for (int n=v*3; valid && n<3*(v+1); n++)
						if (i!=m && j!=n)
							valid = states[m][n].canRemoveCandidate(value);
			}
		}
		if (!valid) {
			view.clearValue(i,j);
			throw new Error("Movimento Inválido");
		}
		
		states[i][j].setValue(value);
		
		for (int k=0; k<9; k++) {
			if (j!=k)
				view.showCandidates(i, k, states[i][k].removeCandidate(value));
			if (i!=k)
				view.showCandidates(k, j, states[k][j].removeCandidate(value));
		}
		for (int m=h*3; m<3*(h+1); m++)
			for (int n=v*3; n<3*(v+1); n++)
				if (i!=m && j!=n)
					view.showCandidates(m, n, states[m][n].removeCandidate(value));

		view.setValue(i,j,value);
	}

	private void restoreCandidates(int i, int j) {
		String value = valueOf(i,j);
		
		// update itself
		states[i][j].setValue("");
		view.clearState(i, j);

		// update neighbors
		for (int k=0; k<9; k++) {
			if (!inNeighborhood(i, k, value))
				states[i][k].addCandidate(value);
			
			view.showCandidates(i, k, states[i][k].toString());
			
			if (!inNeighborhood(k, j, value))
				states[k][j].addCandidate(value);
			
			view.showCandidates(k, j, states[k][j].toString());
		}
		int h = i/3, v = j/3;
		for (int m=h*3; m<3*(h+1); m++) {
			for (int n=v*3; n<3*(v+1); n++) {
				if (!inNeighborhood(m, n, value))
					states[m][n].addCandidate(value);
				
				view.showCandidates(m, n, states[m][n].toString());
			}
		}
	}

	private boolean inNeighborhood(int i, int j, String value) {
		for (int k=0; k<9; k++) {
			if (k!=j)
				if (states[i][k].getValue().equals(value))
					return true;
			if (k!=i)
				if (states[k][j].getValue().equals(value))
					return true;
		}
		int h = i/3, v = j/3;
		for (int m=h*3; m<3*(h+1); m++)
			for (int n=v*3; n<3*(v+1); n++)
				if (m!=i && n!=j)
					if (states[m][n].getValue().equals(value))
						return true;
		return false;
	}
	
	protected void next() {
		int startX = x, startY = y;
		while(true) {
			if (states[x][y].resolve()) {
				view.cpuGuess(x, y, states[x][y].getValue());
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