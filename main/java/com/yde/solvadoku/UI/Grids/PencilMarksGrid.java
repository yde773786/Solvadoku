package com.yde.solvadoku.UI.Grids;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import com.yde.solvadoku.Logic.Cell;
import com.yde.solvadoku.R;

public class PencilMarksGrid extends SquareGrid {

    public PencilMarksGrid(Context context) {
        super(context);
        unit = new TextView[3][3];
        paintPencilMarks();
    }

    public PencilMarksGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        unit = new TextView[3][3];
        paintPencilMarks();
    }

    public PencilMarksGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        unit = new TextView[3][3];
        paintPencilMarks();
    }

    /**
     * Paints the pencil marks grid empty
     */
    public void paintPencilMarks() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                TextView textView = new TextView(getContext());
                textView.setWidth(0);
                textView.setHeight(0);
                GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams(
                        GridLayout.spec(i, 0.33f), GridLayout.spec(j, 0.33f)
                );
                textView.setLayoutParams(gridParams);
                textView.setTextSize(9);
                textView.setGravity(Gravity.CENTER);
                unit[i][j] = textView;
                addView(textView, i + j);
            }
        }
    }

    /**
     * Remove Pencil Marks when not needed
     */
    public void clearPencilMarks() {
        for (TextView[] textViews : unit) {
            for (TextView textView : textViews) {
                textView.setText(R.string.empty);
            }
        }
    }

    /**
     * Adds pencil marks as per candidates of cell required
     *
     * @param cell Cell input provided
     */
    public void insertPencilMarks(Cell cell) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (cell.isCandidate(i * 3 + j + 1)) {
                    unit[i][j].setText(String.valueOf(i * 3 + j + 1));
                }
            }
        }
    }
}
