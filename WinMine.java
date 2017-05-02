/*
David Kalish
CSCI-E10b
final project
Minesweeper
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.concurrent.ThreadLocalRandom;

enum Face {
    OK,
    NERVOUS,
    DEAD,
    WON
}

class MineButton extends JButton {
    int adj_mines;
    boolean isMine;

    MineButton(String s, int myAdjMines, boolean myIsMine) {
        super(s);
        this.adj_mines = myAdjMines;
        this.isMine = myIsMine;
    }

    MineButton(int myAdjMines, boolean myIsMine) {
        this("", myAdjMines, myIsMine);
    }

    public void clickedStyle() {
        super.setContentAreaFilled(false);
        super.setBorderPainted(true);
        super.setOpaque(false);
    }

    public boolean hasMine() {
        return this.isMine;
    }
}

class FaceButton extends JButton {
    Face face;

    FaceButton(Face myFace) {
        this.face = myFace;
    }
    FaceButton() {
        this.face = Face.OK;
    }

    void showFace(Face myface) {
        this.face = myface;
        ImageIcon faceImg;
        switch (face) {
            case OK:
                faceImg = new ImageIcon(
                    getClass().getResource("img/face-ok.png"));
                break;
            case NERVOUS:
                faceImg = new ImageIcon(
                    getClass().getResource("img/face-nervous.png"));
                break;
            case WON:
                faceImg = new ImageIcon(
                    getClass().getResource("img/face-won.png"));
                break;
            case DEAD:
                faceImg = new ImageIcon(
                    getClass().getResource("img/face-dead.png"));
                break;
            default:
                faceImg = new ImageIcon(
                    getClass().getResource("img/face-ok.png"));
                break;
        }
        // System.out.println(faceImg);
        // System.out.println(faceImg.getImageLoadStatus());
        this.setIcon(faceImg);

    }
}

class WinMine {
    public static JTextField MINES_LEFT = new JTextField(2);
    public static JTextField TIMER = new JTextField(2);
    public static FaceButton FACEBUTTON;
    public static JPanel MINESREMAININGDISPLAY;

    public static void main(String[] args) {
        doLayout();
        System.out.println("main()");
    }

    public WinMine() {
        doLayout();
        System.out.println("WinMine");
    }

    private static void doLayout() {
        // set up JFrame
        JFrame wmWindow = new JFrame();
        wmWindow.setResizable(false);
        wmWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // make the layout.
        // make the upper panels
        JPanel timerDisplay = makeDisplay(MINES_LEFT);
        makeFaceButton();
        JPanel minesRemainingDisplay = makeDisplay(TIMER);

        // put upper panel things into a panel
        JPanel upperPanel = new JPanel();
        BorderLayout bordLay = new BorderLayout(0,0);
        upperPanel.setLayout(bordLay);
        upperPanel.add(timerDisplay, bordLay.LINE_START);
        upperPanel.add(FACEBUTTON, bordLay.CENTER);
        upperPanel.add(minesRemainingDisplay, bordLay.LINE_END);

        // make the game board grid
        JPanel buttonPanel = makeButtonGrid(wmWindow, 8, 8, 64);

        // build the window
        wmWindow.add(upperPanel, BorderLayout.PAGE_START);
        wmWindow.add(buttonPanel, BorderLayout.CENTER);

        // pack and display window
        wmWindow.pack();
        wmWindow.setVisible(true);
    }

    private static void makeFaceButton() {
        FACEBUTTON = new FaceButton();
        FACEBUTTON.setPreferredSize(new Dimension(34, 34));
    }

    private static JPanel makeButtonGrid(JFrame window, int rows, int cols,
                                         int numMines) {
        JPanel butPan = new JPanel(new GridLayout(rows, cols));

        MineButton[][] mineGrid = new MineButton[rows][cols];

        // create and distribute mine tiles
        int[] mineList = ThreadLocalRandom.current().ints(0, rows * cols).distinct().limit(numMines);
        for (int asdf : mineList) {
            System.out.println(asdf);
        }
        // make the buttons
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                mineGrid[x][y] = new MineButton(1, true);
                // mineGrid[x][y] = new MineButton(Integer.toString((rows * x) + y), 1, true);
                butPan.add(mineGrid[x][y]);
                mineGrid[x][y].setPreferredSize(new Dimension(50, 50));

                // set up mouse listeners for button clicks
                mineGrid[x][y].addMouseListener(new MouseAdapter(){
                    public void mousePressed(MouseEvent me) {
                        mousePressedHandler(me);
                    }
                    public void mouseReleased(MouseEvent me) {
                        mouseReleasedHandler(me);
                    }
                });
            }
        }
        return butPan;
    }

    private static void mousePressedHandler(MouseEvent me) {
        FACEBUTTON.showFace(Face.NERVOUS);
    }
    private static void mouseReleasedHandler(MouseEvent me) {
        MineButton thisButton = (MineButton)me.getSource();
        if (thisButton.hasMine()) {
            FACEBUTTON.showFace(Face.DEAD);
        } else {
            FACEBUTTON.showFace(Face.OK);
        }
    }


    private static JPanel makeDisplay(JTextField display) {
        JPanel displayPanel = new JPanel();
        // display is the jtextfield
        display.setEditable(false);
        display.setText("123");

        display.setBackground(Color.BLACK);
        Font font = new Font("Courier New", Font.BOLD, 36);
        display.setFont(font);
        display.setForeground(Color.RED);
        display.setHorizontalAlignment(SwingConstants.RIGHT);

        displayPanel.add(display);

        return displayPanel;
    }
}