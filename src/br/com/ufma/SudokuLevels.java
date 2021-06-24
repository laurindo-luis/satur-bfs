package br.com.ufma;

public class SudokuLevels {
    private String level;
    private int maxIter;

    public SudokuLevels(String level, int maxIter) {
        this.level = level;
        this.maxIter = maxIter;
    }

    public String getLevel() {
        return level;
    }

    public int getMaxIter() {
        return maxIter;
    }
}
