/**
 * Java. Game Battle Ship
 *
 * @author Sergey Iryupin
 * @version 0.2 dated October 17, 2016
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class GameBattleShip extends JFrame {

    final String TITLE_OF_PROGRAM = "Battle Ship";
    final String TITLE_OF_HUMAN_FIELD = "Human Ships";
    final String TITLE_OF_AI_FIELD = "AI Ships";
    final String BTN_NEW_GAME = "New Game";
    final String BTN_SHOW_MY = "Show " + TITLE_OF_HUMAN_FIELD;
    final String BTN_SHOW_ENEMY = "Show " + TITLE_OF_AI_FIELD;
    final String BTN_EXIT_GAME = "Exit game";
    final int START_LOCATION = 200;
    final int WINDOW_SIZE = 450;
    final int WINDOW_DX = 6;
    final int WINDOW_DY = 54;
    final int NUMBER_OF_CELLS = 10;
    final int CELL_SIZE = WINDOW_SIZE / NUMBER_OF_CELLS;
    final int MOUSE_BUTTON_LEFT = 1; // for mouse listener
    final int MOUSE_BUTTON_RIGHT = 3;
    Canvas canvas;
    Random random = new Random();
    Game game = new Game();

    public static void main(String[] args) {
        new GameBattleShip();
    }

    GameBattleShip() {
        setTitle(TITLE_OF_PROGRAM + " : " + TITLE_OF_AI_FIELD);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(START_LOCATION, START_LOCATION, WINDOW_SIZE + WINDOW_DX, WINDOW_SIZE + WINDOW_DY);
        setResizable(false);
        // panel for painting
        canvas = new Canvas();
        canvas.setBackground(Color.white);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x = e.getX()/CELL_SIZE;
                int y = e.getY()/CELL_SIZE;
                if (e.getButton() == MOUSE_BUTTON_LEFT) // left button mouse
                    game.getHumanShot(x, y);
                if (e.getButton() == MOUSE_BUTTON_RIGHT) game.getMark(x, y);
            }
        });
        // panel for buttons
        JPanel bp = new JPanel();
        bp.setLayout(new GridLayout());
        JButton show = new JButton(BTN_SHOW_MY);
        show.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.switchView();
                show.setText(game.getShowMode() ? BTN_SHOW_ENEMY : BTN_SHOW_MY);
                setTitle(TITLE_OF_PROGRAM + " : " + (game.getShowMode() ? TITLE_OF_HUMAN_FIELD : TITLE_OF_AI_FIELD));
                canvas.repaint();
            }
        });        
        JButton init = new JButton(BTN_NEW_GAME);
        init.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game = new Game();
                show.setText(BTN_SHOW_MY);
                setTitle(TITLE_OF_PROGRAM + " : " + TITLE_OF_AI_FIELD);
                canvas.repaint();
            }
        });
        JButton exit = new JButton(BTN_EXIT_GAME);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        bp.add(init);
        bp.add(show);
        bp.add(exit);
        add(BorderLayout.CENTER, canvas);
        add(BorderLayout.SOUTH, bp);
        setVisible(true);
    }

    class Game {
        Ships humanShips, aiShips;
        Shots humanShots, aiShots;
        boolean isHumanVisible;

        Game() {
            humanShips = new Ships(false);
            humanShots = new Shots();
            aiShips = new Ships(true);
            aiShots = new Shots();
            isHumanVisible = false;
        }

        void switchView() { isHumanVisible = !isHumanVisible; }

        boolean getShowMode() { return isHumanVisible; }

        void getMark(int x, int y) {
            if (!isHumanVisible) {
                humanShots.add(x, y, false);
                canvas.repaint();
            }
        }

        void getHumanShot(int x, int y) {
            if (!isHumanVisible) {
                if (aiShips.checkHit(x, y)) { // human hit the target
                    if (!aiShips.checkSurvivors())
                        System.out.println("YOU WON!");
                } else { // human missed - AI will shoot
                    humanShots.add(x, y, true);
                    shootsAI();
                }
                canvas.repaint();
            }
        }

        void shootsAI() { // AI shoots
            int x, y;
            do {
                x = random.nextInt(NUMBER_OF_CELLS);
                y = random.nextInt(NUMBER_OF_CELLS);
            } while (aiShots.hitSamePlace(x, y));
            if (!humanShips.checkHit(x, y)) { // AI missed
                System.out.println(x + ":" + y + " AI missed.");
                aiShots.add(x, y, true);
                return;
            } else { // AI hit the target - AI can shoot again
                System.out.println(x + ":" + y + " AI hit the target.");
                if (!humanShips.checkSurvivors())
                    System.out.println("AI WON!");
                else
                    shootsAI();
            }
        }

        void paint(Graphics g) {
            if (isHumanVisible) {
                aiShots.paint(g);
                humanShips.paint(g);
            } else {
                humanShots.paint(g);
                aiShips.paint(g);
            }
        }
    }

    class Ships {
        ArrayList<Ship> ships = new ArrayList<Ship>();
        final int[] pattern = {4, 3, 3, 3, 2, 2, 2, 1, 1, 1, 1};
        boolean hide;

        Ships(boolean hide) {
            for (int i = 0; i < pattern.length; i++) {
                Ship ship;
                do {
                    int x = random.nextInt(NUMBER_OF_CELLS);
                    int y = random.nextInt(NUMBER_OF_CELLS);
                    int position = random.nextInt(2);
                    ship = new Ship(x, y, pattern[i], position);
                } while (ship.isOutOfField(0, NUMBER_OF_CELLS - 1) || isOverlayOrTouch(ship));
                ships.add(ship);
            }
            this.hide = hide;
        }

        boolean isOverlayOrTouch(Ship ctrlShip) {
            for (Ship ship : ships)
                if (ship.isOverlayOrTouch(ctrlShip)) return true;
            return false;
        }

        boolean checkHit(int x, int y) {
            for (Ship ship : ships) if (ship.checkHit(x, y)) return true;
            return false;
        }

        boolean checkSurvivors() {
            for (Ship ship : ships) if (ship.isAlive()) return true;
            return false;
        }

        void paint(Graphics g) {
            for (Ship ship : ships) ship.paint(g, hide);
        }
    }

    class Ship {
        ArrayList<Cell> cells = new ArrayList<Cell>();

        Ship(int x, int y, int length, int position) {
            for (int i = 0; i < length; i++) {
                cells.add(new Cell(x + i * ((position == 1)?0:1), y + i * ((position == 1)?1:0)));
            }
        }

        boolean isOutOfField(int bottom, int top) { // is ship outside the boundary of the field?
            for (Cell cell : cells)
                if (cell.getX() < bottom || cell.getX() > top || cell.getY() < bottom || cell.getY() > top) return true;
            return false;
        }

        boolean isOverlayOrTouch(Ship ctrlShip) { // is ship overlay or touch other ships
            for (Cell cell : cells)
                if (ctrlShip.isOverlayOrTouchCell(cell)) return true;
            return false;
        }

        boolean isOverlayOrTouchCell(Cell ctrlCell) {
            for (Cell cell : cells)
                for (int dx = -1; dx < 2; dx++)
                    for (int dy = -1; dy < 2; dy++)
                        if (ctrlCell.getX() == cell.getX() + dx && ctrlCell.getY() == cell.getY() + dy) return true;
            return false;
        }

        boolean checkHit(int x, int y) {
            for (Cell cell : cells) if (cell.checkHit(x, y)) return true;
            return false;
        }

        boolean isAlive() {
            for (Cell cell : cells) if (cell.isAlive()) return true;
            return false;
        }

        void paint(Graphics g, boolean hide) {
            for (Cell cell : cells) cell.paint(g, hide);
        }
    }

    class Cell {
        int x, y;
        Color color = Color.gray; // default color;

        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int getX() { return x; }
        int getY() { return y; }

        boolean checkHit(int x, int y) {
            if (this.x == x && this.y == y) {
                color = Color.red; // change color if hit
                return true;
            }
            return false;
        }

        boolean isAlive() {
            return color == Color.gray; // judged by color
        }

        void paint(Graphics g, boolean hide) {
            if (!hide || (hide && color == Color.red)) {
                g.setColor(color);
                g.fill3DRect(x*CELL_SIZE + 1, y*CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2, true);
            }
        }
    }

    class Shots {
        ArrayList<Shot> shots = new ArrayList<Shot>();

        void add(int x, int y, boolean shot) {
            shots.add(new Shot(x, y, shot));
        }

        boolean hitSamePlace(int x, int y) {
            for (Shot shot : shots) if (shot.getX() == x && shot.getY() == y) return true;
            return false;
        }

        void paint(Graphics g) {
            for (Shot shot : shots) shot.paint(g);
        }
    }

    class Shot {
        int x, y;
        boolean shot;

        Shot(int x, int y, boolean shot) {
            this.x = x;
            this.y = y;
            this.shot = shot;
        }

        int getX() { return x; }
        int getY() { return y; }

        void paint(Graphics g) {
            g.setColor(Color.black);
            if (shot) g.fillRect(x*CELL_SIZE + CELL_SIZE/2 - 3, y*CELL_SIZE + CELL_SIZE/2 - 3, 8, 8);
            else g.drawRect(x*CELL_SIZE + CELL_SIZE/2 - 3, y*CELL_SIZE + CELL_SIZE/2 - 3, 8, 8);
        }
    }

    class Canvas extends JPanel { // for painting
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (int i = 0; i < NUMBER_OF_CELLS - 1; i++) {
                g.drawLine(0, (i + 1)*CELL_SIZE, WINDOW_SIZE, (i + 1)*CELL_SIZE);
                g.drawLine((i + 1)*CELL_SIZE, 0, (i + 1)*CELL_SIZE, WINDOW_SIZE);
            }
            game.paint(g);
        }
    }
}