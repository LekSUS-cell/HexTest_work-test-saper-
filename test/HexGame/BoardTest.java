package HexGame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board(5, 5, 5, 1);
    }

    @Test
    public void testGenerateBlues() {
        board.generateBlues(2, 2);
        assertEquals(5, board.getBlueCount(), "Должно быть 5 мин");
        assertFalse(board.getCell(2, 2).isBlue(), "Стартовая клетка не должна быть синей");
        int[] neighbors = board.getGrid().getNeighbors(2, 2);
        for (int idx : neighbors) {
            if (idx == -1) continue;
            int nr = idx / board.getGrid().getCols();
            int nc = idx % board.getGrid().getCols();
            assertFalse(board.getCell(nr, nc).isBlue(), "Соседняя клетка не должна быть синей");
        }
    }

    @Test
    public void testGenerateDiamondShape() {
        int[][] expectedActive = {
                {0, 0, 1, 0, 0},
                {0, 1, 1, 1, 0},
                {1, 1, 1, 1, 1},
                {0, 1, 1, 1, 0},
                {0, 0, 1, 0, 0}
        };
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                assertEquals(expectedActive[r][c] == 1, board.isActive(r, c),
                        "Клетка [" + r + "," + c + "] должна быть " + (expectedActive[r][c] == 1 ? "активной" : "неактивной"));
            }
        }
    }

    @Test
    public void testGenerateHorizontalDiamondShape() {
        board = new Board(7, 7, 10, 2);
        int[][] expectedActive = {
                {0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0, 0},
                {0, 1, 1, 1, 1, 1, 0},
                {1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 0},
                {0, 0, 1, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0}
        };
        // Отладочный вывод всей доски
        System.out.println("testGenerateHorizontalDiamondShape: Actual board state:");
        for (int r = 0; r < 7; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < 7; c++) {
                row.append(board.isActive(r, c) ? "1 " : "0 ");
            }
            System.out.println(row.toString());
        }
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                boolean expected = expectedActive[r][c] == 1;
                boolean actual = board.isActive(r, c);
                if (expected != actual) {
                    System.out.println("testGenerateHorizontalDiamondShape: Mismatch at [" + r + "," + c + "]: expected " + expected + ", got " + actual);
                }
                assertEquals(expected, actual,
                        "Клетка [" + r + "," + c + "] должна быть " + (expected ? "активной" : "неактивной"));
            }
        }
    }

    @Test
    public void testGenerateHexagonShape() {
        board = new Board(9, 9, 15, 3);
        assertFalse(board.isActive(2, 2), "Пробел [2,2] должен быть неактивным");
        assertFalse(board.isActive(2, 3), "Пробел [2,3] должен быть неактивным");
        assertFalse(board.isActive(3, 2), "Пробел [3,2] должен быть неактивным");
        assertFalse(board.isActive(6, 6), "Пробел [6,6] должен быть неактивным");
        assertFalse(board.isActive(6, 5), "Пробел [6,5] должен быть неактивным");
        assertFalse(board.isActive(5, 6), "Пробел [5,6] должен быть неактивным");
        int activeCount = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.isActive(r, c)) activeCount++;
            }
        }
        assertTrue(activeCount > 30, "Гексагон должен содержать достаточно активных клеток");
    }
}