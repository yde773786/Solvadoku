package com.yde.solvadoku;

import android.util.Log;

import java.util.Arrays;

/**
 * An instance of the Cell class is used to represent one cell in the sudoku
 * puzzle.It consists of an array (candidates) that contains a list of the
 * possible candidates that a specific cell can have. It also consists of a byte
 * member variable (solution) that is meant to represent the final inserted
 * number into the cell.
 */
public class Cell {

    private final boolean[] candidates;
    private byte solution;
    private byte noOfCandidates;

    /**
     * initializes array possibilities true for all bytes from 1 to 9, indicating
     * that all numbers are candidates initially.
     */
    public Cell() {
        candidates = new boolean[9];
        for (byte i = 0; i < 9; i++)
            candidates[i] = true;

        noOfCandidates = 9;
        solution = 0;
    }// end of constructor

    /**
     * provides the number of candidates for the cell
     */
    public byte getNoOfCandidates() {
        return (byte) noOfCandidates;
    }// end of byte getNoOfCandidates()

    /**
     * checks if the input byte is a current candidate in the cell
     */
    public boolean isCandidate(byte input) {
        return candidates[input - 1];
    }// end of boolean isCandidate(byte)

    /**
     * returns the current Candidate at the entered position.
     */
    public byte getCandidate(int position) {
        int current_pos = -1;
        for (byte i = 0; i < 9; i++) {
            if (candidates[i]) {
                current_pos++;
                if (current_pos == position)
                    return (byte) (i + 1);
            }
        }
        return -1;
    }// end of byte getCandidate(int)

    /**
     * removes the corresponding candidate to the byte input
     */
    public void removeCandidate(byte input) {
        candidates[input - 1] = false;
        noOfCandidates--;
    }// end of void removeCandidate(byte)

    /**
     * checks if the input cell has the same Candidates as the current cell
     */
    public boolean sameCandidates(Cell other) {
        return (Arrays.equals(this.candidates, other.candidates));
    }// end of boolean sameCandidate(Cell)

    /**
     * inserts the byte input to the cell permanently
     */
    public void placeSolution(byte input) { // a final decision is made irrespective of the current number of possibilities and Candidates
        solution = input;
    }// end of void placeSolution(byte)

    /**
     * gets the inserted value
     */
    public byte getSolution() {
        return solution;
    }// end of byte getSolution()

    /**
     * checks if the cell has a value permanently inserted in it or not.
     */
    public boolean isNotSet() {
        return (solution == 0);
    }// end of boolean isNotSet()

    public String listCandidates() {
        String s = "";
        for (byte i = 0; i < 9; i++) {
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