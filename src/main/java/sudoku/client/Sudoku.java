package sudoku.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Sudoku implements EntryPoint, Scanner.SudokuView {
	static {
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void onUncaughtException(Throwable e) {
				Throwable cause = ((UmbrellaException) e).getCause().getCause();
				Window.alert(cause.getMessage());
			}
		});
	}

	private Grid grid = new Grid(9, 9);
	private CellFormatter cf = grid.getCellFormatter();
	private TextBox[][] boxes = new TextBox[9][9];
	private Label[][] labels = new Label[9][9];
	private boolean cpu;

	private Scanner scanner = new Scanner(this);
	
	private enum GameState {
		SETUP, PLAYING, GUESSING
	}
	private GameState gameState = GameState.SETUP;
	
	Sudoku() {
		highlightBorders();
		createWidgets();
		init();
		scanner.init(Games.normal);
	}

	@Override
	public void onModuleLoad() {
		RootPanel.get().add(grid);
		
		final Button b = new Button("Start");
		b.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (gameState==GameState.SETUP) {
					for (int i=0; i<9; i++)
						for (int j=0; j<9; j++)
							if (!scanner.isEmpty(i,j))
								boxes[i][j].setEnabled(false);

					gameState = GameState.PLAYING;
					b.setText("Click to start Guessing");
					RootPanel.get().add(cpuButton());
				} else if (gameState==GameState.PLAYING) {
					gameState = GameState.GUESSING;
					b.setText("Stop Guessing");
				} else {
					gameState = GameState.PLAYING;
					b.setText("Start Guessing");
					
					for (int i=0; i<9; i++) {
						for (int j=0; j<9; j++) {
							if (cf.getStyleName(i, j).contains("guess")) {
								cf.removeStyleName(i, j, "guess");
								cf.addStyleName(i, j, "play");
							}
						}
					}
				}
			}
		});
		RootPanel.get().add(b);
	}

	private void init() {
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				String v = scanner.valueOf(i,j);
				if (!v.isEmpty())
					boxes[i][j].setValue(v, true);
			}
		}
	}

	private void createWidgets() {
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				cf.setHeight(i, j, "40px");

				final TextBox box = new TextBox();
				boxes[i][j] = box;
				final int I = i, J = j; 
				box.addKeyDownHandler(new KeyDownHandler() {
					@Override
					public void onKeyDown(KeyDownEvent event) {
						char c = (char) event.getNativeKeyCode();
						if ((c < '1' || c > '9') &&
								c != KeyCodes.KEY_TAB && c != KeyCodes.KEY_BACKSPACE && c != KeyCodes.KEY_DELETE) {
							event.preventDefault();
							event.stopPropagation();
						}
						if (c >= '1' && c <= '9' && !box.getValue().isEmpty()) {
							event.preventDefault();
							event.stopPropagation();
						}
						cpu = false;
					}
				});
				box.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						scanner.update(I, J, event.getValue());
					}
				});
				
				VerticalPanel vp = new VerticalPanel();
				grid.setWidget(i, j, vp);
				vp.setHeight("100%");
				vp.add(box);
				
				Label label = new Label();
				labels[i][j] = label;
				vp.add(label);
			}
		}
	}

	private void highlightBorders() {
		for (int i=0; i<9; i+=3) {
			for (int j=0; j<9; j++) {
				cf.addStyleName(i, j, "_top");
				cf.addStyleName(j, i, "_left");
			}
		}
		for (int i=0; i<9; i++) {
			cf.addStyleName(8, i, "_bottom");
			cf.addStyleName(i, 8, "_right");
		}
	}

	protected Widget cpuButton() {
		final Button next = new Button("Next (CPU)");
		next.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				scanner.next();
			}
		});
		return next;
	}

	@Override
	public void clearValue(int i, int j) {
		boxes[i][j].setValue("");
	}

	@Override
	public void showCandidates(int i, int j, String candidates) {
		labels[i][j].setText(candidates);
	}

	@Override
	public void setValue(int i, int j, String value) {
		boxes[i][j].setValue(value);
		grid.getCellFormatter().addStyleName(i, j, background());
	}
	
	private String background() {
		String bg;
		if (gameState==GameState.SETUP)
			bg = "setup";
		else if (gameState==GameState.GUESSING)
			bg = "guess";
		else if (cpu)
			bg = "cpu";
		else bg = "play";
		return bg;
	}

	@Override
	public void clearState(int i, int j) {
		labels[i][j].setVisible(true);
		grid.getCellFormatter().removeStyleName(i, j, "setup");
		grid.getCellFormatter().removeStyleName(i, j, "cpu");
		grid.getCellFormatter().removeStyleName(i, j, "guess");
		grid.getCellFormatter().removeStyleName(i, j, "play");
		
		if (gameState!=GameState.SETUP)
			boxes[i][j].setEnabled(true);
	}

	@Override
	public void cpuGuess(int i, int j, String value) {
		cpu = true;
		boxes[i][j].setValue(value, true);
	}
}