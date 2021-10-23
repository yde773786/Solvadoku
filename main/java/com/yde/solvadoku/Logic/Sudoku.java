package com.yde.solvadoku.Logic;

import com.yde.solvadoku.Logic.Cell;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A utility class that consists of all the operations that are to be performed to the sudoku puzzle
 */
public final class Sudoku {

    private static int changeCounter = 0;
    public static int cellCount = 0;
    public static ArrayList<String> algorithm;
    public static ArrayList<String> insert;
    public static ArrayList<String> remove;
    public static ArrayList<String[]> viewHolderList;

    private Sudoku() {
    }

    /**
     * Reset the puzzle for new Solving process
     */
    public static void resetSolution() {
        algorithm = new ArrayList<>();
        insert = new ArrayList<>();
        remove = new ArrayList<>();
        viewHolderList = new ArrayList<>();
    }

    /**
     * placeNumber places a value in the cell of certain row and column in the puzzle. it also
     * alters the value of hasChanged to true signifying that the puzzle has been altered in some way or form
     */
    public static String placeNumber(Cell[][] puzzle, int row, int col, int val) {

        changeCounter++;
        cellCount++;
        puzzle[row][col].placeSolution(val);
        int removeCounter = 0;
        String remove = "Remove Candidate " + val + " at ";

        for (int r = 0; r < 9; r++)//loop running across the column
        {
            if (puzzle[r][col].isCandidate(val) && puzzle[r][col].isNotSet()) {//remove candidates from cells that are not set
                removeCounter++;
                remove += "(" + (r + 1) + "," + (col + 1) + ")" + " ";
                puzzle[r][col].removeCandidate(val);
            }
        }

        for (int c = 0; c < 9; c++)//loop running across the row
        {
            if (puzzle[row][c].isCandidate(val) && puzzle[row][c].isNotSet()) {//remove candidates from cells that are not set
                removeCounter++;
                remove += "(" + (row + 1) + "," + (c + 1) + ")" + " ";
                puzzle[row][c].removeCandidate(val);
            }
        }

        int tmp_r = (row - (row % 3));//starting point (Row) of the subsquare to which the current cell belongs to
        int tmp_c = (col - (col % 3));//starting point (Column) of the subsquare to which the current cell belongs to
        for (int r = tmp_r; r <= tmp_r + 2; r++) {//traverse subsquare
            for (int c = tmp_c; c <= tmp_c + 2; c++) {//traverse subsquare
                if (puzzle[r][c].isCandidate(val) && puzzle[r][c].isNotSet()) {//remove candidates from cells that are not set
                    removeCounter++;
                    remove += "(" + (r + 1) + "," + (c + 1) + ")" + " ";
                    puzzle[r][c].removeCandidate(val);
                }
            }
        }

        if (removeCounter == 0) {
            remove = "";
        }

        return remove;
    }// end of placeNumber()

    /**
     * Naked Single means that in a specific cell only one digit remains possible (the last remaining candidate has no other candidates to hide behind and is thus naked).
     * The digit must then go into that cell.
     */
    private static void nakedSingle(Cell[][] puzzle) {
        for (int r = 0; r < 9; r++) {//traversing the entire puzzle
            for (int c = 0; c < 9; c++) {
                if (puzzle[r][c].getNoOfCandidates() == 1 && puzzle[r][c].isNotSet()) {//if only one Candidate and nothing is inserted, insert the value in the puzzle at the current row and column
                    algorithm.add("Naked Single");
                    int val = puzzle[r][c].getCandidate(0);//since there is only one Candidate, it occupies position 0
                    insert.add("Insert Candidate " + val + " at " + "(" + (r + 1) + "," + (c + 1) + ")");
                    remove.add(placeNumber(puzzle, r, c, val));
                }
            }
        }
    }// end of nakedSingle()

    /**
     * Hidden Single means that for a given digit and house only one cell is left to place that digit.
     * The cell itself has more than one candidate left, the correct digit is thus hidden amongst the rest.
     */
    private static void hiddenSingle(Cell[][] puzzle) {

        int freq;//find when hidden single has been found, if present.

        for (int num = 1; num < 10; num++) {//running through all digits 1 to 9 to check for hidden Singles that contain only num as a candidate

            int index_c = 0;//the column of the cell which is a hiddenSingle
            for (int r = 0; r < 9; r++) {//traversal
                freq = 0;
                for (int c = 0; c < 9 && freq < 2; c++) {
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        freq++;
                        index_c = c;
                    }
                }
                if (freq == 1) {//catches the hidden Single. If there was no candidate, freq would be 0 and if there were many candidates , freq would be 2
                    hiddenSingleCheckSteps(puzzle, num, r, index_c, 1);
                }
            }


            int index_r = 0; //the row of the cell which is a hiddenSingle

            for (int c = 0; c < 9; c++) {//traveral
                freq = 0;
                for (int r = 0; r < 9 && freq < 2; r++) {
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        freq++;
                        index_r = r;
                    }
                }
                if (freq == 1) {//catches the hidden Single. If there was no candidate, freq would be 0 and if there were many candidates , freq would be 2
                    hiddenSingleCheckSteps(puzzle, num, index_r, c, 2);
                }
            }

            index_c = 0;
            index_r = 0;
            for (int r1 = 0; r1 <= 6; r1 += 3) {//jumps to first row position of subsquare
                for (int c1 = 0; c1 <= 6; c1 += 3) {//jumps to first column position of subsquare
                    freq = 0;
                    for (int r = r1; r < r1 + 3; r++) {//traversal (subsquare)
                        for (int c = c1; c < c1 + 3; c++) {
                            if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                                freq++;
                                index_r = r;
                                index_c = c;
                            }
                        }
                    }
                    if (freq == 1) {//catches the hidden Single. If there was no candidate, freq would be 0 and if there were many candidates , freq would be 2
                        hiddenSingleCheckSteps(puzzle, num, index_r, index_c, 3);
                    }
                }
            }
        }
    }// end of hiddenSingle()

    /**
     * add changes made by  hidden single to the check steps linked lists
     */
    private static void hiddenSingleCheckSteps(Cell[][] puzzle, int num, int row, int col, int house) {

        String end_tag = "";

        switch (house) {
            case 1://hidden pair in row
                end_tag = " in Row " + (row + 1);
                break;

            case 2://hidden pair in column
                end_tag = " in Column " + (col + 1);
                break;

            case 3://hidden pair in subsquare
                end_tag = " in Block";
                break;
        }

        algorithm.add("Hidden Single" + end_tag);
        insert.add("Insert Candidate " + num + " at " + "(" + (row + 1) + "," + (col + 1) + ")");
        remove.add(placeNumber(puzzle, row, col, num));
    }// end of hiddenSingleCheckSteps()


    /**
     * two cells,in the same house, that have only the same two candidates left,
     * nakedPair can eliminate that two candidates from all other cells in that house.
     */
    private static void nakedPair(Cell[][] puzzle) {

        /* find naked pairs in each row */
        for (int r = 0; r < 9; r++) {

            boolean pairNotFound = true;//to find if a pair has been found or not
            int[] skip_columns = new int[2];// to find out which columns to skip over when removing candidates ( the naked pairs columns )
            int[] the_pair = new int[2];// to store the pair of values that are common to the naked pair
            String removeFirst = "";//first naked pair candidate string
            String removeSecond = "";//second naked pair candidate string
            boolean chkremoveFirst, chkremoveSecond;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = false;

            for (int c = 1; c < 9; c++) {// each column in row

                if (pairNotFound) {//if a naked pair has not been found

                        /* a loop running from the previous cell in the row till the start of that row (column 0).
                            Checks if the current cell (at [r,c]) has 2 candidates and is not set first.*/
                    for (int rev_c = (c - 1); rev_c >= 0 && puzzle[r][c].getNoOfCandidates() == 2 && puzzle[r][c].isNotSet(); rev_c--) {

                            /* if any cell before [r,c] in the same row has 2 candidates and is not set, check
                                   if that cell has the same candidates as the cell at [r,c]*/
                        if (puzzle[r][rev_c].getNoOfCandidates() == 2 && puzzle[r][rev_c].isNotSet()) {
                            if (puzzle[r][rev_c].sameCandidates(puzzle[r][c])) {

                                pairNotFound = false;//if so, pair has been found
                                skip_columns[0] = rev_c;//store the column of the first of the naked pair
                                skip_columns[1] = c;// store the column of the second of the naked pair
                                the_pair[0] = puzzle[r][c].getCandidate(0);//the first candidate common to the naked pair
                                removeFirst = "Remove Candidate " + the_pair[0] + " at ";
                                the_pair[1] = puzzle[r][c].getCandidate(1);//the second candidate common to the naked pair
                                removeSecond = "Remove Candidate " + the_pair[1] + " at ";
                                break; // exit reverse loop since naked pair is found.

                            }
                        }
                    }
                        /* IF a pair has been found, we have to remove candidates common to the naked pair
                         from all columns of that specific row EXCEPT from the naked pair itself.
                          assign -1 to columns to restart the loop */
                    if (!pairNotFound) {
                        c = -1;
                    }
                }

                /* this segment will only run if a naked pair has been located in the row */
                else {
                    if (c != skip_columns[0] && c != skip_columns[1] && puzzle[r][c].isNotSet()) {//current cell is not set and not one of the naked pairs
                        ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], the_pair);
                        if (removeList.contains(the_pair[0])) {
                            chkremoveFirst = true;
                            removeFirst += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                        if (removeList.contains(the_pair[1])) {
                            chkremoveSecond = true;
                            removeSecond += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                    }
                }
            }

            nakedPairCheckSteps(chkremoveFirst, chkremoveSecond, removeFirst, removeSecond, the_pair, r, skip_columns[0], r, skip_columns[1], 1);

        }

        /* find naked pairs in each column */
        for (int c = 0; c < 9; c++) {

            boolean pairNotFound = true;//to find if a pair has been found or not
            int[] skip_rows = new int[2];// to find out which rows to skip over when removing candidates ( the naked pairs rows )
            int[] the_pair = new int[2];// to store the pair of values that are common to the naked pair
            String removeFirst = "";//first naked pair candidate string
            String removeSecond = "";//second naked pair candidate string
            boolean chkremoveFirst, chkremoveSecond;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = false;

            for (int r = 1; r < 9; r++) {// each row in column

                if (pairNotFound) {//if a naked pair has not been found

                        /* a loop running from the previous cell in the column till the start of that column (row 0).
                            Checks if the current cell (at [r,c]) has 2 candidates and is not set first.*/
                    for (int rev_r = (r - 1); rev_r >= 0 && puzzle[r][c].getNoOfCandidates() == 2 && puzzle[r][c].isNotSet(); rev_r--) {

                            /* if any cell before [r,c] in the same column has 2 candidates and is not set, check
                                   if that cell has the same candidates as the cell at [r,c]*/
                        if (puzzle[rev_r][c].getNoOfCandidates() == 2 && puzzle[rev_r][c].isNotSet()) {

                            if (puzzle[rev_r][c].sameCandidates(puzzle[r][c])) {
                                pairNotFound = false;//if so, pair has been found
                                skip_rows[0] = rev_r;//store the row of the first of the naked pair
                                skip_rows[1] = r;// store the row of the second of the naked pair
                                the_pair[0] = puzzle[r][c].getCandidate(0);//the first candidate common to the naked pair
                                removeFirst = "Remove Candidate " + the_pair[0] + " at ";
                                the_pair[1] = puzzle[r][c].getCandidate(1);//the second candidate common to the naked pair
                                removeSecond = "Remove Candidate " + the_pair[1] + " at ";
                                break;
                            }

                        }
                    }
                        /* IF a pair has been found, we have to remove candidates common to the naked pair
                         from all rows of that specific column EXCEPT from the naked pair itself.
                          assign -1 to columns to restart the loop */
                    if (!pairNotFound) {
                        r = -1;
                    }
                } else {

                    if (r != skip_rows[0] && r != skip_rows[1] && puzzle[r][c].isNotSet()) {//current cell is not set and not one of the naked pairs
                        ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], the_pair);
                        if (removeList.contains(the_pair[0])) {
                            chkremoveFirst = true;
                            removeFirst += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                        if (removeList.contains(the_pair[1])) {
                            chkremoveSecond = true;
                            removeSecond += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                    }

                }
            }

            nakedPairCheckSteps(chkremoveFirst, chkremoveSecond, removeFirst, removeSecond, the_pair, skip_rows[0], c, skip_rows[1], c, 2);

        }

        /* find naked pairs in a subsquare */
        for (int r1 = 0; r1 <= 6; r1 += 3) {//travers subsquare
            for (int c1 = 0; c1 <= 6; c1 += 3) {

                boolean pairNotFound = true;//to find if a pair has been found or not
                int[][] skip_index = new int[2][2];// to find out which indexes to skip over when removing candidates ( the naked pairs of the subsquare )
                int[] the_pair = new int[2];// to store the pair of values that are common to the naked pair
                String removeFirst = "";//first naked pair candidate string
                String removeSecond = "";//second naked pair candidate string
                boolean chkremoveFirst, chkremoveSecond;//to check if either candidate was removed anywhere in puzzle
                chkremoveFirst = chkremoveSecond = false;

                for (int r = r1; r < r1 + 3; r++) {
                    for (int c = (c1 + 1); c < c1 + 3; c++) {

                        if (pairNotFound) {

                                /* a loop running from the previous cell in the column till the start of that subsquare [r1,c1]
                                 Checks if the current cell (at [r,c]) has 2 candidates and is not set first. */
                            for (int rev_r = r; rev_r >= r1 && puzzle[r][c].getNoOfCandidates() == 2 && puzzle[r][c].isNotSet(); rev_r--) {
                                for (int rev_c = (c - 1); rev_c >= c1; rev_c--) {

                                        /* if any cell before [r,c] in the same subsquare has 2 candidates and is not set, check
                                   if that cell has the same candidates as the cell at [r,c] */
                                    if (puzzle[rev_r][rev_c].sameCandidates(puzzle[r][c])) {

                                        pairNotFound = false;//if so, pair has been found
                                        skip_index[0][0] = rev_r;//store the row of the first of the naked pair
                                        skip_index[0][1] = rev_c;//store the column of the first of the naked pair
                                        skip_index[1][0] = r;//store the row of the second of the naked pair
                                        skip_index[1][1] = c;//store the column of the second of the naked pair
                                        the_pair[0] = puzzle[r][c].getCandidate(0);//the first candidate common to the naked pair
                                        removeFirst = "Remove Candidate " + the_pair[0] + " at ";
                                        the_pair[1] = puzzle[r][c].getCandidate(1); //the first candidate common to the naked pair
                                        removeSecond = "Remove Candidate " + the_pair[1] + " at ";
                                        break;

                                    }
                                }
                            }

                                /* IF a pair has been found, we have to remove candidates common to the naked pair
                         from all cells in the subsquare EXCEPT from the naked pair itself.
                          assign r1 and c1-1 to columns to restart the loop */
                            if (!pairNotFound) {
                                r = r1;
                                c = (c1 - 1);
                            }
                        }

                        /* this segment will only run if a naked pair has been located in the column */
                        else {

                            if (!(r == skip_index[0][0] && c == skip_index[0][1]) && !(r == skip_index[1][0] && c == skip_index[1][1]) && puzzle[r][c].isNotSet()) {//current cell is not set and not one of the naked pairs
                                ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], the_pair);
                                if (removeList.contains(the_pair[0])) {
                                    chkremoveFirst = true;
                                    removeFirst += "(" + (r + 1) + "," + (c + 1) + ")";
                                }
                                if (removeList.contains(the_pair[1])) {
                                    chkremoveSecond = true;
                                    removeSecond += "(" + (r + 1) + "," + (c + 1) + ")";
                                }
                            }
                        }
                    }
                }

                nakedPairCheckSteps(chkremoveFirst, chkremoveSecond, removeFirst, removeSecond, the_pair, skip_index[0][0], skip_index[0][1],
                        skip_index[1][0], skip_index[1][1], 3);

            }
        }
    }//end of nakedPair()


    /**
     * eliminate candidate(s) from current cell if it is present
     */
    private static ArrayList<Integer> eliminateFromCell(Cell cell, int[] toBeEliminated) {
        ArrayList<Integer> removedCandidates = new ArrayList<>();

        for (int i = 0; i < toBeEliminated.length; i++) {
            if (cell.isCandidate(toBeEliminated[i])) {
                changeCounter++;
                cell.removeCandidate(toBeEliminated[i]);
                removedCandidates.add(toBeEliminated[i]);
            }
        }

        return removedCandidates;
    }// end of elminiateFromCell()

    /**
     * add changes made by naked pair to the check steps linked lists
     */
    private static void nakedPairCheckSteps(boolean chkremoveFirst, boolean chkremoveSecond, String removeFirst, String removeSecond,
                                            int[] the_pair, int row_1, int column_1, int row_2, int column_2, int house) {

        String end_tag = "";

        switch (house) {
            case 1://naked pair in row
                end_tag = " in Row " + (row_1 + 1);
                break;

            case 2://naked pair in column
                end_tag = " in Column " + (column_1 + 1);
                break;

            case 3://naked pair in subsquare
                end_tag = " in Block";
                break;
        }

        if (chkremoveFirst || chkremoveSecond) {//naked pair changed the puzzle in some way
            algorithm.add("Naked Pair" + end_tag);
            insert.add("Candidates: " + the_pair[0] + "," + the_pair[1] + " are common to cells " + "(" + (row_1 + 1) + "," + (column_1 + 1) + ")"
                    + " and " + "(" + (row_2 + 1) + "," + (column_2 + 1) + ")");
            if (!chkremoveFirst)//no removal of candidate 1
                removeFirst = "";
            if (!chkremoveSecond)//no removal of candidate 2
                removeSecond = "";
            remove.add((removeFirst + " " + removeSecond).trim());
        }

    }// end of nakedPairCheckSteps()    


    /**
     * If in a block all candidates of a certain digit are confined to a row or column, that digit cannot
     * appear outside of that block in that row or column.
     */
    private static void pointingPair(Cell[][] puzzle) {
        // checking for pointing pairs in rows
        int freq;
        for (int num = 1; num < 10; num++) {// num is the candidate we are checking pointing pairs for

            for (int sqr_r = 0; sqr_r < 9; sqr_r += 3) {// nested loop to go to the top left cell of each subsquare
                for (int sqr_c = 0; sqr_c < 9; sqr_c += 3) {

                    for (int r = sqr_r; r < sqr_r + 3; r++) {//nested loop to run through the subsquare
                        freq = 0; // stores frequency of a candidate in a column
                        String[] insert_candidates = new String[3];//the coordinates of pointing pairs (size 3 to avoid out of bounds in case of pointing triple
                        for (int c = sqr_c; c < sqr_c + 3; c++) {

                            if (puzzle[r][c].isNotSet() && puzzle[r][c].isCandidate(num)) {// checking if num is a candidate in the cell
                                insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                                freq++;
                                if (freq == 2) { // if there are 2 cells in the row with num as a candidate
                                    if (!existsInOtherRowsInSubsquare(puzzle, num, r, c)) {// checking if any other row in the subsquare has num as a candidate
                                        pointingPairCheckSteps(puzzle, insert_candidates, num, r, c, 1);
                                    }
                                }
                            }

                        } // c
                    } // r
                } // sqr_c
            } // sqr_r

            //checking for pointing pairs in columns
            for (int sqr_r = 0; sqr_r < 9; sqr_r += 3) {// nested loop to go to the top left cell of each subsquare
                for (int sqr_c = 0; sqr_c < 9; sqr_c += 3) {

                    for (int c = sqr_c; c < sqr_c + 3; c++) {//nested loop to run through the subsquare
                        freq = 0; // stores frequency of a candidate in a column
                        String[] insert_candidates = new String[3];//the coordinates of pointing pairs (size 3 to avoid out of bounds in case of pointing triple
                        for (int r = sqr_r; r < sqr_r + 3; r++) {

                            if (puzzle[r][c].isNotSet() && puzzle[r][c].isCandidate(num)) {// checking if num is a candidate in the cell
                                insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                                freq++;
                                if (freq == 2) { // if there are 2 cells in the column with num as a candidate
                                    if (!existsInOtherColumnsInSubsquare(puzzle, num, r, c)) {// checking if any other column in the subsquare has num as a candidate
                                        pointingPairCheckSteps(puzzle, insert_candidates, num, r, c, 2);
                                    }
                                }
                            }

                        } // c
                    } // r
                } // sqr_c
            } // sqr_r
        } // num
    }// end of pointingPair()

    /**
     * Checks if the candidate is present in other columns of the subsquare
     */
    private static boolean existsInOtherColumnsInSubsquare(Cell[][] puzzle, int num, int current_row, int current_col) {
        int tmp_r = (current_row - (current_row % 3));// starting row of current subsquare
        int tmp_c = (current_col - (current_col % 3));// starting column of current subsquare
        for (int r = tmp_r; r < tmp_r + 3; r++) {
            for (int c = tmp_c; c < tmp_c + 3; c++) {
                if (c != current_col && puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {// avoid the current column and check if num is a candidate in the cell
                    return true;
                }
            }
        }
        return false;
    }// end of checkOtherColumnsInSubsquareFor()

    /**
     * Checks if the candidate is present in other rows of the subsquare
     */
    private static boolean existsInOtherRowsInSubsquare(Cell[][] puzzle, int num, int current_row, int current_col) {
        int tmp_r = (current_row - (current_row % 3));// starting row of current subsquare
        int tmp_c = (current_col - (current_col % 3));// starting column of current subsquare
        for (int r = tmp_r; r < tmp_r + 3; r++) {
            for (int c = tmp_c; c < tmp_c + 3; c++) {
                if (r != current_row && puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {// avoid the current row and check if num is a candidate in the cell
                    return true;
                }
            }
        }
        return false;
    }// end of checkOtherRowsInSubsquareFor()

    /**
     * Remove all non pointing pairs from column
     */
    private static String removeFromColumn(Cell[][] puzzle, int num, int current_row, int current_col) {
        String remove_str = "Remove Candidate " + num + " at ";
        int tmp_r = (current_row - (current_row % 3));// starting row of current subsquare
        for (int r = 0; r < 9; r++) {
            if ((r < tmp_r || r >= tmp_r + 3) && puzzle[r][current_col].isCandidate(num) && puzzle[r][current_col].isNotSet()) {// if statment to avoid removing candidates from the current subsquare
                puzzle[r][current_col].removeCandidate(num);
                remove_str += "(" + (r + 1) + "," + (current_col + 1) + ")";
                changeCounter++;
            }
        }
        return remove_str; //return the coordinates of removed candidate
    }// end of removeFromColumn()

    /**
     * Remove all non pointing pairs from row
     */
    private static String removeFromRow(Cell[][] puzzle, int num, int current_row, int current_col) {
        String remove_str = "Remove Candidate " + num + " at ";
        int tmp_c = (current_col - (current_col % 3));// starting column of current subsquare
        for (int c = 0; c < 9; c++) {
            if ((c < tmp_c || c >= tmp_c + 3) && puzzle[current_row][c].isCandidate(num) && puzzle[current_row][c].isNotSet()) {// if statment to avoid removing candidates from the current subsquare
                puzzle[current_row][c].removeCandidate(num);
                remove_str += "(" + (current_row + 1) + "," + (c + 1) + ")";
                changeCounter++;
            }
        }
        return remove_str; //return the coordinates of removed candidate
    }// end of removeFromRow()

    /**
     * add changes made by pointing pair to the check steps linked lists
     */
    private static void pointingPairCheckSteps(Cell[][] puzzle, String[] insert_candidates, int num, int row, int col, int house) {

        int tmp = changeCounter;
        String remove_str = "";

        switch (house) {
            case 1://pointing pair in row
                remove_str = removeFromRow(puzzle, num, row, col);
                break;

            case 2://pointing pair in column
                remove_str = removeFromColumn(puzzle, num, row, col);
                break;
        }

        if (tmp != changeCounter) {//if tmp != changeCounter, it means that pointing pair has indeed removed candidates somewhere and needs to be added to check steps
            if (house == 1)
                algorithm.add("Pointing Pair in Row " + (row + 1));
            else
                algorithm.add("Pointing Pair in Column " + (col + 1));
            insert.add("Candidate " + num + " is common to cells " + insert_candidates[0] + " " + insert_candidates[1]);
            remove.add(remove_str);
        }

    }// end of pointingPairCheckSteps

    /**
     * If in a row or column all candidates of a certain digit are confined to one block,
     * that candidate that be eliminated from all other cells in that block.
     */
    private static void claimingPair(Cell[][] puzzle) {

        for (int num = 1; num < 10; num++) {//checking all possible candidates

            for (int r = 0; r < 9; r++) {//check each row
                for (int sub_c = 3; sub_c <= 9; sub_c += 3) {//points to the end of each subsquare in the particular row
                    int freq = 0;
                    String[] insert_candidates = new String[3];//the coordinates of claiming pairs (size 3 to avoid out of bounds in case of pointing triple)
                    for (int c = (sub_c - 3); c < sub_c; c++) {//from beginning of subsquare at the row till its end (sub_c)
                        if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {
                            insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                            freq++;
                            if (freq == 2 && notInOtherCellsOfRow(puzzle, sub_c, r, num)) {//number of cells in the row and subsquare is 2. remove all 'num' candidates within the subsquare that are not part of the row
                                claimingPairCheckSteps(puzzle, insert_candidates, num, r, (sub_c - 3), 1);
                            }

                        }

                    }
                }
            }

            for (int c = 0; c < 9; c++) {//check each column
                for (int sub_r = 3; sub_r <= 9; sub_r += 3) {
                    int freq = 0;
                    String[] insert_candidates = new String[3];
                    for (int r = (sub_r - 3); r < sub_r; r++) {//from beginning of subsquare at the column till its end (sub_r)
                        if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {
                            insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                            freq++;
                            if (freq == 2 && notInOtherCellsOfColumn(puzzle, sub_r, c, num)) {
                                claimingPairCheckSteps(puzzle, insert_candidates, num, c, (sub_r - 3), 2);
                            }
                        }
                    }
                }
            }
        }

    }// end of claimingPair()

    /**
     * Checks if there are any other cells in the row other than subsquare that have the candidate
     */
    private static boolean notInOtherCellsOfRow(Cell[][] puzzle, int sub_c, int r, int num) {

        for (int c = 0; c < 9; c++) {
            if (c < sub_c - 3 || c >= sub_c) {//is not part of the current subsquare
                if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {
                    return false;
                }
            }
        }
        return true;
    }// end of notInOtherCellsOfRow

    /**
     * Checks if there are any other cells in the column other than subsquare that have the candidate
     */
    private static boolean notInOtherCellsOfColumn(Cell[][] puzzle, int sub_r, int c, int num) {
        for (int r = 0; r < 9; r++) {
            if (r < sub_r - 3 || r >= sub_r) {//is not part of the current subsquare
                if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {
                    return false;
                }
            }
        }
        return true;
    }// end of notInOtherCellsOfColumn()

    /**
     * removes the num candidate from all cells in a subsquare as long as it is not part of a specific row or column,
     * which is determined by int i.
     */
    private static String removeFromSubsquare(Cell[][] puzzle, int current, int tmp_start, int num, int i) {

        String remove_str = "Remove Candidate " + num + " at ";
        int tmp_r, tmp_c;// storing the index beginning of the subsquare required

        if (i == 1) {// if i is 1 , the claimingPair is being checked row-wise
            tmp_r = (current - current % 3);//finds first row of the subsquare to which the current row belongs
            tmp_c = tmp_start;//tmp_start already contains the index of first column of subsquare
        } else {// if i is 2 , the claimingPair is being checked column-wise
            tmp_r = tmp_start;//tmp_start already contains the index of first row of subsquare
            tmp_c = (current - current % 3);//finds first column of the subsquare to which the current row belongs
        }

        for (int r = tmp_r; r < tmp_r + 3; r++) {
            if (!(i == 1 && current == r)) {//if claimPair is being checked row-wise and the r is the current_row, we should not remove any candidates
                for (int c = tmp_c; c < tmp_c + 3; c++) {
                    if (!(i == 2 && current == c)) {//if claimPair is being checked column-wise and the c is the current column, we should not remove any candidates
                        if (puzzle[r][c].isNotSet() && puzzle[r][c].isCandidate(num)) {
                            remove_str += "(" + (r + 1) + "," + (c + 1) + ")";
                            puzzle[r][c].removeCandidate(num);
                            changeCounter++;
                        }
                    }
                }
            }
        }

        return remove_str;

    }// end of removeFromSubsquare()

    /**
     * add changes made by claiming pair to the check steps linked lists
     */
    private static void claimingPairCheckSteps(Cell[][] puzzle, String[] insert_candidates, int num, int current, int tmp_start, int house) {

        int tmp = changeCounter;
        String remove_str = "";

        switch (house) {
            case 1://claiming pair in row
                remove_str = removeFromSubsquare(puzzle, current, tmp_start, num, 1);
                break;

            case 2://claiming pair in column
                remove_str = removeFromSubsquare(puzzle, current, tmp_start, num, 2);
                break;
        }

        if (tmp != changeCounter) {//if tmp is not equal to changeCounter, it means that pointing pair has indeed removed candidates somewhere and needs  to be added to check steps
            if (house == 1)
                algorithm.add("Claiming Pair in Row " + (current + 1));
            else
                algorithm.add("Claiming Pair in Column " + (current + 1));
            insert.add("Candidate " + num + " is common to cells " + insert_candidates[0] + " " + insert_candidates[1]);
            remove.add(remove_str);
        }

    }//end of claimingPairCheckSteps()

    /**
     * when two cells in a house each contain two candidates which appear nowhere outside those two cells in that house, those two candidates must be placed in the two cells.
     * All other candidates can therefore be eliminated.
     */
    private static void hiddenPair(Cell[][] puzzle) {
        //checking for hidden pairs in rows first
        int[] pair = new int[2];//array to store the values of the pair
        for (int r = 0; r < 9; r++) {
            ArrayList<Integer> eligible_columns = new ArrayList<>();//columns in the current row that could possibly contain a hidden pairs
            for (int c = 0; c < 9; c++) { //loop to find eligible cells in this row
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_columns.add(c);
            }//c

            if (eligible_columns.size() >= 2) {
                for (int i = 0; i < eligible_columns.size() - 1; i++) {//loop to go through combinations of the eligible cells in the rows to find valid hidden pairs
                    for (int j = (i + 1); j < eligible_columns.size(); j++) {
                        int index_1 = eligible_columns.get(i);
                        int index_2 = eligible_columns.get(j);
                        ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[r][index_1], puzzle[r][index_2]);//store the similar candidates between chosen candidates
                        if (similarCandidates.size() >= 2) {//checking if there are exactly 2 similar candidates
                            for (int outer = 0; outer < similarCandidates.size() - 1; outer++) {
                                for (int inner = (outer + 1); inner < similarCandidates.size(); inner++) {//choose any 2 of the similar candidates
                                    pair[0] = similarCandidates.get(outer);
                                    pair[1] = similarCandidates.get(inner);
                                    if (pairDoesNotExistInOtherCellsInRow(puzzle, r, pair, index_1, index_2))//checking if any other cells in the same row also have pair[0] or pair[1] as candidates
                                        hiddenPairCheckSteps(puzzle, r, index_1, r, index_2, pair, 1);
                                }
                            }

                        }//if statement
                    }//index2
                }//index1
            }//if
        }//r

        //checking for hidden pairs in columns
        for (int c = 0; c < 9; c++) {
            ArrayList<Integer> eligible_rows = new ArrayList<>();//rows in the current column that could possibly contain a hidden pairs
            for (int r = 0; r < 9; r++) { //loop to find eligible cells in this column
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_rows.add(r);
            }//r

            if (eligible_rows.size() >= 2) {
                for (int i = 0; i < eligible_rows.size() - 1; i++) {//loop to go through combinations of the eligible cells in the columns to find valid hidden pairs
                    for (int j = (i + 1); j < eligible_rows.size(); j++) {
                        int index_1 = eligible_rows.get(i);
                        int index_2 = eligible_rows.get(j);
                        ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[index_1][c], puzzle[index_2][c]);//store the similar candidates between chosen candidates
                        if (similarCandidates.size() >= 2) {//checking if there are more than or equal to 2 similar candidates
                            for (int outer = 0; outer < similarCandidates.size() - 1; outer++) {
                                for (int inner = (outer + 1); inner < similarCandidates.size(); inner++) {//choose any 2 of the similar candidates
                                    pair[0] = similarCandidates.get(outer);
                                    pair[1] = similarCandidates.get(inner);
                                    if (pairDoesNotExistInOtherCellsInColumn(puzzle, c, pair, index_1, index_2))//checking if any other cells in the same column also have pair[0] or pair[1] as candidates
                                        hiddenPairCheckSteps(puzzle, index_1, c, index_2, c, pair, 2);
                                }
                            }
                        }//if statement
                    }//index2
                }//index1
            }//if
        }//c


        for (int r1 = 0; r1 <= 6; r1 += 3) {//jumps to first row position of subsquare
            for (int c1 = 0; c1 <= 6; c1 += 3) {//jumps to first column position of subsquare
                ArrayList<Integer> eligible_rows = new ArrayList<>();//rows  that could possibly contain a hidden pairs
                ArrayList<Integer> eligible_columns = new ArrayList<>();//columns that could possibly contain hidden pairs
                for (int r = r1; r < r1 + 3; r++) {//traversal (subsquare)
                    for (int c = c1; c < c1 + 3; c++) {
                        if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2) {//if cell has 2 or more candidates, it is eligible
                            eligible_rows.add(r);
                            eligible_columns.add(c);
                        }
                    }
                }

                int eligibleCellsSize = eligible_columns.size();
                if (eligibleCellsSize >= 2) {
                    for (int i = 0; i < eligibleCellsSize - 1; i++) {//loop to go through combinations of the eligible cells in the subquare to find valid hidden pairs
                        for (int j = (i + 1); j < eligibleCellsSize; j++) {
                            int index_1_r = eligible_rows.get(i);
                            int index_1_c = eligible_columns.get(i);
                            int index_2_r = eligible_rows.get(j);
                            int index_2_c = eligible_columns.get(j);
                            ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[index_1_r][index_1_c], puzzle[index_2_r][index_2_c]);//store the similar candidates between chosen candidates
                            if (similarCandidates.size() >= 2) {//checking if there are more than or equal to 2 similar candidates
                                for (int outer = 0; outer < similarCandidates.size() - 1; outer++) {
                                    for (int inner = (outer + 1); inner < similarCandidates.size(); inner++) {//choose any 2 of the similar candidates
                                        pair[0] = similarCandidates.get(outer);
                                        pair[1] = similarCandidates.get(inner);
                                        if (pairDoesNotExistInOtherCellsInSubsquare(puzzle, r1, c1, pair, index_1_r, index_1_c, index_2_r, index_2_c))//checking if any other cells in the same subsquare also have pair[0] or pair[1] as candidates
                                            hiddenPairCheckSteps(puzzle, index_1_r, index_1_c, index_2_r, index_2_c, pair, 3);
                                    }
                                }
                            }//if statement
                        }//inner for loop
                    }//outer for loop
                }//greater if statement
            }
        }
    }// end of hiddenPair()


    private static ArrayList<Integer> getSimilarCandidates(Cell cell_1, Cell cell_2) {
        ArrayList<Integer> similar_candidates = new ArrayList<>();//stores the similar candidates
        for (int num = 1; num < 10; num++) {
            if (cell_1.isCandidate(num) && cell_2.isCandidate(num)) {
                similar_candidates.add(num);//adding similar candidates to array
            }// if statement
        }
        return similar_candidates;
    }// end of getSimilarCandidates()


    private static String[] removeNonPairCandidates(Cell cell_1, Cell cell_2, int[] pair) {

        String[] removePair = new String[2];
        removePair[0] = removePair[1] = "Remove Candidates ";
        boolean tmp_0, tmp_1;
        tmp_0 = tmp_1 = true;
        for (int num = 1; num < 10; num++) {
            if (num != pair[0] && num != pair[1]) {
                if (cell_1.isCandidate(num)) {
                    cell_1.removeCandidate(num);
                    removePair[0] += num + ",";
                    tmp_0 = false;
                    changeCounter++;
                }
                if (cell_2.isCandidate(num)) {
                    cell_2.removeCandidate(num);
                    removePair[1] += num + ",";
                    tmp_1 = false;
                    changeCounter++;
                }
            }
        }

        if (tmp_0)
            removePair[0] = "";
        else
            removePair[0] = removePair[0].substring(0, removePair[0].length() - 1) + " from ";

        if (tmp_1)
            removePair[1] = "";
        else
            removePair[1] = removePair[1].substring(0, removePair[1].length() - 1) + " from ";


        return removePair;
    }// end of removeNonPairCandidates()


    private static boolean pairDoesNotExistInOtherCellsInRow(Cell[][] puzzle, int row, int[] pair, int index1, int index2) {
        for (int c = 0; c < 9; c++) {
            if ((puzzle[row][c].isCandidate(pair[0]) || puzzle[row][c].isCandidate(pair[1])) && (c != index1 && c != index2) && puzzle[row][c].isNotSet()) {
                return false;
            }
        }
        return true;
    }// end of pairDoesNotExistInOtherCellsInRow()

    private static boolean pairDoesNotExistInOtherCellsInColumn(Cell[][] puzzle, int col, int[] pair, int index1, int index2) {
        for (int r = 0; r < 9; r++) {
            if ((puzzle[r][col].isCandidate(pair[0]) || puzzle[r][col].isCandidate(pair[1])) && (r != index1 && r != index2) && puzzle[r][col].isNotSet()) {
                return false;
            }
        }
        return true;
    }// end of pairDoesNotExistInOtherCellsInColumn()

    private static boolean pairDoesNotExistInOtherCellsInSubsquare(Cell[][] puzzle, int r1, int c1, int[] pair, int index_1_r, int index_1_c, int index_2_r, int index_2_c) {
        for (int r = r1; r < r1 + 3; r++) {//traversal (subsquare)
            for (int c = c1; c < c1 + 3; c++) {
                if ((puzzle[r][c].isCandidate(pair[0]) || puzzle[r][c].isCandidate(pair[1])) && (!(r == index_1_r && c == index_1_c) && !(r == index_2_r && c == index_2_c)) && puzzle[r][c].isNotSet()) {
                    return false;
                }
            }
        }
        return true;
    }//end of pairDoesNotExistInOtherCellsInSubsquare

    /**
     * adds changes made by hidden pair to the check steps linked lists
     */
    private static void hiddenPairCheckSteps(Cell[][] puzzle, int row_1, int col_1, int row_2, int col_2, int[] pair, int house) {

        int tmp = changeCounter;
        String end_tag = "";

        switch (house) {
            case 1://hidden pair in row
                end_tag = " in Row " + (row_1 + 1);
                break;

            case 2://hidden pair in column
                end_tag = " in Column " + (col_1 + 1);
                break;

            case 3://hidden pair in subsquare
                end_tag = " in Block";
                break;
        }

        String[] removeSet = removeNonPairCandidates(puzzle[row_1][col_1], puzzle[row_2][col_2], pair);//contains the remove string for each cell
        if (tmp != changeCounter) {//if tmp is != changeCounter, there has been a change caused by hiddenPair()
            algorithm.add("Hidden Pair" + end_tag);
            insert.add("Candidates: " + pair[0] + "," + pair[1] + " are common to cells " + "(" + (row_1 + 1) + "," + (col_1 + 1) + ")"
                    + " and " + "(" + (row_2 + 1) + "," + (col_2 + 1) + ")");
            String cell_1 = removeSet[0].length() == 0 ? "" : "(" + (row_1 + 1) + "," + (col_1 + 1) + ")";//if we don't have to remove any candidates, don't print
            String cell_2 = removeSet[1].length() == 0 ? "" : "(" + (row_2 + 1) + "," + (col_2 + 1) + ")";//if we don't have to remove any candidates, don't print
            remove.add((removeSet[0] + cell_1 + " " + removeSet[1] + cell_2).trim());
        }

    }// end of hiddenPairCheckSteps()

    /**
     * if three cells, all in the same house,have only the same three candidates left,
     * eliminate that candidates from all other cells in that house.
     */

    private static void nakedTriple(Cell[][] puzzle) {

        for (int r = 0; r < 9; r++) {//find nakedTriples in each row
            ArrayList<Integer> eligible_columns = new ArrayList<>();//an arraylist containing all columns in a row that are eligible  candidates of nakedTriple
            for (int c = 0; c < 9; c++) {
                if (checkNakedTripleEligible(puzzle[r][c])) {//checks if eligible
                    eligible_columns.add(c);//if so, add it to the arrayList
                }
            }

            int[] skip_columns = new int[3];//stores the columns of nakedTriple (if existing) , to be skipped.
            int[] the_triplet = new int[3];//stores the candidates tn=hat are common to the nakedTriplet of existing.
            boolean triplet_not_found = true;//checks if a triplet has been found
            String removeFirst = "";//first naked triple candidate string
            String removeSecond = "";//second naked triple candidate string
            String removeThird = ""; //third naked triple candidiate string
            boolean chkremoveFirst, chkremoveSecond, chkremoveThird;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = chkremoveThird = false;

            if (eligible_columns.size() >= 3) {//if the number of eligible columns is less than 3, a naked triple cannot exist.

                for (int first = 0; first < eligible_columns.size() && triplet_not_found; first++) {//loop generating the first cell that 'could' be a naked triple

                    ArrayList<Integer> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of three cells whose columns were eligible
                    addIfAbsent(candidateList, puzzle[r][eligible_columns.get(first)]);

                    for (int second = 0; second < eligible_columns.size() && triplet_not_found; second++) {//loop generating the second cell that 'could' be a naked triple

                        if (first != second) {//make sure that the same cell is not the first cell as well , to create valid permutations

                            int countSecondsCandidates = addIfAbsent(candidateList, puzzle[r][eligible_columns.get(second)]); // to count the candidates of second cell that are added to candidateList

                            for (int third = 0; third < eligible_columns.size() && triplet_not_found; third++) {//loop generating the third cell that 'could' be a naked triple
                                if (first != third && second != third) {//make sure that the same cell is not either first or second cell, to create valid permutations

                                    int countThirdsCandidates = addIfAbsent(candidateList, puzzle[r][eligible_columns.get(third)]);

                                    if (candidateList.size() == 3) {//if the list of unique candidates amongst the first, second and third cells  is 3, they must be a naked triple.
                                        /*Store the columns and triplets to be skipped.*/
                                        skip_columns[0] = eligible_columns.get(first);
                                        skip_columns[1] = eligible_columns.get(second);
                                        skip_columns[2] = eligible_columns.get(third);
                                        the_triplet[0] = candidateList.get(0);
                                        removeFirst = "Remove Candidate " + the_triplet[0] + " at ";
                                        the_triplet[1] = candidateList.get(1);
                                        removeSecond = "Remove Candidate " + the_triplet[1] + " at ";
                                        the_triplet[2] = candidateList.get(2);
                                        removeThird = "Remove Candidate " + the_triplet[2] + " at ";
                                        triplet_not_found = false;//stop searching for combinations, a triplet has been found.
                                    }

                                    removeRecentAdditions(candidateList, countThirdsCandidates);//make space for addition of a new third cell
                                }
                            }

                            removeRecentAdditions(candidateList, countSecondsCandidates);//make space for addition of a new second cell
                        }
                    }
                }
            }

            if (!triplet_not_found) {//if the triplet has ben found, remove candidates from that row that aren't naked triplets or set.
                for (int c = 0; c < 9; c++) {
                    if (c != skip_columns[0] && c != skip_columns[1] && c != skip_columns[2] && puzzle[r][c].isNotSet()) {
                        ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], the_triplet);//gets the candidates removed from the particular cell
                        if (removeList.contains(the_triplet[0])) {
                            chkremoveFirst = true;
                            removeFirst += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                        if (removeList.contains(the_triplet[1])) {
                            chkremoveSecond = true;
                            removeSecond += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                        if (removeList.contains(the_triplet[2])) {
                            chkremoveThird = true;
                            removeThird += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                    }
                }
            }

            nakedTripleCheckSteps(chkremoveFirst, chkremoveSecond, chkremoveThird, removeFirst, removeSecond, removeThird, the_triplet
                    , r, skip_columns[0], r, skip_columns[1], r, skip_columns[2], 1);

        }

        for (int c = 0; c < 9; c++) {//find nakedTriples in each column
            ArrayList<Integer> eligible_rows = new ArrayList<>();//an arraylist containing all rows in a column that are eligible  candidates of nakedTriple
            for (int r = 0; r < 9; r++) {
                if (checkNakedTripleEligible(puzzle[r][c])) {//checks if eligible
                    eligible_rows.add(r);//if so, add it to the arrayList
                }
            }

            int[] skip_rows = new int[3];//stores the rows of nakedTriple (if existing) , to be skipped.
            int[] the_triplet = new int[3];//stores the candidates tn=hat are common to the nakedTriplet of existing.
            boolean triplet_not_found = true;//checks if a triplet has been found
            String removeFirst = "";//first naked triple candidate string
            String removeSecond = "";//second naked triple candidate string
            String removeThird = ""; //third naked triple candidiate string
            boolean chkremoveFirst, chkremoveSecond, chkremoveThird;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = chkremoveThird = false;

            if (eligible_rows.size() >= 3) {//if the number of eligible columns is less than 3, a naked triple cannot exist.

                for (int first = 0; first < eligible_rows.size() && triplet_not_found; first++) {//loop generating the first cell that 'could' be a naked triple

                    ArrayList<Integer> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of three cells whose columns were eligible
                    addIfAbsent(candidateList, puzzle[eligible_rows.get(first)][c]);

                    for (int second = 0; second < eligible_rows.size() && triplet_not_found; second++) {//loop generating the second cell that 'could' be a naked triple

                        if (first != second) {//make sure that the same cell is not the first cell as well , to create valid permutations
                            int countSecondsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(second)][c]); //to count the candidates of second cell that are added to candidateList

                            for (int third = 0; third < eligible_rows.size() && triplet_not_found; third++) {//loop generating the third cell that 'could' be a naked triple

                                if (first != third && second != third) {//make sure that the same cell is not either first or second cell, to create valid permutations
                                    int countThirdsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(third)][c]);//to remove the candidates of third cell if naked triple is not found

                                    if (candidateList.size() == 3) {//if the list of unique candidates amongst the first, second and third cells  is 3, they must be a naked triple.
                                        /*Store the rows to be skipped and triplets .*/
                                        skip_rows[0] = eligible_rows.get(first);
                                        skip_rows[1] = eligible_rows.get(second);
                                        skip_rows[2] = eligible_rows.get(third);
                                        the_triplet[0] = candidateList.get(0);
                                        removeFirst = "Remove Candidate " + the_triplet[0] + " at ";
                                        the_triplet[1] = candidateList.get(1);
                                        removeSecond = "Remove Candidate " + the_triplet[1] + " at ";
                                        the_triplet[2] = candidateList.get(2);
                                        removeThird = "Remove Candidate " + the_triplet[2] + " at ";
                                        triplet_not_found = false;//stop searching for combinations, a triplet has been found.
                                    }

                                    removeRecentAdditions(candidateList, countThirdsCandidates);//make space for addition of a new third cell
                                }
                            }

                            removeRecentAdditions(candidateList, countSecondsCandidates);//make space for addition of a new second cell
                        }
                    }
                }
            }

            if (!triplet_not_found) {//if the triplet has ben found, remove candidates from that column that aren't naked triplets or set.
                for (int r = 0; r < 9; r++) {
                    if (r != skip_rows[0] && r != skip_rows[1] && r != skip_rows[2] && puzzle[r][c].isNotSet()) {
                        ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], the_triplet);//gets the candidates removed from the particular cell
                        if (removeList.contains(the_triplet[0])) {
                            chkremoveFirst = true;
                            removeFirst += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                        if (removeList.contains(the_triplet[1])) {
                            chkremoveSecond = true;
                            removeSecond += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                        if (removeList.contains(the_triplet[2])) {
                            chkremoveThird = true;
                            removeThird += "(" + (r + 1) + "," + (c + 1) + ")";
                        }
                    }
                }
            }

            nakedTripleCheckSteps(chkremoveFirst, chkremoveSecond, chkremoveThird, removeFirst, removeSecond, removeThird, the_triplet, skip_rows[0]
                    , c, skip_rows[1], c, skip_rows[2], c, 2);

        }

        for (int r1 = 0; r1 <= 6; r1 += 3) {//travers subsquare
            for (int c1 = 0; c1 <= 6; c1 += 3) {

                ArrayList<Integer> eligible_columns = new ArrayList<>();//an arraylist containing all columns in a subset that are eligible  candidates of nakedTriple
                ArrayList<Integer> eligible_rows = new ArrayList<>();//an arraylist containing all rows in a subset that are eligible  candidates of nakedTriple
                for (int r = r1; r < r1 + 3; r++) {//in a particular subsquare
                    for (int c = c1; c < c1 + 3; c++) {
                        if (checkNakedTripleEligible(puzzle[r][c])) {//checks if eligible
                            eligible_columns.add(c);//if so, add column to the arrayList
                            eligible_rows.add(r); // add row to the arrayList
                        }
                    }

                }

                int[] skip_rows = new int[3];//stores the rows of nakedTriple (if existing) , to be skipped.
                int[] skip_columns = new int[3];//stores the columns of nakedTriple (if existing) , to be skipped.
                int[] the_triplet = new int[3];//stores the candidates that are common to the nakedTriplet of existing.
                boolean triplet_not_found = true;//checks if a triplet has been found
                int eligibleCellsLength = eligible_columns.size();
                String removeFirst = "";//first naked triple candidate string
                String removeSecond = "";//second naked triple candidate string
                String removeThird = ""; //third naked triple candidiate string
                boolean chkremoveFirst, chkremoveSecond, chkremoveThird;//to check if either candidate was removed anywhere in puzzle
                chkremoveFirst = chkremoveSecond = chkremoveThird = false;


                if (eligibleCellsLength >= 3) {//if the number of eligible cells in subsquare is less than 3, a naked triple cannot exist.

                    for (int first = 0; first < eligibleCellsLength && triplet_not_found; first++) {

                        ArrayList<Integer> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of three cells whose cells were eligible
                        addIfAbsent(candidateList, puzzle[eligible_rows.get(first)][eligible_columns.get(first)]);

                        for (int second = 0; second < eligibleCellsLength && triplet_not_found; second++) {//loop generating the second cell that 'could' be a naked triple

                            if (first != second) {//make sure that the same cell is not the first cell as well , to create valid permutations
                                int countSecondsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(second)][eligible_columns.get(second)]); //to count the candidates of second cell that are added to candidateList

                                for (int third = 0; third < eligibleCellsLength && triplet_not_found; third++) {//loop generating the third cell that 'could' be a naked triple
                                    if (first != third && second != third) {//make sure all cells are unique for valid permutation
                                        int countThirdsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(third)][eligible_columns.get(third)]);

                                        if (candidateList.size() == 3) {
                                            skip_rows[0] = eligible_rows.get(first);
                                            skip_columns[0] = eligible_columns.get(first);
                                            skip_rows[1] = eligible_rows.get(second);
                                            skip_columns[1] = eligible_columns.get(second);
                                            skip_rows[2] = eligible_rows.get(third);
                                            skip_columns[2] = eligible_columns.get(third);
                                            the_triplet[0] = candidateList.get(0);
                                            removeFirst = "Remove Candidate " + the_triplet[0] + " at ";
                                            the_triplet[1] = candidateList.get(1);
                                            removeSecond = "Remove Candidate " + the_triplet[1] + " at ";
                                            the_triplet[2] = candidateList.get(2);
                                            removeThird = "Remove Candidate " + the_triplet[2] + " at ";
                                            triplet_not_found = false;//stop searching for combinations, a triplet has been found.
                                        }

                                        removeRecentAdditions(candidateList, countThirdsCandidates);//make space for addition of a new third cell
                                    }
                                }

                                removeRecentAdditions(candidateList, countSecondsCandidates);//make space for addition of a new second cell
                            }
                        }
                    }
                }

                if (!triplet_not_found) {//if the triplet has ben found, remove candidates from that column that aren't naked triplets or set.
                    for (int r = r1; r < r1 + 3; r++) {
                        for (int c = c1; c < c1 + 3; c++) {
                            if (!(r == skip_rows[0] && c == skip_columns[0]) && !(r == skip_rows[1] && c == skip_columns[1]) && !(r == skip_rows[2] && c == skip_columns[2]) && puzzle[r][c].isNotSet()) {
                                ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], the_triplet);//gets the candidates removed from the particular cell
                                if (removeList.contains(the_triplet[0])) {
                                    chkremoveFirst = true;
                                    removeFirst += "(" + (r + 1) + "," + (c + 1) + ")";
                                }
                                if (removeList.contains(the_triplet[1])) {
                                    chkremoveSecond = true;
                                    removeSecond += "(" + (r + 1) + "," + (c + 1) + ")";
                                }
                                if (removeList.contains(the_triplet[2])) {
                                    chkremoveThird = true;
                                    removeThird += "(" + (r + 1) + "," + (c + 1) + ")";
                                }
                            }
                        }
                    }
                }

                nakedTripleCheckSteps(chkremoveFirst, chkremoveSecond, chkremoveThird, removeFirst, removeSecond, removeThird, the_triplet,
                        skip_rows[0], skip_columns[0], skip_rows[1], skip_columns[1], skip_rows[2], skip_columns[2], 3);

            }
        }
    } // end of nakedTriple()

    /**
     * Checks if the current cell is eligible for checking if it's a nakedTriple
     */
    private static boolean checkNakedTripleEligible(Cell cell) {
        return (cell.getNoOfCandidates() == 2 || cell.getNoOfCandidates() == 3) && cell.isNotSet();
    }// end of checkNakedTripleEligible()

    /**
     * Adds a  unique candidate of cell to the candidateList arraylist used in nakedTriple. Returns the number of such
     * unique candidates inserted.
     */
    private static int addIfAbsent(ArrayList<Integer> candidateList, Cell cell) {
        int count = 0;
        for (int position = 0; position < cell.getNoOfCandidates(); position++) {
            if (!candidateList.contains(cell.getCandidate(position))) {
                candidateList.add(cell.getCandidate(position));
                count++;
            }
        }
        return count;
    }// end of addIfAbsent()

    /**
     * If a Naked Triple or Quadruple is not found, the recently added members of the candidateList needs to be removed
     * to make another combination of 3/4 cells to check for nakedTriple or Quadruple
     */
    private static void removeRecentAdditions(ArrayList<Integer> candidateList, int numberOfRemovals) {
        for (int i = 0; i < numberOfRemovals; i++) {//remove the current second cell's candidates to restart
            candidateList.remove(candidateList.size() - 1);
        }
    }// end of removeRecentAdditions()

    /**
     * add changes made by naked triple to the check steps linked lists
     */
    private static void nakedTripleCheckSteps(boolean chkremoveFirst, boolean chkremoveSecond, boolean chkremoveThird,
                                              String removeFirst, String removeSecond, String removeThird,
                                              int[] the_triplet, int row_1, int column_1, int row_2, int column_2,
                                              int row_3, int column_3, int house) {

        String end_tag = "";

        switch (house) {
            case 1://naked triple in row
                end_tag = " in Row " + (row_1 + 1);
                break;

            case 2://naked triple in column
                end_tag = " in Column " + (column_1 + 1);
                break;

            case 3://naked triple in subsquare
                end_tag = " in Block";
                break;
        }

        if (chkremoveFirst || chkremoveSecond || chkremoveThird) {//naked triple changed the puzzle in some way

            algorithm.add("Naked Triple" + end_tag);
            insert.add("Candidates: " + the_triplet[0] + "," + the_triplet[1] + "," + the_triplet[2] + " are common to cells " +
                    "(" + (row_1 + 1) + "," + (column_1 + 1) + ")" + " and " + "(" + (row_2 + 1) + "," + (column_2 + 1) + ")"
                    + " and " + "(" + (row_3 + 1) + "," + (column_3 + 1) + ")");
            if (!chkremoveFirst)//no removal of candidate 1
                removeFirst = "";
            if (!chkremoveSecond)//no removal of candidate 2
                removeSecond = "";
            if (!chkremoveThird)//no removal of candidate 3
                removeThird = "";
            remove.add((removeFirst + " " + removeSecond + " " + removeThird).trim());

        }
    }//end of nakedTripleCheckSteps()


    private static void hiddenTriple(Cell[][] puzzle) {
        int[] triple = new int[3];//array to store the values of the triple

        //checking for HIDDEN TRIPLE in Rows
        for (int r = 0; r < 9; r++) {
            ArrayList<Integer> eligible_columns = new ArrayList<>();//columns in the current row that could possibly contain a hidden triple
            for (int c = 0; c < 9; c++) { //loop to find eligible cells in this row
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_columns.add(c);
            }//c

            if (eligible_columns.size() >= 3) {//there needs to be at least 3 eligible cells for a hidden triple to exist

                for (int i = 0; i < eligible_columns.size() - 2; i++) {//loop to go through combinations of the eligible cells in the rows to find valid hidden triple
                    for (int j = (i + 1); j < eligible_columns.size() - 1; j++) {
                        for (int k = (j + 1); k < eligible_columns.size(); k++) {
                            int index_1 = eligible_columns.get(i);
                            int index_2 = eligible_columns.get(j);
                            int index_3 = eligible_columns.get(k);
                            ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[r][index_1], puzzle[r][index_2], puzzle[r][index_3]);//store the similar candidates between chosen candidates

                            if (similarCandidates.size() >= 3) {//checking if there are at least 3 similar candidates

                                for (int inc0 = 0; inc0 < similarCandidates.size() - 2; inc0++) {
                                    for (int inc1 = (inc0 + 1); inc1 < similarCandidates.size() - 1; inc1++) {
                                        for (int inc2 = (inc1 + 1); inc2 < similarCandidates.size(); inc2++) {//choose any 3 of the similar candidates
                                            triple[0] = similarCandidates.get(inc0);
                                            triple[1] = similarCandidates.get(inc1);
                                            triple[2] = similarCandidates.get(inc2);

                                            if (tripleDoesNotExistInOtherCellsInRow(puzzle, r, triple, index_1, index_2, index_3))//checking if any other cells in the same row also have triple[0], triple[1] or triple[2] as candidates
                                                hiddenTripleCheckSteps(puzzle, r, index_1, r, index_2, r, index_3, triple, 1);
                                        }//inc2
                                    }//inc1
                                }//inc0

                            }//if statement

                        }//k
                    }//j
                }//i

            }//if
        }//r

        //checking for HIDDEN TRIPLE in Columns
        for (int c = 0; c < 9; c++) {
            ArrayList<Integer> eligible_rows = new ArrayList<>();//rows in the current column that could possibly contain a hidden triple
            for (int r = 0; r < 9; r++) { //loop to find eligible cells in this column
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_rows.add(r);
            }//r

            if (eligible_rows.size() >= 3) {//there needs to be at least 3 eligible cells for a hidden triple to exist

                for (int i = 0; i < eligible_rows.size() - 2; i++) {//loop to go through combinations of the eligible cells in the columns to find valid hidden triple
                    for (int j = (i + 1); j < eligible_rows.size() - 1; j++) {
                        for (int k = (j + 1); k < eligible_rows.size(); k++) {
                            int index_1 = eligible_rows.get(i);
                            int index_2 = eligible_rows.get(j);
                            int index_3 = eligible_rows.get(k);
                            ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[index_1][c], puzzle[index_2][c], puzzle[index_3][c]);//store the similar candidates between chosen candidates

                            if (similarCandidates.size() >= 3) {//checking if there are 2 or 3 similar candidates

                                for (int inc0 = 0; inc0 < similarCandidates.size() - 2; inc0++) {
                                    for (int inc1 = (inc0 + 1); inc1 < similarCandidates.size() - 1; inc1++) {
                                        for (int inc2 = (inc1 + 1); inc2 < similarCandidates.size(); inc2++) {//choose any 3 of the similar candidates
                                            triple[0] = similarCandidates.get(inc0);
                                            triple[1] = similarCandidates.get(inc1);
                                            triple[2] = similarCandidates.get(inc2);

                                            if (tripleDoesNotExistInOtherCellsInColumn(puzzle, c, triple, index_1, index_2, index_3))//checking if any other cells in the same column also have triple[0], triple[1] or triple[2] as candidates
                                                hiddenTripleCheckSteps(puzzle, index_1, c, index_2, c, index_3, c, triple, 2);
                                        }//inc2
                                    }//inc1
                                }//inc0

                            }//if statement

                        }//k
                    }//j
                }//i

            }//if
        }//c

        //checking for HIDDEN TRIPLE in Block
        for (int r1 = 0; r1 <= 6; r1 += 3) {//jumps to first row position of block
            for (int c1 = 0; c1 <= 6; c1 += 3) {//jumps to first column position of block
                ArrayList<Integer> eligible_rows = new ArrayList<>();//rows that could possibly contain a hidden triple
                ArrayList<Integer> eligible_columns = new ArrayList<>();//columns that could possibly contain hidden triple
                for (int r = r1; r < r1 + 3; r++) {//traversal block
                    for (int c = c1; c < c1 + 3; c++) {
                        if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2) {//if cell has 2 or more candidates, it is eligible
                            eligible_rows.add(r);
                            eligible_columns.add(c);
                        }
                    }
                }

                int eligibleCellsSize = eligible_columns.size();
                if (eligibleCellsSize >= 3) {

                    for (int i = 0; i < eligibleCellsSize - 2; i++) {//loop to go through combinations of the eligible cells in the subquare to find valid hidden triple
                        for (int j = (i + 1); j < eligibleCellsSize - 1; j++) {
                            for (int k = (j + 1); k < eligibleCellsSize; k++) {
                                int index_1_r = eligible_rows.get(i);
                                int index_1_c = eligible_columns.get(i);

                                int index_2_r = eligible_rows.get(j);
                                int index_2_c = eligible_columns.get(j);

                                int index_3_r = eligible_rows.get(k);
                                int index_3_c = eligible_columns.get(k);

                                ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[index_1_r][index_1_c], puzzle[index_2_r][index_2_c], puzzle[index_3_r][index_3_c]);//store the similar candidates between chosen candidates

                                if (similarCandidates.size() >= 2) {//checking if there are more than or equal to 2 similar candidates
                                    for (int inc0 = 0; inc0 < similarCandidates.size() - 2; inc0++) {
                                        for (int inc1 = (inc0 + 1); inc1 < similarCandidates.size() - 1; inc1++) {
                                            for (int inc2 = (inc1 + 1); inc2 < similarCandidates.size(); inc2++) {//choose any 3 of the similar candidates
                                                triple[0] = similarCandidates.get(inc0);
                                                triple[1] = similarCandidates.get(inc1);
                                                triple[2] = similarCandidates.get(inc2);

                                                if (tripleDoesNotExistInOtherCellsInSubsquare(puzzle, r1, c1, triple, index_1_r, index_1_c, index_2_r, index_2_c, index_3_r, index_3_c))//checking if any other cells in the same subsquare also have triple[0], triple[1] or triple[2] as candidates
                                                    hiddenTripleCheckSteps(puzzle, index_1_r, index_1_c, index_2_r, index_2_c, index_3_r, index_3_c, triple, 3);
                                            }//inc2
                                        }//inc1
                                    }//inc0

                                }//if statement

                            }//k
                        }//j
                    }//i

                }//outer if statement
            }//c1
        }//r1

    }// end of hiddenTriple()


    private static ArrayList<Integer> getSimilarCandidates(Cell cell_1, Cell cell_2, Cell cell_3) {
        Cell[] cellArray = {cell_1, cell_2, cell_3};
        boolean flag;//to exist the inner nested loops and jump straight to the next iteration of outermost loop
        ArrayList<Integer> similar_candidates = new ArrayList<>();// stores the similar candidates
        for (int num = 1; num < 10; num++) {
            flag = false;
            for (int index1 = 0; index1 < cellArray.length - 1; index1++) {
                for (int index2 = (index1 + 1); index2 < cellArray.length; index2++) {
                    if (cellArray[index1].isCandidate(num) && cellArray[index2].isCandidate(num)) {
                        similar_candidates.add(num);// adding the similar candidate to the array list
                        flag = true;
                        break;
                    }
                } // index2
                if (flag)
                    break;
            } // index1
        } // num
        return similar_candidates;
    }// end of getSimilarCandidates()


    private static String[] removeNonTripleCandidates(Cell cell_1, Cell cell_2, Cell cell_3, int[] triple) {
        Cell[] cellArray = {cell_1, cell_2, cell_3};

        String[] removeTriple = new String[3];//stores the text for check steps when candidates are removed
        for (int i = 0; i < 3; i++)
            removeTriple[i] = "Remove Candidates ";

        boolean[] tmp = new boolean[3];//records if any candidates have been removed anywhere
        for (int i = 0; i < 3; i++)
            tmp[i] = true;

        for (int num = 1; num < 10; num++) {//loop to remove TRIPLE candidates from the other cells in the House(Row, Column or Block)
            if (num != triple[0] && num != triple[1] && num != triple[2]) {
                for (int i = 0; i < 3; i++) {
                    if (cellArray[i].isCandidate(num)) {
                        cellArray[i].removeCandidate(num);
                        removeTriple[i] += num + ",";
                        tmp[i] = false;
                        changeCounter++;
                    }
                }//i
            }
        }//num

        for (int i = 0; i < 3; i++) {
            if (tmp[i])
                removeTriple[i] = "";
            else
                removeTriple[i] = removeTriple[i].substring(0, removeTriple[i].length() - 1) + " from ";
        }
        return removeTriple;
    }// end of removeNonTripleCandidates()


    private static boolean tripleDoesNotExistInOtherCellsInRow(Cell[][] puzzle, int row, int[] triple, int index1, int index2, int index3) {
        for (int c = 0; c < 9; c++) {
            if (puzzle[row][c].isNotSet() && (c != index1 && c != index2 && c != index3))
                if (puzzle[row][c].isCandidate(triple[0]) || puzzle[row][c].isCandidate(triple[1]) || puzzle[row][c].isCandidate(triple[2]))
                    return false;
        }
        return true;
    }// end of tripleDoesNotExistInOtherCellsInRow()

    private static boolean tripleDoesNotExistInOtherCellsInColumn(Cell[][] puzzle, int col, int[] triple, int index1, int index2, int index3) {
        for (int r = 0; r < 9; r++) {
            if (puzzle[r][col].isNotSet() && (r != index1 && r != index2 && r != index3))
                if (puzzle[r][col].isCandidate(triple[0]) || puzzle[r][col].isCandidate(triple[1]) || puzzle[r][col].isCandidate(triple[2]))
                    return false;
        }
        return true;
    }// end of tripleDoesNotExistInOtherCellsInColumn()

    private static boolean tripleDoesNotExistInOtherCellsInSubsquare(Cell[][] puzzle, int r1, int c1, int[] triple, int index_1_r, int index_1_c, int index_2_r, int index_2_c, int index_3_r, int index_3_c) {
        for (int r = r1; r < r1 + 3; r++) {//traversal (subsquare)
            for (int c = c1; c < c1 + 3; c++) {
                if (puzzle[r][c].isNotSet() && (!(r == index_1_r && c == index_1_c) && !(r == index_2_r && c == index_2_c) && !(r == index_3_r && c == index_3_c)))
                    if (puzzle[r][c].isCandidate(triple[0]) || puzzle[r][c].isCandidate(triple[1]) || puzzle[r][c].isCandidate(triple[2]))
                        return false;
            }
        }
        return true;
    }//end of tripleDoesNotExistInOtherCellsInSubsquare

    private static void hiddenTripleCheckSteps(Cell[][] puzzle, int row_1, int col_1, int row_2, int col_2, int row_3, int col_3, int[] triple, int house) {

        int tmp = changeCounter;
        String end_tag = "";

        switch (house) {
            case 1://hidden triple in row
                end_tag = " in Row " + (row_1 + 1);
                break;

            case 2://hidden triple in column
                end_tag = " in Column " + (col_1 + 1);
                break;

            case 3://hidden triple in subsquare
                end_tag = " in Block";
                break;
        }

        String[] removeSet = removeNonTripleCandidates(puzzle[row_1][col_1], puzzle[row_2][col_2], puzzle[row_3][col_3], triple);//contains the remove string for each cell
        if (tmp != changeCounter) {//if tmp is != changeCounter, there has been a change caused by hiddenTriple()
            algorithm.add("Hidden Triple" + end_tag);
            insert.add("Candidates: " + triple[0] + ", " + triple[1] + ", " + triple[2] + " are common to cells " + "(" + (row_1 + 1) + "," + (col_1 + 1) + "), "
                    + "(" + (row_2 + 1) + "," + (col_2 + 1) + ")" + " and " + "(" + (row_3 + 1) + "," + (col_3 + 1) + ")");

            String cell_1 = removeSet[0].length() == 0 ? "" : "(" + (row_1 + 1) + "," + (col_1 + 1) + ")";//if we don't have to remove any candidates, don't print
            String cell_2 = removeSet[1].length() == 0 ? "" : "(" + (row_2 + 1) + "," + (col_2 + 1) + ")";//if we don't have to remove any candidates, don't print
            String cell_3 = removeSet[2].length() == 0 ? "" : "(" + (row_3 + 1) + "," + (col_3 + 1) + ")";//if we don't have to remove any candidates, don't print
            remove.add((removeSet[0] + cell_1 + " " + removeSet[1] + cell_2 + " " + removeSet[2] + cell_3).trim());
        }
    }// end of hiddenTripleCheckSteps()


    private static void nakedQuad(Cell[][] puzzle) {

        //checking for NAKED QUAD in Rows
        for (int r = 0; r < 9; r++) {
            ArrayList<Integer> eligible_columns = new ArrayList<>();//an arraylist containing all columns in the row that are eligible to possibily contain Naked Quad
            for (int c = 0; c < 9; c++) {
                if (checkNakedQuadEligible(puzzle[r][c])) {//checks if eligible
                    eligible_columns.add(c);//if so, add it to the arrayList
                }
            }

            if (eligible_columns.size() >= 4) {//if the number of eligible columns is less than 4, a Naked Quad cannot exist.

                int[] skip_columns = new int[4];//stores the columns of nakedQuad (if existing), to be skipped.
                int[] quad = new int[4];//stores the candidates that are common to the Naked Quad if existing.
                boolean quad_found = false;//checks if a quad has been found

                String[] removeCandidateText = new String[4];//stores the text message for the check steps
                for (int i = 0; i < 4; i++) {
                    removeCandidateText[i] = "";
                }
                boolean[] checkCandidateRemoved = new boolean[4];//to check if either candidate was removed anywhere in puzzle
                for (int i = 0; i < 4; i++) {
                    checkCandidateRemoved[i] = false;
                }


                for (int index1 = 0; index1 < eligible_columns.size() - 3 && !quad_found; index1++) {//loop generating the first cell that 'could' be a naked quad

                    ArrayList<Integer> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of four cells whose columns were eligible
                    addIfAbsent(candidateList, puzzle[r][eligible_columns.get(index1)]);

                    for (int index2 = (index1 + 1); index2 < eligible_columns.size() - 2 && !quad_found; index2++) {//loop generating the second cell that 'could' be a naked quad

                        int countSecondsCandidates = addIfAbsent(candidateList, puzzle[r][eligible_columns.get(index2)]); // to count the candidates of second cell that are added to candidateList

                        for (int index3 = (index2 + 1); index3 < eligible_columns.size() - 1 && !quad_found; index3++) {//loop generating the third cell that 'could' be a naked quad

                            int countThirdsCandidates = addIfAbsent(candidateList, puzzle[r][eligible_columns.get(index3)]);

                            for (int index4 = (index3 + 1); index4 < eligible_columns.size() && !quad_found; index4++) {//loop generating the fourth cell that 'could' be a naked quad

                                int countFourthsCandidates = addIfAbsent(candidateList, puzzle[r][eligible_columns.get(index4)]); // to count the candidates of fourth cell that are added to candidateList

                                if (candidateList.size() == 4) {//if the number of unique candidates amongst the 4 cells is 4, they must be a naked quad.
                                    /*Store the columns and quads to be skipped.*/
                                    skip_columns[0] = eligible_columns.get(index1);
                                    skip_columns[1] = eligible_columns.get(index2);
                                    skip_columns[2] = eligible_columns.get(index3);
                                    skip_columns[3] = eligible_columns.get(index4);

                                    quad[0] = candidateList.get(0);
                                    removeCandidateText[0] = "Remove Candidate " + quad[0] + " at ";

                                    quad[1] = candidateList.get(1);
                                    removeCandidateText[1] = "Remove Candidate " + quad[1] + " at ";

                                    quad[2] = candidateList.get(2);
                                    removeCandidateText[2] = "Remove Candidate " + quad[2] + " at ";

                                    quad[3] = candidateList.get(3);
                                    removeCandidateText[3] = "Remove Candidate " + quad[3] + " at ";

                                    quad_found = true;//stop searching for combinations, a quad has been found.
                                }//if Statement

                                removeRecentAdditions(candidateList, countFourthsCandidates);//make space for addition of a new fourth cell

                            }//index4

                            removeRecentAdditions(candidateList, countThirdsCandidates);//make space for addition of a new third cell

                        }//index3

                        removeRecentAdditions(candidateList, countSecondsCandidates);//make space for addition of a new second cell

                    }//index2

                }//index1


                if (quad_found) {//if the quad has been found, remove candidates from other cells in that row that are naked quad candidates.
                    for (int c = 0; c < 9; c++) {
                        if (c != skip_columns[0] && c != skip_columns[1] && c != skip_columns[2] && c != skip_columns[3] && puzzle[r][c].isNotSet()) {
                            ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], quad);//gets the candidates removed from the particular cell
                            if (removeList.contains(quad[0])) {
                                checkCandidateRemoved[0] = true;
                                removeCandidateText[0] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                            if (removeList.contains(quad[1])) {
                                checkCandidateRemoved[1] = true;
                                removeCandidateText[1] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                            if (removeList.contains(quad[2])) {
                                checkCandidateRemoved[2] = true;
                                removeCandidateText[2] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                            if (removeList.contains(quad[3])) {
                                checkCandidateRemoved[3] = true;
                                removeCandidateText[3] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                        }
                    }
                }// if (quad_found)

                nakedQuadCheckSteps(checkCandidateRemoved, removeCandidateText, quad,
                        r, skip_columns[0], r, skip_columns[1], r, skip_columns[2], r, skip_columns[3], 1);

            }//if (eligible columns >= 4)            

        }//r (end of row checking)


        //checking for NAKED QUAD in Columns
        for (int c = 0; c < 9; c++) {
            ArrayList<Integer> eligible_rows = new ArrayList<>();//an arraylist containing all rows in the column that are eligible to possibily contain Naked Quad
            for (int r = 0; r < 9; r++) {
                if (checkNakedQuadEligible(puzzle[r][c])) {//checks if eligible
                    eligible_rows.add(r);//if so, add it to the arrayList
                }
            }

            if (eligible_rows.size() >= 4) {//if the number of eligible rows is less than 4, a Naked Quad cannot exist.

                int[] skip_rows = new int[4];//stores the rows of nakedQuad (if existing), to be skipped.
                int[] quad = new int[4];//stores the candidates that are common to the Naked Quad if existing.
                boolean quad_found = false;//checks if a quad has been found

                String[] removeCandidateText = new String[4];//stores the text message for the check steps
                for (int i = 0; i < 4; i++) {
                    removeCandidateText[i] = "";
                }
                boolean[] checkCandidateRemoved = new boolean[4];//to check if either candidate was removed anywhere in puzzle
                for (int i = 0; i < 4; i++) {
                    checkCandidateRemoved[i] = false;
                }


                for (int index1 = 0; index1 < eligible_rows.size() - 3 && !quad_found; index1++) {//loop generating the first cell that 'could' be a naked quad

                    ArrayList<Integer> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of four cells whose rows were eligible
                    addIfAbsent(candidateList, puzzle[eligible_rows.get(index1)][c]);

                    for (int index2 = (index1 + 1); index2 < eligible_rows.size() - 2 && !quad_found; index2++) {//loop generating the second cell that 'could' be a naked quad

                        int countSecondsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(index2)][c]); // to count the candidates of second cell that are added to candidateList

                        for (int index3 = (index2 + 1); index3 < eligible_rows.size() - 1 && !quad_found; index3++) {//loop generating the third cell that 'could' be a naked quad

                            int countThirdsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(index3)][c]); // to count the candidates of third cell that are added to candidateList

                            for (int index4 = (index3 + 1); index4 < eligible_rows.size() && !quad_found; index4++) {//loop generating the third cell that 'could' be a naked quad

                                int countFourthsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(index4)][c]); // to count the candidates of fourth cell that are added to candidateList

                                if (candidateList.size() == 4) {//if the number of unique candidates amongst the 4 cells is 4, they must be a naked quad.
                                    /*Store the rows and quads to be skipped.*/
                                    skip_rows[0] = eligible_rows.get(index1);
                                    skip_rows[1] = eligible_rows.get(index2);
                                    skip_rows[2] = eligible_rows.get(index3);
                                    skip_rows[3] = eligible_rows.get(index4);

                                    quad[0] = candidateList.get(0);
                                    removeCandidateText[0] = "Remove Candidate " + quad[0] + " at ";

                                    quad[1] = candidateList.get(1);
                                    removeCandidateText[1] = "Remove Candidate " + quad[1] + " at ";

                                    quad[2] = candidateList.get(2);
                                    removeCandidateText[2] = "Remove Candidate " + quad[2] + " at ";

                                    quad[3] = candidateList.get(3);
                                    removeCandidateText[3] = "Remove Candidate " + quad[3] + " at ";

                                    quad_found = true;//stop searching for combinations, a quad has been found.
                                }//if Statement

                                removeRecentAdditions(candidateList, countFourthsCandidates);//make space for addition of a new fourth cell

                            }//index4

                            removeRecentAdditions(candidateList, countThirdsCandidates);//make space for addition of a new third cell

                        }//index3

                        removeRecentAdditions(candidateList, countSecondsCandidates);//make space for addition of a new second cell

                    }//index2

                }//index1


                if (quad_found) {//if the quad has been found, remove candidates from other cells in that row that are naked quad candidates.
                    for (int r = 0; r < 9; r++) {
                        if (r != skip_rows[0] && r != skip_rows[1] && r != skip_rows[2] && r != skip_rows[3] && puzzle[r][c].isNotSet()) {
                            ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], quad);//gets the candidates removed from the particular cell
                            if (removeList.contains(quad[0])) {
                                checkCandidateRemoved[0] = true;
                                removeCandidateText[0] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                            if (removeList.contains(quad[1])) {
                                checkCandidateRemoved[1] = true;
                                removeCandidateText[1] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                            if (removeList.contains(quad[2])) {
                                checkCandidateRemoved[2] = true;
                                removeCandidateText[2] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                            if (removeList.contains(quad[3])) {
                                checkCandidateRemoved[3] = true;
                                removeCandidateText[3] += "(" + (r + 1) + "," + (c + 1) + ")";
                            }
                        }
                    }
                }// if (quad_found)

                nakedQuadCheckSteps(checkCandidateRemoved, removeCandidateText, quad,
                        skip_rows[0], c, skip_rows[1], c, skip_rows[2], c, skip_rows[3], c, 2);

            }//if (eligible rows >= 4)  

        }//c (end of column checking)


        //checking for NAKED QUADS in Block                     
        for (int r1 = 0; r1 <= 6; r1 += 3) {
            for (int c1 = 0; c1 <= 6; c1 += 3) {

                ArrayList<Integer> eligible_columns = new ArrayList<>();//an arraylist containing all columns in a subset that are eligible candidates of nakedQuad
                ArrayList<Integer> eligible_rows = new ArrayList<>();//an arraylist containing all rows in a subset that are eligible candidates of nakedQuad
                for (int r = r1; r < r1 + 3; r++) {//in a particular subsquare
                    for (int c = c1; c < c1 + 3; c++) {
                        if (checkNakedQuadEligible(puzzle[r][c])) {//checks if eligible
                            eligible_columns.add(c);// if so, add column to the arrayList
                            eligible_rows.add(r); // add row to the arrayList
                        }
                    }
                }

                int eligibleCellsLength = eligible_columns.size();

                if (eligibleCellsLength >= 4) {//if the number of eligible cells in the block is less than 4, a naked quad cannot exist.

                    int[] skip_rows = new int[4];//stores the rows of nakedQuad (if existing) , to be skipped.
                    int[] skip_columns = new int[4];//stores the columns of nakedQuad (if existing) , to be skipped.
                    int[] quad = new int[4];//stores the candidates that are common to the nakedQuad of existing.
                    boolean quad_found = false;//checks if a quad has been found

                    String[] removeCandidateText = new String[4];//stores the text message for the check steps
                    for (int i = 0; i < 4; i++) {
                        removeCandidateText[i] = "";
                    }
                    boolean[] checkCandidateRemoved = new boolean[4];//to check if either candidate was removed anywhere in puzzle
                    for (int i = 0; i < 4; i++) {
                        checkCandidateRemoved[i] = false;
                    }


                    for (int index1 = 0; index1 < eligibleCellsLength - 3 && !quad_found; index1++) {

                        ArrayList<Integer> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of four cells whose cells were eligible
                        addIfAbsent(candidateList, puzzle[eligible_rows.get(index1)][eligible_columns.get(index1)]);

                        for (int index2 = (index1 + 1); index2 < eligibleCellsLength - 2 && !quad_found; index2++) {//loop generating the second cell that 'could' be a naked quad

                            int countSecondsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(index2)][eligible_columns.get(index2)]); //to count the candidates of second cell that are added to candidateList

                            for (int index3 = (index2 + 1); index3 < eligibleCellsLength - 1 && !quad_found; index3++) {//loop generating the third cell that 'could' be a naked quad

                                int countThirdsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(index3)][eligible_columns.get(index3)]); //to count the candidates of third cell that are added to candidateList

                                for (int index4 = (index3 + 1); index4 < eligibleCellsLength && !quad_found; index4++) {//loop generating the fourth cell that 'could' be a naked quad

                                    int countFourthsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(index4)][eligible_columns.get(index4)]); //to count the candidates of fourth cell that are added to candidateList

                                    if (candidateList.size() == 4) {//there need to be exactly 4 unique candidates in the set of candidates of these 4 cells for a quad to exist
                                        skip_rows[0] = eligible_rows.get(index1);
                                        skip_columns[0] = eligible_columns.get(index1);

                                        skip_rows[1] = eligible_rows.get(index2);
                                        skip_columns[1] = eligible_columns.get(index2);

                                        skip_rows[2] = eligible_rows.get(index3);
                                        skip_columns[2] = eligible_columns.get(index3);

                                        skip_rows[3] = eligible_rows.get(index4);
                                        skip_columns[3] = eligible_columns.get(index4);

                                        quad[0] = candidateList.get(0);
                                        removeCandidateText[0] = "Remove Candidate " + quad[0] + " at ";

                                        quad[1] = candidateList.get(1);
                                        removeCandidateText[1] = "Remove Candidate " + quad[1] + " at ";

                                        quad[2] = candidateList.get(2);
                                        removeCandidateText[2] = "Remove Candidate " + quad[2] + " at ";

                                        quad[3] = candidateList.get(3);
                                        removeCandidateText[3] = "Remove Candidate " + quad[3] + " at ";

                                        quad_found = true;//stop searching for combinations, a triplet has been found.
                                    }
                                    removeRecentAdditions(candidateList, countFourthsCandidates);//make space for addition of a new fourth cell
                                }

                                removeRecentAdditions(candidateList, countThirdsCandidates);//make space for addition of a new third cell

                            }

                            removeRecentAdditions(candidateList, countSecondsCandidates);//make space for addition of a new second cell

                        }
                    }

                    if (quad_found) {//if the quad has been found, remove candidates from the other cells in the block that are naked quad candidates.
                        for (int r = r1; r < r1 + 3; r++) {
                            for (int c = c1; c < c1 + 3; c++) {
                                if (!(r == skip_rows[0] && c == skip_columns[0]) && !(r == skip_rows[1] && c == skip_columns[1]) && !(r == skip_rows[2] && c == skip_columns[2]) && !(r == skip_rows[3] && c == skip_columns[3]) && puzzle[r][c].isNotSet()) {
                                    ArrayList<Integer> removeList = eliminateFromCell(puzzle[r][c], quad);//gets the candidates removed from the particular cell

                                    if (removeList.contains(quad[0])) {
                                        checkCandidateRemoved[0] = true;
                                        removeCandidateText[0] += "(" + (r + 1) + "," + (c + 1) + ")";
                                    }
                                    if (removeList.contains(quad[1])) {
                                        checkCandidateRemoved[1] = true;
                                        removeCandidateText[1] += "(" + (r + 1) + "," + (c + 1) + ")";
                                    }
                                    if (removeList.contains(quad[2])) {
                                        checkCandidateRemoved[2] = true;
                                        removeCandidateText[2] += "(" + (r + 1) + "," + (c + 1) + ")";
                                    }
                                    if (removeList.contains(quad[3])) {
                                        checkCandidateRemoved[3] = true;
                                        removeCandidateText[3] += "(" + (r + 1) + "," + (c + 1) + ")";
                                    }
                                }
                            }
                        }
                    }// if(!quad_found)

                    nakedQuadCheckSteps(checkCandidateRemoved, removeCandidateText, quad, skip_rows[0], skip_columns[0],
                            skip_rows[1], skip_columns[1], skip_rows[2], skip_columns[2], skip_rows[3], skip_columns[3], 3);

                }// if( eligibleCellSize >= 4)      

            }//c1
        }//r1  (end of block checking)
    } // end of nakedTriple()

    /**
     * Checks if the current cell is eligible for checking if it's a nakedTriple
     */
    private static boolean checkNakedQuadEligible(Cell cell) {
        return (cell.getNoOfCandidates() >= 2 && cell.getNoOfCandidates() <= 4) && cell.isNotSet();
    }// end of checkNakedTripleEligible()

    /**
     * add changes made by naked triple to the check steps linked lists
     */
    private static void nakedQuadCheckSteps(boolean[] checkCandidateRemoved, String[] removeCandidateText,
                                            int[] quad, int row_1, int column_1, int row_2, int column_2,
                                            int row_3, int column_3, int row_4, int column_4, int house) {

        String end_tag = "";

        switch (house) {
            case 1://naked quad in row
                end_tag = " in Row " + (row_1 + 1);
                break;

            case 2://naked quad in column
                end_tag = " in Column " + (column_1 + 1);
                break;

            case 3://naked quad in subsquare
                end_tag = " in Block";
                break;
        }

        if (checkCandidateRemoved[0] || checkCandidateRemoved[1] || checkCandidateRemoved[2] || checkCandidateRemoved[3]) {//checking if nakedQuad() changed the puzzle in some way

            algorithm.add("Naked Quad" + end_tag);
            insert.add("Candidates: " + quad[0] + ", " + quad[1] + ", " + quad[2] + " and " + quad[3] + " are common to cells " +
                    "(" + (row_1 + 1) + "," + (column_1 + 1) + "), (" + (row_2 + 1) + "," + (column_2 + 1) + "), ("
                    + (row_3 + 1) + "," + (column_3 + 1) + ") and (" + (row_4 + 1) + "," + (column_4 + 1) + ")");

            if (!checkCandidateRemoved[0])//no removal of candidate 1
                removeCandidateText[0] = "";

            if (!checkCandidateRemoved[1])//no removal of candidate 2
                removeCandidateText[1] = "";

            if (!checkCandidateRemoved[2])//no removal of candidate 3
                removeCandidateText[2] = "";

            if (!checkCandidateRemoved[3])//no removal of candidate 4
                removeCandidateText[3] = "";

            remove.add((removeCandidateText[0] + " " + removeCandidateText[1] + " " + removeCandidateText[2] + " " + removeCandidateText[3]).trim());
        }//if
    }//end of nakedQuadCheckSteps()


    private static void hiddenQuad(Cell[][] puzzle) {
        int[] quad = new int[4];//array to store the values of the quad

        //checking for HIDDEN QUAD in Rows
        for (int r = 0; r < 9; r++) {
            ArrayList<Integer> eligible_columns = new ArrayList<>();//columns in the current row that could possibly contain a hidden quad
            for (int c = 0; c < 9; c++) { //loop to find eligible cells in this row
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_columns.add(c);
            }//c

            if (eligible_columns.size() >= 4) {//there needs to be at least 4 eligible cells for a hidden quad to exist

                for (int i = 0; i < eligible_columns.size() - 3; i++) {//loop to go through combinations of the eligible cells in the rows to find valid hidden quad
                    for (int j = (i + 1); j < eligible_columns.size() - 2; j++) {
                        for (int k = (j + 1); k < eligible_columns.size() - 1; k++) {
                            for (int l = (k + 1); l < eligible_columns.size(); l++) {
                                int index_1 = eligible_columns.get(i);
                                int index_2 = eligible_columns.get(j);
                                int index_3 = eligible_columns.get(k);
                                int index_4 = eligible_columns.get(l);
                                ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[r][index_1], puzzle[r][index_2], puzzle[r][index_3], puzzle[r][index_4]);//store the similar candidates between chosen candidates

                                if (similarCandidates.size() >= 4) {//checking if there are 4 or more similar candidates

                                    for (int inc0 = 0; inc0 < similarCandidates.size() - 3; inc0++) {
                                        for (int inc1 = (inc0 + 1); inc1 < similarCandidates.size() - 2; inc1++) {
                                            for (int inc2 = (inc1 + 1); inc2 < similarCandidates.size() - 1; inc2++) {
                                                for (int inc3 = (inc2 + 1); inc3 < similarCandidates.size(); inc3++) {//choose any 4 of the similar candidates
                                                    quad[0] = similarCandidates.get(inc0);
                                                    quad[1] = similarCandidates.get(inc1);
                                                    quad[2] = similarCandidates.get(inc2);
                                                    quad[3] = similarCandidates.get(inc3);

                                                    if (quadDoesNotExistInOtherCellsInRow(puzzle, r, quad, index_1, index_2, index_3, index_4))//checking if any other cells in the same row also have quad[0], quad[1], quad[2] or quad[3] as candidates
                                                        hiddenQuadCheckSteps(puzzle, r, index_1, r, index_2, r, index_3, r, index_4, quad, 1);
                                                }//inc3
                                            }//inc2
                                        }//inc1
                                    }//inc0

                                }//if statement

                            }//l
                        }//k
                    }//j
                }//i

            }//if
        }//r

        //checking for HIDDEN QUAD in Columns
        for (int c = 0; c < 9; c++) {
            ArrayList<Integer> eligible_rows = new ArrayList<>();//rows in the current column that could possibly contain a hidden quad
            for (int r = 0; r < 9; r++) { //loop to find eligible cells in this column
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_rows.add(r);
            }//r

            if (eligible_rows.size() >= 4) {//there needs to be at least 4 eligible cells for a hidden quad to exist

                for (int i = 0; i < eligible_rows.size() - 3; i++) {//loop to go through combinations of the eligible cells in the columns to find valid hidden quad
                    for (int j = (i + 1); j < eligible_rows.size() - 2; j++) {
                        for (int k = (j + 1); k < eligible_rows.size() - 1; k++) {
                            for (int l = (k + 1); l < eligible_rows.size(); l++) {
                                int index_1 = eligible_rows.get(i);
                                int index_2 = eligible_rows.get(j);
                                int index_3 = eligible_rows.get(k);
                                int index_4 = eligible_rows.get(l);

                                ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[index_1][c], puzzle[index_2][c], puzzle[index_3][c], puzzle[index_4][c]);//store the similar candidates between chosen candidates

                                if (similarCandidates.size() >= 4) {//checking if there are at least 4 similar candidates

                                    for (int inc0 = 0; inc0 < similarCandidates.size() - 3; inc0++) {
                                        for (int inc1 = (inc0 + 1); inc1 < similarCandidates.size() - 2; inc1++) {
                                            for (int inc2 = (inc1 + 1); inc2 < similarCandidates.size() - 1; inc2++) {
                                                for (int inc3 = (inc2 + 1); inc3 < similarCandidates.size(); inc3++) {//choose any 4 of the similar candidates
                                                    quad[0] = similarCandidates.get(inc0);
                                                    quad[1] = similarCandidates.get(inc1);
                                                    quad[2] = similarCandidates.get(inc2);
                                                    quad[3] = similarCandidates.get(inc3);

                                                    if (quadDoesNotExistInOtherCellsInColumn(puzzle, c, quad, index_1, index_2, index_3, index_4))//checking if any other cells in the same column also have quad[0], quad[1], quad[2] or quad[3] as candidates
                                                        hiddenQuadCheckSteps(puzzle, index_1, c, index_2, c, index_3, c, index_4, c, quad, 2);
                                                }//inc3
                                            }//inc2
                                        }//inc1
                                    }//inc0

                                }//if statement

                            }//l
                        }//k
                    }//j
                }//i

            }//if
        }//c

        //checking for HIDDEN QUAD in Block
        for (int r1 = 0; r1 <= 6; r1 += 3) {//jumps to first row position of block
            for (int c1 = 0; c1 <= 6; c1 += 3) {//jumps to first column position of block
                ArrayList<Integer> eligible_rows = new ArrayList<>();//rows that could possibly contain a hidden block
                ArrayList<Integer> eligible_columns = new ArrayList<>();//columns that could possibly contain hidden block
                for (int r = r1; r < r1 + 3; r++) {//traversal of block
                    for (int c = c1; c < c1 + 3; c++) {
                        if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2) {//if cell has 2 or more candidates, it is eligible
                            eligible_rows.add(r);
                            eligible_columns.add(c);
                        }
                    }
                }

                int eligibleCellsSize = eligible_columns.size();
                if (eligibleCellsSize >= 4) {

                    for (int i = 0; i < eligibleCellsSize - 3; i++) {//loop to go through combinations of the eligible cells in the subquare to find valid hidden quad
                        for (int j = (i + 1); j < eligibleCellsSize - 2; j++) {
                            for (int k = (j + 1); k < eligibleCellsSize - 1; k++) {
                                for (int l = (k + 1); l < eligibleCellsSize; l++) {
                                    int index_1_r = eligible_rows.get(i);
                                    int index_1_c = eligible_columns.get(i);

                                    int index_2_r = eligible_rows.get(j);
                                    int index_2_c = eligible_columns.get(j);

                                    int index_3_r = eligible_rows.get(k);
                                    int index_3_c = eligible_columns.get(k);

                                    int index_4_r = eligible_rows.get(l);
                                    int index_4_c = eligible_columns.get(l);

                                    ArrayList<Integer> similarCandidates = getSimilarCandidates(puzzle[index_1_r][index_1_c], puzzle[index_2_r][index_2_c], puzzle[index_3_r][index_3_c], puzzle[index_4_r][index_4_c]);//store the similar candidates between chosen candidates

                                    if (similarCandidates.size() >= 4) {//checking if there are at least 4 similar candidates
                                        for (int inc0 = 0; inc0 < similarCandidates.size() - 3; inc0++) {
                                            for (int inc1 = (inc0 + 1); inc1 < similarCandidates.size() - 2; inc1++) {
                                                for (int inc2 = (inc1 + 1); inc2 < similarCandidates.size() - 1; inc2++) {
                                                    for (int inc3 = (inc2 + 1); inc3 < similarCandidates.size(); inc3++) {//choose any 2 of the similar candidates
                                                        quad[0] = similarCandidates.get(inc0);
                                                        quad[1] = similarCandidates.get(inc1);
                                                        quad[2] = similarCandidates.get(inc2);
                                                        quad[3] = similarCandidates.get(inc3);

                                                        if (quadDoesNotExistInOtherCellsInSubsquare(puzzle, r1, c1, quad, index_1_r, index_1_c, index_2_r, index_2_c, index_3_r, index_3_c, index_4_r, index_4_c))//checking if any other cells in the same block also have quad[0], quad[1], quad[2] or quad[3] as candidates
                                                            hiddenQuadCheckSteps(puzzle, index_1_r, index_1_c, index_2_r, index_2_c, index_3_r, index_3_c, index_4_r, index_4_c, quad, 3);
                                                    }//inc3
                                                }//inc2
                                            }//inc1
                                        }//inc0

                                    }//if statement

                                }//l
                            }//k
                        }//j
                    }//i

                }//outer if statement
            }//c1
        }//r1
    }// end of hiddenQuad()

    private static ArrayList<Integer> getSimilarCandidates(Cell cell_1, Cell cell_2, Cell cell_3, Cell cell_4) {
        Cell[] cellArray = {cell_1, cell_2, cell_3, cell_4};
        boolean flag;
        ArrayList<Integer> similar_candidates = new ArrayList<>();// stores the similar candidates
        for (int num = 1; num < 10; num++) {
            flag = false;
            for (int index1 = 0; index1 < cellArray.length - 1; index1++) {
                for (int index2 = (index1 + 1); index2 < cellArray.length; index2++) {
                    if (cellArray[index1].isCandidate(num) && cellArray[index2].isCandidate(num)) {
                        similar_candidates.add(num);// adding the similar candidate to the array list
                        flag = true;
                        break;
                    }
                } // index2
                if (flag)
                    break;
            } // index1
        } // num
        return similar_candidates;
    }// end of getSimilarCandidates()

    private static String[] removeNonQuadCandidates(Cell cell_1, Cell cell_2, Cell cell_3, Cell cell_4, int[] quad) {
        Cell[] cellArray = {cell_1, cell_2, cell_3, cell_4};

        String[] removeQuad = new String[4];//stores the text for check steps when candidates are removed
        for (int i = 0; i < 4; i++)
            removeQuad[i] = "Remove Candidates ";

        boolean[] tmp = new boolean[4];//records if any candidates have been removed anywhere
        for (int i = 0; i < 4; i++)
            tmp[i] = true;

        for (int num = 1; num < 10; num++) {//loop to remove QUAD candidates from the other cells in the House(Row, Column or Block)
            if (num != quad[0] && num != quad[1] && num != quad[2] && num != quad[3]) {
                for (int i = 0; i < 4; i++) {
                    if (cellArray[i].isCandidate(num)) {
                        cellArray[i].removeCandidate(num);
                        removeQuad[i] += num + ",";
                        tmp[i] = false;
                        changeCounter++;
                    }
                }//i
            }
        }//num

        for (int i = 0; i < 4; i++) {
            if (tmp[i])
                removeQuad[i] = "";
            else
                removeQuad[i] = removeQuad[i].substring(0, removeQuad[i].length() - 1) + " from ";
        }
        return removeQuad;
    }// end of removeNonQuadCandidates()


    private static boolean quadDoesNotExistInOtherCellsInRow(Cell[][] puzzle, int row, int[] quad, int index1, int index2, int index3, int index4) {
        for (int c = 0; c < 9; c++) {
            if (puzzle[row][c].isNotSet() && (c != index1 && c != index2 && c != index3 && c != index4))
                if (puzzle[row][c].isCandidate(quad[0]) || puzzle[row][c].isCandidate(quad[1]) || puzzle[row][c].isCandidate(quad[2]) || puzzle[row][c].isCandidate(quad[3]))
                    return false;
        }
        return true;
    }// end of quadDoesNotExistInOtherCellsInRow()

    private static boolean quadDoesNotExistInOtherCellsInColumn(Cell[][] puzzle, int col, int[] quad, int index1, int index2, int index3, int index4) {
        for (int r = 0; r < 9; r++) {
            if (puzzle[r][col].isNotSet() && (r != index1 && r != index2 && r != index3 && r != index4))
                if (puzzle[r][col].isCandidate(quad[0]) || puzzle[r][col].isCandidate(quad[1]) || puzzle[r][col].isCandidate(quad[2]) || puzzle[r][col].isCandidate(quad[3]))
                    return false;
        }
        return true;
    }// end of quadDoesNotExistInOtherCellsInColumn()

    private static boolean quadDoesNotExistInOtherCellsInSubsquare(Cell[][] puzzle, int r1, int c1, int[] quad, int index_1_r, int index_1_c, int index_2_r, int index_2_c, int index_3_r, int index_3_c, int index_4_r, int index_4_c) {
        for (int r = r1; r < r1 + 3; r++) {//traversal (subsquare)
            for (int c = c1; c < c1 + 3; c++) {
                if (puzzle[r][c].isNotSet() && (!(r == index_1_r && c == index_1_c) && !(r == index_2_r && c == index_2_c) && !(r == index_3_r && c == index_3_c) && !(r == index_4_r && c == index_4_c)))
                    if (puzzle[r][c].isCandidate(quad[0]) || puzzle[r][c].isCandidate(quad[1]) || puzzle[r][c].isCandidate(quad[2]) || puzzle[r][c].isCandidate(quad[3]))
                        return false;
            }
        }
        return true;
    }//end of quadDoesNotExistInOtherCellsInSubsquare

    private static void hiddenQuadCheckSteps(Cell[][] puzzle, int row_1, int col_1, int row_2, int col_2, int row_3, int col_3, int row_4, int col_4, int[] quad, int house) {

        int tmp = changeCounter;
        String end_tag = "";

        switch (house) {
            case 1://hidden quad in row
                end_tag = " in Row " + (row_1 + 1);
                break;

            case 2://hidden quad in column
                end_tag = " in Column " + (col_1 + 1);
                break;

            case 3://hidden quad in subsquare
                end_tag = " in Block";
                break;
        }

        String[] removeSet = removeNonQuadCandidates(puzzle[row_1][col_1], puzzle[row_2][col_2], puzzle[row_3][col_3], puzzle[row_4][col_4], quad);//contains the remove string for each cell
        if (tmp != changeCounter) {//if tmp is != changeCounter, there has been a change caused by hiddenQuad()
            algorithm.add("Hidden Quad" + end_tag);
            insert.add("Candidates: " + quad[0] + ", " + quad[1] + ", " + quad[2] + " and " + quad[3] + " are common to cells (" + (row_1 + 1) + "," + (col_1 + 1) + "), ("
                    + (row_2 + 1) + "," + (col_2 + 1) + "), (" + (row_3 + 1) + "," + (col_3 + 1) + ") and (" + (row_4 + 1) + "," + (col_4 + 1) + ")");

            String cell_1 = removeSet[0].length() == 0 ? "" : "(" + (row_1 + 1) + "," + (col_1 + 1) + ")";//if we don't have to remove any candidates, don't print
            String cell_2 = removeSet[1].length() == 0 ? "" : "(" + (row_2 + 1) + "," + (col_2 + 1) + ")";//if we don't have to remove any candidates, don't print
            String cell_3 = removeSet[2].length() == 0 ? "" : "(" + (row_3 + 1) + "," + (col_3 + 1) + ")";//if we don't have to remove any candidates, don't print
            String cell_4 = removeSet[3].length() == 0 ? "" : "(" + (row_4 + 1) + "," + (col_4 + 1) + ")";//if we don't have to remove any candidates, don't print
            remove.add((removeSet[0] + cell_1 + " " + removeSet[1] + cell_2 + " " + removeSet[2] + cell_3 + " " + removeSet[3] + cell_4).trim());
        }
    }// end of hiddenQuadCheckSteps()


    private static void xWing(Cell[][] puzzle) {
        basicFish(puzzle, 1);
    }// end of xWing()

    private static void swordfish(Cell[][] puzzle) {
        basicFish(puzzle, 2);
    }// end of swordfish()

    private static void jellyfish(Cell[][] puzzle) {
        basicFish(puzzle, 3);
    }// end of jellyfish()

    private static void basicFish(Cell[][] puzzle, int type) {

        int freq;//counter to find fish , if present.

        for (int num = 1; num <= 9; num++) {

            //checking Rows
            ArrayList<Integer> r_eligibleRows = new ArrayList<>();//to store row of swordfish candidates
            ArrayList<int[]> r_eligibleColumns = new ArrayList<>();//to store column of swordfish candidates

            for (int r = 0; r < 9; r++) {//traversal
                freq = 0;
                int[] tmp_columns = new int[type + 2];//the temporary columns
                for (int c = 0; c < 9 && freq < type + 2; c++) {
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        tmp_columns[freq] = c;//store column in row which has num as a candidate
                        freq++;
                    }
                }
                if (freq != 0) {
                    for (int i = freq; i < tmp_columns.length; i++) {
                        tmp_columns[i] = -1;
                    }
                }
                if (freq > 0 && freq <= type + 1) {//catches the locked triplet of cells in a particular column
                    r_eligibleRows.add(r);//since the freq is type+1, there are only as many cells in the row that have num as a candidate (locked set)
                    r_eligibleColumns.add(tmp_columns);
                }

            }
            basicFishRemoval(puzzle, r_eligibleRows, r_eligibleColumns, num, 1, type);


            //checking Columns
            ArrayList<Integer> c_eligibleColumns = new ArrayList<>();//to store column of swordfish candidates (column check)
            ArrayList<int[]> c_eligibleRows = new ArrayList<>();//to store row of swordfish candidates (row check)

            for (int c = 0; c < 9; c++) {
                freq = 0;
                int[] tmp_rows = new int[type + 2];//the temporary rows
                for (int r = 0; r < 9 && freq < type + 2; r++) {
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        tmp_rows[freq] = r;//store column in row which has num as a candidate
                        freq++;
                    }
                }
                if (freq != 0) {
                    for (int i = freq; i < tmp_rows.length; i++) {
                        tmp_rows[i] = -1;
                    }
                }
                if (freq > 0 && freq <= type + 1) {//catches the locked set of cells in a particular row
                    c_eligibleColumns.add(c);//since the freq is type +1, there are only as many cells in the column that have num as a candidate (locked set)
                    c_eligibleRows.add(tmp_rows);
                }
            }
            basicFishRemoval(puzzle, c_eligibleColumns, c_eligibleRows, num, 2, type);
        }

    }

    private static void basicFishRemoval(Cell[][] puzzle, ArrayList<Integer> house_in, ArrayList<int[]> candidate_pos, int num, int which, int type) {
        if (house_in.size() >= type + 1) {//if the rows/columns where the swordfish candidates are in is more than 3, a swordfish may exist

            if (type == 1) {
                for (int i = 0; i < house_in.size() - 1; i++) {
                    for (int j = i + 1; j < house_in.size(); j++) {
                        ArrayList<Integer> other_house = compareCandidatePos(candidate_pos.get(i), candidate_pos.get(j), null, null);//find the unique set of houses from where candidates need to be removed
                        if (other_house.size() == 2) {//two unique houses from where candidates are to be removed (xwing)
                            if (which == 1) {
                                String title = "X-wing in Row " + (house_in.get(i) + 1) + " and " + (house_in.get(j) + 1);
                                String insert_s = "Candidate " + num + " is common to cells ";
                                for (int tmp = 0; tmp < other_house.size(); tmp++) {//list all xwing cells
                                    if (puzzle[house_in.get(i)][other_house.get(tmp)].isCandidate(num)
                                            && puzzle[house_in.get(i)][other_house.get(tmp)].isNotSet())
                                        insert_s += "(" + (house_in.get(i) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                    if (puzzle[house_in.get(j)][other_house.get(tmp)].isCandidate(num)
                                            && puzzle[house_in.get(j)][other_house.get(tmp)].isNotSet())
                                        insert_s += "(" + (house_in.get(j) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                }

                                String remove_s = "Remove Candidate " + num + " from ";
                                int changechk = changeCounter;
                                for (int r = 0; r < 9; r++) {
                                    if (r != house_in.get(i) && r != house_in.get(j)) {
                                        if (puzzle[r][other_house.get(0)].isCandidate(num) && puzzle[r][other_house.get(0)].isNotSet()) {
                                            changeCounter++;
                                            remove_s += "(" + (r + 1) + "," + (other_house.get(0) + 1) + ")" + " ";
                                            puzzle[r][other_house.get(0)].removeCandidate(num);
                                        }
                                        if (puzzle[r][other_house.get(1)].isCandidate(num) && puzzle[r][other_house.get(1)].isNotSet()) {
                                            changeCounter++;
                                            remove_s += "(" + (r + 1) + "," + (other_house.get(1) + 1) + ")" + " ";
                                            puzzle[r][other_house.get(1)].removeCandidate(num);
                                        }
                                    }
                                }

                                if (changechk != changeCounter) {
                                    algorithm.add(title);
                                    insert.add(insert_s);
                                    remove.add(remove_s);
                                }

                            } else {

                                String title = "X-Wing in Column " + (house_in.get(i) + 1) + " and " + (house_in.get(j) + 1);
                                String insert_s = "Candidate " + num + " is common to cells ";
                                for (int tmp = 0; tmp < other_house.size(); tmp++) {
                                    if (puzzle[other_house.get(tmp)][house_in.get(i)].isCandidate(num)
                                            && puzzle[other_house.get(tmp)][house_in.get(i)].isNotSet())
                                        insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(i) + 1) + ") ";
                                    if (puzzle[other_house.get(tmp)][house_in.get(j)].isCandidate(num)
                                            && puzzle[other_house.get(tmp)][house_in.get(j)].isNotSet())
                                        insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(j) + 1) + ") ";
                                }

                                String remove_s = "Remove Candidate " + num + " from ";
                                int changechk = changeCounter;
                                for (int c = 0; c < 9; c++) {
                                    if (c != house_in.get(i) && c != house_in.get(j)) {
                                        if (puzzle[other_house.get(0)][c].isCandidate(num) && puzzle[other_house.get(0)][c].isNotSet()) {
                                            changeCounter++;
                                            remove_s += "(" + (other_house.get(0) + 1) + "," + (c + 1) + ")" + " ";
                                            puzzle[other_house.get(0)][c].removeCandidate(num);
                                        }
                                        if (puzzle[other_house.get(1)][c].isCandidate(num) && puzzle[other_house.get(1)][c].isNotSet()) {
                                            changeCounter++;
                                            remove_s += "(" + (other_house.get(1) + 1) + "," + (c + 1) + ")" + " ";
                                            puzzle[other_house.get(1)][c].removeCandidate(num);
                                        }

                                    }
                                }

                                if (changechk != changeCounter) {
                                    algorithm.add(title);
                                    insert.add(insert_s);
                                    remove.add(remove_s);
                                }

                            }
                        }
                    }
                }
            } // first if statement

            else if (type == 2) {//swordfish
                for (int i = 0; i < house_in.size() - 2; i++) {
                    for (int j = i + 1; j < house_in.size() - 1; j++) {
                        for (int k = j + 1; k < house_in.size(); k++) {//these iterators are to find different combinations of row/columns and see if a swordfish can be found
                            ArrayList<Integer> other_house = compareCandidatePos(candidate_pos.get(i), candidate_pos.get(j), candidate_pos.get(k), null);//find the unique set of houses from where candidates need to be removed
                            if (other_house.size() == 3) {//three unique houses from where candidates are to be removed (swordfish)
                                if (which == 1) {
                                    String title = "Swordfish in Row " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
                                            " and " + (house_in.get(k) + 1);
                                    String insert_s = "Candidate " + num + " is common to cells ";
                                    for (int tmp = 0; tmp < other_house.size(); tmp++) {//list all swordfish cells
                                        if (puzzle[house_in.get(i)][other_house.get(tmp)].isCandidate(num)
                                                && puzzle[house_in.get(i)][other_house.get(tmp)].isNotSet())
                                            insert_s += "(" + (house_in.get(i) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                        if (puzzle[house_in.get(j)][other_house.get(tmp)].isCandidate(num)
                                                && puzzle[house_in.get(j)][other_house.get(tmp)].isNotSet())
                                            insert_s += "(" + (house_in.get(j) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                        if (puzzle[house_in.get(k)][other_house.get(tmp)].isCandidate(num)
                                                && puzzle[house_in.get(k)][other_house.get(tmp)].isNotSet())
                                            insert_s += "(" + (house_in.get(k) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                    }

                                    String remove_s = "Remove Candidate " + num + " from ";
                                    int changechk = changeCounter;
                                    for (int r = 0; r < 9; r++) {
                                        if (r != house_in.get(i) && r != house_in.get(j) && r != house_in.get(k)) {
                                            if (puzzle[r][other_house.get(0)].isCandidate(num) && puzzle[r][other_house.get(0)].isNotSet()) {
                                                changeCounter++;
                                                remove_s += "(" + (r + 1) + "," + (other_house.get(0) + 1) + ")" + " ";
                                                puzzle[r][other_house.get(0)].removeCandidate(num);
                                            }
                                            if (puzzle[r][other_house.get(1)].isCandidate(num) && puzzle[r][other_house.get(1)].isNotSet()) {
                                                changeCounter++;
                                                remove_s += "(" + (r + 1) + "," + (other_house.get(1) + 1) + ")" + " ";
                                                puzzle[r][other_house.get(1)].removeCandidate(num);
                                            }
                                            if (puzzle[r][other_house.get(2)].isCandidate(num) && puzzle[r][other_house.get(2)].isNotSet()) {
                                                changeCounter++;
                                                remove_s += "(" + (r + 1) + "," + (other_house.get(2) + 1) + ")" + " ";
                                                puzzle[r][other_house.get(2)].removeCandidate(num);
                                            }
                                        }
                                    }

                                    if (changechk != changeCounter) {
                                        algorithm.add(title);
                                        insert.add(insert_s);
                                        remove.add(remove_s);
                                    }

                                } else {

                                    String title = "Swordfish in Column " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
                                            " and " + (house_in.get(k) + 1);
                                    String insert_s = "Candidate " + num + " is common to cells ";
                                    for (int tmp = 0; tmp < other_house.size(); tmp++) {
                                        if (puzzle[other_house.get(tmp)][house_in.get(i)].isCandidate(num)
                                                && puzzle[other_house.get(tmp)][house_in.get(i)].isNotSet())
                                            insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(i) + 1) + ") ";
                                        if (puzzle[other_house.get(tmp)][house_in.get(j)].isCandidate(num)
                                                && puzzle[other_house.get(tmp)][house_in.get(j)].isNotSet())
                                            insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(j) + 1) + ") ";
                                        if (puzzle[other_house.get(tmp)][house_in.get(k)].isCandidate(num)
                                                && puzzle[other_house.get(tmp)][house_in.get(k)].isNotSet())
                                            insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(k) + 1) + ") ";
                                    }

                                    String remove_s = "Remove Candidate " + num + " from ";
                                    int changechk = changeCounter;
                                    for (int c = 0; c < 9; c++) {
                                        if (c != house_in.get(i) && c != house_in.get(j) && c != house_in.get(k)) {
                                            if (puzzle[other_house.get(0)][c].isCandidate(num) && puzzle[other_house.get(0)][c].isNotSet()) {
                                                changeCounter++;
                                                remove_s += "(" + (other_house.get(0) + 1) + "," + (c + 1) + ")" + " ";
                                                puzzle[other_house.get(0)][c].removeCandidate(num);
                                            }
                                            if (puzzle[other_house.get(1)][c].isCandidate(num) && puzzle[other_house.get(1)][c].isNotSet()) {
                                                changeCounter++;
                                                remove_s += "(" + (other_house.get(1) + 1) + "," + (c + 1) + ")" + " ";
                                                puzzle[other_house.get(1)][c].removeCandidate(num);
                                            }
                                            if (puzzle[other_house.get(2)][c].isCandidate(num) && puzzle[other_house.get(2)][c].isNotSet()) {
                                                changeCounter++;
                                                remove_s += "(" + (other_house.get(2) + 1) + "," + (c + 1) + ")" + " ";
                                                puzzle[other_house.get(2)][c].removeCandidate(num);
                                            }
                                        }
                                    }

                                    if (changechk != changeCounter) {
                                        algorithm.add(title);
                                        insert.add(insert_s);
                                        remove.add(remove_s);
                                    }

                                }
                            }
                        }
                    }
                }
            } else if (type == 3) {
                for (int i = 0; i < house_in.size() - 3; i++) {
                    for (int j = i + 1; j < house_in.size() - 2; j++) {
                        for (int k = j + 1; k < house_in.size() - 1; k++) {
                            for (int l = k + 1; l < house_in.size(); l++) {
                                ArrayList<Integer> other_house = compareCandidatePos(candidate_pos.get(i), candidate_pos.get(j), candidate_pos.get(k), candidate_pos.get(l));
                                if (other_house.size() == 4) {//four unique houses from where candidates are to be removed (jellyfish)
                                    if (which == 1) {
                                        String title = "Jellyfish in Row " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
                                                "," + (house_in.get(k) + 1) + " and " + (house_in.get(l) + 1);
                                        String insert_s = "Candidate " + num + " is common to cells ";
                                        for (int tmp = 0; tmp < other_house.size(); tmp++) {//list all swordfish cells
                                            if (puzzle[house_in.get(i)][other_house.get(tmp)].isCandidate(num)
                                                    && puzzle[house_in.get(i)][other_house.get(tmp)].isNotSet())
                                                insert_s += "(" + (house_in.get(i) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                            if (puzzle[house_in.get(j)][other_house.get(tmp)].isCandidate(num)
                                                    && puzzle[house_in.get(j)][other_house.get(tmp)].isNotSet())
                                                insert_s += "(" + (house_in.get(j) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                            if (puzzle[house_in.get(k)][other_house.get(tmp)].isCandidate(num)
                                                    && puzzle[house_in.get(k)][other_house.get(tmp)].isNotSet())
                                                insert_s += "(" + (house_in.get(k) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                            if (puzzle[house_in.get(l)][other_house.get(tmp)].isCandidate(num)
                                                    && puzzle[house_in.get(l)][other_house.get(tmp)].isNotSet())
                                                insert_s += "(" + (house_in.get(l) + 1) + "," + (other_house.get(tmp) + 1) + ") ";
                                        }

                                        String remove_s = "Remove Candidate " + num + " from ";
                                        int changechk = changeCounter;
                                        for (int r = 0; r < 9; r++) {
                                            if (r != house_in.get(i) && r != house_in.get(j) && r != house_in.get(k) && r != house_in.get(l)) {
                                                if (puzzle[r][other_house.get(0)].isCandidate(num) && puzzle[r][other_house.get(0)].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (r + 1) + "," + (other_house.get(0) + 1) + ")" + " ";
                                                    puzzle[r][other_house.get(0)].removeCandidate(num);
                                                }
                                                if (puzzle[r][other_house.get(1)].isCandidate(num) && puzzle[r][other_house.get(1)].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (r + 1) + "," + (other_house.get(1) + 1) + ")" + " ";
                                                    puzzle[r][other_house.get(1)].removeCandidate(num);
                                                }
                                                if (puzzle[r][other_house.get(2)].isCandidate(num) && puzzle[r][other_house.get(2)].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (r + 1) + "," + (other_house.get(2) + 1) + ")" + " ";
                                                    puzzle[r][other_house.get(2)].removeCandidate(num);
                                                }
                                                if (puzzle[r][other_house.get(3)].isCandidate(num) && puzzle[r][other_house.get(3)].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (r + 1) + "," + (other_house.get(3) + 1) + ")" + " ";
                                                    puzzle[r][other_house.get(3)].removeCandidate(num);
                                                }
                                            }
                                        }

                                        if (changechk != changeCounter) {
                                            algorithm.add(title);
                                            insert.add(insert_s);
                                            remove.add(remove_s);
                                        }

                                    } else {
                                        String title = "Jellyfish in Column " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
                                                "," + (house_in.get(k) + 1) + " and " + (house_in.get(l) + 1);
                                        String insert_s = "Candidate " + num + " is common to cells ";
                                        for (int tmp = 0; tmp < other_house.size(); tmp++) {
                                            if (puzzle[other_house.get(tmp)][house_in.get(i)].isCandidate(num)
                                                    && puzzle[other_house.get(tmp)][house_in.get(i)].isNotSet())
                                                insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(i) + 1) + ") ";
                                            if (puzzle[other_house.get(tmp)][house_in.get(j)].isCandidate(num)
                                                    && puzzle[other_house.get(tmp)][house_in.get(j)].isNotSet())
                                                insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(j) + 1) + ") ";
                                            if (puzzle[other_house.get(tmp)][house_in.get(k)].isCandidate(num)
                                                    && puzzle[other_house.get(tmp)][house_in.get(k)].isNotSet())
                                                insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(k) + 1) + ") ";
                                            if (puzzle[other_house.get(tmp)][house_in.get(l)].isCandidate(num)
                                                    && puzzle[other_house.get(tmp)][house_in.get(l)].isNotSet())
                                                insert_s += "(" + (other_house.get(tmp) + 1) + "," + (house_in.get(l) + 1) + ") ";
                                        }

                                        String remove_s = "Remove Candidate " + num + " from ";
                                        int changechk = changeCounter;
                                        for (int c = 0; c < 9; c++) {
                                            if (c != house_in.get(i) && c != house_in.get(j) && c != house_in.get(k) && c != house_in.get(l)) {
                                                if (puzzle[other_house.get(0)][c].isCandidate(num) && puzzle[other_house.get(0)][c].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (other_house.get(0) + 1) + "," + (c + 1) + ")" + " ";
                                                    puzzle[other_house.get(0)][c].removeCandidate(num);
                                                }
                                                if (puzzle[other_house.get(1)][c].isCandidate(num) && puzzle[other_house.get(1)][c].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (other_house.get(1) + 1) + "," + (c + 1) + ")" + " ";
                                                    puzzle[other_house.get(1)][c].removeCandidate(num);
                                                }
                                                if (puzzle[other_house.get(2)][c].isCandidate(num) && puzzle[other_house.get(2)][c].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (other_house.get(2) + 1) + "," + (c + 1) + ")" + " ";
                                                    puzzle[other_house.get(2)][c].removeCandidate(num);
                                                }
                                                if (puzzle[other_house.get(3)][c].isCandidate(num) && puzzle[other_house.get(3)][c].isNotSet()) {
                                                    changeCounter++;
                                                    remove_s += "(" + (other_house.get(3) + 1) + "," + (c + 1) + ")" + " ";
                                                    puzzle[other_house.get(3)][c].removeCandidate(num);
                                                }
                                            }
                                        }

                                        if (changechk != changeCounter) {
                                            algorithm.add(title);
                                            insert.add(insert_s);
                                            remove.add(remove_s);
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }// first if statement
    }

    private static void finnedXWing(Cell[][] puzzle) {
        finnedFish(puzzle, 1);
    }

    private static void finnedSwordfish(Cell[][] puzzle) {
        finnedFish(puzzle, 2);
    }

    private static void finnedJellyfish(Cell[][] puzzle) {
        finnedFish(puzzle, 3);
    }

    private static void finnedFish(Cell[][] puzzle, int type) {

        int freq;//counter to find fish , if present.

        for (int num = 1; num <= 9; num++) {

            //checking Rows
            ArrayList<Integer> r_eligibleFinRows = new ArrayList<>();//to store row of finned wing candidates
            ArrayList<int[]> r_eligibleFinColumns = new ArrayList<>();//to store column of finned wing candidates

            for (int r = 0; r < 9; r++) {//traversal
                freq = 0;
                int[] tmp_columns = new int[type + 4];//the temporary columns
                for (int c = 0; c < 9 && freq < type + 4; c++) {//max of 2 fins
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        tmp_columns[freq] = c;//store column in row which has num as a candidate
                        freq++;
                    }
                }
                if (freq != 0) {
                    for (int i = freq; i < tmp_columns.length; i++) {
                        tmp_columns[i] = -1;
                    }
                }
                if (freq > 0 && freq <= type + 3) {//catches the locked triplet of cells in a particular column
                    r_eligibleFinRows.add(r);//since the freq is type+1, there are only as many cells in the row that have num as a candidate (locked set)
                    r_eligibleFinColumns.add(tmp_columns);
                }

            }
            finnedFishRemoval(puzzle, r_eligibleFinRows, r_eligibleFinColumns, num, 1, type);


            //checking Columns
            ArrayList<Integer> c_eligibleFinColumns = new ArrayList<>();//to store column of finned wing candidates
            ArrayList<int[]> c_eligibleFinRows = new ArrayList<>();//to store row of finned wing candidates

            for (int c = 0; c < 9; c++) {
                freq = 0;
                int[] tmp_rows = new int[type + 4];//the temporary rows
                for (int r = 0; r < 9 && freq < type + 4; r++) {
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        tmp_rows[freq] = r;//store column in row which has num as a candidate
                        freq++;
                    }
                }
                if (freq != 0) {
                    for (int i = freq; i < tmp_rows.length; i++) {
                        tmp_rows[i] = -1;
                    }
                }
                if (freq > 0 && freq <= type + 3) {//catches the locked set of cells in a particular row
                    c_eligibleFinColumns.add(c);//since the freq is type +1, there are only as many cells in the column that have num as a candidate (locked set)
                    c_eligibleFinRows.add(tmp_rows);
                }
            }
            finnedFishRemoval(puzzle, c_eligibleFinColumns, c_eligibleFinRows, num, 2, type);
        }

    }

    private static void finnedFishRemoval(Cell[][] puzzle, ArrayList<Integer> houseFin_in, ArrayList<int[]> candidateFin_pos,
                                          int num, int which, int type) {

        if (houseFin_in.size() >= type + 1) {//more than the minimum number of houses required for finned xwing to be present
            if (type == 1) {
                for (int i = 0; i < houseFin_in.size() - 1; i++) {
                    for (int j = i + 1; j < houseFin_in.size(); j++) {//combination of prospective wings
                        ArrayList<Integer> sample_other_house = compareCandidatePos(candidateFin_pos.get(i), candidateFin_pos.get(j), null, null);//generate unique set of the other house
                        if (sample_other_house.size() > 2 && sample_other_house.size() < 5) {//for finned xWing, the other house must have a size of 3 or 4
                            for (int sample_1 = 0; sample_1 < sample_other_house.size() - 1; sample_1++) {
                                for (int sample_2 = sample_1 + 1; sample_2 < sample_other_house.size(); sample_2++) {//take a combination from the other house that could be the xWing

                                    ArrayList<Integer> tmp = new ArrayList<>(sample_other_house);
                                    tmp.remove(sample_other_house.get(sample_1));
                                    tmp.remove(sample_other_house.get(sample_2));//tmp now only contains the other house which could be fins
                                    ArrayList<int[]> fin_coordinates = new ArrayList<>();//the coordinates of the could be fins
                                    ArrayList<Integer> fin_in_first = finPresent(candidateFin_pos.get(i), tmp);//store the other house if prospective fin present
                                    ArrayList<Integer> fin_in_second = finPresent(candidateFin_pos.get(j), tmp);

                                    if (which == 1 && (fin_in_first == null || fin_in_second == null)) {//row wise finned X Wing prospective

                                        addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 1);
                                        addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 1);

                                        int[] subsquare_markers;
                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                            int sample_house_1 = sample_other_house.get(sample_1);
                                            int sample_house_2 = sample_other_house.get(sample_2);
                                            String title = "Finned X-Wing in Row " + (houseFin_in.get(i) + 1) + " and " + (houseFin_in.get(j) + 1);
                                            String insert_s = "Candidate " + num + " is common to X-Wing cells : ";
                                            String remove_s = "Remove Candidate " + num + " from ";
                                            String fins_s = ", with fin(s) : ";

                                            for (int temp = 0; temp < sample_other_house.size(); temp++) {//list all finned xwing cells
                                                String[] house_1_insert = finInserter(puzzle, houseFin_in.get(i), sample_other_house.get(temp), num, fin_coordinates);
                                                String[] house_2_insert = finInserter(puzzle, houseFin_in.get(j), sample_other_house.get(temp), num, fin_coordinates);
                                                insert_s = house_1_insert[0] != null ? insert_s + house_1_insert[0] : insert_s;
                                                insert_s = house_2_insert[0] != null ? insert_s + house_2_insert[0] : insert_s;
                                                fins_s = house_1_insert[1] != null ? fins_s + house_1_insert[1] : fins_s;
                                                fins_s = house_2_insert[1] != null ? fins_s + house_2_insert[1] : fins_s;
                                            }

                                            int var = changeCounter;
                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_1, houseFin_in.get(i), houseFin_in.get(j), null, null, 1, num);
                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_2, houseFin_in.get(i), houseFin_in.get(j), null, null, 1, num);

                                            if (var != changeCounter) {
                                                algorithm.add(title);
                                                insert.add(insert_s + fins_s);
                                                remove.add(remove_s);
                                            }
                                        }
                                    } else if (which == 2 && (fin_in_first == null || fin_in_second == null)) {

                                        addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 2);
                                        addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 2);

                                        int[] subsquare_markers;
                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {
                                            int sample_house_1 = sample_other_house.get(sample_1);
                                            int sample_house_2 = sample_other_house.get(sample_2);
                                            String title = "Finned X-Wing in Column " + (houseFin_in.get(i) + 1) + " and " + (houseFin_in.get(j) + 1);
                                            String insert_s = "Candidate " + num + " is common to X-Wing cells : ";
                                            String remove_s = "Remove Candidate " + num + " from ";
                                            String fins_s = ", with fin(s) : ";

                                            for (int temp = 0; temp < sample_other_house.size(); temp++) {//list all finned xwing cells
                                                String[] house_1_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(i), num, fin_coordinates);
                                                String[] house_2_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(j), num, fin_coordinates);
                                                insert_s = house_1_insert[0] != null ? insert_s + house_1_insert[0] : insert_s;
                                                insert_s = house_2_insert[0] != null ? insert_s + house_2_insert[0] : insert_s;
                                                fins_s = house_1_insert[1] != null ? fins_s + house_1_insert[1] : fins_s;
                                                fins_s = house_2_insert[1] != null ? fins_s + house_2_insert[1] : fins_s;
                                            }

                                            int var = changeCounter;
                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_1, houseFin_in.get(i), houseFin_in.get(j), null, null, 2, num);
                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_2, houseFin_in.get(i), houseFin_in.get(j), null, null, 2, num);

                                            if (var != changeCounter) {
                                                algorithm.add(title);
                                                insert.add(insert_s + fins_s);
                                                remove.add(remove_s);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (type == 2) {
                for (int i = 0; i < houseFin_in.size() - 2; i++) {
                    for (int j = i + 1; j < houseFin_in.size() - 1; j++) {
                        for (int k = j + 1; k < houseFin_in.size(); k++) {
                            ArrayList<Integer> sample_other_house = compareCandidatePos(candidateFin_pos.get(i), candidateFin_pos.get(j),
                                    candidateFin_pos.get(k), null);
                            if (sample_other_house.size() > 3 && sample_other_house.size() < 6) {
                                for (int sample_1 = 0; sample_1 < sample_other_house.size() - 2; sample_1++) {
                                    for (int sample_2 = sample_1 + 1; sample_2 < sample_other_house.size() - 1; sample_2++) {//take a combination from the other house that could be the swordfish
                                        for (int sample_3 = sample_2 + 1; sample_3 < sample_other_house.size(); sample_3++) {
                                            ArrayList<Integer> tmp = new ArrayList<>(sample_other_house);
                                            tmp.remove(sample_other_house.get(sample_1));
                                            tmp.remove(sample_other_house.get(sample_2));//tmp now only contains the other house which could be fins
                                            tmp.remove(sample_other_house.get(sample_3));
                                            ArrayList<int[]> fin_coordinates = new ArrayList<>();//the coordinates of the could be fins
                                            ArrayList<Integer> fin_in_first = finPresent(candidateFin_pos.get(i), tmp);//store the other house if prospective fin present
                                            ArrayList<Integer> fin_in_second = finPresent(candidateFin_pos.get(j), tmp);
                                            ArrayList<Integer> fin_in_third = finPresent(candidateFin_pos.get(k), tmp);
                                            if (which == 1) {

                                                addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 1);
                                                addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 1);
                                                addFinCoordinates(fin_in_third, houseFin_in.get(k), fin_coordinates, 1);

                                                int[] subsquare_markers;
                                                if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                    int sample_house_1 = sample_other_house.get(sample_1);
                                                    int sample_house_2 = sample_other_house.get(sample_2);
                                                    int sample_house_3 = sample_other_house.get(sample_3);
                                                    String title = "Finned Swordfish in Row " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " and " + (houseFin_in.get(k) + 1);
                                                    String insert_s = "Candidate " + num + " is common to Swordfish cells : ";
                                                    String remove_s = "Remove Candidate " + num + " from ";
                                                    String fins_s = ", with fin(s) : ";
                                                    for (int temp = 0; temp < sample_other_house.size(); temp++) {//list all finned swordfish cells

                                                        String[] house_1_insert = finInserter(puzzle, houseFin_in.get(i), sample_other_house.get(temp), num, fin_coordinates);
                                                        String[] house_2_insert = finInserter(puzzle, houseFin_in.get(j), sample_other_house.get(temp), num, fin_coordinates);
                                                        String[] house_3_insert = finInserter(puzzle, houseFin_in.get(k), sample_other_house.get(temp), num, fin_coordinates);
                                                        insert_s = house_1_insert[0] != null ? insert_s + house_1_insert[0] : insert_s;
                                                        insert_s = house_2_insert[0] != null ? insert_s + house_2_insert[0] : insert_s;
                                                        insert_s = house_3_insert[0] != null ? insert_s + house_3_insert[0] : insert_s;
                                                        fins_s = house_1_insert[1] != null ? fins_s + house_1_insert[1] : fins_s;
                                                        fins_s = house_2_insert[1] != null ? fins_s + house_2_insert[1] : fins_s;
                                                        fins_s = house_3_insert[1] != null ? fins_s + house_3_insert[1] : fins_s;

                                                    }

                                                    int var = changeCounter;
                                                    remove_s += finRemover(puzzle, subsquare_markers, sample_house_1, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), null, 1, num);
                                                    remove_s += finRemover(puzzle, subsquare_markers, sample_house_2, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), null, 1, num);
                                                    remove_s += finRemover(puzzle, subsquare_markers, sample_house_3, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), null, 1, num);

                                                    if (var != changeCounter) {
                                                        algorithm.add(title);
                                                        insert.add(insert_s + fins_s);
                                                        remove.add(remove_s);
                                                    }
                                                }
                                            } else {

                                                addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 2);
                                                addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 2);
                                                addFinCoordinates(fin_in_third, houseFin_in.get(k), fin_coordinates, 2);

                                                int[] subsquare_markers;
                                                if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                    int sample_house_1 = sample_other_house.get(sample_1);
                                                    int sample_house_2 = sample_other_house.get(sample_2);
                                                    int sample_house_3 = sample_other_house.get(sample_3);
                                                    String title = "Finned Swordfish in Columns " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " and " + (houseFin_in.get(k) + 1);
                                                    String insert_s = "Candidate " + num + " is common to Swordfish cells : ";
                                                    String remove_s = "Remove Candidate " + num + " from ";
                                                    String fins_s = ", with fin(s) : ";

                                                    for (int temp = 0; temp < sample_other_house.size(); temp++) {//list all finned swordfish cells

                                                        String[] house_1_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(i), num, fin_coordinates);
                                                        String[] house_2_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(j), num, fin_coordinates);
                                                        String[] house_3_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(k), num, fin_coordinates);
                                                        insert_s = house_1_insert[0] != null ? insert_s + house_1_insert[0] : insert_s;
                                                        insert_s = house_2_insert[0] != null ? insert_s + house_2_insert[0] : insert_s;
                                                        insert_s = house_3_insert[0] != null ? insert_s + house_3_insert[0] : insert_s;
                                                        fins_s = house_1_insert[1] != null ? fins_s + house_1_insert[1] : fins_s;
                                                        fins_s = house_2_insert[1] != null ? fins_s + house_2_insert[1] : fins_s;
                                                        fins_s = house_3_insert[1] != null ? fins_s + house_3_insert[1] : fins_s;

                                                    }

                                                    int var = changeCounter;
                                                    remove_s += finRemover(puzzle, subsquare_markers, sample_house_1, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), null, 2, num);
                                                    remove_s += finRemover(puzzle, subsquare_markers, sample_house_2, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), null, 2, num);
                                                    remove_s += finRemover(puzzle, subsquare_markers, sample_house_3, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), null, 2, num);

                                                    if (var != changeCounter) {
                                                        algorithm.add(title);
                                                        insert.add(insert_s + fins_s);
                                                        remove.add(remove_s);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (type == 3) {
                for (int i = 0; i < houseFin_in.size() - 3; i++) {
                    for (int j = i + 1; j < houseFin_in.size() - 2; j++) {
                        for (int k = j + 1; k < houseFin_in.size() - 1; k++) {
                            for (int l = k + 1; l < houseFin_in.size(); l++) {
                                ArrayList<Integer> sample_other_house = compareCandidatePos(candidateFin_pos.get(i), candidateFin_pos.get(j),
                                        candidateFin_pos.get(k), candidateFin_pos.get(l));
                                if (sample_other_house.size() > 4 && sample_other_house.size() < 7) {
                                    for (int sample_1 = 0; sample_1 < sample_other_house.size() - 3; sample_1++) {
                                        for (int sample_2 = sample_1 + 1; sample_2 < sample_other_house.size() - 2; sample_2++) {//take a combination from the other house that could be the swordfish
                                            for (int sample_3 = sample_2 + 1; sample_3 < sample_other_house.size() - 1; sample_3++) {
                                                for (int sample_4 = sample_3 + 1; sample_4 < sample_other_house.size(); sample_4++) {
                                                    ArrayList<Integer> tmp = new ArrayList<>(sample_other_house);
                                                    tmp.remove(sample_other_house.get(sample_1));
                                                    tmp.remove(sample_other_house.get(sample_2));//tmp now only contains the other house which could be fins
                                                    tmp.remove(sample_other_house.get(sample_3));
                                                    tmp.remove(sample_other_house.get(sample_4));
                                                    ArrayList<int[]> fin_coordinates = new ArrayList<>();//the coordinates of the could be fins
                                                    ArrayList<Integer> fin_in_first = finPresent(candidateFin_pos.get(i), tmp);//store the other house if prospective fin present
                                                    ArrayList<Integer> fin_in_second = finPresent(candidateFin_pos.get(j), tmp);
                                                    ArrayList<Integer> fin_in_third = finPresent(candidateFin_pos.get(k), tmp);
                                                    ArrayList<Integer> fin_in_fourth = finPresent(candidateFin_pos.get(l), tmp);
                                                    if (which == 1) {

                                                        addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 1);
                                                        addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 1);
                                                        addFinCoordinates(fin_in_third, houseFin_in.get(k), fin_coordinates, 1);
                                                        addFinCoordinates(fin_in_fourth, houseFin_in.get(l), fin_coordinates, 1);

                                                        int[] subsquare_markers;
                                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                            int sample_house_1 = sample_other_house.get(sample_1);
                                                            int sample_house_2 = sample_other_house.get(sample_2);
                                                            int sample_house_3 = sample_other_house.get(sample_3);
                                                            int sample_house_4 = sample_other_house.get(sample_4);
                                                            String title = "Finned Jellyfish in Row " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " , " + (houseFin_in.get(k) + 1) +
                                                                    " and " + (houseFin_in.get(l) + 1);
                                                            String insert_s = "Candidate " + num + " is common to Jellyfish cells : ";
                                                            String remove_s = "Remove Candidate " + num + " from ";
                                                            String fins_s = ", with fin(s) : ";

                                                            for (int temp = 0; temp < sample_other_house.size(); temp++) {//list all finned swordfish cells

                                                                String[] house_1_insert = finInserter(puzzle, houseFin_in.get(i), sample_other_house.get(temp), num, fin_coordinates);
                                                                String[] house_2_insert = finInserter(puzzle, houseFin_in.get(j), sample_other_house.get(temp), num, fin_coordinates);
                                                                String[] house_3_insert = finInserter(puzzle, houseFin_in.get(k), sample_other_house.get(temp), num, fin_coordinates);
                                                                String[] house_4_insert = finInserter(puzzle, houseFin_in.get(l), sample_other_house.get(temp), num, fin_coordinates);
                                                                insert_s = house_1_insert[0] != null ? insert_s + house_1_insert[0] : insert_s;
                                                                insert_s = house_2_insert[0] != null ? insert_s + house_2_insert[0] : insert_s;
                                                                insert_s = house_3_insert[0] != null ? insert_s + house_3_insert[0] : insert_s;
                                                                insert_s = house_4_insert[0] != null ? insert_s + house_4_insert[0] : insert_s;
                                                                fins_s = house_1_insert[1] != null ? fins_s + house_1_insert[1] : fins_s;
                                                                fins_s = house_2_insert[1] != null ? fins_s + house_2_insert[1] : fins_s;
                                                                fins_s = house_3_insert[1] != null ? fins_s + house_3_insert[1] : fins_s;
                                                                fins_s = house_4_insert[1] != null ? fins_s + house_4_insert[1] : fins_s;

                                                            }

                                                            int var = changeCounter;
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_1, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 1, num);
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_2, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 1, num);
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_3, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 1, num);
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_4, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 1, num);

                                                            if (var != changeCounter) {
                                                                algorithm.add(title);
                                                                insert.add(insert_s + fins_s);
                                                                remove.add(remove_s);
                                                            }
                                                        }

                                                    } else {

                                                        addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 2);
                                                        addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 2);
                                                        addFinCoordinates(fin_in_third, houseFin_in.get(k), fin_coordinates, 2);
                                                        addFinCoordinates(fin_in_fourth, houseFin_in.get(l), fin_coordinates, 2);

                                                        int[] subsquare_markers;
                                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                            int sample_house_1 = sample_other_house.get(sample_1);
                                                            int sample_house_2 = sample_other_house.get(sample_2);
                                                            int sample_house_3 = sample_other_house.get(sample_3);
                                                            int sample_house_4 = sample_other_house.get(sample_4);
                                                            String title = "Finned Jellyfish in Columns " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " , " + (houseFin_in.get(k) + 1) +
                                                                    " and " + (houseFin_in.get(l) + 1);
                                                            String insert_s = "Candidate " + num + " is common to Jellyfish cells : ";
                                                            String remove_s = "Remove Candidate " + num + " from ";
                                                            String fins_s = ", with fin(s) : ";

                                                            for (int temp = 0; temp < sample_other_house.size(); temp++) {//list all finned swordfish cells

                                                                String[] house_1_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(i), num, fin_coordinates);
                                                                String[] house_2_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(j), num, fin_coordinates);
                                                                String[] house_3_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(k), num, fin_coordinates);
                                                                String[] house_4_insert = finInserter(puzzle, sample_other_house.get(temp), houseFin_in.get(l), num, fin_coordinates);
                                                                insert_s = house_1_insert[0] != null ? insert_s + house_1_insert[0] : insert_s;
                                                                insert_s = house_2_insert[0] != null ? insert_s + house_2_insert[0] : insert_s;
                                                                insert_s = house_3_insert[0] != null ? insert_s + house_3_insert[0] : insert_s;
                                                                insert_s = house_4_insert[0] != null ? insert_s + house_4_insert[0] : insert_s;
                                                                fins_s = house_1_insert[1] != null ? fins_s + house_1_insert[1] : fins_s;
                                                                fins_s = house_2_insert[1] != null ? fins_s + house_2_insert[1] : fins_s;
                                                                fins_s = house_3_insert[1] != null ? fins_s + house_3_insert[1] : fins_s;
                                                                fins_s = house_4_insert[1] != null ? fins_s + house_4_insert[1] : fins_s;

                                                            }

                                                            int var = changeCounter;
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_1, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 2, num);
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_2, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 2, num);
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_3, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 2, num);
                                                            remove_s += finRemover(puzzle, subsquare_markers, sample_house_4, houseFin_in.get(i), houseFin_in.get(j), houseFin_in.get(k), houseFin_in.get(l), 2, num);

                                                            if (var != changeCounter) {
                                                                algorithm.add(title);
                                                                insert.add(insert_s + fins_s);
                                                                remove.add(remove_s);
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private static String finRemover(Cell[][] puzzle, int[] subsquare_markers, int constant_house, Integer skip_house, Integer skip_house2, Integer skip_house3, Integer skip_house4, int which, int num) {

        String remove_s = "";
        if (which == 1) {
            if (constant_house - constant_house % 3 == subsquare_markers[1]) {
                for (int row = subsquare_markers[0]; row <= subsquare_markers[0] + 2; row++) {
                    if (!((Integer) row).equals(skip_house) && !((Integer) row).equals(skip_house2) && !((Integer) row).equals(skip_house3) && !((Integer) row).equals(skip_house4)) {
                        if (puzzle[row][constant_house].isCandidate(num) && puzzle[row][constant_house].isNotSet()) {
                            puzzle[row][constant_house].removeCandidate(num);
                            changeCounter++;
                            remove_s += "(" + (row + 1) + "," + (constant_house + 1) + ") ";
                        }
                    }
                }
            }
        } else {
            if (constant_house - constant_house % 3 == subsquare_markers[0]) {
                for (int col = subsquare_markers[1]; col <= subsquare_markers[1] + 2; col++) {
                    if (!((Integer) col).equals(skip_house) && !((Integer) col).equals(skip_house2) && !((Integer) col).equals(skip_house3) && !((Integer) col).equals(skip_house4)) {
                        if (puzzle[constant_house][col].isCandidate(num) && puzzle[constant_house][col].isNotSet()) {
                            puzzle[constant_house][col].removeCandidate(num);
                            changeCounter++;
                            remove_s += "(" + (constant_house + 1) + "," + (col + 1) + ") ";
                        }
                    }
                }
            }
        }
        return remove_s;

    }

    private static String[] finInserter(Cell[][] puzzle, int row, int column, int num, ArrayList<int[]> fin_coordinates) {

        String[] finInsert = new String[2];
        finInsert[0] = "";
        finInsert[1] = "";
        if (puzzle[row][column].isCandidate(num)
                && puzzle[row][column].isNotSet() &&
                !contains(fin_coordinates, new int[]{row, column}))
            finInsert[0] += "(" + (row + 1) + "," + (column + 1) + ") ";
        else if (puzzle[row][column].isCandidate(num)
                && puzzle[row][column].isNotSet() &&
                contains(fin_coordinates, new int[]{row, column}))
            finInsert[1] += "(" + (row + 1) + "," + (column + 1) + ") ";

        return finInsert;

    }

    private static void addFinCoordinates(ArrayList<Integer> finnedOtherHouse, Integer finHouse, ArrayList<int[]> fin_coordinates, int which) {
        if (which == 1) {
            if (finnedOtherHouse != null) {//prospective fin is in the first column
                int[] first = new int[2];
                first[1] = finnedOtherHouse.get(0);
                first[0] = finHouse;
                fin_coordinates.add(first);
                if (finnedOtherHouse.size() == 2) {
                    int[] second = new int[2];
                    second[1] = finnedOtherHouse.get(1);
                    second[0] = finHouse;
                    fin_coordinates.add(second);
                }
            }
        } else {
            if (finnedOtherHouse != null) {//prospective fin is in the first column
                int[] first = new int[2];
                first[0] = finnedOtherHouse.get(0);
                first[1] = finHouse;
                fin_coordinates.add(first);
                if (finnedOtherHouse.size() == 2) {
                    int[] second = new int[2];
                    second[0] = finnedOtherHouse.get(1);
                    second[1] = finHouse;
                    fin_coordinates.add(second);
                }
            }
        }
    }

    private static boolean contains(ArrayList<int[]> fin_coordinates, int[] integers) {
        for (int[] bytearr : fin_coordinates) {
            if (Arrays.equals(bytearr, integers)) {
                return true;
            }
        }
        return false;
    }

    private static int[] commonSubsquare(ArrayList<int[]> fin_coordinates) {
        int row_s = (fin_coordinates.get(0)[0] - fin_coordinates.get(0)[0] % 3);
        int col_s = (fin_coordinates.get(0)[1] - fin_coordinates.get(0)[1] % 3);
        for (int i = 1; i < fin_coordinates.size(); i++) {
            if (!((fin_coordinates.get(i)[0] - fin_coordinates.get(i)[0] % 3) == row_s &&
                    (fin_coordinates.get(i)[1] - fin_coordinates.get(i)[1] % 3) == col_s)) {
                return null;
            }
        }
        return new int[]{row_s, col_s};
    }

    private static ArrayList<Integer> finPresent(int[] integers, ArrayList<Integer> tmp) {
        ArrayList<Integer> output = new ArrayList<>();
        for (int aByte : integers) {
            if (tmp.contains(aByte)) {
                output.add(aByte);
            }
        }
        if (output.size() > 0) {
            return output;
        }
        return null;
    }

    private static ArrayList<Integer> compareCandidatePos(int[] integers, int[] bytes1, int[] bytes2, int[] bytes3) {
        ArrayList<Integer> unique_dig = new ArrayList<>();
        if (integers != null) {
            for (int aByte : integers) {
                if (aByte != -1)
                    unique_dig.add(aByte);
            }
        }
        if (bytes1 != null) {
            for (int b : bytes1) {
                if (b != -1 && !unique_dig.contains(b))
                    unique_dig.add(b);
            }
        }
        if (bytes2 != null) {
            for (int b : bytes2) {
                if (b != -1 && !unique_dig.contains(b))
                    unique_dig.add(b);
            }
        }
        if (bytes3 != null) {
            for (int b : bytes3) {
                if (b != -1 && !unique_dig.contains(b))
                    unique_dig.add(b);
            }
        }
        return unique_dig;
    }// end of compareCandidatePos()      


    /**
     * A last resort for the sudoku solver to solve puzzles if all of the previous logics do not work
     */
    private static boolean bruteForce(Cell[][] puzzle, int previous_row) {//brute forcing will be done row wise
        int noOfCandidates;
        int[] coordinates = findNextEmptyCell(puzzle, previous_row);
        int r = coordinates[0];
        int c = coordinates[1];
        if (r == 9)//no more empty cells remaining, which means all cells have been successfully filled
            return true;
        else {
            noOfCandidates = puzzle[r][c].getNoOfCandidates();
            for (int inc = 0; inc < noOfCandidates; inc++) {
                if (numCanExistInCell(puzzle, r, c, puzzle[r][c].getCandidate(inc))) {
                    puzzle[r][c].placeSolution(puzzle[r][c].getCandidate(inc));
                    if (bruteForce(puzzle, r)) {//if further brute force was successful
                        return true;
                    }//second if statement
                }//first if statement
            }//for loop
            puzzle[r][c].placeSolution(0);//making the cell empty again
            return false;//if no candidate can be successfully placed in the cell
        }
    }//end of bruteForce()

    private static int[] findNextEmptyCell(Cell[][] puzzle, int r) {// finding next empty cell for the brute force function
        for (; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (puzzle[r][c].isNotSet())
                    return new int[]{r, c};//returning the coordinates of the next empty cell
            }
        }
        return new int[]{9, 9};//returning (9,9) if there are no more empty cells remaining in the puzzle
    }// end of findNextEmptyCell()

    private static boolean numCanExistInCell(Cell[][] puzzle, int row, int col, int num) {//checks if a number can exist in a particular cell
        //checking row
        for (int c = 0; c < 9; c++) {
            if (num == puzzle[row][c].getSolution())
                return false;
        }

        //checking column
        for (int r = 0; r < 9; r++) {
            if (num == puzzle[r][col].getSolution())
                return false;
        }

        //checking subsquare
        int tmp_r = (row - (row % 3));// starting row of current subsquare
        int tmp_c = (col - (col % 3));// starting column of current subsquare
        for (int r = tmp_r; r < tmp_r + 3; r++) {
            for (int c = tmp_c; c < tmp_c + 3; c++) {
                if (num == puzzle[r][c].getSolution()) {// avoid the current column and check if num is a candidate in the cell
                    return false;
                }
            }
        }

        return true;//if the number can exist in the cell, return true
    }// end of numCanExistInCell()

    public static void partiallySolve(Cell[][] puzzle, ArrayList<String> chosenStrategies) {

        final String[] logics = {"Naked Single", "Hidden Single", "Naked Pair", "Pointing Pair",
                "Claiming Pair", "Hidden Pair", "Naked Triple", "Hidden Triple", "X-Wing", "Swordfish", "Jellyfish",
                "Naked Quad", "Hidden Quad", "Finned X-Wing", "Finned Swordfish", "Finned Jellyfish", "Brute Force"};

        boolean[] strategy = new boolean[logics.length];//array that indicates which solving strategies the user wants to use
        //Order of the strategies is the same as that of the array logics[] shown above

        for (int i = 0; i < logics.length; i++) {//loop to check which all solving strategies (algorithms) have been chosen by the user
            if (chosenStrategies.contains(logics[i])) {
                strategy[i] = true;
            }
        }

        int levelUpdater = 0;//this variable determines which all algorithms are used in an iteration of the do while loop below,
        //higher the value of levelUpdater - more complex algorithms are used

        int beforeUsingStrategy;//temporary variable used to check if there is a change in the value of changeCounter after a certain strategy is used

        do {//loop to call the functions to solve the puzzle
            changeCounter = 0;//reinitializing the counter to 0 before the start of every iteration
            switch (levelUpdater) {

                case 15:
                    if (strategy[15]) {
                        beforeUsingStrategy = changeCounter;
                        finnedJellyfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedJellyfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 14:
                    if (strategy[14]) {
                        beforeUsingStrategy = changeCounter;
                        finnedSwordfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedSwordfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 13:
                    if (strategy[13]) {
                        beforeUsingStrategy = changeCounter;
                        finnedXWing(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedXWing()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 12:
                    if (strategy[12]) {
                        beforeUsingStrategy = changeCounter;
                        hiddenQuad(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by hiddenQuad()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 11:
                    if (strategy[11]) {
                        beforeUsingStrategy = changeCounter;
                        nakedQuad(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by nakedQuad()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 10:
                    if (strategy[10]) {
                        beforeUsingStrategy = changeCounter;
                        jellyfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by jellyfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 9:
                    if (strategy[9]) {
                        beforeUsingStrategy = changeCounter;
                        swordfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by swordfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 8:
                    if (strategy[8]) {
                        beforeUsingStrategy = changeCounter;
                        xWing(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by xWing()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 7:
                    if (strategy[7]) {
                        beforeUsingStrategy = changeCounter;
                        hiddenTriple(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by hiddenTriple()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 6:
                    if (strategy[6]) {
                        beforeUsingStrategy = changeCounter;
                        nakedTriple(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by nakedTriple()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 5:
                    if (strategy[5]) {
                        beforeUsingStrategy = changeCounter;
                        hiddenPair(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by hiddenPair()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 4:
                    if (strategy[4]) {
                        beforeUsingStrategy = changeCounter;
                        claimingPair(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by claimingPair()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 3:
                    if (strategy[3]) {
                        beforeUsingStrategy = changeCounter;
                        pointingPair(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by pointingPair()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 2:
                    if (strategy[2]) {
                        beforeUsingStrategy = changeCounter;
                        nakedPair(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by nakedPair()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 1:
                    if (strategy[1])
                        hiddenSingle(puzzle);

                case 0:
                    if (strategy[0])
                        nakedSingle(puzzle);
            }//end of switch case

            if (cellCount == 81)//checking if the puzzle is solved (all 81 cells will be filled), if true, exit the loop
                break;

            if (changeCounter == 0)//if no changes have been made to the puzzle with the current set of algorithms, increase the level to utilize more complex solving strategies
                levelUpdater++;

        } while (levelUpdater <= 15);//if the levelUpdater goes greater than 15, it means after using the chosen algorithms - the puzzle could still not be solved

        //if the solving strategies could not completely solve the puzzle, resort to brute force
        if (cellCount != 81 && strategy[logics.length - 1]) {//checking if the puzzle is unsolved AND the user has chosen to use Brute Force
            bruteForce(puzzle, 0);//calling the brute force method
            algorithm.add("Brute Force");
            insert.add("Solvadoku solved the rest of this puzzle using brute force. There may be " +
                    "some other logic to solve the puzzle or none at all if it is a puzzle with multiple solutions");
            remove.add("");
            cellCount = 81;//brute force will always output a complete solution for the puzzle, hence all 81 cells are filled
        }

        for (int i = 0; i < algorithm.size(); i++) {
            viewHolderList.add(new String[3]);
            viewHolderList.get(i)[0] = algorithm.get(i);
            viewHolderList.get(i)[1] = insert.get(i);
            viewHolderList.get(i)[2] = remove.get(i);
        }

    }// end of partiallySolve()
} // end of Class Sudoku
