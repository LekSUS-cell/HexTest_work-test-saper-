package HexGame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CellTest {
    private Cell cell;

    @BeforeEach
    public void setUp() {
        cell = new Cell();
    }

    @Test
    public void testToggleFlag() {
        assertFalse(cell.isFlagged(), "Изначально флаг не должен быть установлен");
        cell.toggleFlag(); // Исправлено: без параметра
        assertTrue(cell.isFlagged(), "Флаг должен быть установлен");
        cell.toggleFlag(); // Исправлено: без параметра
        assertFalse(cell.isFlagged(), "Флаг должен быть снят");
    }

    @Test
    public void testReveal() {
        assertFalse(cell.isRevealed(), "Изначально клетка не открыта");
        cell.reveal();
        assertTrue(cell.isRevealed(), "Клетка должна быть открыта");
    }

    @Test
    public void testSetBlue() {
        assertFalse(cell.isBlue(), "Изначально клетка не синяя");
        cell.setBlue(true);
        assertTrue(cell.isBlue(), "Клетка должна быть синей");
        cell.setBlue(false);
        assertFalse(cell.isBlue(), "Клетка не должна быть синей");
    }

    @Test
    public void testSetClueType() {
        assertEquals("normal", cell.getClueType(), "Изначально clueType = normal");
        cell.setClueType("line");
        assertEquals("line", cell.getClueType(), "clueType должен быть line");
        cell.setClueType("nonline");
        assertEquals("nonline", cell.getClueType(), "clueType должен быть nonline");
    }

    @Test
    public void testSetClue() {
        assertEquals(0, cell.getClue(), "Изначально clue = 0");
        cell.setClue(2);
        assertEquals(2, cell.getClue(), "clue должен быть 2");
    }
}