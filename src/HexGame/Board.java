package HexGame;

import java.util.*;

public class Board {
    private final HexGrid grid;
    private final Cell[][] cells;
    private int blueCount;
    private final int level; // 1 = EASY, 2 = MEDIUM, 3 = HARD

    public Board(int rows, int cols, int blueCount, int level) {
        this.grid = new HexGrid(rows, cols);
        this.cells = new Cell[rows][cols];
        this.blueCount = blueCount;
        this.level = level;
        initializeCells();
        if (level == 2) {
            generateHorizontalDiamondShape();
        } else if (level == 3) {
            generateHexagonShape();
        } else {
            generateDiamondShape();
        }
    }

    private void initializeCells() {
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                cells[r][c] = new Cell();
                cells[r][c].active = false; // По умолчанию все клетки неактивны
            }
        }
    }

    private void generateDiamondShape() {
        // Ромб 5x5 для уровня EASY
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                if (Math.abs(r - 2) + Math.abs(c - 2) <= 2) {
                    cells[r][c].active = true;
                }
            }
        }
    }

    private void generateHorizontalDiamondShape() {
        // Ромб 7x7 для уровня MEDIUM, длинные стороны по горизонтали
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                // Вытянутый ромб: больше клеток по горизонтали
                if (Math.abs(r - 3) * 1.5 + Math.abs(c - 3) <= 4) {
                    cells[r][c].active = true;
                }
            }
        }
    }

    private void generateHexagonShape() {
        // Гексагон 9x9 для уровня HARD с двумя пробелами по 3 гексагона
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                // Формируем большой гексагон
                if (Math.abs(r - 4) + Math.abs(c - 4) + Math.abs(r + c - 8) <= 8) {
                    cells[r][c].active = true;
                }
            }
        }
        // Пробел 1: треугольник вверху слева
        cells[2][2].active = false;
        cells[2][3].active = false;
        cells[3][2].active = false;
        // Пробел 2: треугольник внизу справа
        cells[6][6].active = false;
        cells[6][5].active = false;
        cells[5][6].active = false;
    }

    public void generateBlues(int safeR, int safeC) {
        Random rand = new Random();
        int targetBlues = blueCount;
        blueCount = 0;
        List<Integer> availableCells = new ArrayList<>();
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                if (!cells[r][c].active || (r == safeR && c == safeC)) continue;
                boolean isSafeNeighbor = false;
                for (int neighbor : grid.getNeighbors(r, c)) {
                    if (neighbor == -1) continue;
                    int nr = neighbor / grid.getCols();
                    int nc = neighbor % grid.getCols();
                    if (nr == safeR && nc == safeC) {
                        isSafeNeighbor = true;
                        break;
                    }
                }
                if (!isSafeNeighbor) {
                    availableCells.add(r * grid.getCols() + c);
                }
            }
        }
        Collections.shuffle(availableCells, rand);
        for (int i = 0; i < Math.min(targetBlues, availableCells.size()); i++) {
            int idx = availableCells.get(i);
            int r = idx / grid.getCols();
            int c = idx % grid.getCols();
            cells[r][c].setBlue(true);
            blueCount++;
            System.out.println("Синяя клетка размещена в [" + r + "," + c + "]");
        }
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                if (!cells[r][c].active || cells[r][c].isBlue()) continue;
                int clue = 0;
                for (int neighbor : grid.getNeighbors(r, c)) {
                    if (neighbor == -1) continue;
                    int nr = neighbor / grid.getCols();
                    int nc = neighbor % grid.getCols();
                    if (!cells[nr][nc].active) continue;
                    if (cells[nr][nc].isBlue()) clue++;
                }
                cells[r][c].setClue(clue);
            }
        }
        System.out.println("Стартовая ячейка [" + safeR + "," + safeC + "] безопасная");
    }

    public int getBlueCount() { return blueCount; }
    public Cell getCell(int r, int c) { return cells[r][c]; }
    public HexGrid getGrid() { return grid; }
    public boolean isActive(int r, int c) { return cells[r][c].active; }
}