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
import com.google.gwt.user.client.ui.HorizontalPanel;
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
				GWT.log("Error", e);
				Throwable cause = ((UmbrellaException) e).getCause().getCause();
				Window.alert(cause.getMessage());
			}
		});
	}

	private Grid grid = new Grid(9, 9);
	private CellFormatter cf = grid.getCellFormatter();
	private TextBox[][] boxes = new TextBox[9][9];
	private Label[][] labels = new Label[9][9];
	private String cpuGuess;

	private Scanner scanner = new Scanner(this);
	
	private enum GameState {
		SETUP, PLAYING, GUESSING
	}
	private GameState gameState = GameState.SETUP;
	private Button cpuButton, mainButton;
	
	Sudoku() {
		highlightBorders();
		createWidgets();
		scanner.init(Games.empty);
	}

	@Override
	public void onModuleLoad() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(grid);
		hp.add(gamesMenu());
		RootPanel.get().add(hp);
		
		mainButton = new Button("Start");
		mainButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (gameState==GameState.SETUP) {
					for (int i=0; i<9; i++)
						for (int j=0; j<9; j++)
							if (!scanner.isEmpty(i,j))
								boxes[i][j].setEnabled(false);
					
					gameState = GameState.PLAYING;
					mainButton.setText("Click to start Guessing");
					RootPanel.get().add(cpuButton);
				} else if (gameState==GameState.PLAYING) {
					gameState = GameState.GUESSING;
					mainButton.setText("Stop Guessing");
				} else {
					gameState = GameState.PLAYING;
					mainButton.setText("Start Guessing");
					
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
		RootPanel.get().add(mainButton);
	}

	private Widget gamesMenu() {
		VerticalPanel menu = new VerticalPanel();
		Button clean = new Button("Clear"),
			easy = new Button("Easy"), 
			normal = new Button("Normal"), 
			hard = new Button("Hard");
		
		class Handler implements ClickHandler {
			private String[][] gameData;
			
			Handler(String[][] gameData) {
				this.gameData = gameData;
			}
			
			@Override
			public void onClick(ClickEvent event) {
				reset();
				gameState = GameState.SETUP;
				mainButton.setText("Start");
				scanner = new Scanner(Sudoku.this);
				scanner.init(gameData);
			}
		}
		
		clean.addClickHandler(new Handler(Games.empty));
		easy.addClickHandler(new Handler(Games.easy));
		normal.addClickHandler(new Handler(Games.normal));
		hard.addClickHandler(new Handler(Games.advanced));
		
		menu.add(clean);
		menu.add(easy);
		menu.add(normal);
		menu.add(hard);
		
		return menu;
	}

	private void reset() {
		cpuButton.removeFromParent();
		for (int i=0; i<9; i++) {
			for (int j=0; j<9; j++) {
				boxes[i][j].setValue("");
				boxes[i][j].setEnabled(true);
				cf.removeStyleName(i, j, "setup");
				cf.removeStyleName(i, j, "play");
				cf.removeStyleName(i, j, "guess");
				cf.removeStyleName(i, j, "single");
				cf.removeStyleName(i, j, "subtract");
			}
		}
	}

	private void createWidgets() {
		cpuButton();
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
						cpuGuess = null;
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
		cpuButton = new Button("Next (CPU)");
		cpuButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				scanner.next();
			}
		});
		return cpuButton;
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
		else if (cpuGuess!=null)
			bg = cpuGuess;
		else bg = "play";
		return bg;
	}

	@Override
	public void resetState(int i, int j) {
		labels[i][j].setVisible(true);
		grid.getCellFormatter().removeStyleName(i, j, "setup");
		grid.getCellFormatter().removeStyleName(i, j, "single");
		grid.getCellFormatter().removeStyleName(i, j, "subtract");
		grid.getCellFormatter().removeStyleName(i, j, "guess");
		grid.getCellFormatter().removeStyleName(i, j, "play");
		
		if (gameState!=GameState.SETUP)
			boxes[i][j].setEnabled(true);
	}

	@Override
	public void cpuGuess(int i, int j, String value, String type) {
		cpuGuess = type;
		boxes[i][j].setValue(value, true);
	}
}