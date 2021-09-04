package com.yde.solvadoku.UI.Grids;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;


import androidx.gridlayout.widget.GridLayout;

import com.yde.solvadoku.R;

import java.util.HashMap;
import java.util.Objects;

public class SudokuGrid extends SquareGrid {

    private HashMap<TextView, int[]> cellToState = new HashMap<>();
    private TextView[][] unit;
    private final int CLEAR = 0, DISABLED = 1, SELECTED = 2, INVALID = 3;
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
                            focusedCell.setBackgroundResource(Objects.requireNonNull(cellToState.get(focusedCell))[CLEAR]);
                        }
                        focusedCell = textView;
                        textView.setBackgroundResource(Objects.requireNonNull(cellToState.get(textView))[SELECTED]);
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
                        R.drawable.bottom_rightblue, R.drawable.bottom_rightred});
            } else if (r == 3 || r == 6) {
                cellToState.put(textView, new int[]{R.drawable.top_right, R.drawable.top_rightgrey,
                        R.drawable.top_rightblue, R.drawable.top_rightred});
            } else
                cellToState.put(textView, new int[]{R.drawable.right_border, R.drawable.right_bordergrey,
                        R.drawable.right_borderblue, R.drawable.right_borderred});
        } else if (c == 3 || c == 6) {
            if (r == 2 || r == 5) {
                cellToState.put(textView, new int[]{R.drawable.bottom_left, R.drawable.bottom_leftgrey,
                        R.drawable.bottom_leftblue, R.drawable.bottom_leftred});
            } else if (r == 3 || r == 6) {
                cellToState.put(textView, new int[]{R.drawable.top_left, R.drawable.top_leftgrey,
                        R.drawable.top_leftblue, R.drawable.top_leftred});
            } else
                cellToState.put(textView, new int[]{R.drawable.left_border, R.drawable.left_bordergrey,
                        R.drawable.left_borderblue, R.drawable.left_borderred});
        } else if (r == 2 || r == 5) {
            cellToState.put(textView, new int[]{R.drawable.bottom_border, R.drawable.bottom_bordergrey,
                    R.drawable.bottom_borderblue, R.drawable.bottom_borderred});
        } else if (r == 3 || r == 6) {
            cellToState.put(textView, new int[]{R.drawable.top_border, R.drawable.top_bordergrey,
                    R.drawable.top_borderblue, R.drawable.top_borderred});
        } else
            cellToState.put(textView, new int[]{R.drawable.border, R.drawable.bordergrey,
                    R.drawable.borderblue, R.drawable.borderred});
    }

    /**
     * Set the focused cel as value inputted
     *
     * @param value Value of focused cell required
     */
    public void setFocusedValue(String value) {
        if (focusedCell != null) {
            focusedCell.setText(value);
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

}
