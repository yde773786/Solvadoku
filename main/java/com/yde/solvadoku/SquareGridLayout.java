package com.yde.solvadoku;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.gridlayout.widget.GridLayout;


public class SquareGridLayout extends GridLayout {


    public SquareGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);
        Log.i("Size" , (double)width/height + "");
        int makeSquare = Math.min(width, height);
        if((double)width/height<=0.66){
            super.onMeasure(MeasureSpec.makeMeasureSpec(makeSquare, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(makeSquare, MeasureSpec.EXACTLY));
        }
        else if((double)width/height>0.66 && (double)width/height<=0.7){
            super.onMeasure(MeasureSpec.makeMeasureSpec((int)(makeSquare*0.8), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int)(makeSquare*0.8), MeasureSpec.EXACTLY));
        }
        else{
            super.onMeasure(MeasureSpec.makeMeasureSpec((int)(makeSquare*0.7), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int)(makeSquare*0.7), MeasureSpec.EXACTLY));
        }
    }
}
