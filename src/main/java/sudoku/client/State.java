package sudoku.client;

import java.util.Arrays;
import java.util.TreeSet;

public class State {
	private String value = "";
	private TreeSet<String> candidates;
	
	public State() {
		candidates = new TreeSet<String>(Arrays.asList("1","2","3","4","5","6","7","8","9"));
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public void addCandidate(String value) {
		candidates.add(value);
	}
	public String removeCandidate(String value) {
		candidates.remove(value);
		return candidates.toString();
	}

	@Override
	public String toString() {
		return candidates.toString();
	}

	public boolean isValid(String value) {
		return candidates.contains(value);
	}

	public boolean canRemoveCandidate(String value) {
		return !(candidates.size()==1 && candidates.iterator().next().equals(value));
	}

	public boolean resolve() {
		if (value.isEmpty() && candidates.size()==1) {
			value = candidates.iterator().next();
			return true;
		}
		return false;
	}
}