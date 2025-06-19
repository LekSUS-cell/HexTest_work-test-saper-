package HexGame;

import java.util.*;

public class Game {
    private final Board board;
    private boolean gameOver;
    private boolean won;
    private int[] hint;
    private boolean firstMove;
    private int lineClueCount; // Счётчик для -N-
    private int nonlineClueCount; // Счётчик для {N}

    public Game(Board board) {
        this.board = board;
        this.gameOver = false;
        this.won = false;
        this.hint = null;
        this.firstMove = true;
        this.lineClueCount = 0;
        this.nonlineClueCount = 0;
    }

    public void openFirstCell(int r, int c) {
        if (!board.getGrid().isValid(r, c) || !board.isActive(r, c) || !firstMove) return;
        Cell cell = board.getCell(r, c);
        if (cell.isFlagged() || cell.isRevealed()) return;

        // Генерируем поле, исключая стартовую клетку и её соседей
        board.generateBlues(r, c);
        revealCell(r, c);
        firstMove = false;
        hint = null;
        lineClueCount = 0; // Сбрасываем счётчики
        nonlineClueCount = 0;
        if (checkWin()) {
            gameOver = true;
            won = true;
        }
    }

    public void openCell(int r, int c) {
        if (gameOver || !board.getGrid().isValid(r, c) || !board.isActive(r, c)) return;
        Cell cell = board.getCell(r, c);
        if (cell.isFlagged() || cell.isRevealed()) return;
        revealCell(r, c);
        hint = null;
        if (cell.isBlue()) {
            gameOver = true;
            won = false;
        } else if (checkWin()) {
            gameOver = true;
            won = true;
        }
    }

    private void revealCell(int r, int c) {
        Cell cell = board.getCell(r, c);
        cell.reveal();
        if (!cell.isBlue()) {
            // Устанавливаем clueType при открытии ячейки
            String clueType = determineClueType(r, c);
            cell.setClueType(clueType);
        }
    }

    private String determineClueType(int r, int c) {
        int clue = board.getCell(r, c).getClue();
        if (clue == 0) return "normal";

        // Собираем индексы синих соседей
        List<Integer> blueNeighbors = new ArrayList<>();
        int[] neighbors = board.getGrid().getNeighbors(r, c);
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] == -1) continue;
            int nr = neighbors[i] / board.getGrid().getCols();
            int nc = neighbors[i] % board.getGrid().getCols();
            if (!board.isActive(nr, nc)) continue;
            if (board.getCell(nr, nc).isBlue()) {
                blueNeighbors.add(i);
            }
        }

        // Проверяем, что все синие соседи соответствуют clue
        if (blueNeighbors.size() == clue) {
            if (blueNeighbors.size() == 2 && lineClueCount < 3) {
                int idx1 = blueNeighbors.get(0);
                int idx2 = blueNeighbors.get(1);
                // Противоположные соседи (линейные: 0-3, 1-4, 2-5)
                if ((idx1 == 0 && idx2 == 3) || (idx1 == 3 && idx2 == 0) ||
                        (idx1 == 1 && idx2 == 4) || (idx1 == 4 && idx2 == 1) ||
                        (idx1 == 2 && idx2 == 5) || (idx1 == 5 && idx2 == 2)) {
                    lineClueCount++;
                    return "line"; // -N-
                }
            } else if (blueNeighbors.size() == 2 && nonlineClueCount < 3) {
                nonlineClueCount++;
                return "nonline"; // {N}
            }

            if (blueNeighbors.size() == 3) {
                // Проверяем линейность для трёх соседей
                Collections.sort(blueNeighbors);
                int idx1 = blueNeighbors.get(0);
                int idx2 = blueNeighbors.get(1);
                int idx3 = blueNeighbors.get(2);
                // Линейные конфигурации: три соседа в "полулинии"
                if ((idx1 == 0 && idx2 == 1 && idx3 == 5) ||
                        (idx1 == 1 && idx2 == 2 && idx3 == 0) ||
                        (idx1 == 2 && idx2 == 3 && idx3 == 1) ||
                        (idx1 == 3 && idx2 == 4 && idx3 == 2) ||
                        (idx1 == 4 && idx2 == 5 && idx3 == 3) ||
                        (idx1 == 5 && idx2 == 0 && idx3 == 4)) {
                    if (lineClueCount < 3) {
                        lineClueCount++;
                        return "line"; // -N-
                    }
                } else if (nonlineClueCount < 3) {
                    nonlineClueCount++;
                    return "nonline"; // {N}
                }
            }
        }
        return "normal"; // Просто цифра
    }

    public String getClueType(int r, int c) {
        if (!board.getGrid().isValid(r, c) || !board.isActive(r, c) || !board.getCell(r, c).isRevealed() || board.getCell(r, c).isBlue()) {
            return "normal";
        }
        return board.getCell(r, c).getClueType();
    }

    public void toggleFlag(int r, int c) {
        if (gameOver || !board.getGrid().isValid(r, c) || !board.isActive(r, c)) return;
        Cell cell = board.getCell(r, c);
        if (!cell.isRevealed()) {
            cell.toggleFlag();
            hint = null; // Сбрасываем подсказку после действия игрока
            if (checkWin()) {
                gameOver = true;
                won = true;
            }
        }
    }

    private boolean checkWin() {
        for (int r = 0; r < board.getGrid().getRows(); r++) {
            for (int c = 0; c < board.getGrid().getCols(); c++) {
                if (!board.isActive(r, c)) continue;
                Cell cell = board.getCell(r, c);
                if (cell.isBlue() && !cell.isFlagged()) return false;
                if (!cell.isBlue() && !cell.isRevealed()) return false;
            }
        }
        return true;
    }

    public int[] getHint() {
        if (hint != null) return hint;

        int totalBlues = board.getBlueCount();
        int flaggedCount = getFlaggedCount();

        // Случай 1: Все мины помечены, открываем безопасные клетки
        if (flaggedCount == totalBlues) {
            for (int r = 0; r < board.getGrid().getRows(); r++) {
                for (int c = 0; c < board.getGrid().getCols(); c++) {
                    if (!board.isActive(r, c) || board.getCell(r, c).isRevealed() || board.getCell(r, c).isFlagged()) continue;
                    hint = new int[]{r, c, 0}; // 0 = открыть (зелёный)
                    return hint;
                }
            }
        }

        // Случай 2: Проверяем открытые клетки
        for (int r = 0; r < board.getGrid().getRows(); r++) {
            for (int c = 0; c < board.getGrid().getCols(); c++) {
                if (!board.isActive(r, c) || board.getCell(r, c).isBlue() || !board.getCell(r, c).isRevealed()) continue;

                int clue = board.getCell(r, c).getClue();
                int unrevealedNeighbors = 0; // Неоткрытые и нефлагованные соседи
                int flaggedNeighbors = 0;    // Соседи с флагами

                // Подсчитываем соседей
                for (int neighbor : board.getGrid().getNeighbors(r, c)) {
                    if (neighbor == -1) continue;
                    int nr = neighbor / board.getGrid().getCols();
                    int nc = neighbor % board.getGrid().getCols();
                    if (!board.isActive(nr, nc)) continue;
                    if (!board.getCell(nr, nc).isRevealed() && !board.getCell(nr, nc).isFlagged()) unrevealedNeighbors++;
                    if (board.getCell(nr, nc).isFlagged()) flaggedNeighbors++;
                }

                // Подсказка 1: clue - flagged = unrevealed (все неоткрытые соседи синие)
                if (clue - flaggedNeighbors == unrevealedNeighbors && unrevealedNeighbors > 0) {
                    for (int neighbor : board.getGrid().getNeighbors(r, c)) {
                        if (neighbor == -1) continue;
                        int nr = neighbor / board.getGrid().getCols();
                        int nc = neighbor % board.getGrid().getCols();
                        if (!board.isActive(nr, nc) || board.getCell(nr, nc).isRevealed() || board.getCell(nr, nc).isFlagged()) continue;
                        hint = new int[]{nr, nc, 1}; // 1 = флаг (оранжевый)
                        return hint;
                    }
                }

                // Подсказка 2: clue - flagged = 0 (все неоткрытые соседи безопасны)
                if (clue - flaggedNeighbors == 0 && unrevealedNeighbors > 0) {
                    for (int neighbor : board.getGrid().getNeighbors(r, c)) {
                        if (neighbor == -1) continue;
                        int nr = neighbor / board.getGrid().getCols();
                        int nc = neighbor % board.getGrid().getCols();
                        if (!board.isActive(nr, nc) || board.getCell(nr, nc).isRevealed() || board.getCell(nr, nc).isFlagged()) continue;
                        hint = new int[]{nr, nc, 0}; // 0 = открыть (зелёный)
                        return hint;
                    }
                }
            }
        }

        // Случай 3: Нет детерминированных подсказок ("50 на 50")
        return null; // Бот не может помочь, игрок должен угадать
    }

    public int getBlueCount() {
        return board.getBlueCount();
    }

    public int getFlaggedCount() {
        int count = 0;
        for (int r = 0; r < board.getGrid().getRows(); r++) {
            for (int c = 0; c < board.getGrid().getCols(); c++) {
                if (board.isActive(r, c) && board.getCell(r, c).isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isGameOver() { return gameOver; }
    public boolean isWon() { return won; }
    public Board getBoard() { return board; }
}