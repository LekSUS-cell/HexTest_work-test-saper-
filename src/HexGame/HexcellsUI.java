package HexGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class HexcellsUI extends JPanel {
    private final Game game;
    private final HexGrid grid;
    private final int level; // 1 = EASY, 2 = MEDIUM, 3 = HARD
    private final JFrame frame;
    private final JPanel container;
    private final CardLayout cardLayout;
    private final int hexSize = 30;
    private final int hexSpacing = 3; // Расстояние между гексагонами (пиксели)
    private final double sqrt3 = Math.sqrt(3);
    private double offsetX;
    private double offsetY;
    private BufferedImage backgroundImage;
    private boolean firstClick;
    private JLabel mineCountLabel;
    private JLabel flagCountLabel;
    private int[] hint;

    public HexcellsUI(JFrame frame, JPanel container, CardLayout cardLayout, int rows, int cols, int blueCount, int level) {
        this.game = new Game(new Board(rows, cols, blueCount, level));
        this.grid = game.getBoard().getGrid();
        this.level = level;
        this.frame = frame;
        this.container = container;
        this.cardLayout = cardLayout;
        this.firstClick = true;
        setLayout(new BorderLayout());

        // Загружаем фоновое изображение
        try {
            backgroundImage = ImageIO.read(new File("background.png"));
        } catch (IOException e) {
            System.err.println("Не удалось загрузить фоновое изображение: " + e.getMessage());
            backgroundImage = null;
        }

        // Панель управления
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // Счётчик мин
        mineCountLabel = new JLabel("Мины: " + game.getBlueCount());
        mineCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        controlPanel.add(mineCountLabel);

        // Счётчик флагов
        flagCountLabel = new JLabel("Флаги: 0");
        flagCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        controlPanel.add(flagCountLabel);

        // Кнопка подсказки
        JButton hintButton = new JButton("Подсказка");
        hintButton.addActionListener(e -> {
            int[] hint = game.getHint();
            if (hint != null) {
                setHint(hint);
                repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "Нет детерминированных подсказок!");
            }
        });
        controlPanel.add(hintButton);

        // Кнопка меню
        JButton menuButton = new JButton("Меню");
        menuButton.addActionListener(e -> {
            Dimension size = frame.getSize();
            cardLayout.show(container, "MainMenu");
            frame.setSize(size);
        });
        controlPanel.add(menuButton);

        // Основная панель
        add(controlPanel, BorderLayout.NORTH);
        setBackground(Color.BLACK);

        // Слушатель изменения размера панели
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calculateOffsets();
                repaint();
            }
        });

        // Обработчик мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int[] coords = getHexAt(e.getX(), e.getY());
                if (coords != null) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (firstClick) {
                            game.openFirstCell(coords[0], coords[1]);
                            firstClick = false;
                            updateCounters();
                        } else {
                            game.openCell(coords[0], coords[1]);
                        }
                        if (game.isGameOver() && !game.isWon()) {
                            showGameOverDialog();
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        game.toggleFlag(coords[0], coords[1]);
                        updateCounters();
                    }
                    setHint(null);
                    repaint();
                }
            }
        });
    }

    private void calculateOffsets() {
        int windowWidth = getWidth();
        int windowHeight = getHeight() - 100; // Учитываем панель управления
        int gridWidth = (int) (grid.getCols() * (hexSize * 1.5 + hexSpacing));
        int gridHeight = (int) (grid.getRows() * hexSize * sqrt3 + hexSize * grid.getRows());
        offsetX = (windowWidth - gridWidth) / 2.0;
        offsetY = (windowHeight - gridHeight) / 2.0 + 100; // Смещение с учетом панели
    }

    private void updateCounters() {
        mineCountLabel.setText("Мины: " + game.getBlueCount());
        flagCountLabel.setText("Флаги: " + game.getFlaggedCount());
    }

    private void setHint(int[] hint) {
        this.hint = hint;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем фоновое изображение
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                if (game.getBoard().isActive(r, c)) {
                    drawHex(g2d, r, c);
                }
            }
        }

        if (game.isGameOver() && game.isWon()) {
            g2d.setColor(new Color(0, 0, 0, 128));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Победа!", getWidth() / 2 - 50, getHeight() / 2);
        }
    }

    private void drawHex(Graphics2D g2d, int r, int c) {
        double x = c * (hexSize * 1.5 + hexSpacing) + hexSize + offsetX;
        double y = r * hexSize * sqrt3 + (c % 2 == 0 ? hexSize : hexSize * (sqrt3 / 2 + 1)) + hexSpacing * r + offsetY;
        Path2D hex = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i);
            double px = x + hexSize * Math.cos(angle);
            double py = y + hexSize * Math.sin(angle);
            if (i == 0) {
                hex.moveTo(px, py);
            } else {
                hex.lineTo(px, py);
            }
        }
        hex.closePath();

        Cell cell = game.getBoard().getCell(r, c);
        if (hint != null && hint[0] == r && hint[1] == c) {
            g2d.setColor(hint[2] == 0 ? Color.GREEN : Color.ORANGE);
            g2d.fill(hex);
        } else if (cell.isFlagged()) {
            g2d.setColor(Color.RED);
            g2d.fill(hex);
        } else if (cell.isRevealed()) {
            g2d.setColor(cell.isBlue() ? Color.BLUE : Color.LIGHT_GRAY);
            g2d.fill(hex);
            if (!cell.isBlue()) {
                g2d.setColor(Color.BLACK);
                String clueType = cell.getClueType();
                String clueText = cell.getClue() == 0 ? "0" :
                        clueType.equals("line") ? "-" + cell.getClue() + "-" :
                                clueType.equals("nonline") ? "{" + cell.getClue() + "}" :
                                        String.valueOf(cell.getClue());
                g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                FontMetrics fm = g2d.getFontMetrics();
                float textWidth = fm.stringWidth(clueText);
                g2d.drawString(clueText, (float) (x - textWidth / 2), (float) (y + fm.getAscent() / 2));
            }
        } else {
            g2d.setColor(Color.GRAY);
            g2d.fill(hex);
        }
        g2d.setColor(Color.BLACK);
        g2d.draw(hex);
    }

    private int[] getHexAt(int px, int py) {
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                if (!game.getBoard().isActive(r, c)) continue;
                double x = c * (hexSize * 1.5 + hexSpacing) + hexSize + offsetX;
                double y = r * hexSize * sqrt3 + (c % 2 == 0 ? hexSize : hexSize * (sqrt3 / 2 + 1)) + hexSpacing * r + offsetY;
                double dx = px - x;
                double dy = py - y;
                if (Math.sqrt(dx * dx + dy * dy) < hexSize) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private void showGameOverDialog() {
        JDialog dialog = new JDialog(frame, "Игра окончена!", true);
        dialog.setLayout(new GridLayout(2, 1, 10, 10));

        JLabel messageLabel = new JLabel("Поражение! Что дальше?");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(messageLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton restartButton = new JButton("Начать заново");
        restartButton.addActionListener(e -> {
            Dimension size = frame.getSize();
            container.remove(this);
            HexcellsUI newGame = new HexcellsUI(frame, container, cardLayout,
                    level == 1 ? 5 : level == 2 ? 7 : 9,
                    level == 1 ? 5 : level == 2 ? 7 : 9,
                    level == 1 ? 5 : level == 2 ? 10 : 15,
                    level);
            newGame.setName("Level" + level + "Game");
            container.add(newGame, "Level" + level + "Game");
            cardLayout.show(container, "Level" + level + "Game");
            frame.setSize(size);
            dialog.dispose();
        });
        buttonPanel.add(restartButton);

        JButton menuButton = new JButton("В главное меню");
        menuButton.addActionListener(e -> {
            Dimension size = frame.getSize();
            cardLayout.show(container, "MainMenu");
            frame.setSize(size);
            dialog.dispose();
        });
        buttonPanel.add(menuButton);

        dialog.add(buttonPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}