package com.yde.solvadoku.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.yde.solvadoku.Logic.Cell;
import com.yde.solvadoku.Logic.Sudoku;
import com.yde.solvadoku.R;
import com.yde.solvadoku.UI.Grids.SudokuGrid;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 0;
    private TextView[][] unit;
    private Button partial;
    private Button solve;
    private SudokuGrid sudokuGrid;
    private Button checkSteps;
    Cell[][] puzzle = new Cell[9][9];
    Button[] keypad = new Button[9];
    private static final float actualFontSize = 25;
    private static boolean putPencilMarks;
    final ArrayList<String> checkedItems = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        try {
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (NullPointerException ignored) {
        }

        putPencilMarks = false;
        sudokuGrid = findViewById(R.id.gridLayout);
        unit = sudokuGrid.getUnit();

        keypad = new Button[]{findViewById(R.id.one), findViewById(R.id.two), findViewById(R.id.three), findViewById(R.id.four),
                findViewById(R.id.five), findViewById(R.id.six), findViewById(R.id.seven), findViewById(R.id.eight), findViewById(R.id.nine)};

        for (int i = 0; i < keypad.length; i++) {
            final int finalI = i;
            keypad[i].setOnClickListener(view ->
                    sudokuGrid.setFocusedValue(getString(R.string.cell_focus_value, finalI + 1)));

            keypad[i].setOnTouchListener((view, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    keypad[finalI].setBackgroundResource(R.drawable.keypad_unfocused);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    keypad[finalI].setBackgroundResource(R.drawable.keypad_focused);
                }
                return false;
            });
        }

        solve = findViewById(R.id.solve);
        solve.setOnClickListener(view -> {

            if (sudokuGrid.getIsLegalPuzzle()) {
                Context context = new ContextThemeWrapper(MainActivity.this, R.style.CustomDialog);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = getLayoutInflater();
                View customTitle = inflater.inflate(R.layout.custom_title, null);

                final String[] logics = {"Naked Single", "Hidden Single", "Naked Pair", "Pointing Pair",
                        "Claiming Pair", "Hidden Pair", "Naked Triple", "Hidden Triple", "X-Wing", "Swordfish", "Jellyfish",
                        "Naked Quad", "Hidden Quad", "Finned X-Wing", "Finned Swordfish", "Finned Jellyfish", "Brute Force"};
                boolean[] previous = new boolean[logics.length];
                for (int i = 0; i < logics.length; i++) {
                    if (checkedItems.contains(logics[i])) {
                        previous[i] = true;
                    }
                }
                builder.setCustomTitle(customTitle).setMultiChoiceItems(logics, previous, (dialogInterface, i, isChecked) -> {
                    if (isChecked) {
                        checkedItems.add(logics[i]);
                    } else checkedItems.remove(logics[i]);
                })
                        .setPositiveButton(R.string.solve, (dialogInterface, x) -> {
                            if (sudokuGrid.getIsLegalPuzzle()) {
                                checkSteps.setEnabled(true);
                                puzzle = new Cell[9][9];
                                Sudoku.cellCount = 0;

                                for (int i = 0; i < 9; i++) {
                                    for (int j = 0; j < 9; j++) {
                                        puzzle[i][j] = new Cell();
                                    }
                                }

                                for (int i = 0; i < 9; i++) {
                                    for (int j = 0; j < 9; j++) {
                                        if (sudokuGrid.hasValue(unit[i][j])) {
                                            Sudoku.placeNumber(puzzle, i, j, value(unit[i][j]));
                                        }
                                    }
                                }

                                Sudoku.algorithm = new ArrayList<>();
                                Sudoku.insert = new ArrayList<>();
                                Sudoku.remove = new ArrayList<>();
                                Sudoku.viewHolderList = new ArrayList<>();

                                Sudoku.Solve(puzzle);

                                for (int i = 0; i < 9; i++) {
                                    for (int j = 0; j < 9; j++) {
                                        if (!sudokuGrid.hasValue(unit[i][j]) && puzzle[i][j].getSolution() != 0) {
                                            sudokuGrid.switchBackground(unit[i][j], sudokuGrid.DISABLED);
                                            sudokuGrid.switchTextColor(unit[i][j], sudokuGrid.SOLVED);
                                            unit[i][j].setText(String.valueOf(puzzle[i][j].getSolution()));
                                        }
                                        unit[i][j].setEnabled(false);
                                    }
                                }

                                if (Sudoku.cellCount == 81) {
                                    solve.setEnabled(false);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, R.string.invalid_input, Toast.LENGTH_LONG).show();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();


            } else {
                Toast.makeText(MainActivity.this, R.string.invalid_input, Toast.LENGTH_LONG).show();
            }
        });

        checkSteps = findViewById(R.id.check_steps);
        checkSteps.setEnabled(false);
        checkSteps.setOnClickListener(view -> {
            ArrayList<String[]> viewHolderList = Sudoku.viewHolderList;
            if (viewHolderList.size() != 0) {
                Intent intent = new Intent(MainActivity.this, StepsActivity.class);
                intent.putExtra("CheckSteps", viewHolderList);
                startActivity(intent);
            } else
                Toast.makeText(MainActivity.this, R.string.no_effect, Toast.LENGTH_LONG).show();
        });

    }

    private int value(TextView textView) {
        return Integer.parseInt(textView.getText().toString());
    }

    private void makeEditable(TextView textView) {
        textView.setFocusableInTouchMode(true);
        textView.setTextColor(getResources().getColor(R.color.colorBlack));
        textView.setEnabled(true);
        textView.setCursorVisible(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_clear_all:
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        unit[i][j].setGravity(Gravity.CENTER);
                        unit[i][j].setTextSize(actualFontSize);
                        unit[i][j].setKeyListener(DigitsKeyListener.getInstance("123456789"));
                        unit[i][j].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                        makeEditable(unit[i][j]);
                        unit[i][j].setText("");
                        unit[i][j].clearFocus();
                    }
                }
                solve.setEnabled(true);
                partial.setEnabled(true);
                checkSteps.setEnabled(false);
                invalidateOptionsMenu();
                return true;

            case R.id.about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                intent.putExtra("input", putPencilMarks);
                startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                putPencilMarks = data.getBooleanExtra("key", true);
                gridAsNecessary();
            }
        }
    }

    private void gridAsNecessary() {

    }

}