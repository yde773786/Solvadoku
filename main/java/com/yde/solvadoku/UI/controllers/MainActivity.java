package com.yde.solvadoku.UI.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.yde.solvadoku.Logic.Cell;
import com.yde.solvadoku.Logic.Sudoku;
import com.yde.solvadoku.R;
import com.yde.solvadoku.UI.grids.SudokuGrid;
import com.yde.solvadoku.UI.persistence.MainActivityViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TextView[][] unit;
    private boolean isInitialBoard;
    private boolean[][] initialBoard;
    private SudokuGrid sudokuGrid;
    private Button solve_btn;
    private Button check_steps_btn;
    private ImageButton choose_strategies_btn;
    private ImageButton pencil_marks_btn;
    private ImageButton about_btn;
    private ImageButton next_cell_btn;
    private MainActivityViewModel mViewModel;
    private ImageButton clear_board_btn;
    private ImageButton erase_cell_btn;
    Cell[][] puzzle;
    Button[] keypad = new Button[9];
    private static boolean putPencilMarks;
    private ArrayList<String> checkedItems;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        check_steps_btn = findViewById(R.id.check_steps);
        keypad = new Button[]{findViewById(R.id.one), findViewById(R.id.two), findViewById(R.id.three), findViewById(R.id.four),
                findViewById(R.id.five), findViewById(R.id.six), findViewById(R.id.seven), findViewById(R.id.eight), findViewById(R.id.nine)};
        next_cell_btn = findViewById(R.id.next_cell);
        choose_strategies_btn = findViewById(R.id.choose_strategies);
        clear_board_btn = findViewById(R.id.clear_board);
        erase_cell_btn = findViewById(R.id.erase);
        pencil_marks_btn = findViewById(R.id.pencil_marks_button);
        about_btn = findViewById(R.id.about_button);
        solve_btn = findViewById(R.id.solve);

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        checkedItems = mViewModel.getCheckedItems();

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        try {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ignored) {
        }

        putPencilMarks = mViewModel.isPencilMarks();
        isInitialBoard = true;
        initialBoard = new boolean[9][9];
        sudokuGrid = findViewById(R.id.sudoku_board);
        unit = sudokuGrid.getGrid();
        puzzle = new Cell[9][9];

        String[][] values = mViewModel.getValues();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                unit[i][j].setText(values[i][j]);
            }
        }
        sudokuGrid.paintUpdate();

        if (mViewModel.isSolving()) {
            solver();
        }

        for (int i = 0; i < keypad.length; i++) {
            final int finalI = i;
            keypad[i].setOnClickListener(view -> {
                if (sudokuGrid.setFocusedValue(getString(R.string.cell_focus_value, finalI + 1))) {
                    sudokuGrid.giveNextCellFocus();
                } else {
                    Toast.makeText(MainActivity.this, R.string.select_cell_toast, Toast.LENGTH_SHORT).show();
                }
            });
        }

        clear_board_btn.setOnClickListener(view -> {
            sudokuGrid.resetSudoku();
            initialBoard = new boolean[9][9];
            setEnabledSudokuButton(solve_btn, true);
            setEnabledEditingButtons(true);
            setActivatedSudokuButton(pencil_marks_btn,false);

            sudokuGrid.resetFocusedCell();
            setEnabledSudokuButton(check_steps_btn, false);

            if (putPencilMarks) {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        sudokuGrid.pencilClear(i, j);
                    }
                }
            }

            isInitialBoard = true;
        });

        choose_strategies_btn.setOnClickListener(view -> {
            Context context = new ContextThemeWrapper(MainActivity.this, R.style.CustomDialog);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = getLayoutInflater();
            View customTitle = inflater.inflate(R.layout.choose_strategies, null);

            boolean[] previous = new boolean[MainActivityViewModel.LOGICS.length];

            for (int i = 0; i < MainActivityViewModel.LOGICS.length; i++) {
                if (checkedItems.contains(MainActivityViewModel.LOGICS[i])) {
                    previous[i] = true;
                }
            }

            builder.setCustomTitle(customTitle).setMultiChoiceItems(MainActivityViewModel.LOGICS, previous, (dialogInterface, i, isChecked) -> {
                if (isChecked) {
                    checkedItems.add(MainActivityViewModel.LOGICS[i]);
                } else checkedItems.remove(MainActivityViewModel.LOGICS[i]);
            });
            builder.setPositiveButton(R.string.confirm, (dialogInterface, x) -> { });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        solve_btn.setOnClickListener(view -> {
            solver();
        });

        erase_cell_btn.setOnClickListener(view -> sudokuGrid.setFocusedValue(getString(R.string.empty)));

        next_cell_btn.setOnClickListener(view -> sudokuGrid.giveNextCellFocus());

        setEnabledSudokuButton(check_steps_btn, mViewModel.isSolving());

        check_steps_btn.setOnClickListener(view -> {
            ArrayList<String[]> viewHolderList = Sudoku.viewHolderList;
            if (viewHolderList.size() != 0) {
                Intent intent = new Intent(MainActivity.this, StepsActivity.class);
                intent.putExtra("CheckSteps", viewHolderList);
                startActivity(intent);
            } else
                Toast.makeText(MainActivity.this, R.string.no_effect_toast, Toast.LENGTH_SHORT).show();
        });

        // Disabling the Pencil Marks Button.
        setActivatedSudokuButton(pencil_marks_btn, mViewModel.isSolving());
        pencil_marks_btn.setOnClickListener(view -> {
            // Checking if the Pencil Marks Button is enabled.
            if (pencil_marks_btn.isActivated()) {
                // Toggling the Pencil Marks state.
                putPencilMarks = !putPencilMarks;
                // Toggling the Pencil Marks icon to represent its state (disabled or enabled).
                pencil_marks_btn.setBackgroundResource(putPencilMarks ? R.drawable.button_pencil_marks_enabled : R.drawable.button_pencil_marks_disabled);
                // Loop to display the Pencil Marks in all the empty cells.
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (putPencilMarks && !sudokuGrid.hasValue(unit[i][j])) {
                            sudokuGrid.pencilDisplay(puzzle[i][j], i, j);
                        } else {
                            sudokuGrid.pencilClear(i, j);
                        }
                    }
                }
            }
            // If the Pencil Marks Button is not enabled, display toast message.
            else {
                Toast.makeText(MainActivity.this, R.string.pencil_marks_disabled_toast, Toast.LENGTH_LONG).show();
            }
        });

        about_btn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });


    } // end of onCreate method

    private void solver() {
        if (sudokuGrid.getIsLegalPuzzle()) {
            setEnabledSudokuButton(check_steps_btn, true);
            Sudoku.cellCount = 0;

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    puzzle[i][j] = new Cell();
                }
            }

            setEnabledEditingButtons(false);
            setActivatedSudokuButton(pencil_marks_btn, true);

            // Finds initial board pieces (Runs first time only)
            if (isInitialBoard) {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (sudokuGrid.hasValue(unit[i][j])) {
                            initialBoard[i][j] = true;
                        }
                    }
                }

                isInitialBoard = false;
            }

            // Places the initial board on every solve click
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (initialBoard[i][j]) {
                        Sudoku.placeNumber(puzzle, i, j, value(unit[i][j]));
                    }
                }
            }

            Sudoku.resetSolution();
            Sudoku.partiallySolve(puzzle, checkedItems);

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {

                    if (!initialBoard[i][j]) {
                        if (puzzle[i][j].getSolution() != 0) { // Just solved cell
                            sudokuGrid.switchBackground(unit[i][j], sudokuGrid.DISABLED);
                            sudokuGrid.switchTextColor(unit[i][j], sudokuGrid.SOLVED);
                            unit[i][j].setText(String.valueOf(puzzle[i][j].getSolution()));
                        } else { // Previously solved cell
                            sudokuGrid.switchBackground(unit[i][j], sudokuGrid.CLEAR);
                            unit[i][j].setText(R.string.empty);
                        }
                    }

                    if (putPencilMarks && !sudokuGrid.hasValue(unit[i][j])) {
                        sudokuGrid.pencilDisplay(puzzle[i][j], i, j);
                    } else {
                        sudokuGrid.pencilClear(i, j);
                    }
                    unit[i][j].setEnabled(false);
                }
            }

            if (Sudoku.cellCount == 81) {
                setEnabledSudokuButton(solve_btn, false);
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.invalid_input, Toast.LENGTH_SHORT).show();
        }
    }

    private int value(TextView textView) {
        return Integer.parseInt(textView.getText().toString());
    }


    private void setEnabledSudokuButton(Button button, boolean isEnabled) {
        button.setEnabled(isEnabled);
        button.setAlpha(isEnabled ? 1.0f : 0.4f);
    }

    // Activated allows the button to still be clickable, but it won't carry out its function.
    private void setActivatedSudokuButton(ImageButton button, boolean isActivated) {
        button.setActivated(isActivated);
        button.setAlpha(isActivated ? 1.0f : 0.4f);
    }

    private void setEnabledEditingButtons(boolean isEnabled) {
        for (Button key : keypad) {
            key.setEnabled(isEnabled);
        }
        next_cell_btn.setEnabled(isEnabled);
        erase_cell_btn.setEnabled(isEnabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.setCheckedItems(checkedItems);
        mViewModel.setPencilMarks(putPencilMarks);
        mViewModel.setSolving(check_steps_btn.isEnabled());

        String[][] values = new String[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (initialBoard[i][j] && mViewModel.isSolving()) {
                    values[i][j] = unit[i][j].getText().toString();
                } else if (!mViewModel.isSolving()) {
                    values[i][j] = unit[i][j].getText().toString();
                }
            }
        }

        mViewModel.setValues(values);
    }
} // end of MainActivity class