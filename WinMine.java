/*
David Kalish
CSCI-E10b
final project
Minesweeper
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MineButton extends JButton {
    int adj_mines;
    boolean isMine;

    MineButton(int myAdjMines, boolean myIsMine) {
        super("");
        this.adj_mines = myAdjMines;
        this.isMine = myIsMine;
    }

    MineButton(String s, int myAdjMines, boolean myIsMine) {
        super(s);
        this.adj_mines = myAdjMines;
        this.isMine = myIsMine;
    }
}

class WinMine {
    public static JTextField MINES_LEFT = new JTextField(2);
    public static JTextField TIMER = new JTextField(2);

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
        JPanel minesRemainingDisplay = makeDisplay(TIMER);

        // put upper panel things into a panel
        JPanel upperPanel = new JPanel();
        BorderLayout bordLay = new BorderLayout(0,0);
        upperPanel.setLayout(bordLay);
        upperPanel.add(timerDisplay, bordLay.LINE_START);
        upperPanel.add(minesRemainingDisplay, bordLay.LINE_END);

        // make the game board grid
        JPanel buttonPanel = makeButtonPanel(wmWindow, 8, 8);

        // build the window
        wmWindow.add(upperPanel, BorderLayout.PAGE_START);
        wmWindow.add(buttonPanel, BorderLayout.CENTER);

        // pack and display window
        wmWindow.pack();
        wmWindow.setVisible(true);
    }

    private static JPanel makeButtonPanel(JFrame window, int rows, int cols) {
        JPanel butPan = new JPanel(new GridLayout(rows, cols));

        MineButton[][] mineGrid = new MineButton[rows][cols];

        // make the buttons
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                mineGrid[x][y] = new MineButton(Integer.toString((rows * x) + y), 1, true);
                butPan.add(mineGrid[x][y]);
                mineGrid[x][y].setPreferredSize(new Dimension(50, 50));
            }
        }
        return butPan;
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