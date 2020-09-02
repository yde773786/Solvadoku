package com.yde.solvadoku;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A utility class that consists of all the operations that are to be performed to the sudoku puzzle
 */
public final class Sudoku {

    private static int changeCounter = 0;
    public static int cellCount = 0;
    public static ArrayList<String> algorithm = new ArrayList<>();
    public static ArrayList<String> insert = new ArrayList<>();
    public static ArrayList<String> remove = new ArrayList<>();
    public static ArrayList<String[]> viewHolderList = new ArrayList<>();

    private Sudoku() {
    }

    /**
     * placeNumber places a value in the cell of certain row and column in the puzzle. it also
     * alters the value of hasChanged to true signifying that the puzzle has been altered in some way or form
     */
    public static String placeNumber(Cell[][] puzzle, byte row, byte col, byte val) {

        changeCounter++;
        cellCount++;
        puzzle[row][col].placeSolution(val);
        int removeCounter = 0;
        String remove = "Remove Candidate " + val + " at ";

        for (byte r = 0; r < 9; r++)//loop running across the column
        {
            if (puzzle[r][col].isCandidate(val) && puzzle[r][col].isNotSet()) {//remove candidates from cells that are not set
                removeCounter++;
                remove += "(" + (r + 1) + "," + (col + 1) + ")" + " ";
                puzzle[r][col].removeCandidate(val);
            }
        }

        for (byte c = 0; c < 9; c++)//loop running across the row
        {
            if (puzzle[row][c].isCandidate(val) && puzzle[row][c].isNotSet()) {//remove candidates from cells that are not set
                removeCounter++;
                remove += "(" + (row + 1) + "," + (c + 1) + ")" + " ";
                puzzle[row][c].removeCandidate(val);
            }
        }

        byte tmp_r = (byte) (row - (row % 3));//starting point (Row) of the subsquare to which the current cell belongs to
        byte tmp_c = (byte) (col - (col % 3));//starting point (Column) of the subsquare to which the current cell belongs to
        for (byte r = tmp_r; r <= tmp_r + 2; r++) {//traverse subsquare
            for (byte c = tmp_c; c <= tmp_c + 2; c++) {//traverse subsquare
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
        for (byte r = 0; r < 9; r++) {//traversing the entire puzzle
            for (byte c = 0; c < 9; c++) {
                if (puzzle[r][c].getNoOfCandidates() == 1 && puzzle[r][c].isNotSet()) {//if only one Candidate and nothing is inserted, insert the value in the puzzle at the current row and column
                    algorithm.add("Naked Single");
                    byte val = puzzle[r][c].getCandidate(0);//since there is only one Candidate, it occupies position 0
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

        byte freq;//find when hidden single has been found, if present.

        for (byte num = 1; num < 10; num++) {//running through all digits 1 to 9 to check for hidden Singles that contain only num as a candidate

            byte index_c = 0;//the column of the cell which is a hiddenSingle
            for (byte r = 0; r < 9; r++) {//traversal
                freq = 0;
                for (byte c = 0; c < 9 && freq < 2; c++) {
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        freq++;
                        index_c = c;
                    }
                }
                if (freq == 1) {//catches the hidden Single. If there was no candidate, freq would be 0 and if there were many candidates , freq would be 2
                    hiddenSingleCheckSteps(puzzle, num, r, index_c, (byte) 1);
                }
            }


            byte index_r = 0; //the row of the cell which is a hiddenSingle

            for (byte c = 0; c < 9; c++) {//traveral
                freq = 0;
                for (byte r = 0; r < 9 && freq < 2; r++) {
                    if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                        freq++;
                        index_r = r;
                    }
                }
                if (freq == 1) {//catches the hidden Single. If there was no candidate, freq would be 0 and if there were many candidates , freq would be 2
                    hiddenSingleCheckSteps(puzzle, num, index_r, c, (byte) 2);
                }
            }

            index_c = 0;
            index_r = 0;
            for (byte r1 = 0; r1 <= 6; r1 += 3) {//jumps to first row position of subsquare
                for (byte c1 = 0; c1 <= 6; c1 += 3) {//jumps to first column position of subsquare
                    freq = 0;
                    for (byte r = r1; r < r1 + 3; r++) {//traversal (subsquare)
                        for (byte c = c1; c < c1 + 3; c++) {
                            if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {//if candidate and not set
                                freq++;
                                index_r = r;
                                index_c = c;
                            }
                        }
                    }
                    if (freq == 1) {//catches the hidden Single. If there was no candidate, freq would be 0 and if there were many candidates , freq would be 2
                        hiddenSingleCheckSteps(puzzle, num, index_r, index_c, (byte) 3);
                    }
                }
            }
        }
    }// end of hiddenSingle()

    /**
     * add changes made by  hidden single to the check steps linked lists
     */
    private static void hiddenSingleCheckSteps(Cell[][] puzzle, byte num, byte row, byte col, byte house) {

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
        for (byte r = 0; r < 9; r++) {

            boolean pairNotFound = true;//to find if a pair has been found or not
            byte[] skip_columns = new byte[2];// to find out which columns to skip over when removing candidates ( the naked pairs columns )
            byte[] the_pair = new byte[2];// to store the pair of values that are common to the naked pair
            String removeFirst = "";//first naked pair candidate string
            String removeSecond = "";//second naked pair candidate string
            boolean chkremoveFirst, chkremoveSecond;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = false;

            for (byte c = 1; c < 9; c++) {// each column in row

                if (pairNotFound) {//if a naked pair has not been found

                        /* a loop running from the previous cell in the row till the start of that row (column 0).
                            Checks if the current cell (at [r,c]) has 2 candidates and is not set first.*/
                    for (byte rev_c = (byte) (c - 1); rev_c >= 0 && puzzle[r][c].getNoOfCandidates() == 2 && puzzle[r][c].isNotSet(); rev_c--) {

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
                        ArrayList<Byte> removeList = eliminateFromCell(puzzle[r][c], the_pair);
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

            nakedPairCheckSteps(chkremoveFirst, chkremoveSecond, removeFirst, removeSecond, the_pair, r, skip_columns[0], r, skip_columns[1], (byte) 1);

        }

        /* find naked pairs in each column */
        for (byte c = 0; c < 9; c++) {

            boolean pairNotFound = true;//to find if a pair has been found or not
            byte[] skip_rows = new byte[2];// to find out which rows to skip over when removing candidates ( the naked pairs rows )
            byte[] the_pair = new byte[2];// to store the pair of values that are common to the naked pair
            String removeFirst = "";//first naked pair candidate string
            String removeSecond = "";//second naked pair candidate string
            boolean chkremoveFirst, chkremoveSecond;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = false;

            for (byte r = 1; r < 9; r++) {// each row in column

                if (pairNotFound) {//if a naked pair has not been found

                        /* a loop running from the previous cell in the column till the start of that column (row 0).
                            Checks if the current cell (at [r,c]) has 2 candidates and is not set first.*/
                    for (byte rev_r = (byte) (r - 1); rev_r >= 0 && puzzle[r][c].getNoOfCandidates() == 2 && puzzle[r][c].isNotSet(); rev_r--) {

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
                        ArrayList<Byte> removeList = eliminateFromCell(puzzle[r][c], the_pair);
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

            nakedPairCheckSteps(chkremoveFirst, chkremoveSecond, removeFirst, removeSecond, the_pair, skip_rows[0], c, skip_rows[1], c, (byte) 2);

        }

        /* find naked pairs in a subsquare */
        for (byte r1 = 0; r1 <= 6; r1 += 3) {//travers subsquare
            for (byte c1 = 0; c1 <= 6; c1 += 3) {

                boolean pairNotFound = true;//to find if a pair has been found or not
                byte[][] skip_index = new byte[2][2];// to find out which indexes to skip over when removing candidates ( the naked pairs of the subsquare )
                byte[] the_pair = new byte[2];// to store the pair of values that are common to the naked pair
                String removeFirst = "";//first naked pair candidate string
                String removeSecond = "";//second naked pair candidate string
                boolean chkremoveFirst, chkremoveSecond;//to check if either candidate was removed anywhere in puzzle
                chkremoveFirst = chkremoveSecond = false;

                for (byte r = r1; r < r1 + 3; r++) {
                    for (byte c = (byte) (c1 + 1); c < c1 + 3; c++) {

                        if (pairNotFound) {

                                /* a loop running from the previous cell in the column till the start of that subsquare [r1,c1]
                                 Checks if the current cell (at [r,c]) has 2 candidates and is not set first. */
                            for (byte rev_r = r; rev_r >= r1 && puzzle[r][c].getNoOfCandidates() == 2 && puzzle[r][c].isNotSet(); rev_r--) {
                                for (byte rev_c = (byte) (c - 1); rev_c >= c1; rev_c--) {

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
                                c = (byte) (c1 - 1);
                            }
                        }

                        /* this segment will only run if a naked pair has been located in the column */
                        else {

                            if (!(r == skip_index[0][0] && c == skip_index[0][1]) && !(r == skip_index[1][0] && c == skip_index[1][1]) && puzzle[r][c].isNotSet()) {//current cell is not set and not one of the naked pairs
                                ArrayList<Byte> removeList = eliminateFromCell(puzzle[r][c], the_pair);
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
                        skip_index[1][0], skip_index[1][1], (byte) 3);

            }
        }
    }//end of nakedPair()


    /**
     * eliminate candidate(s) from current cell if it is present
     */
    private static ArrayList<Byte> eliminateFromCell(Cell cell, byte[] toBeEliminated) {
        ArrayList<Byte> removedCandidates = new ArrayList<>();

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
                                            byte[] the_pair, byte row_1, byte column_1, byte row_2, byte column_2, byte house) {

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
     * Checks if the current cell is eligible for checking if it's a nakedTriple
     */
    private static boolean checkNakedTripleEligible(Cell cell) {
        return (cell.getNoOfCandidates() == 2 || cell.getNoOfCandidates() == 3) && cell.isNotSet();
    }// end of checkNakedTripleEligible()

    /**
     * if three cells, all in the same house,have only the same three candidates left,
     * eliminate that candidates from all other cells in that house.
     */
    private static void nakedTriple(Cell[][] puzzle) {

        for (byte r = 0; r < 9; r++) {//find nakedTriples in each row
            ArrayList<Byte> eligible_columns = new ArrayList<>();//an arraylist containing all columns in a row that are eligible  candidates of nakedTriple
            for (byte c = 0; c < 9; c++) {
                if (checkNakedTripleEligible(puzzle[r][c])) {//checks if eligible
                    eligible_columns.add(c);//if so, add it to the arrayList
                }
            }

            byte[] skip_columns = new byte[3];//stores the columns of nakedTriple (if existing) , to be skipped.
            byte[] the_triplet = new byte[3];//stores the candidates tn=hat are common to the nakedTriplet of existing.
            boolean triplet_not_found = true;//checks if a triplet has been found
            String removeFirst = "";//first naked triple candidate string
            String removeSecond = "";//second naked triple candidate string
            String removeThird = ""; //third naked triple candidiate string
            boolean chkremoveFirst, chkremoveSecond, chkremoveThird;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = chkremoveThird = false;

            if (eligible_columns.size() >= 3) {//if the number of eligible columns is less than 3, a naked triple cannot exist.

                for (byte first = 0; first < eligible_columns.size() && triplet_not_found; first++) {//loop generating the first cell that 'could' be a naked triple

                    ArrayList<Byte> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of three cells whose columns were eligible
                    addIfAbsent(candidateList, puzzle[r][eligible_columns.get(first)]);

                    for (byte second = 0; second < eligible_columns.size() && triplet_not_found; second++) {//loop generating the second cell that 'could' be a naked triple

                        if (first != second) {//make sure that the same cell is not the first cell as well , to create valid permutations

                            byte countSecondsCandidates = addIfAbsent(candidateList, puzzle[r][eligible_columns.get(second)]); // to count the candidates of second cell that are added to candidateList

                            for (byte third = 0; third < eligible_columns.size() && triplet_not_found; third++) {//loop generating the third cell that 'could' be a naked triple
                                if (first != third && second != third) {//make sure that the same cell is not either first or second cell, to create valid permutations

                                    byte countThirdsCandidates = addIfAbsent(candidateList, puzzle[r][eligible_columns.get(third)]);

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
                        ArrayList<Byte> removeList = eliminateFromCell(puzzle[r][c], the_triplet);//gets the candidates removed from the particular cell
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

        for (byte c = 0; c < 9; c++) {//find nakedTriples in each column
            ArrayList<Byte> eligible_rows = new ArrayList<>();//an arraylist containing all rows in a column that are eligible  candidates of nakedTriple
            for (byte r = 0; r < 9; r++) {
                if (checkNakedTripleEligible(puzzle[r][c])) {//checks if eligible
                    eligible_rows.add(r);//if so, add it to the arrayList
                }
            }

            byte[] skip_rows = new byte[3];//stores the rows of nakedTriple (if existing) , to be skipped.
            byte[] the_triplet = new byte[3];//stores the candidates tn=hat are common to the nakedTriplet of existing.
            boolean triplet_not_found = true;//checks if a triplet has been found
            String removeFirst = "";//first naked triple candidate string
            String removeSecond = "";//second naked triple candidate string
            String removeThird = ""; //third naked triple candidiate string
            boolean chkremoveFirst, chkremoveSecond, chkremoveThird;//to check if either candidate was removed anywhere in puzzle
            chkremoveFirst = chkremoveSecond = chkremoveThird = false;

            if (eligible_rows.size() >= 3) {//if the number of eligible columns is less than 3, a naked triple cannot exist.

                for (byte first = 0; first < eligible_rows.size() && triplet_not_found; first++) {//loop generating the first cell that 'could' be a naked triple

                    ArrayList<Byte> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of three cells whose columns were eligible
                    addIfAbsent(candidateList, puzzle[eligible_rows.get(first)][c]);

                    for (byte second = 0; second < eligible_rows.size() && triplet_not_found; second++) {//loop generating the second cell that 'could' be a naked triple

                        if (first != second) {//make sure that the same cell is not the first cell as well , to create valid permutations
                            byte countSecondsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(second)][c]); //to count the candidates of second cell that are added to candidateList

                            for (byte third = 0; third < eligible_rows.size() && triplet_not_found; third++) {//loop generating the third cell that 'could' be a naked triple

                                if (first != third && second != third) {//make sure that the same cell is not either first or second cell, to create valid permutations
                                    byte countThirdsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(third)][c]);//to remove the candidates of third cell if naked triple is not found

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
                        ArrayList<Byte> removeList = eliminateFromCell(puzzle[r][c], the_triplet);//gets the candidates removed from the particular cell
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

        for (byte r1 = 0; r1 <= 6; r1 += 3) {//travers subsquare
            for (byte c1 = 0; c1 <= 6; c1 += 3) {

                ArrayList<Byte> eligible_columns = new ArrayList<>();//an arraylist containing all columns in a subset that are eligible  candidates of nakedTriple
                ArrayList<Byte> eligible_rows = new ArrayList<>();//an arraylist containing all rows in a subset that are eligible  candidates of nakedTriple
                for (byte r = r1; r < r1 + 3; r++) {//in a particular subsquare
                    for (byte c = c1; c < c1 + 3; c++) {
                        if (checkNakedTripleEligible(puzzle[r][c])) {//checks if eligible
                            eligible_columns.add(c);//if so, add column to the arrayList
                            eligible_rows.add(r); // add row to the arrayList
                        }
                    }

                }

                byte[] skip_rows = new byte[3];//stores the rows of nakedTriple (if existing) , to be skipped.
                byte[] skip_columns = new byte[3];//stores the columns of nakedTriple (if existing) , to be skipped.
                byte[] the_triplet = new byte[3];//stores the candidates that are common to the nakedTriplet of existing.
                boolean triplet_not_found = true;//checks if a triplet has been found
                int eligibleCellsLength = eligible_columns.size();
                String removeFirst = "";//first naked triple candidate string
                String removeSecond = "";//second naked triple candidate string
                String removeThird = ""; //third naked triple candidiate string
                boolean chkremoveFirst, chkremoveSecond, chkremoveThird;//to check if either candidate was removed anywhere in puzzle
                chkremoveFirst = chkremoveSecond = chkremoveThird = false;


                if (eligibleCellsLength >= 3) {//if the number of eligible cells in subsquare is less than 3, a naked triple cannot exist.

                    for (byte first = 0; first < eligibleCellsLength && triplet_not_found; first++) {

                        ArrayList<Byte> candidateList = new ArrayList<>();//stores the list of unique candidates amongst a combination of three cells whose cells were eligible
                        addIfAbsent(candidateList, puzzle[eligible_rows.get(first)][eligible_columns.get(first)]);

                        for (byte second = 0; second < eligibleCellsLength && triplet_not_found; second++) {//loop generating the second cell that 'could' be a naked triple

                            if (first != second) {//make sure that the same cell is not the first cell as well , to create valid permutations
                                byte countSecondsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(second)][eligible_columns.get(second)]); //to count the candidates of second cell that are added to candidateList

                                for (byte third = 0; third < eligibleCellsLength && triplet_not_found; third++) {//loop generating the third cell that 'could' be a naked triple
                                    if (first != third && second != third) {//make sure all cells are unique for valid permutation
                                        byte countThirdsCandidates = addIfAbsent(candidateList, puzzle[eligible_rows.get(third)][eligible_columns.get(third)]);

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
                    for (byte r = r1; r < r1 + 3; r++) {
                        for (byte c = c1; c < c1 + 3; c++) {
                            if (!(r == skip_rows[0] && c == skip_columns[0]) && !(r == skip_rows[1] && c == skip_columns[1]) && !(r == skip_rows[2] && c == skip_columns[2]) && puzzle[r][c].isNotSet()) {
                                ArrayList<Byte> removeList = eliminateFromCell(puzzle[r][c], the_triplet);//gets the candidates removed from the particular cell
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
     * Adds a  unique candidate of cell to the candidateList arraylist used in nakedTriple. Returns the number of such
     * unique candidates inserted.
     */
    private static byte addIfAbsent(ArrayList<Byte> candidateList, Cell cell) {
        byte count = 0;
        for (byte position = 0; position < cell.getNoOfCandidates(); position++) {
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
    private static void removeRecentAdditions(ArrayList<Byte> candidateList, byte numberOfRemovals) {
        for (byte i = 0; i < numberOfRemovals; i++) {//remove the current second cell's candidates to restart
            candidateList.remove(candidateList.size() - 1);
        }
    }// end of removeRecentAdditions()

    /**
     * add changes made by naked triple to the check steps linked lists
     */
    private static void nakedTripleCheckSteps(boolean chkremoveFirst, boolean chkremoveSecond, boolean chkremoveThird,
                                              String removeFirst, String removeSecond, String removeThird,
                                              byte[] the_triplet, byte row_1, byte column_1, byte row_2, byte column_2,
                                              byte row_3, byte column_3, int house) {

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

    /**
     * If in a block all candidates of a certain digit are confined to a row or column, that digit cannot
     * appear outside of that block in that row or column.
     */
    private static void pointingPair(Cell[][] puzzle) {
        // checking for pointing pairs in rows
        byte freq;
        for (byte num = 1; num < 10; num++) {// num is the candidate we are checking pointing pairs for

            for (byte sqr_r = 0; sqr_r < 9; sqr_r += 3) {// nested loop to go to the top left cell of each subsquare
                for (byte sqr_c = 0; sqr_c < 9; sqr_c += 3) {

                    for (byte r = sqr_r; r < sqr_r + 3; r++) {//nested loop to run through the subsquare
                        freq = 0; // stores frequency of a candidate in a column
                        String[] insert_candidates = new String[3];//the coordinates of pointing pairs (size 3 to avoid out of bounds in case of pointing triple
                        for (byte c = sqr_c; c < sqr_c + 3; c++) {

                            if (puzzle[r][c].isNotSet() && puzzle[r][c].isCandidate(num)) {// checking if num is a candidate in the cell
                                insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                                freq++;
                                if (freq == 2) { // if there are 2 cells in the row with num as a candidate
                                    if (!existsInOtherRowsInSubsquare(puzzle, num, r, c)) {// checking if any other row in the subsquare has num as a candidate
                                        pointingPairCheckSteps(puzzle, insert_candidates, num, r, c, (byte) 1);
                                    }
                                }
                            }

                        } // c
                    } // r
                } // sqr_c
            } // sqr_r

            //checking for pointing pairs in columns
            for (byte sqr_r = 0; sqr_r < 9; sqr_r += 3) {// nested loop to go to the top left cell of each subsquare
                for (byte sqr_c = 0; sqr_c < 9; sqr_c += 3) {

                    for (byte c = sqr_c; c < sqr_c + 3; c++) {//nested loop to run through the subsquare
                        freq = 0; // stores frequency of a candidate in a column
                        String[] insert_candidates = new String[3];//the coordinates of pointing pairs (size 3 to avoid out of bounds in case of pointing triple
                        for (byte r = sqr_r; r < sqr_r + 3; r++) {

                            if (puzzle[r][c].isNotSet() && puzzle[r][c].isCandidate(num)) {// checking if num is a candidate in the cell
                                insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                                freq++;
                                if (freq == 2) { // if there are 2 cells in the column with num as a candidate
                                    if (!existsInOtherColumnsInSubsquare(puzzle, num, r, c)) {// checking if any other column in the subsquare has num as a candidate
                                        pointingPairCheckSteps(puzzle, insert_candidates, num, r, c, (byte) 2);
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
    private static boolean existsInOtherColumnsInSubsquare(Cell[][] puzzle, byte num, byte current_row, byte current_col) {
        byte tmp_r = (byte) (current_row - (current_row % 3));// starting row of current subsquare
        byte tmp_c = (byte) (current_col - (current_col % 3));// starting column of current subsquare
        for (byte r = tmp_r; r < tmp_r + 3; r++) {
            for (byte c = tmp_c; c < tmp_c + 3; c++) {
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
    private static boolean existsInOtherRowsInSubsquare(Cell[][] puzzle, byte num, byte current_row, byte current_col) {
        byte tmp_r = (byte) (current_row - (current_row % 3));// starting row of current subsquare
        byte tmp_c = (byte) (current_col - (current_col % 3));// starting column of current subsquare
        for (byte r = tmp_r; r < tmp_r + 3; r++) {
            for (byte c = tmp_c; c < tmp_c + 3; c++) {
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
    private static String removeFromColumn(Cell[][] puzzle, byte num, byte current_row, byte current_col) {
        String remove_str = "Remove Candidate " + num + " at ";
        byte tmp_r = (byte) (current_row - (current_row % 3));// starting row of current subsquare
        for (byte r = 0; r < 9; r++) {
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
    private static String removeFromRow(Cell[][] puzzle, byte num, byte current_row, byte current_col) {
        String remove_str = "Remove Candidate " + num + " at ";
        byte tmp_c = (byte) (current_col - (current_col % 3));// starting column of current subsquare
        for (byte c = 0; c < 9; c++) {
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
    private static void pointingPairCheckSteps(Cell[][] puzzle, String[] insert_candidates, byte num, byte row, byte col, byte house) {

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
            insert.add("Candidate: " + num + " is common to cells " + insert_candidates[0] + " " + insert_candidates[1]);
            remove.add(remove_str);
        }

    }// end of pointingPairCheckSteps

    /**
     * If in a row or column all candidates of a certain digit are confined to one block,
     * that candidate that be eliminated from all other cells in that block.
     */
    private static void claimingPair(Cell[][] puzzle) {

        for (byte num = 1; num < 10; num++) {//checking all possible candidates

            for (byte r = 0; r < 9; r++) {//check each row
                for (byte sub_c = 3; sub_c <= 9; sub_c += 3) {//points to the end of each subsquare in the particular row
                    byte freq = 0;
                    String[] insert_candidates = new String[3];//the coordinates of claiming pairs (size 3 to avoid out of bounds in case of pointing triple)
                    for (byte c = (byte) (sub_c - 3); c < sub_c; c++) {//from beginning of subsquare at the row till its end (sub_c)
                        if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {
                            insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                            freq++;
                            if (freq == 2 && notInOtherCellsOfRow(puzzle, sub_c, r, num)) {//number of cells in the row and subsquare is 2. remove all 'num' candidates within the subsquare that are not part of the row
                                claimingPairCheckSteps(puzzle, insert_candidates, num, r, (byte) (sub_c - 3), (byte) 1);
                            }

                        }

                    }
                }
            }

            for (byte c = 0; c < 9; c++) {//check each column
                for (byte sub_r = 3; sub_r <= 9; sub_r += 3) {
                    byte freq = 0;
                    String[] insert_candidates = new String[3];
                    for (byte r = (byte) (sub_r - 3); r < sub_r; r++) {//from beginning of subsquare at the column till its end (sub_r)
                        if (puzzle[r][c].isCandidate(num) && puzzle[r][c].isNotSet()) {
                            insert_candidates[freq] = "(" + (r + 1) + "," + (c + 1) + ")";
                            freq++;
                            if (freq == 2 && notInOtherCellsOfColumn(puzzle, sub_r, c, num)) {
                                claimingPairCheckSteps(puzzle, insert_candidates, num, c, (byte) (sub_r - 3), (byte) 2);
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
    private static boolean notInOtherCellsOfRow(Cell[][] puzzle, byte sub_c, byte r, byte num) {

        for (byte c = 0; c < 9; c++) {
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
    private static boolean notInOtherCellsOfColumn(Cell[][] puzzle, byte sub_r, byte c, byte num) {
        for (byte r = 0; r < 9; r++) {
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
    private static String removeFromSubsquare(Cell[][] puzzle, byte current, byte tmp_start, byte num, int i) {

        String remove_str = "Remove Candidate " + num + " at ";
        byte tmp_r, tmp_c;// storing the index beginning of the subsquare required

        if (i == 1) {// if i is 1 , the claimingPair is being checked row-wise
            tmp_r = (byte) (current - current % 3);//finds first row of the subsquare to which the current row belongs
            tmp_c = tmp_start;//tmp_start already contains the index of first column of subsquare
        } else {// if i is 2 , the claimingPair is being checked column-wise
            tmp_r = tmp_start;//tmp_start already contains the index of first row of subsquare
            tmp_c = (byte) (current - current % 3);//finds first column of the subsquare to which the current row belongs
        }

        for (byte r = tmp_r; r < tmp_r + 3; r++) {
            if (!(i == 1 && current == r)) {//if claimPair is being checked row-wise and the r is the current_row, we should not remove any candidates
                for (byte c = tmp_c; c < tmp_c + 3; c++) {
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
    private static void claimingPairCheckSteps(Cell[][] puzzle, String[] insert_candidates, byte num, byte current, byte tmp_start, byte house) {

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
            insert.add("Candidate: " + num + " is common to cells " + insert_candidates[0] + " " + insert_candidates[1]);
            remove.add(remove_str);
        }

    }//end of claimingPairCheckSteps()

    /**
     * when two cells in a house each contain two candidates which appear nowhere outside those two cells in that house, those two candidates must be placed in the two cells.
     * All other candidates can therefore be eliminated.
     */
    private static void hiddenPair(Cell[][] puzzle) {
        //checking for hidden pairs in rows first
        byte[] pair = new byte[2];//array to store the values of the pair
        for (byte r = 0; r < 9; r++) {
            ArrayList<Byte> eligible_columns = new ArrayList<>();//columns in the current row that could possibly contain a hidden pairs
            for (byte c = 0; c < 9; c++) { //loop to find eligible cells in this row
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_columns.add(c);
            }//c

            if (eligible_columns.size() >= 2) {
                for (byte i = 0; i < eligible_columns.size() - 1; i++) {//loop to go through combinations of the eligible cells in the rows to find valid hidden pairs
                    for (byte j = (byte) (i + 1); j < eligible_columns.size(); j++) {
                        byte index_1 = eligible_columns.get(i);
                        byte index_2 = eligible_columns.get(j);
                        ArrayList<Byte> similarCandidates = getSimilarCandidates(puzzle[r][index_1], puzzle[r][index_2]);//store the similar candidates between chosen candidates
                        if (similarCandidates.size() >= 2) {//checking if there are exactly 2 similar candidates
                            for (byte outer = 0; outer < similarCandidates.size() - 1; outer++) {
                                for (byte inner = (byte) (outer + 1); inner < similarCandidates.size(); inner++) {//choose any 2 of the similar candidates
                                    pair[0] = similarCandidates.get(outer);
                                    pair[1] = similarCandidates.get(inner);
                                    if (pairDoesNotExistInOtherCellsInRow(puzzle, r, pair, index_1, index_2))//checking if any other cells in the same row also have pair[0] or pair[1] as candidates
                                        hiddenPairCheckSteps(puzzle, r, index_1, r, index_2, pair, (byte) 1);
                                }
                            }

                        }//if statement
                    }//index2
                }//index1
            }//if
        }//r

        //checking for hidden pairs in columns
        for (byte c = 0; c < 9; c++) {
            ArrayList<Byte> eligible_rows = new ArrayList<>();//rows in the current column that could possibly contain a hidden pairs
            for (byte r = 0; r < 9; r++) { //loop to find eligible cells in this column
                if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2)//if cell has 2 or more candidates, it is eligible
                    eligible_rows.add(r);
            }//r

            if (eligible_rows.size() >= 2) {
                for (byte i = 0; i < eligible_rows.size() - 1; i++) {//loop to go through combinations of the eligible cells in the columns to find valid hidden pairs
                    for (byte j = (byte) (i + 1); j < eligible_rows.size(); j++) {
                        byte index_1 = eligible_rows.get(i);
                        byte index_2 = eligible_rows.get(j);
                        ArrayList<Byte> similarCandidates = getSimilarCandidates(puzzle[index_1][c], puzzle[index_2][c]);//store the similar candidates between chosen candidates
                        if (similarCandidates.size() >= 2) {//checking if there are more than or equal to 2 similar candidates
                            for (byte outer = 0; outer < similarCandidates.size() - 1; outer++) {
                                for (byte inner = (byte) (outer + 1); inner < similarCandidates.size(); inner++) {//choose any 2 of the similar candidates
                                    pair[0] = similarCandidates.get(outer);
                                    pair[1] = similarCandidates.get(inner);
                                    if (pairDoesNotExistInOtherCellsInColumn(puzzle, c, pair, index_1, index_2))//checking if any other cells in the same column also have pair[0] or pair[1] as candidates
                                        hiddenPairCheckSteps(puzzle, index_1, c, index_2, c, pair, (byte) 2);
                                }
                            }
                        }//if statement
                    }//index2
                }//index1
            }//if
        }//c


        for (byte r1 = 0; r1 <= 6; r1 += 3) {//jumps to first row position of subsquare
            for (byte c1 = 0; c1 <= 6; c1 += 3) {//jumps to first column position of subsquare
                ArrayList<Byte> eligible_rows = new ArrayList<>();//rows  that could possibly contain a hidden pairs
                ArrayList<Byte> eligible_columns = new ArrayList<>();//columns that could possibly contain hidden pairs
                for (byte r = r1; r < r1 + 3; r++) {//traversal (subsquare)
                    for (byte c = c1; c < c1 + 3; c++) {
                        if (puzzle[r][c].isNotSet() && puzzle[r][c].getNoOfCandidates() >= 2) {//if cell has 2 or more candidates, it is eligible
                            eligible_rows.add(r);
                            eligible_columns.add(c);
                        }
                    }
                }

                int eligibleCellsSize = eligible_columns.size();
                if (eligibleCellsSize >= 2) {
                    for (byte i = 0; i < eligibleCellsSize - 1; i++) {//loop to go through combinations of the eligible cells in the subquare to find valid hidden pairs
                        for (byte j = (byte) (i + 1); j < eligibleCellsSize; j++) {
                            byte index_1_r = eligible_rows.get(i);
                            byte index_1_c = eligible_columns.get(i);
                            byte index_2_r = eligible_rows.get(j);
                            byte index_2_c = eligible_columns.get(j);
                            ArrayList<Byte> similarCandidates = getSimilarCandidates(puzzle[index_1_r][index_1_c], puzzle[index_2_r][index_2_c]);//store the similar candidates between chosen candidates
                            if (similarCandidates.size() >= 2) {//checking if there are more than or equal to 2 similar candidates
                                for (byte outer = 0; outer < similarCandidates.size() - 1; outer++) {
                                    for (byte inner = (byte) (outer + 1); inner < similarCandidates.size(); inner++) {//choose any 2 of the similar candidates
                                        pair[0] = similarCandidates.get(outer);
                                        pair[1] = similarCandidates.get(inner);
                                        if (pairDoesNotExistInOtherCellsInSubsquare(puzzle, r1, c1, pair, index_1_r, index_1_c, index_2_r, index_2_c))//checking if any other cells in the same subsquare also have pair[0] or pair[1] as candidates
                                            hiddenPairCheckSteps(puzzle, index_1_r, index_1_c, index_2_r, index_2_c, pair, (byte) 3);
                                    }
                                }
                            }//if statement
                        }//inner for loop
                    }//outer for loop
                }//greater if statement
            }
        }
    }// end of hiddenPair()


    private static ArrayList<Byte> getSimilarCandidates(Cell cell_1, Cell cell_2) {
        ArrayList<Byte> similar_candidates = new ArrayList<>();//stores the similar candidates
        for (byte num = 1; num < 10; num++) {
            if (cell_1.isCandidate(num) && cell_2.isCandidate(num)) {
                similar_candidates.add(num);//adding similar candidates to array
            }// if statement
        }
        return similar_candidates;
    }// end of getSimilarCandidates()


    private static String[] removeNonPairCandidates(Cell cell_1, Cell cell_2, byte[] pair) {

        String[] removePair = new String[2];
        removePair[0] = removePair[1] = "Remove Candidates ";
        boolean tmp_0, tmp_1;
        tmp_0 = tmp_1 = true;
        for (byte num = 1; num < 10; num++) {
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


    private static boolean pairDoesNotExistInOtherCellsInRow(Cell[][] puzzle, byte row, byte[] pair, byte index1, byte index2) {
        for (byte c = 0; c < 9; c++) {
            if ((puzzle[row][c].isCandidate(pair[0]) || puzzle[row][c].isCandidate(pair[1])) && (c != index1 && c != index2) && puzzle[row][c].isNotSet()) {
                return false;
            }
        }
        return true;
    }// end of pairDoesNotExistInOtherCellsInRow()

    private static boolean pairDoesNotExistInOtherCellsInColumn(Cell[][] puzzle, byte col, byte[] pair, byte index1, byte index2) {
        for (byte r = 0; r < 9; r++) {
            if ((puzzle[r][col].isCandidate(pair[0]) || puzzle[r][col].isCandidate(pair[1])) && (r != index1 && r != index2) && puzzle[r][col].isNotSet()) {
                return false;
            }
        }
        return true;
    }// end of pairDoesNotExistInOtherCellsInColumn()

    private static boolean pairDoesNotExistInOtherCellsInSubsquare(Cell[][] puzzle, byte r1, byte c1, byte[] pair, byte index_1_r, byte index_1_c, byte index_2_r, byte index_2_c) {
        for (byte r = r1; r < r1 + 3; r++) {//traversal (subsquare)
            for (byte c = c1; c < c1 + 3; c++) {
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
    private static void hiddenPairCheckSteps(Cell[][] puzzle, byte row_1, byte col_1, byte row_2, byte col_2, byte[] pair, byte house) {

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

    private static void xWing(Cell[][] puzzle) {
        basicFish(puzzle, (byte) 1);
    }// end of xWing()

    private static void swordfish(Cell[][] puzzle) {
        basicFish(puzzle, (byte) 2);
    }// end of swordfish()

    private static void jellyfish(Cell[][] puzzle) {
        basicFish(puzzle, (byte) 3);
    }// end of jellyfish()

    private static void basicFish(Cell[][] puzzle, byte type) {

        byte freq;//counter to find fish , if present.

        for (byte num = 1; num <= 9; num++) {

            //checking Rows
            ArrayList<Byte> r_eligibleRows = new ArrayList<>();//to store row of swordfish candidates
            ArrayList<byte[]> r_eligibleColumns = new ArrayList<>();//to store column of swordfish candidates

            for (byte r = 0; r < 9; r++) {//traversal
                freq = 0;
                byte[] tmp_columns = new byte[type + 2];//the temporary columns
                for (byte c = 0; c < 9 && freq < type + 2; c++) {
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
            basicFishRemoval(puzzle, r_eligibleRows, r_eligibleColumns, num, (byte) 1, type);


            //checking Columns
            ArrayList<Byte> c_eligibleColumns = new ArrayList<>();//to store column of swordfish candidates (column check)
            ArrayList<byte[]> c_eligibleRows = new ArrayList<>();//to store row of swordfish candidates (row check)

            for (byte c = 0; c < 9; c++) {
                freq = 0;
                byte[] tmp_rows = new byte[type + 2];//the temporary rows
                for (byte r = 0; r < 9 && freq < type + 2; r++) {
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
            basicFishRemoval(puzzle, c_eligibleColumns, c_eligibleRows, num, (byte) 2, type);
        }

    }

    private static void basicFishRemoval(Cell[][] puzzle, ArrayList<Byte> house_in, ArrayList<byte[]> candidate_pos, byte num, byte which, byte type) {
        if (house_in.size() >= type + 1) {//if the rows/columns where the swordfish candidates are in is more than 3, a swordfish may exist

            if (type == 1) {
                for (int i = 0; i < house_in.size() - 1; i++) {
                    for (int j = i + 1; j < house_in.size(); j++) {
                        ArrayList<Byte> other_house = compareCandidatePos(candidate_pos.get(i), candidate_pos.get(j), null, null);//find the unique set of houses from where candidates need to be removed
                        if (other_house.size() == 2) {//two unique houses from where candidates are to be removed (xwing)
                            if (which == 1) {
                                String title = "X-wing in " + " Row " + (house_in.get(i) + 1) + " and " + (house_in.get(j) + 1);
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
                                for (byte r = 0; r < 9; r++) {
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

                                String title = "X-Wing in " + " Column " + (house_in.get(i) + 1) + " and " + (house_in.get(j) + 1);
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
                                for (byte c = 0; c < 9; c++) {
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
                            ArrayList<Byte> other_house = compareCandidatePos(candidate_pos.get(i), candidate_pos.get(j), candidate_pos.get(k), null);//find the unique set of houses from where candidates need to be removed
                            if (other_house.size() == 3) {//three unique houses from where candidates are to be removed (swordfish)
                                if (which == 1) {
                                    String title = "Swordfish in " + " Row " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
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
                                    for (byte r = 0; r < 9; r++) {
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

                                    String title = "Swordfish in " + " Column " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
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
                                    for (byte c = 0; c < 9; c++) {
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
                                ArrayList<Byte> other_house = compareCandidatePos(candidate_pos.get(i), candidate_pos.get(j), candidate_pos.get(k), candidate_pos.get(l));
                                if (other_house.size() == 4) {//four unique houses from where candidates are to be removed (jellyfish)
                                    if (which == 1) {
                                        String title = "Jellyfish in " + " Row " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
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
                                        for (byte r = 0; r < 9; r++) {
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
                                        String title = "JellyFish in " + " Column " + (house_in.get(i) + 1) + "," + (house_in.get(j) + 1) +
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
                                        for (byte c = 0; c < 9; c++) {
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
        finnedFish(puzzle, (byte) 1);
    }

    private static void finnedSwordfish(Cell[][] puzzle) {
        finnedFish(puzzle, (byte) 2);
    }

    private static void finnedJellyfish(Cell[][] puzzle) {
        finnedFish(puzzle, (byte) 3);
    }

    private static void finnedFish(Cell[][] puzzle, byte type) {

        byte freq;//counter to find fish , if present.

        for (byte num = 1; num <= 9; num++) {

            //checking Rows
            ArrayList<Byte> r_eligibleFinRows = new ArrayList<>();//to store row of finned wing candidates
            ArrayList<byte[]> r_eligibleFinColumns = new ArrayList<>();//to store column of finned wing candidates

            for (byte r = 0; r < 9; r++) {//traversal
                freq = 0;
                byte[] tmp_columns = new byte[type + 4];//the temporary columns
                for (byte c = 0; c < 9 && freq < type + 4; c++) {//max of 2 fins
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
            finnedFishRemoval(puzzle, r_eligibleFinRows, r_eligibleFinColumns, num, (byte) 1, type);


            //checking Columns
            ArrayList<Byte> c_eligibleFinColumns = new ArrayList<>();//to store column of finned wing candidates
            ArrayList<byte[]> c_eligibleFinRows = new ArrayList<>();//to store row of finned wing candidates

            for (byte c = 0; c < 9; c++) {
                freq = 0;
                byte[] tmp_rows = new byte[type + 4];//the temporary rows
                for (byte r = 0; r < 9 && freq < type + 4; r++) {
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
            finnedFishRemoval(puzzle, c_eligibleFinColumns, c_eligibleFinRows, num, (byte) 2, type);
        }

    }

    private static void finnedFishRemoval(Cell[][] puzzle, ArrayList<Byte> houseFin_in, ArrayList<byte[]> candidateFin_pos,
                                          byte num, byte which, byte type) {

        if (houseFin_in.size() >= type + 1) {//more than the minimum number of houses required for finned xwing to be present
            if (type == 1) {
                for (int i = 0; i < houseFin_in.size() - 1; i++) {
                    for (int j = i + 1; j < houseFin_in.size(); j++) {//combination of prospective wings
                        ArrayList<Byte> sample_other_house = compareCandidatePos(candidateFin_pos.get(i), candidateFin_pos.get(j), null, null);//generate unique set of the other house
                        if (sample_other_house.size() > 2 && sample_other_house.size() < 5) {//for finned xWing, the other house must have a size of 3 or 4
                            for (int sample_1 = 0; sample_1 < sample_other_house.size() - 1; sample_1++) {
                                for (int sample_2 = sample_1 + 1; sample_2 < sample_other_house.size(); sample_2++) {//take a combination from the other house that could be the xWing

                                    ArrayList<Byte> tmp = new ArrayList<>(sample_other_house);
                                    tmp.remove(sample_other_house.get(sample_1));
                                    tmp.remove(sample_other_house.get(sample_2));//tmp now only contains the other house which could be fins
                                    ArrayList<byte[]> fin_coordinates = new ArrayList<>();//the coordinates of the could be fins
                                    ArrayList<Byte> fin_in_first = finPresent(candidateFin_pos.get(i), tmp);//store the other house if prospective fin present
                                    ArrayList<Byte> fin_in_second = finPresent(candidateFin_pos.get(j), tmp);

                                    if (which == 1 && (fin_in_first == null || fin_in_second == null)) {//row wise finned X Wing prospective

                                        addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 1);
                                        addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 1);

                                        byte[] subsquare_markers;
                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                            byte sample_house_1 = sample_other_house.get(sample_1);
                                            byte sample_house_2 = sample_other_house.get(sample_2);
                                            String title = "Finned X-Wing in " + " Row " + (houseFin_in.get(i) + 1) + " and " + (houseFin_in.get(j) + 1);
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

                                        byte[] subsquare_markers;
                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {
                                            byte sample_house_1 = sample_other_house.get(sample_1);
                                            byte sample_house_2 = sample_other_house.get(sample_2);
                                            String title = "Finned X-Wing in " + " Column " + (houseFin_in.get(i) + 1) + " and " + (houseFin_in.get(j) + 1);
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
                            ArrayList<Byte> sample_other_house = compareCandidatePos(candidateFin_pos.get(i), candidateFin_pos.get(j),
                                    candidateFin_pos.get(k), null);
                            if (sample_other_house.size() > 3 && sample_other_house.size() < 6) {
                                for (int sample_1 = 0; sample_1 < sample_other_house.size() - 2; sample_1++) {
                                    for (int sample_2 = sample_1 + 1; sample_2 < sample_other_house.size() - 1; sample_2++) {//take a combination from the other house that could be the swordfish
                                        for (int sample_3 = sample_2 + 1; sample_3 < sample_other_house.size(); sample_3++) {
                                            ArrayList<Byte> tmp = new ArrayList<>(sample_other_house);
                                            tmp.remove(sample_other_house.get(sample_1));
                                            tmp.remove(sample_other_house.get(sample_2));//tmp now only contains the other house which could be fins
                                            tmp.remove(sample_other_house.get(sample_3));
                                            ArrayList<byte[]> fin_coordinates = new ArrayList<>();//the coordinates of the could be fins
                                            ArrayList<Byte> fin_in_first = finPresent(candidateFin_pos.get(i), tmp);//store the other house if prospective fin present
                                            ArrayList<Byte> fin_in_second = finPresent(candidateFin_pos.get(j), tmp);
                                            ArrayList<Byte> fin_in_third = finPresent(candidateFin_pos.get(k), tmp);
                                            if (which == 1) {

                                                addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 1);
                                                addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 1);
                                                addFinCoordinates(fin_in_third, houseFin_in.get(k), fin_coordinates, 1);

                                                byte[] subsquare_markers;
                                                if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                    byte sample_house_1 = sample_other_house.get(sample_1);
                                                    byte sample_house_2 = sample_other_house.get(sample_2);
                                                    byte sample_house_3 = sample_other_house.get(sample_3);
                                                    String title = "Finned Swordfish in " + " Row " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " and " + (houseFin_in.get(k) + 1);
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

                                                byte[] subsquare_markers;
                                                if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                    byte sample_house_1 = sample_other_house.get(sample_1);
                                                    byte sample_house_2 = sample_other_house.get(sample_2);
                                                    byte sample_house_3 = sample_other_house.get(sample_3);
                                                    String title = "Finned Swordfish in " + " Columns " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " and " + (houseFin_in.get(k) + 1);
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
                                ArrayList<Byte> sample_other_house = compareCandidatePos(candidateFin_pos.get(i), candidateFin_pos.get(j),
                                        candidateFin_pos.get(k), candidateFin_pos.get(l));
                                if (sample_other_house.size() > 4 && sample_other_house.size() < 7) {
                                    for (int sample_1 = 0; sample_1 < sample_other_house.size() - 3; sample_1++) {
                                        for (int sample_2 = sample_1 + 1; sample_2 < sample_other_house.size() - 2; sample_2++) {//take a combination from the other house that could be the swordfish
                                            for (int sample_3 = sample_2 + 1; sample_3 < sample_other_house.size() - 1; sample_3++) {
                                                for (int sample_4 = sample_3 + 1; sample_4 < sample_other_house.size(); sample_4++) {
                                                    ArrayList<Byte> tmp = new ArrayList<>(sample_other_house);
                                                    tmp.remove(sample_other_house.get(sample_1));
                                                    tmp.remove(sample_other_house.get(sample_2));//tmp now only contains the other house which could be fins
                                                    tmp.remove(sample_other_house.get(sample_3));
                                                    tmp.remove(sample_other_house.get(sample_4));
                                                    ArrayList<byte[]> fin_coordinates = new ArrayList<>();//the coordinates of the could be fins
                                                    ArrayList<Byte> fin_in_first = finPresent(candidateFin_pos.get(i), tmp);//store the other house if prospective fin present
                                                    ArrayList<Byte> fin_in_second = finPresent(candidateFin_pos.get(j), tmp);
                                                    ArrayList<Byte> fin_in_third = finPresent(candidateFin_pos.get(k), tmp);
                                                    ArrayList<Byte> fin_in_fourth = finPresent(candidateFin_pos.get(l), tmp);
                                                    if (which == 1) {

                                                        addFinCoordinates(fin_in_first, houseFin_in.get(i), fin_coordinates, 1);
                                                        addFinCoordinates(fin_in_second, houseFin_in.get(j), fin_coordinates, 1);
                                                        addFinCoordinates(fin_in_third, houseFin_in.get(k), fin_coordinates, 1);
                                                        addFinCoordinates(fin_in_fourth, houseFin_in.get(l), fin_coordinates, 1);

                                                        byte[] subsquare_markers;
                                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                            byte sample_house_1 = sample_other_house.get(sample_1);
                                                            byte sample_house_2 = sample_other_house.get(sample_2);
                                                            byte sample_house_3 = sample_other_house.get(sample_3);
                                                            byte sample_house_4 = sample_other_house.get(sample_4);
                                                            String title = "Finned Jellyfish in " + " Row " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " , " + (houseFin_in.get(k) + 1) +
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

                                                        byte[] subsquare_markers;
                                                        if ((subsquare_markers = commonSubsquare(fin_coordinates)) != null) {//all the fins are in the same subsquare
                                                            byte sample_house_1 = sample_other_house.get(sample_1);
                                                            byte sample_house_2 = sample_other_house.get(sample_2);
                                                            byte sample_house_3 = sample_other_house.get(sample_3);
                                                            byte sample_house_4 = sample_other_house.get(sample_4);
                                                            String title = "Finned Jellyfish in " + " Columns " + (houseFin_in.get(i) + 1) + ", " + (houseFin_in.get(j) + 1) + " , " + (houseFin_in.get(k) + 1) +
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

    private static String finRemover(Cell[][] puzzle, byte[] subsquare_markers, byte constant_house, Byte skip_house, Byte skip_house2, Byte skip_house3, Byte skip_house4, int which, byte num) {

        String remove_s = "";
        if (which == 1) {
            if (constant_house - constant_house % 3 == subsquare_markers[1]) {
                for (byte row = subsquare_markers[0]; row <= subsquare_markers[0] + 2; row++) {
                    if (!((Byte) row).equals(skip_house) && !((Byte) row).equals(skip_house2) && !((Byte) row).equals(skip_house3) && !((Byte) row).equals(skip_house4)) {
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
                for (byte col = subsquare_markers[1]; col <= subsquare_markers[1] + 2; col++) {
                    if (!((Byte) col).equals(skip_house) && !((Byte) col).equals(skip_house2) && !((Byte) col).equals(skip_house3) && !((Byte) col).equals(skip_house4)) {
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

    private static String[] finInserter(Cell[][] puzzle, int row, int column, byte num, ArrayList<byte[]> fin_coordinates) {

        String[] finInsert = new String[2];
        finInsert[0] = "";
        finInsert[1] = "";
        if (puzzle[row][column].isCandidate(num)
                && puzzle[row][column].isNotSet() &&
                !contains(fin_coordinates, new byte[]{(byte) row, (byte) column}))
            finInsert[0] += "(" + (row + 1) + "," + (column + 1) + ") ";
        else if (puzzle[row][column].isCandidate(num)
                && puzzle[row][column].isNotSet() &&
                contains(fin_coordinates, new byte[]{(byte) row, (byte) column}))
            finInsert[1] += "(" + (row + 1) + "," + (column + 1) + ") ";

        return finInsert;

    }

    private static void addFinCoordinates(ArrayList<Byte> finnedOtherHouse, Byte finHouse, ArrayList<byte[]> fin_coordinates, int which) {
        if (which == 1) {
            if (finnedOtherHouse != null) {//prospective fin is in the first column
                byte[] first = new byte[2];
                first[1] = finnedOtherHouse.get(0);
                first[0] = finHouse;
                fin_coordinates.add(first);
                if (finnedOtherHouse.size() == 2) {
                    byte[] second = new byte[2];
                    second[1] = finnedOtherHouse.get(1);
                    second[0] = finHouse;
                    fin_coordinates.add(second);
                }
            }
        } else {
            if (finnedOtherHouse != null) {//prospective fin is in the first column
                byte[] first = new byte[2];
                first[0] = finnedOtherHouse.get(0);
                first[1] = finHouse;
                fin_coordinates.add(first);
                if (finnedOtherHouse.size() == 2) {
                    byte[] second = new byte[2];
                    second[0] = finnedOtherHouse.get(1);
                    second[1] = finHouse;
                    fin_coordinates.add(second);
                }
            }
        }
    }

    private static boolean contains(ArrayList<byte[]> fin_coordinates, byte[] bytes) {
        for (byte[] bytearr : fin_coordinates) {
            if (Arrays.equals(bytearr, bytes)) {
                return true;
            }
        }
        return false;
    }

    private static byte[] commonSubsquare(ArrayList<byte[]> fin_coordinates) {
        byte row_s = (byte) (fin_coordinates.get(0)[0] - fin_coordinates.get(0)[0] % 3);
        byte col_s = (byte) (fin_coordinates.get(0)[1] - fin_coordinates.get(0)[1] % 3);
        for (int i = 1; i < fin_coordinates.size(); i++) {
            if (!((byte) (fin_coordinates.get(i)[0] - fin_coordinates.get(i)[0] % 3) == row_s &&
                    (byte) (fin_coordinates.get(i)[1] - fin_coordinates.get(i)[1] % 3) == col_s)) {
                return null;
            }
        }
        return new byte[]{row_s, col_s};
    }

    private static ArrayList<Byte> finPresent(byte[] bytes, ArrayList<Byte> tmp) {
        ArrayList<Byte> output = new ArrayList<>();
        for (byte aByte : bytes) {
            if (tmp.contains(aByte)) {
                output.add(aByte);
            }
        }
        if (output.size() > 0) {
            return output;
        }
        return null;
    }

    private static ArrayList<Byte> compareCandidatePos(byte[] bytes, byte[] bytes1, byte[] bytes2, byte[] bytes3) {
        ArrayList<Byte> unique_dig = new ArrayList<>();
        if (bytes != null) {
            for (byte aByte : bytes) {
                if (aByte != -1)
                    unique_dig.add(aByte);
            }
        }
        if (bytes1 != null) {
            for (byte b : bytes1) {
                if (b != -1 && !unique_dig.contains(b))
                    unique_dig.add(b);
            }
        }
        if (bytes2 != null) {
            for (byte b : bytes2) {
                if (b != -1 && !unique_dig.contains(b))
                    unique_dig.add(b);
            }
        }
        if (bytes3 != null) {
            for (byte b : bytes3) {
                if (b != -1 && !unique_dig.contains(b))
                    unique_dig.add(b);
            }
        }
        return unique_dig;
    }// end of compareCandidatePos()

    /**
     * A last resort for the sudoku solver to solve puzzles if all of the previous logics do not work
     */
    private static boolean bruteForce(Cell[][] puzzle, byte previous_row) {//brute forcing will be done row wise
        byte noOfCandidates;
        byte[] coordinates = findNextEmptyCell(puzzle, previous_row);
        byte r = coordinates[0];
        byte c = coordinates[1];
        if (r == 9)//no more empty cells remaining, which means all cells have been successfully filled
            return true;
        else {
            noOfCandidates = puzzle[r][c].getNoOfCandidates();
            for (byte inc = 0; inc < noOfCandidates; inc++) {
                if (numCanExistInCell(puzzle, r, c, puzzle[r][c].getCandidate(inc))) {
                    puzzle[r][c].placeSolution(puzzle[r][c].getCandidate(inc));
                    if (bruteForce(puzzle, r)) {//if further brute force was successful
                        return true;
                    }//second if statement
                }//first if statement
            }//for loop
            puzzle[r][c].placeSolution((byte) 0);//making the cell empty again
            return false;//if no candidate can be successfully placed in the cell
        }
    }//end of bruteForce()

    private static byte[] findNextEmptyCell(Cell[][] puzzle, byte r) {// finding next empty cell for the brute force function
        for (; r < 9; r++) {
            for (byte c = 0; c < 9; c++) {
                if (puzzle[r][c].isNotSet())
                    return new byte[]{r, c};//returning the coordinates of the next empty cell
            }
        }
        return new byte[]{9, 9};//returning (9,9) if there are no more empty cells remaining in the puzzle
    }// end of findNextEmptyCell()

    private static boolean numCanExistInCell(Cell[][] puzzle, byte row, byte col, byte num) {//checks if a number can exist in a particular cell
        //checking row
        for (byte c = 0; c < 9; c++) {
            if (num == puzzle[row][c].getSolution())
                return false;
        }

        //checking column
        for (byte r = 0; r < 9; r++) {
            if (num == puzzle[r][col].getSolution())
                return false;
        }

        //checking subsquare
        byte tmp_r = (byte) (row - (row % 3));// starting row of current subsquare
        byte tmp_c = (byte) (col - (col % 3));// starting column of current subsquare
        for (byte r = tmp_r; r < tmp_r + 3; r++) {
            for (byte c = tmp_c; c < tmp_c + 3; c++) {
                if (num == puzzle[r][c].getSolution()) {// avoid the current column and check if num is a candidate in the cell
                    return false;
                }
            }
        }

        return true;//if the number can exist in the cell, return true
    }// end of numCanExistInCell()

    /**
     * Solves the puzzle
     */
    public static void Solve(Cell[][] puzzle) {

        byte levelUpdater = 0;//this variable determines which all algorithms are used in an iteration of the do while loop below,
        //higher the value of levelUpdater - more complex algorithms are used

        int beforeUsingStrategy;//temporary variable used to check if there is a change in the value of changeCounter after a certain strategy is used

        do {//loop to call the functions to solve the puzzle
            changeCounter = 0;//reinitializing the counter to 0 before the start of every iteration

            switch (levelUpdater) {

                case 11:
                    beforeUsingStrategy = changeCounter;
                    finnedJellyfish(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedJellyfish()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)


                case 10:
                    beforeUsingStrategy = changeCounter;
                    finnedSwordfish(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedSwordfish()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 9:
                    beforeUsingStrategy = changeCounter;
                    finnedXWing(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedXWing()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 8:
                    beforeUsingStrategy = changeCounter;
                    jellyfish(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by jellyfish()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 7:
                    beforeUsingStrategy = changeCounter;
                    swordfish(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by swordfish()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 6:
                    beforeUsingStrategy = changeCounter;
                    xWing(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by xWing()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 5:
                    beforeUsingStrategy = changeCounter;
                    nakedTriple(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by nakedTriple()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 4:
                    beforeUsingStrategy = changeCounter;
                    hiddenPair(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by hiddenPair()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 3:
                    beforeUsingStrategy = changeCounter;
                    pointingPair(puzzle);
                    claimingPair(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by pointingPair() and claimingPair()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 2:
                    beforeUsingStrategy = changeCounter;
                    nakedPair(puzzle);
                    if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by nakedPair()
                        levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)

                case 1:
                    hiddenSingle(puzzle);

                case 0:
                    nakedSingle(puzzle);
            }//end of switch case

            if (cellCount == 81)//checking if the puzzle is solved (all 81 cells are filled), if true, exit the loop
                break;

            if (changeCounter == 0)//if no changes have been made to the puzzle with the current set of algorithms, increase the level to utilize more complex solving strategies
                levelUpdater++;

        } while (levelUpdater <= 11);//if the levelUpdater goes greater than 11, it means despite using all the algorithms available - the puzzle could not be solved

        //if the solving strategies could not completely solve the puzzle, resort to brute force
        if (cellCount != 81) {//checking if the puzzle is unsolved
            bruteForce(puzzle, (byte) 0);//calling the brute force method
            algorithm.add("Brute Force");//adding Brute Force to the list of algorithms used
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

    }// end of Solve()

    public static void partiallySolve(Cell[][] puzzle, ArrayList<String> chosenStrategies) {

        final String[] logics = {"Naked Single", "Hidden Single", "Naked Pair", "Pointing Pair",
                "Claiming Pair", "Hidden Pair", "Naked Triple", "X-Wing", "Swordfish", "Jellyfish", 
                "Finned X-Wing", "Finned Swordfish", "Finned Jellyfish", "Brute Force"};

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

                case 12:
                    if (strategy[12]) {
                        beforeUsingStrategy = changeCounter;
                        finnedJellyfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedJellyfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 11:
                    if (strategy[11]) {
                        beforeUsingStrategy = changeCounter;
                        finnedSwordfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedSwordfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 10:
                    if (strategy[10]) {
                        beforeUsingStrategy = changeCounter;
                        finnedXWing(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by finnedXWing()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 9:
                    if (strategy[9]) {
                        beforeUsingStrategy = changeCounter;
                        jellyfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by jellyfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 8:
                    if (strategy[8]) {
                        beforeUsingStrategy = changeCounter;
                        swordfish(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by swordfish()
                            levelUpdater = 1;//reinitialize the levelUpdater to 1 (this is done for better efficiency of the code)
                    }

                case 7:
                    if (strategy[7]) {
                        beforeUsingStrategy = changeCounter;
                        xWing(puzzle);
                        if (beforeUsingStrategy != changeCounter)//if they are not equal, change was effected by xWing()
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

        } while (levelUpdater <= 12);//if the levelUpdater goes greater than 12, it means after using the chosen algorithms - the puzzle could still not be solved

        //if the solving strategies could not completely solve the puzzle, resort to brute force
        if (cellCount != 81 && strategy[logics.length - 1]) {//checking if the puzzle is unsolved AND the user has chosen to use Brute Force
            bruteForce(puzzle, (byte) 0);//calling the brute force method
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
