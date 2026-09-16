import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BrickBreaker extends JFrame {
    public BrickBreaker() {
        super("Brick Breaker — Dynamic Arcade Edition");
        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BrickBreaker::new);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {

    // ---- Board dimensions ----
    static final int WIDTH = 800;
    static final int HEIGHT = 650;
    static final int BRICK_ROWS_BASE = 4;
    static final int BRICK_COLS = 10;
    static final int BRICK_WIDTH = 70;
    static final int BRICK_HEIGHT = 24;
    static final int BRICK_TOP_OFFSET = 70;
    static final int BRICK_GAP = 6;

    // ---- Game state ----
    enum State { MENU, LAUNCH, RUNNING, PAUSED, LEVEL_COMPLETE, GAME_OVER, WIN }
    State state = State.MENU;

    Timer timer;
    Random rand = new Random();

    Paddle paddle;
    List<Ball> balls = new ArrayList<>();
    List<Brick> bricks = new ArrayList<>();
    List<PowerUp> powerUps = new ArrayList<>();
    List<Particle> particles = new ArrayList<>();

    int score = 0;
    int lives = 3;
    int level = 1;
    static final int MAX_LEVEL = 5;

    boolean movingLeft = false;
    boolean movingRight = false;

    // Timed effects
    int widePaddleTimer = 0;
    int slowBallTimer = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(15, 15, 25));
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        initGame();
    }

    /* ---------------- INITIALIZATION ---------------- */

    void initGame() {
        score = 0;
        lives = 3;
        level = 1;
        state = State.MENU;
        paddle = new Paddle(WIDTH / 2 - 45, HEIGHT - 40);
        balls.clear();
        balls.add(new Ball(WIDTH / 2, HEIGHT - 60));
        powerUps.clear();
        particles.clear();
        buildLevel(level);
    }

    void buildLevel(int lvl) {
        bricks.clear();
        int rows = Math.min(BRICK_ROWS_BASE + lvl, 9);
        int totalWidth = BRICK_COLS * (BRICK_WIDTH + BRICK_GAP) - BRICK_GAP;
        int startX = (WIDTH - totalWidth) / 2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < BRICK_COLS; c++) {
                // Skip some bricks randomly at higher levels for variety/gaps
                if (lvl >= 3 && rand.nextInt(10) == 0) continue;

                int hp = 1;
                double roll = rand.nextDouble();
                if (r < rows / 3) {
                    hp = 3; // top rows tougher
                } else if (r < (2 * rows) / 3) {
                    hp = (roll < 0.5 + lvl * 0.03) ? 2 : 1;
                } else {
                    hp = 1;
                }
                // Occasional unbreakable "steel" brick at higher levels
                boolean steel = (lvl >= 4 && rand.nextInt(18) == 0 && r < rows / 2);

                int x = startX + c * (BRICK_WIDTH + BRICK_GAP);
                int y = BRICK_TOP_OFFSET + r * (BRICK_HEIGHT + BRICK_GAP);
                bricks.add(new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, hp, steel));
            }
        }
    }

    void resetBallOnPaddle() {
        balls.clear();
        balls.add(new Ball(paddle.x + paddle.width / 2, paddle.y - 12));
        state = State.LAUNCH;
    }

    /* ---------------- MAIN LOOP ---------------- */

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
        repaint();
    }

    void update() {
        updateParticles();

        if (state == State.RUNNING) {
            movePaddle();
            moveBalls();
            movePowerUps();
            checkBrickCollisions();
            checkLevelComplete();
            tickTimers();
        } else if (state == State.LAUNCH) {
            movePaddle();
            // Ball follows paddle until launch
            if (!balls.isEmpty()) {
                Ball b = balls.get(0);
                b.x = paddle.x + paddle.width / 2.0;
                b.y = paddle.y - b.radius - 2;
            }
        }
    }

    void tickTimers() {
        if (widePaddleTimer > 0) {
            widePaddleTimer--;
            if (widePaddleTimer == 0) paddle.setWide(false);
        }
        if (slowBallTimer > 0) {
            slowBallTimer--;
            if (slowBallTimer == 0) {
                for (Ball b : balls) b.setSpeedMultiplier(1.0);
            }
        }
    }

    void movePaddle() {
        if (movingLeft) paddle.x -= paddle.speed;
        if (movingRight) paddle.x += paddle.speed;
        if (paddle.x < 10) paddle.x = 10;
        if (paddle.x + paddle.width > WIDTH - 10) paddle.x = WIDTH - 10 - paddle.width;
    }

    void moveBalls() {
        List<Ball> lost = new ArrayList<>();
        for (Ball b : balls) {
            b.move();

            // wall collisions
            if (b.x - b.radius <= 10) {
                b.x = 10 + b.radius;
                b.dx = -b.dx;
            }
            if (b.x + b.radius >= WIDTH - 10) {
                b.x = WIDTH - 10 - b.radius;
                b.dx = -b.dx;
            }
            if (b.y - b.radius <= 10) {
                b.y = 10 + b.radius;
                b.dy = -b.dy;
            }

            // paddle collision
            if (b.dy > 0 && b.y + b.radius >= paddle.y && b.y + b.radius <= paddle.y + paddle.height + 12
                    && b.x >= paddle.x - b.radius && b.x <= paddle.x + paddle.width + b.radius) {
                double hitPos = (b.x - (paddle.x + paddle.width / 2.0)) / (paddle.width / 2.0);
                hitPos = Math.max(-1, Math.min(1, hitPos));
                double angle = hitPos * Math.toRadians(65); // max 65 degree deflection
                double speed = b.currentSpeed();
                b.dx = speed * Math.sin(angle);
                b.dy = -Math.abs(speed * Math.cos(angle));
                b.y = paddle.y - b.radius - 1;
            }

            // fell off bottom
            if (b.y - b.radius > HEIGHT) {
                lost.add(b);
            }
        }
        balls.removeAll(lost);

        if (balls.isEmpty() && (state == State.RUNNING)) {
            loseLife();
        }
    }

    void loseLife() {
        lives--;
        spawnBurst(paddle.x + paddle.width / 2, paddle.y, new Color(255, 90, 90), 20);
        if (lives <= 0) {
            state = State.GAME_OVER;
        } else {
            resetBallOnPaddle();
        }
    }

    void movePowerUps() {
        List<PowerUp> caught = new ArrayList<>();
        List<PowerUp> missed = new ArrayList<>();
        for (PowerUp p : powerUps) {
            p.y += p.fallSpeed;
            if (p.y + p.size >= paddle.y && p.y <= paddle.y + paddle.height
                    && p.x + p.size >= paddle.x && p.x <= paddle.x + paddle.width) {
                caught.add(p);
                applyPowerUp(p.type);
                spawnBurst(p.x + p.size / 2, p.y, p.color(), 14);
            } else if (p.y > HEIGHT) {
                missed.add(p);
            }
        }
        powerUps.removeAll(caught);
        powerUps.removeAll(missed);
    }

    void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case WIDEN:
                paddle.setWide(true);
                widePaddleTimer = 600; // ~10s
                break;
            case SHRINK:
                paddle.setWide(false);
                paddle.shrinkPenalty(180); // brief penalty width
                break;
            case MULTIBALL:
                List<Ball> extra = new ArrayList<>();
                for (Ball b : new ArrayList<>(balls)) {
                    Ball b1 = b.copyWithAngleOffset(Math.toRadians(25));
                    Ball b2 = b.copyWithAngleOffset(Math.toRadians(-25));
                    extra.add(b1);
                    extra.add(b2);
                }
                balls.addAll(extra);
                break;
            case SLOW:
                for (Ball b : balls) b.setSpeedMultiplier(0.6);
                slowBallTimer = 480;
                break;
            case FAST:
                for (Ball b : balls) b.setSpeedMultiplier(1.5);
                slowBallTimer = 300;
                break;
            case EXTRA_LIFE:
                lives++;
                break;
        }
    }

    void checkBrickCollisions() {
        for (Ball b : balls) {
            Brick hit = null;
            for (Brick brick : bricks) {
                if (!brick.alive) continue;
                if (b.x + b.radius >= brick.x && b.x - b.radius <= brick.x + brick.width
                        && b.y + b.radius >= brick.y && b.y - b.radius <= brick.y + brick.height) {
                    hit = brick;
                    break;
                }
            }
            if (hit != null) {
                resolveBrickHit(b, hit);
            }
        }
    }

    void resolveBrickHit(Ball b, Brick brick) {
        // Determine bounce direction based on overlap sides
        double overlapLeft = (b.x + b.radius) - brick.x;
        double overlapRight = (brick.x + brick.width) - (b.x - b.radius);
        double overlapTop = (b.y + b.radius) - brick.y;
        double overlapBottom = (brick.y + brick.height) - (b.y - b.radius);

        double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));
        if (minOverlap == overlapLeft || minOverlap == overlapRight) {
            b.dx = -b.dx;
        } else {
            b.dy = -b.dy;
        }

        if (!brick.steel) {
            brick.hp--;
            spawnBurst(brick.x + brick.width / 2.0, brick.y + brick.height / 2.0, brick.color(), 8);
            if (brick.hp <= 0) {
                brick.alive = false;
                score += brick.maxHp * 10;
                maybeDropPowerUp(brick);
            } else {
                score += 5;
            }
        } else {
            score += 1; // small reward for hitting steel
        }
    }

    void maybeDropPowerUp(Brick brick) {
        if (rand.nextDouble() < 0.18) {
            PowerUp.Type[] types = PowerUp.Type.values();
            PowerUp.Type t = types[rand.nextInt(types.length)];
            powerUps.add(new PowerUp(brick.x + brick.width / 2.0 - 10, brick.y, t));
        }
    }

    void checkLevelComplete() {
        boolean anyBreakable = false;
        for (Brick b : bricks) {
            if (b.alive && !b.steel) { anyBreakable = true; break; }
        }
        if (!anyBreakable) {
            if (level >= MAX_LEVEL) {
                state = State.WIN;
            } else {
                state = State.LEVEL_COMPLETE;
            }
        }
    }

    void nextLevel() {
        level++;
        buildLevel(level);
        powerUps.clear();
        paddle.setWide(false);
        widePaddleTimer = 0;
        slowBallTimer = 0;
        resetBallOnPaddle();
    }

    /* ---------------- PARTICLES (visual flair) ---------------- */

    void spawnBurst(double x, double y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            double ang = rand.nextDouble() * Math.PI * 2;
            double spd = 1 + rand.nextDouble() * 3;
            particles.add(new Particle(x, y, Math.cos(ang) * spd, Math.sin(ang) * spd, color));
        }
    }

    void updateParticles() {
        List<Particle> dead = new ArrayList<>();
        for (Particle p : particles) {
            p.update();
            if (p.life <= 0) dead.add(p);
        }
        particles.removeAll(dead);
    }

    /* ---------------- RENDERING ---------------- */

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g);
        drawBorder(g);

        for (Brick b : bricks) b.draw(g);
        for (PowerUp p : powerUps) p.draw(g);
        for (Particle p : particles) p.draw(g);

        paddle.draw(g);
        for (Ball b : balls) b.draw(g);

        drawHUD(g);

        switch (state) {
            case MENU -> drawCenteredOverlay(g, "BRICK BREAKER",
                    "Press SPACE to start   |   ←/→ or A/D to move");
            case LAUNCH -> drawCenteredOverlay(g, "Level " + level,
                    "Press SPACE to launch the ball");
            case PAUSED -> drawCenteredOverlay(g, "PAUSED", "Press P to resume");
            case LEVEL_COMPLETE -> drawCenteredOverlay(g, "Level " + level + " Complete!",
                    "Press SPACE for next level");
            case GAME_OVER -> drawCenteredOverlay(g, "GAME OVER",
                    "Score: " + score + "   |   Press R to restart");
            case WIN -> drawCenteredOverlay(g, "YOU WIN!",
                    "Final Score: " + score + "   |   Press R to play again");
            default -> {}
        }
    }

    void drawBackground(Graphics2D g) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 40), 0, HEIGHT, new Color(8, 8, 16));
        g.setPaint(gp);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    void drawBorder(Graphics2D g) {
        g.setColor(new Color(80, 80, 120));
        g.setStroke(new BasicStroke(4));
        g.drawRect(5, 5, WIDTH - 10, HEIGHT - 10);
    }

    void drawHUD(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 35);
        g.drawString("Level: " + level + " / " + MAX_LEVEL, WIDTH / 2 - 50, 35);

        // lives as small hearts/paddles
        g.drawString("Lives:", WIDTH - 150, 35);
        for (int i = 0; i < lives; i++) {
            g.setColor(new Color(255, 80, 100));
            g.fillRoundRect(WIDTH - 90 + i * 22, 22, 16, 10, 4, 4);
        }
    }

    void drawCenteredOverlay(Graphics2D g, String title, String subtitle) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 42));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(title);
        g.drawString(title, (WIDTH - tw) / 2, HEIGHT / 2 - 20);

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        fm = g.getFontMetrics();
        int sw = fm.stringWidth(subtitle);
        g.drawString(subtitle, (WIDTH - sw) / 2, HEIGHT / 2 + 20);
    }

    /* ---------------- INPUT ---------------- */

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) movingLeft = true;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) movingRight = true;

        if (code == KeyEvent.VK_SPACE) {
            if (state == State.MENU) {
                state = State.LAUNCH;
            } else if (state == State.LAUNCH) {
                launchBall();
            } else if (state == State.LEVEL_COMPLETE) {
                nextLevel();
            }
        }

        if (code == KeyEvent.VK_P) {
            if (state == State.RUNNING) state = State.PAUSED;
            else if (state == State.PAUSED) state = State.RUNNING;
        }

        if (code == KeyEvent.VK_R) {
            if (state == State.GAME_OVER || state == State.WIN) {
                initGame();
            }
        }
    }

    void launchBall() {
        if (!balls.isEmpty()) {
            Ball b = balls.get(0);
            double angle = Math.toRadians(-60 + rand.nextInt(30) - 15); // roughly up-ish
            double speed = b.baseSpeed;
            b.dx = speed * Math.sin(angle);
            b.dy = -Math.abs(speed * Math.cos(angle));
        }
        state = State.RUNNING;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) movingLeft = false;
        if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) movingRight = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

/* =========================================================
 *  PADDLE
 * ========================================================= */
class Paddle {
    double x, y;
    int width = 90;
    int height = 16;
    int baseWidth = 90;
    int wideWidth = 140;
    int shrinkWidth = 55;
    double speed = 7.5;
    int shrinkTimer = 0;

    Paddle(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void setWide(boolean wide) {
        width = wide ? wideWidth : baseWidth;
    }

    void shrinkPenalty(int frames) {
        width = shrinkWidth;
        shrinkTimer = frames;
    }

    void draw(Graphics2D g) {
        if (shrinkTimer > 0) {
            shrinkTimer--;
            if (shrinkTimer == 0) width = baseWidth;
        }
        GradientPaint gp = new GradientPaint((float) x, (float) y, new Color(90, 180, 255),
                (float) x, (float) (y + height), new Color(40, 100, 200));
        g.setPaint(gp);
        g.fillRoundRect((int) x, (int) y, width, height, 10, 10);
        g.setColor(new Color(200, 230, 255));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect((int) x, (int) y, width, height, 10, 10);
    }
}

/* =========================================================
 *  BALL
 * ========================================================= */
class Ball {
    double x, y;
    double dx = 0, dy = 0;
    double radius = 8;
    double baseSpeed = 5.5;
    double speedMultiplier = 1.0;

    Ball(double x, double y) {
        this.x = x;
        this.y = y;
    }

    double currentSpeed() {
        return Math.hypot(dx, dy) == 0 ? baseSpeed * speedMultiplier : Math.hypot(dx, dy);
    }

    void setSpeedMultiplier(double m) {
        double curSpeed = Math.hypot(dx, dy);
        if (curSpeed > 0) {
            double factor = (baseSpeed * m) / curSpeed;
            dx *= factor;
            dy *= factor;
        }
        speedMultiplier = m;
    }

    void move() {
        x += dx;
        y += dy;
    }

    Ball copyWithAngleOffset(double angleOffset) {
        Ball nb = new Ball(x, y);
        double speed = Math.hypot(dx, dy);
        if (speed == 0) speed = baseSpeed;
        double curAngle = Math.atan2(dy, dx);
        double newAngle = curAngle + angleOffset;
        nb.dx = speed * Math.cos(newAngle);
        nb.dy = speed * Math.sin(newAngle);
        nb.radius = radius;
        nb.baseSpeed = baseSpeed;
        nb.speedMultiplier = speedMultiplier;
        return nb;
    }

    void draw(Graphics2D g) {
        g.setColor(new Color(255, 240, 180));
        g.fillOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
        g.setColor(new Color(255, 255, 255, 150));
        g.fillOval((int) (x - radius / 2), (int) (y - radius / 2), (int) radius, (int) radius);
    }
}

/* =========================================================
 *  BRICK
 * ========================================================= */
class Brick {
    int x, y, width, height;
    int hp, maxHp;
    boolean alive = true;
    boolean steel;

    Brick(int x, int y, int width, int height, int hp, boolean steel) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.maxHp = hp;
        this.steel = steel;
    }

    Color color() {
        if (steel) return new Color(120, 120, 130);
        return switch (hp) {
            case 3 -> new Color(220, 70, 70);
            case 2 -> new Color(230, 160, 50);
            default -> new Color(80, 190, 110);
        };
    }

    void draw(Graphics2D g) {
        if (!alive) return;
        Color base = color();
        GradientPaint gp = new GradientPaint(x, y, base.brighter(), x, y + height, base.darker());
        g.setPaint(gp);
        g.fillRoundRect(x, y, width, height, 6, 6);
        g.setColor(new Color(255, 255, 255, 60));
        g.drawRoundRect(x, y, width, height, 6, 6);

        if (steel) {
            g.setColor(new Color(60, 60, 70));
            g.drawLine(x + 4, y + height / 2, x + width - 4, y + height / 2);
        }
    }
}

/* =========================================================
 *  POWER-UP
 * ========================================================= */
class PowerUp {
    enum Type { WIDEN, SHRINK, MULTIBALL, SLOW, FAST, EXTRA_LIFE }

    double x, y;
    int size = 20;
    double fallSpeed = 2.4;
    Type type;

    PowerUp(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    Color color() {
        return switch (type) {
            case WIDEN -> new Color(80, 200, 255);
            case SHRINK -> new Color(255, 100, 100);
            case MULTIBALL -> new Color(255, 220, 80);
            case SLOW -> new Color(160, 120, 255);
            case FAST -> new Color(255, 140, 60);
            case EXTRA_LIFE -> new Color(255, 90, 160);
        };
    }

    String label() {
        return switch (type) {
            case WIDEN -> "W";
            case SHRINK -> "S";
            case MULTIBALL -> "M";
            case SLOW -> "↓";
            case FAST -> "↑";
            case EXTRA_LIFE -> "+";
        };
    }

    void draw(Graphics2D g) {
        g.setColor(color());
        g.fillRoundRect((int) x, (int) y, size, size, 8, 8);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        String s = label();
        int tw = fm.stringWidth(s);
        g.drawString(s, (int) (x + size / 2.0 - tw / 2.0), (int) (y + size / 2.0 + 5));
    }
}

/* =========================================================
 *  PARTICLE (small visual burst effect on hits)
 * ========================================================= */
class Particle {
    double x, y, vx, vy;
    int life = 24;
    Color color;

    Particle(double x, double y, double vx, double vy, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
    }

    void update() {
        x += vx;
        y += vy;
        vy += 0.08; // slight gravity
        life--;
    }

    void draw(Graphics2D g) {
        if (life <= 0) return;
        float alpha = Math.max(0f, life / 24f);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
        g.fillOval((int) x - 2, (int) y - 2, 4, 4);
    }
}
