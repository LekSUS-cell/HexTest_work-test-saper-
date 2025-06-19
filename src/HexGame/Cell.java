package HexGame;

public class Cell {
    public boolean active;
    private boolean blue;
    private boolean revealed;
    private boolean flagged;
    private int clue;
    private String clueType; // "line", "nonline", "normal"

    public Cell() {
        this.active = true;
        this.blue = false;
        this.revealed = false;
        this.flagged = false;
        this.clue = 0;
        this.clueType = "normal";
    }

    public void setBlue(boolean blue) { this.blue = blue; }
    public boolean isBlue() { return blue; }
    public void reveal() { this.revealed = true; }
    public boolean isRevealed() { return revealed; }
    public void toggleFlag() { this.flagged = !flagged; }
    public boolean isFlagged() { return flagged; }
    public void setClue(int clue) { this.clue = clue; }
    public int getClue() { return clue; }
    public void setClueType(String clueType) { this.clueType = clueType; }
    public String getClueType() { return clueType; }
}