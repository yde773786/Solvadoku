package com.yde.solvadoku.Logic;

import java.util.Arrays;

/**
 * Each cell in the Sudoku board is an instance of the Cell class.
 * <p>
 * It consists of an array (candidates) that keeps record of the
 * possible solutions for the specific cell.
 * <p>
 * It also consists of a int member variable (solution) that represents the
 * final solution inserted into the cell.
 */
public class Cell {

    private final boolean[] candidates;
    private int solution;
    private int noOfCandidates;

    /**
     * Initializes array possibilities to true for all numbers from 1 to 9,
     * Thus indicating that all numbers are candidates for the cell initially.
     */
    public Cell() {
        candidates = new boolean[9];
        for (int i = 0; i < 9; i++)
            candidates[i] = true;

        noOfCandidates = 9;
        solution = 0; // 0 means the cell does not have a solution yet.
    }// end of constructor

    /**
     * @return The number of possible solutions for the cell.
     */
    public int getNoOfCandidates() {
        return noOfCandidates;
    }// end of int getNoOfCandidates()

    /**
     * Checks if a number is a possible solution for the cell.
     *
     * @return True if the input is a possible solution for the cell.
     */
    public boolean isCandidate(int input) {
        return candidates[input - 1];
    }// end of boolean isCandidate(int)

    /**
     * Returns the candidate in a given position from the list of candidates.
     * <p>
     * For example, let us consider the candidates for a cell is 1,2,5,7,8
     * and we want the candidate in position 2, the required output will be 5
     * because it is in the 2nd position (because we start counting from 0)
     * in the list of candidates.
     *
     * @param position Position of the candidate.
     * @return The candidate at the entered position, else returns -1 if there
     * is no candidate in that position.
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
     * Removes a number from the list of candidates for the cell
     * by setting the corresponding array index in {@code:candidates} to false.
     *
     * @param candidate_to_remove The number to be removed as a candidate for the cell.
     */
    public void removeCandidate(int candidate_to_remove) {
        candidates[candidate_to_remove - 1] = false;
        noOfCandidates--;
    }// end of void removeCandidate()

    /**
     * Checks if another cell has the same list of candidates as the current cell.
     *
     * @param other Cell instance of the second cell.
     * @return True if the current cell and other cell have the same candidates.
     */
    public boolean sameCandidates(Cell other) {
        return (Arrays.equals(this.candidates, other.candidates));
    }// end of boolean sameCandidate(Cell)

    /**
     * Inserts the final solution in the cell permanently.
     * <p>
     * This is done irrespective of the current number of possible candidates.
     *
     * @param solution The number that is the final solution for the cell.
     */
    public void placeSolution(int solution) {
        this.solution = solution;
    }// end of void placeSolution(int)

    /**
     * @return The solution for the cell if a solution is already found, else 0.
     */
    public int getSolution() {
        return this.solution;
    } // end of int getSolution()

    /**
     * Checks if the cell has a solution or not.
     *
     * @return True if the cell has a solution.
     */
    public boolean isNotSet() {
        return (solution == 0);
    } // end of boolean isNotSet()

    /**
     * Method to generate a String of the candidates for the cell.
     *
     * @return A String containing the possible solutions for the cell.
     */
    public String listCandidates() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            if (candidates[i]) {
                s.append(i + 1);
            } else
                s.append(" ");
            if ((i + 1) % 3 == 0) {
                s.append('\n');
            } else
                s.append(" ");
        }
        return s.toString();
    } // end of String listCandidates()

}// end of class Cell