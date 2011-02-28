package sudoku.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

public class State {
	private Integer x, y;
	private String value = "";
	private TreeSet<String> candidates;
	private State[] horizontalNeighboors, verticalNeighboors, sectorNeighboors;

	public State(int x, int y) {
		this.x = x;
		this.y = y;
		candidates = new TreeSet<String>(Arrays.asList("1","2","3","4","5","6","7","8","9"));
	}

	public String getValue() {
		return value;
	}
	public void setValue(String newValue) {
		if (newValue.isEmpty()) {
			// must update state before addCandidate calls
			String oldValue = value;
			this.value = newValue;
			for (State s : horizontalNeighboors)
				if (!s.inNeighborhood(oldValue))
					s.addCandidate(oldValue);
			for (State s : verticalNeighboors)
				if (!s.inNeighborhood(oldValue))
					s.addCandidate(oldValue);
			for (State s : sectorNeighboors)
				if (!s.inNeighborhood(oldValue))
					s.addCandidate(oldValue);
		}
		else {
			validate(newValue);
			// only update after validation
			this.value = newValue;
			for (State s : horizontalNeighboors)
				s.removeCandidate(newValue);
			for (State s : verticalNeighboors)
				s.removeCandidate(newValue);
			for (State s : sectorNeighboors)
				s.removeCandidate(newValue);
		}
	}
	
	private void validate(String value) {
		String msg = "Movimento Inválido. Causa: ";
		boolean valid = isValid(value);
		if (!valid)
			msg += "Valor inválido para a célula " + this;
		else {
			State conflict = hasHorizontalConflict(value);
			if (conflict!=null)
				msg += "Não pode remover valor " + value + " do vizinho horizontal " + conflict;
			else {
				conflict = hasVerticalConflict(value);
				if (conflict!=null)
					msg += "Não pode remover valor " + value + " do vizinho vertical " + conflict;
				else {
					conflict = hasSectorialConflict(value);
					if (conflict!=null)
						msg += "Não pode remover valor " + value + " do vizinho setorial " + conflict;
				}
			}
			valid = conflict==null;
		}
		if (!valid)
			throw new Error(msg);
	}

	public void addCandidate(String value) {
		if (value.matches("[1-9]"))
			candidates.add(value);
	}
	
	public String candidatesString() {
		return candidates.toString();
	}
	private String removeCandidate(String value) {
		candidates.remove(value);
		return candidates.toString();
	}

	@Override
	public String toString() {
		return "[" + x + "," + y + "] " + candidates.toString();
	}

	private boolean isValid(String value) {
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

	public Integer getX() {
		return x;
	}
	public Integer getY() {
		return y;
	}

	public void setNeighborhood(
			State[] horizontalNeighboors,
			State[] verticalNeighboors, 
			State[] sectorNeighboors) {
		this.horizontalNeighboors = horizontalNeighboors;
		this.verticalNeighboors = verticalNeighboors;
		this.sectorNeighboors = sectorNeighboors;
	}

	public State hasHorizontalConflict(String value) {
		return conflictingState(value, horizontalNeighboors);
	}
	public State hasVerticalConflict(String value) {
		return conflictingState(value, verticalNeighboors);
	}
	public State hasSectorialConflict(String value) {
		return conflictingState(value, sectorNeighboors);
	}
	
	private State conflictingState(String value, State[] neighboors) {
		for (State s : neighboors)
			if (!s.canRemoveCandidate(value))
				return s;
		return null;
	}
	
	public State[] getHorizontalNeighboors() {
		return horizontalNeighboors;
	}
	public State[] getVerticalNeighboors() {
		return verticalNeighboors;
	}
	public State[] getSectorNeighboors() {
		return sectorNeighboors;
	}
	
	private boolean inNeighborhood(String value) {
		if (!value.isEmpty()) {
			for (State s : horizontalNeighboors)
				if (s.getValue().equals(value))
					return true;
			
			for (State s : verticalNeighboors)
				if (s.getValue().equals(value))
					return true;
	
			for (State s : sectorNeighboors)
				if (s.getValue().equals(value))
					return true;
		}
		return false;
	}

	public boolean resolveSubtractingState() {
		if (value.isEmpty()) {
			if (resolveSubtractingState(horizontalNeighboors))
				return true;
			if (resolveSubtractingState(verticalNeighboors))
				return true;
			if (resolveSubtractingState(sectorNeighboors))
				return true;
		}
		return false;
	}

	private boolean resolveSubtractingState(State[] neighboors) {
		HashSet<String> candidates = new HashSet<String>(this.candidates);
		for (State s : neighboors) {
			if (s.value.isEmpty()) {
				candidates.removeAll(s.candidates);
				if (candidates.size()==0)
					return false;
			}
		}
		if (candidates.size()==1) {
			setValue(candidates.iterator().next());
			return true;
		}
		return false;
	}
}