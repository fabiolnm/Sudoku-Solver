package sudoku.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

public class State {
	private Integer x, y;
	private String value = "";
	private TreeSet<String> candidates;
	
	public State(int x, int y) {
		this.x = x;
		this.y = y;
		candidates = new TreeSet<String>(Arrays.asList("1","2","3","4","5","6","7","8","9"));
	}

	private State(Collection<String> candidates) {
		candidates = new TreeSet<String>(candidates);
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public void addCandidate(String value) {
		if (value.matches("[^1-9]"))
			candidates.add(value);
	}
	
	public String candidatesString() {
		return candidates.toString();
	}
	public String removeCandidate(String value) {
		candidates.remove(value);
		return candidates.toString();
	}

	@Override
	public String toString() {
		return "[" + x + "," + y + "] " + candidates.toString();
	}

	public boolean isValid(String value) {
		return candidates.contains(value);
	}
	public boolean isEmpty() {
		return candidates.isEmpty();
	}

	public boolean canRemoveCandidate(String value) {
		return !(candidates.size()==1 && candidates.iterator().next().equals(value));
	}

	public boolean resolveSingleState() {
		if (value.isEmpty() && candidates.size()==1) {
			value = candidates.iterator().next();
			return true;
		}
		return false;
	}

	public State copy() {
		return new State(candidates);
	}
	
	public State subtractCandidates(State s) {
		State res = new State(candidates);
		res.candidates.removeAll(s.candidates);
		return res;
	}
	
	public Integer getX() {
		return x;
	}
	public Integer getY() {
		return y;
	}
}