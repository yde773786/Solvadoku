package com.yde.solvadoku;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.GridLayout;

public class SudokuGrid extends GridLayout {

    public SudokuGrid(Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.sudoku_grid, this);
    }

    public SudokuGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.sudoku_grid, this);
    }

    public SudokuGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.sudoku_grid, this);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);
        int makeSquare = Math.min(width, height);

        if ((double) width / height <= 0.66) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(makeSquare, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(makeSquare, MeasureSpec.EXACTLY));
        } else if ((double) width / height > 0.66 && (double) width / height <= 0.7) {
            super.onMeasure(MeasureSpec.makeMeasureSpec((int) (makeSquare * 0.8), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) (makeSquare * 0.8), MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(MeasureSpec.makeMeasureSpec((int) (makeSquare * 0.7), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) (makeSquare * 0.7), MeasureSpec.EXACTLY));
        }
    }
}
