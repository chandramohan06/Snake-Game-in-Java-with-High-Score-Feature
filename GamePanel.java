import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 150;
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten;
    int highScore = 0;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean running = false;
    boolean paused = false;
    Timer timer;
    Random random;
    JButton pauseButton;
    JButton restartButton;
    File highScoreFile = new File("highscore.txt");

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT + 50));
        this.setBackground(Color.black);
        this.setLayout(null);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        pauseButton = new JButton("Pause");
        pauseButton.setBounds(10, SCREEN_HEIGHT + 10, 100, 30);
        pauseButton.addActionListener(e -> togglePause());
        this.add(pauseButton);

        restartButton = new JButton("Restart");
        restartButton.setBounds(120, SCREEN_HEIGHT + 10, 100, 30);
        restartButton.addActionListener(e -> restartGame());
        this.add(restartButton);

        loadHighScore();
        startGame();
    }

    public void loadHighScore() {
        try {
            if (highScoreFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(highScoreFile));
                highScore = Integer.parseInt(reader.readLine());
                reader.close();
            }
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    public void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(highScoreFile));
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        applesEaten = 0;
        bodyParts = 6;
        direction = 'R';
        for (int i = 0; i < x.length; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        newApple();
        running = true;
        paused = false;
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, this);
        timer.start();
        requestFocusInWindow();
    }

    public void togglePause() {
        if (running) {
            paused = !paused;
            if (paused) {
                timer.stop();
                pauseButton.setText("Resume");
            } else {
                timer.start();
                pauseButton.setText("Pause");
            }
            repaint();
        }
    }

    public void restartGame() {
        if (applesEaten > highScore) {
            highScore = applesEaten;
            saveHighScore();
        }
        startGame();
        repaint();
        pauseButton.setText("Pause");
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten,
                    (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2,
                    g.getFont().getSize());

            g.setColor(Color.yellow);
            g.setFont(new Font("Ink Free", Font.BOLD, 20));
            g.drawString("High Score: " + highScore, 400, SCREEN_HEIGHT + 30);

            if (paused) {
                g.setColor(Color.white);
                g.setFont(new Font("Ink Free", Font.BOLD, 50));
                FontMetrics pauseMetrics = getFontMetrics(g.getFont());
                g.drawString("Paused", (SCREEN_WIDTH - pauseMetrics.stringWidth("Paused")) / 2, SCREEN_HEIGHT / 2);
            }
        } else {
            gameOver(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int)(SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int)(SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH ||
                y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            if (applesEaten > highScore) {
                highScore = applesEaten;
                saveHighScore();
            }
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten,
                (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2,
                g.getFont().getSize());

        g.setColor(Color.red);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over",
                (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2,
                SCREEN_HEIGHT / 2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
            }
        }
    }
}
