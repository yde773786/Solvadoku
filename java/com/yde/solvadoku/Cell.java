package com.yde.solvadoku;

import android.util.Log;

import java.util.Arrays;

/**
 * An instance of the Cell class is used to represent one cell in the sudoku
 * puzzle.It consists of an array (candidates) that contains a list of the
 * possible candidates that a specific cell can have. It also consists of a int
 * member variable (solution) that is meant to represent the final inserted
 * number into the cell.
 */
public class Cell {

    private final boolean[] candidates;
    private int solution;
    private int noOfCandidates;

    /**
     * initializes array possibilities true for all ints from 1 to 9, indicating
     * that all numbers are candidates initially.
     */
    public Cell() {
        candidates = new boolean[9];
        for (int i = 0; i < 9; i++)
            candidates[i] = true;

        noOfCandidates = 9;
        solution = 0;
    }// end of constructor

    /**
     * provides the number of candidates for the cell
     */
    public int getNoOfCandidates() {
        return noOfCandidates;
    }// end of int getNoOfCandidates()

    /**
     * checks if the input int is a current candidate in the cell
     */
    public boolean isCandidate(int input) {
        return candidates[input - 1];
    }// end of boolean isCandidate

    /**
     * returns the current Candidate at the entered position.
     */
    public int getCandidate(int position) {
        int current_pos = -1;
        for (int i = 0; i < 9; i++) {
            if (candidates[i]) {
                current_pos++;
                if (current_pos == position)
                    return (i + 1);
            }
        }
        return -1;
    }// end of int getCandidate(int)

    /**
     * removes the corresponding candidate to the int input
     */
    public void removeCandidate(int input) {
        candidates[input - 1] = false;
        noOfCandidates--;
    }// end of void removeCandidate

    /**
     * checks if the input cell has the same Candidates as the current cell
     */
    public boolean sameCandidates(Cell other) {
        return (Arrays.equals(this.candidates, other.candidates));
    }// end of boolean sameCandidate(Cell)

    /**
     * inserts the int input to the cell permanently
     */
    public void placeSolution(int input) { // a final decision is made irrespective of the current number of possibilities and Candidates
        solution = input;
    }// end of void placeSolution

    /**
     * gets the inserted value
     */
    public int getSolution() {
        return solution;
    }// end of int getSolution()

    /**
     * checks if the cell has a value permanently inserted in it or not.
     */
    public boolean isNotSet() {
        return (solution == 0);
    }// end of boolean isNotSet()

    public String listCandidates() {
        String s = "";
        for (int i = 0; i < 9; i++) {
            if (candidates[i]) {
                s += (i + 1);
            } else
                s += "_";
            if ((i + 1) % 3 == 0) {
                s += '\n';
            } else
                s += " ";
        }
        return s;
    }

    // public int pencilMarks() {
    //}
}// end of class Cell