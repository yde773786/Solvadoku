package com.yde.solvadoku.UI.grids;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;
import androidx.gridlayout.widget.GridLayout;

import com.yde.solvadoku.Logic.Cell;
import com.yde.solvadoku.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SudokuGrid extends SquareGrid {

    private final HashMap<TextView, int[]> cellToState = new HashMap<>();
    private final HashMap<TextView, int[]> cellToIndex = new HashMap<>();
    private final HashMap<String, Typeface> fontCache = new HashMap<>();
    private boolean isLegalPuzzle;
    private boolean[][] isError;
    PencilMarksGrid[][] pencilMarksGrids;
    public final int CLEAR = 0, DISABLED = 1, SELECTED = 2, INVALID = 3, CURRENT = 4;
    public final int SOLVED = R.color.solved_cell_text, INPUT = R.color.input_cell_text;
    private TextView focusedCell;

    public SudokuGrid(Context context) {
        super(context);
        grid = new TextView[9][9];
        pencilMarksGrids = new PencilMarksGrid[9][9];
        isLegalPuzzle = true;
        paintSudoku();
    }

    /**
     * Sets up the Sudoku grid by painting on top of SquareGrid and providing the
     * TextViews with required functionality and backgrounds
     */
    private void paintSudoku() {
        final int text_color = getResources().getColor(R.color.input_cell_text);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                @SuppressLint("InflateParams")
                RelativeLayout cellGrid = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.cell_grid, null);

                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams(
                        GridLayout.spec(i, 0.11f), GridLayout.spec(j, 0.11f)
                );
                gridParams.height = 0;
                gridParams.width = 0;
                cellGrid.setLayoutParams(gridParams);

                TextView textView = cellGrid.findViewById(R.id.main_display);
                textView.setGravity(Gravity.CENTER);
                textView.setTypeface(getTypeface());
                textView.setTextColor(text_color);
                TextViewCompat.setAutoSizeTextTypeWithDefaults(textView,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

                addCellToState(textView, i, j);
                cellToIndex.put(textView, new int[]{i, j});

                textView.setOnClickListener(view -> {
                    if (focusedCell != null) {
                        switchBackground(focusedCell, getCurrentState(focusedCell));
                    }
                    focusedCell = textView;
                    switchBackground(textView, SELECTED);
                });

                grid[i][j] = textView;
                pencilMarksGrids[i][j] = cellGrid.findViewById(R.id.add_display);
                switchBackground(textView, CLEAR);
                addView(cellGrid, i + j);
            }
        }
    }

    /**
     * Method to get the Typeface from the cache
     * If Typeface does not exist in cache, it will retrieve it from the
     * assets folder and add it to the cache
     *
     * @return the Typeface if it is present in assets, else return default Typeface.
     */
    private Typeface getTypeface() {
        // Name of the typeface for the cell.
        final String cell_typeface = "majormono.ttf";

        // Attempting to get the Typeface from the fontCache.
        Typeface tf = fontCache.get(cell_typeface);

        // If the requested Typeface does not exist in cache, add it from the assets folder.
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/"+ cell_typeface);
            }
            catch (Exception e) {
                // returning the default system typeface.
                return Typeface.DEFAULT;
            }
            fontCache.put(cell_typeface, tf);
        }
        return tf;
    } // end of getTypeface()

    /**
     * Repaints sudoku puzzle on click of 'reset puzzle'
     */
    public void resetSudoku() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                grid[i][j].setText(R.string.empty);
                grid[i][j].setEnabled(true);
                grid[i][j].setText(R.string.empty);
                switchBackground(grid[i][j], CLEAR);
                switchTextColor(grid[i][j], INPUT);
            }
        }
    }

    public SudokuGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        isLegalPuzzle = true;
        pencilMarksGrids = new PencilMarksGrid[9][9];
        grid = new TextView[9][9];
        paintSudoku();
    }

    public SudokuGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        grid = new TextView[9][9];
        pencilMarksGrids = new PencilMarksGrid[9][9];
        isLegalPuzzle = true;
        paintSudoku();
    }

    public boolean hasValue(TextView textView) {
        return !textView.getText().equals("");
    }

    /**
     * Links the cell to all possible backgrounds it can have.
     *
     * @param textView Cell required
     * @param r        Row of cell
     * @param c        Column of cell
     */
    private void addCellToState(TextView textView, int r, int c) {
        if (c == 2 || c == 5) {
            if (r == 2 || r == 5) {
                cellToState.put(textView, new int[]{R.drawable.bottom_right, R.drawable.bottom_rightgreen,
                        R.drawable.bottom_rightblue, R.drawable.bottom_rightred, 0});
            } else if (r == 3 || r == 6) {
                cellToState.put(textView, new int[]{R.drawable.top_right, R.drawable.top_rightgreen,
                        R.drawable.top_rightblue, R.drawable.top_rightred, 0});
            } else
                cellToState.put(textView, new int[]{R.drawable.right_border, R.drawable.right_bordergreen,
                        R.drawable.right_borderblue, R.drawable.right_borderred, 0});
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                cellToState.put(textView, new int[]{R.drawable.bottom_left, R.drawable.bottom_leftgreen,
                        R.drawable.bottom_leftblue, R.drawable.bottom_leftred, 0});
            } else if (r == 3 || r == 6) {
                cellToState.put(textView, new int[]{R.drawable.top_left, R.drawable.top_leftgreen,
                        R.drawable.top_leftblue, R.drawable.top_leftred, 0});
            } else
                cellToState.put(textView, new int[]{R.drawable.left_border, R.drawable.left_bordergreen,
                        R.drawable.left_borderblue, R.drawable.left_borderred, 0});
        } else if (r == 2 || r == 5) {
            cellToState.put(textView, new int[]{R.drawable.bottom_border, R.drawable.bottom_bordergreen,
                    R.drawable.bottom_borderblue, R.drawable.bottom_borderred, 0});
        } else if (r == 3 || r == 6) {
            cellToState.put(textView, new int[]{R.drawable.top_border, R.drawable.top_bordergreen,
                    R.drawable.top_borderblue, R.drawable.top_borderred, 0});
        } else
            cellToState.put(textView, new int[]{R.drawable.border, R.drawable.bordergreen,
                    R.drawable.borderblue, R.drawable.borderred, 0});
    }

    /**
     * Set the focused cell as value inputted.
     *
     * @param value Value of focused cell required
     * @return If value set successfully ( returns false if focused cell is null)
     */
    public boolean setFocusedValue(String value) {
        if (focusedCell != null) {
            focusedCell.setText(value);
            paintUpdate();
            return true;
        }
        return false;
    }

    /**
     * Makes all cells unfocused
     */
    public void resetFocusedCell() {
        focusedCell = null;
    }

    /**
     * Getter for Sudoku grid
     *
     * @return Sudoku grid required.
     */
    public TextView[][] getGrid() {
        return grid;
    }

    /**
     * Paints the row, column or block as per current condition of board
     */
    public void paintUpdate() {
        isLegalPuzzle = true;
        isError = new boolean[9][9];

        for (int i = 0; i < 9; i++) {
            validateBlock(i);
            validateRow(i);
            validateColumn(i);
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (isError[i][j]) {
                    switchBackground(grid[i][j], INVALID);
                } else {
                    switchBackground(grid[i][j], CLEAR);
                }
            }
        }
    }

    /**
     * Displays pencil marks of current location
     *
     * @param cell Cell at current location
     * @param i    row index
     * @param j    column index
     */
    public void pencilDisplay(Cell cell, int i, int j) {
        pencilMarksGrids[i][j].insertPencilMarks(cell);
    }

    /**
     * Clears pencil marks at location
     *
     * @param i row index
     * @param j column index
     */
    public void pencilClear(int i, int j) {
        pencilMarksGrids[i][j].clearPencilMarks();
    }

    /**
     * Find if erroneous input is present in column and update cell
     * error status as required.
     *
     * @param i Index of column
     */
    private void validateColumn(int i) {
        Set<Integer> repeat = new HashSet<>();

        for (int j = 0; j < 9; j++) {
            Integer currValue = grid[j][i].getText().toString().equals("") ? 0 : Integer.parseInt(grid[j][i].getText().toString());
            if (repeat.contains(currValue) && currValue != 0) {
                for (int k = 0; k < 9; k++) {
                    isError[k][i] = true;
                }
                isLegalPuzzle = false;
                return;
            }
            repeat.add(currValue);
        }
    }

    /**
     * Find if erroneous input is present in row and update cell
     * error status as required.
     *
     * @param i Index of row
     */
    private void validateRow(int i) {
        Set<Integer> repeat = new HashSet<>();

        for (int j = 0; j < 9; j++) {
            Integer currValue = grid[i][j].getText().toString().equals("") ? 0 : Integer.parseInt(grid[i][j].getText().toString());
            if (repeat.contains(currValue) && currValue != 0) {
                for (int k = 0; k < 9; k++) {
                    isError[i][k] = true;
                }
                isLegalPuzzle = false;
                return;
            }
            repeat.add(currValue);
        }
    }

    /**
     * Find if erroneous input is present in block and update cell
     * error status as required.
     *
     * @param i Index of block
     */
    private void validateBlock(int i) {
        int startI = 3 * (i / 3);
        int startJ = 3 * (i % 3);
        Set<Integer> repeat = new HashSet<>();

        for (int k = startI; k < startI + 3; k++) {
            for (int l = startJ; l < startJ + 3; l++) {
                Integer currValue = grid[k][l].getText().toString().equals("") ? 0 : Integer.parseInt(grid[k][l].getText().toString());

                if (repeat.contains(currValue) && currValue != 0) {
                    for (int m = startI; m < startI + 3; m++) {
                        for (int n = startJ; n < startJ + 3; n++) {
                            isError[m][n] = true;
                        }
                    }
                    isLegalPuzzle = false;
                    return;
                }

                repeat.add(currValue);
            }
        }
    }

    /**
     * Switches the background of cell and updates its state
     *
     * @param textView Cell required
     * @param state    To be updated to (doesn't update if selected)
     */
    public void switchBackground(TextView textView, int state) {
        textView.setBackgroundResource(Objects.requireNonNull(cellToState.get(textView))[state]);
        if (state != SELECTED) {
            Objects.requireNonNull(cellToState.get(textView))[CURRENT] = state;
        }
    }

    /**
     * Switches the text color of cell
     *
     * @param textView Cell required
     * @param state    To be updated to
     */
    public void switchTextColor(TextView textView, int state) {
        textView.setTextColor(getResources().getColor(state));
    }

    /**
     * Gets the current state of the cell
     *
     * @param textView Cell required
     * @return state cell is in currently
     */
    private int getCurrentState(TextView textView) {
        return Objects.requireNonNull(cellToState.get(textView))[CURRENT];
    }

    public boolean getIsLegalPuzzle() {
        return isLegalPuzzle;
    }

    /**
     * Gives the adjacent to currently focused cell focus.
     */
    public void giveNextCellFocus() {
        if(focusedCell != null){
            int row, col;
            int[] focused_cell_index = cellToIndex.get(focusedCell);
            switchBackground(focusedCell, getCurrentState(focusedCell));

            assert focused_cell_index != null;
            row = focused_cell_index[0];
            col = focused_cell_index[1];

            // Moving to the next column.
            col++;
            // If the next column goes beyond the number of columns in the board, go to next row.
            if(col % 9 == 0)
                row++;

            focusedCell = grid[row % 9][col % 9];
        }
        else{
            focusedCell = grid[0][0];
        }
        switchBackground(focusedCell, SELECTED);
    }
}
