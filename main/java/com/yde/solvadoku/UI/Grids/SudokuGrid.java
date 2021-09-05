package com.yde.solvadoku.UI.Grids;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;


import androidx.gridlayout.widget.GridLayout;

import com.yde.solvadoku.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SudokuGrid extends SquareGrid {

    private HashMap<TextView, int[]> cellToState = new HashMap<>();
    private TextView[][] unit;
    private boolean isLegalPuzzle;
    private boolean[][] isError;
    private final int CLEAR = 0, DISABLED = 1, SELECTED = 2, INVALID = 3, CURRENT = 4;
    private TextView focusedCell;

    public SudokuGrid(Context context) {
        super(context);
        unit = new TextView[9][9];
        paintSudoku();
    }

    /**
     * Sets up the Sudoku grid by painting on top of SquareGrid and providing the
     * TextViews with required functionality and backgrounds
     */
    private void paintSudoku() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final TextView textView = new TextView(getContext());
                textView.setWidth(0);
                textView.setHeight(0);

                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams(
                        GridLayout.spec(i, 0.11f), GridLayout.spec(j, 0.11f)
                );
                textView.setLayoutParams(gridParams);
                textView.setTextSize(25);
                textView.setGravity(Gravity.CENTER);

                addCellToState(textView, i, j);

                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (focusedCell != null) {
                            switchBackground(focusedCell, getCurrentState(focusedCell));
                        }
                        focusedCell = textView;
                        switchBackground(textView, SELECTED);
                    }
                });

                unit[i][j] = textView;
                addView(textView, i + j);

                textView.setBackgroundResource(Objects.requireNonNull(cellToState.get(textView))[CLEAR]);
            }
        }
    }

    public SudokuGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        unit = new TextView[9][9];
        paintSudoku();
    }

    public SudokuGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        unit = new TextView[9][9];
        paintSudoku();
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
                cellToState.put(textView, new int[]{R.drawable.bottom_right, R.drawable.bottom_rightgrey,
                        R.drawable.bottom_rightblue, R.drawable.bottom_rightred, 0});
            } else if (r == 3 || r == 6) {
                cellToState.put(textView, new int[]{R.drawable.top_right, R.drawable.top_rightgrey,
                        R.drawable.top_rightblue, R.drawable.top_rightred, 0});
            } else
                cellToState.put(textView, new int[]{R.drawable.right_border, R.drawable.right_bordergrey,
                        R.drawable.right_borderblue, R.drawable.right_borderred, 0});
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                cellToState.put(textView, new int[]{R.drawable.bottom_left, R.drawable.bottom_leftgrey,
                        R.drawable.bottom_leftblue, R.drawable.bottom_leftred, 0});
            } else if (r == 3 || r == 6) {
                cellToState.put(textView, new int[]{R.drawable.top_left, R.drawable.top_leftgrey,
                        R.drawable.top_leftblue, R.drawable.top_leftred, 0});
            } else
                cellToState.put(textView, new int[]{R.drawable.left_border, R.drawable.left_bordergrey,
                        R.drawable.left_borderblue, R.drawable.left_borderred, 0});
        } else if (r == 2 || r == 5) {
            cellToState.put(textView, new int[]{R.drawable.bottom_border, R.drawable.bottom_bordergrey,
                    R.drawable.bottom_borderblue, R.drawable.bottom_borderred, 0});
        } else if (r == 3 || r == 6) {
            cellToState.put(textView, new int[]{R.drawable.top_border, R.drawable.top_bordergrey,
                    R.drawable.top_borderblue, R.drawable.top_borderred, 0});
        } else
            cellToState.put(textView, new int[]{R.drawable.border, R.drawable.bordergrey,
                    R.drawable.borderblue, R.drawable.borderred, 0});
    }

    /**
     * Set the focused cell as value inputted.
     *
     * @param value Value of focused cell required
     */
    public void setFocusedValue(String value) {
        if (focusedCell != null) {
            focusedCell.setText(value);
            paintErrors();
        }
    }

    /**
     * Getter for Sudoku grid
     *
     * @return Sudoku grid required.
     */
    public TextView[][] getUnit() {
        return unit;
    }

    /**
     * Paints the row, column or block with invalid input, if any.
     */
    private void paintErrors() {
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
                    switchBackground(unit[i][j], INVALID);
                } else {
                    switchBackground(unit[i][j], CLEAR);
                }
            }
        }
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
            Integer currValue = unit[j][i].getText().toString().equals("") ? 0 : Integer.parseInt(unit[j][i].getText().toString());
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
            Integer currValue = unit[i][j].getText().toString().equals("") ? 0 : Integer.parseInt(unit[i][j].getText().toString());
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
                Integer currValue = unit[k][l].getText().toString().equals("") ? 0 : Integer.parseInt(unit[k][l].getText().toString());

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
     * @param state    state to be updated to (doesn't update if selected)
     */
    private void switchBackground(TextView textView, int state) {
        textView.setBackgroundResource(Objects.requireNonNull(cellToState.get(textView))[state]);
        if (state != SELECTED) {
            Objects.requireNonNull(cellToState.get(textView))[CURRENT] = state;
        }
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

}
