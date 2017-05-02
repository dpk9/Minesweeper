/*
David Kalish
CSCI-E10b
final project
Minesweeper
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

// different looks for the face icon
enum Face {OK,
           NERVOUS,
           DEAD,
           WON
          }

// for keeping the mouseReleased from firing if the mouse has been moved off
// the tile before release
enum MOUSE_STATUS {PRESSED,
                   RELEASED,
                   EXITED
                  }

class MineButton extends JButton {
    // Class for the mine button grid, extending JButton. 
    private int adjMines;
    private boolean isMine;
    private boolean flagged;
    private boolean revealed;
    private int row;
    private int col;

    MineButton(String s, boolean myIsMine, int myRow, int myCol) {
        // MineButton constructor
        super(s);
        super.setRolloverEnabled(false);
        super.setFocusPainted(false);
        this.isMine = myIsMine;
        this.flagged = false;
        this.revealed = false;
        this.row = myRow;
        this.col = myCol;
    }

    MineButton(boolean myIsMine, int myRow, int myCol) {
        // MineButton constructor (no string first arg)
        this("", myIsMine, myRow, myCol);
    }

    public void decorateClicked() {
        // Decorate the tile when it has been clicked (mouseReleased).
        // Button look goes away
        super.setContentAreaFilled(false);
        super.setBorderPainted(true);
        super.setOpaque(false);
        this.revealed = true;

        // if the tile has a mine, show the mine icon
        ImageIcon tileIcon;
        if (hasMine()) {
            tileIcon = new ImageIcon(getClass().getResource("img/mine.png"));
            this.setIcon(tileIcon);
        } else {
        // if no mine, display number of adjacent mines
            decorateAdjMineNum();
        }
    }

    public void setAdjMines(int n) {
        // set this tile's number of adjacent mines
        this.adjMines = n;
    }

    public int[] getPosition() {
        // return the {row, column} position of this mine
        int[] position = {this.row, this.col};
        return position;
    }

    public boolean hasMine() {
        // return whether or not this tile has a mine 
        return this.isMine;
    }

    public boolean getRevealed() {
        // return whether or not this tile has already been revealed
        return this.revealed;
    }

    public void toggleFlag() {
        // toggle whether this mine has a flag or not
        if (this.flagged) this.flagged = false;
        else this.flagged = true;

        // display/remove the flag icon
        this.decorateFlag();
    }

    public boolean getFlagged() {
        // return whether or not this tile is currently flagged
        return this.flagged;
    }

    private void decorateFlag() {
        // show a flag if this.flagged == true, remove it if its == false
        if (getFlagged()) {
            ImageIcon flagIcon = new ImageIcon(
                getClass().getResource("img/flag.png"));
            this.setIcon(flagIcon);
        } else {
            this.setIcon(null);
        }

        // keep the button looking unpressed and normal
        super.setModel(new DefaultButtonModel() {
            @Override
            public boolean isArmed()
            {
                return false;
            }

            @Override
            public boolean isPressed()
            {
                return false;
            }
        });
    }

    private void decorateAdjMineNum() {
        // show the number of mines adjacent to this tile. leave blank if no
        // adjacent mines.
        ImageIcon numIcon;
        int n = this.adjMines;
        String imgPath;

        // get number image for # of adjacent mines
        switch (n) {
            case 1:
                imgPath = "img/num-1.png";
                break;
            case 2:
                imgPath = "img/num-2.png";
                break;
            case 3:
                imgPath = "img/num-3.png";
                break;
            case 4:
                imgPath = "img/num-4.png";
                break;
            case 5:
                imgPath = "img/num-5.png";
                break;
            case 6:
                imgPath = "img/num-6.png";
                break;
            case 7:
                imgPath = "img/num-7.png";
                break;
            case 8:
                imgPath = "img/num-8.png";
                break;
            default:
                imgPath = null;
                break;
        }
        // if path defined, show number icon
        if (imgPath != null) {
            numIcon = new ImageIcon(getClass().getResource(imgPath));
            this.setIcon(numIcon);
        // if path null, leave it blank (no adjacent mines)
        } else this.setIcon(null);
    }
}

class FaceButton extends JButton {
    // Class for the fac button at the top of the window.
    Face face;

    FaceButton(Face myFace) {
        // FaceButton constructor
        this.face = myFace;
        super.setFocusPainted(false);
        showFace(this.face);
    }
    FaceButton() {
        // FaceButton constructor
        this(Face.OK);
    }

    void showFace(Face myface) {
        // Show one of the four possible faces, depending on the enum Face arg
        this.face = myface;
        ImageIcon faceImg;
        String imgPath;

        switch (face) {
            case OK:
                imgPath = "img/face-ok.png";
                break;
            case NERVOUS:
                imgPath = "img/face-nervous.png";
                break;
            case WON:
                imgPath = "img/face-won.png";
                break;
            case DEAD:
                imgPath = "img/face-dead.png";
                break;
            default:
                imgPath = "img/face-ok.png";
                break;
        }
        faceImg = new ImageIcon(getClass().getResource(imgPath));
        // System.out.println(faceImg);
        // System.out.println(faceImg.getImageLoadStatus());
        this.setIcon(faceImg);
    }
}

class WinMine {
    // Main class for the game

    // game window
    public static JFrame WM_WINDOW;
    // text field for remaining mines to be flagged
    public static JTextField MINES_LEFT = new JTextField(2);
    // panel for MINES_LEFT
    public static JPanel MINESREMAININGDISPLAY;
    // elapsed seconds timer display
    public static JTextField TIMER = new JTextField(2);
    // face button
    public static FaceButton FACEBUTTON;
    // the main game grid
    public static MineButton[][] MINE_GRID;

    // main function
    public static void main(String[] args) {
        doLayout();
        System.out.println("main()");
    }

    // public WinMine() {
    //     doLayout();
    //     System.out.println("WinMine");
    // }

    // set up the whole game board
    private static void doLayout() {
        // set up JFrame window
        WM_WINDOW = new JFrame();
        WM_WINDOW.setResizable(false);
        WM_WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        JPanel buttonPanel = makeButtonGrid(WM_WINDOW, 8, 8, 10);

        // build the window
        WM_WINDOW.add(upperPanel, BorderLayout.PAGE_START);
        WM_WINDOW.add(buttonPanel, BorderLayout.CENTER);

        // pack and display window
        WM_WINDOW.pack();
        WM_WINDOW.setVisible(true);
    }

    private static void makeFaceButton() {
        FACEBUTTON = new FaceButton();
        FACEBUTTON.setPreferredSize(new Dimension(34, 34));
        // make an action listener to start new game when clicked
        FACEBUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newGame();
            }
        });
    }

    private static void newGame() {
        // Start a new game
        WM_WINDOW.dispose();
        doLayout();
    }

    private static ArrayList<Integer> mineLocs(int numMines, int numButtons) {
        // determine where all the mines will be
        ArrayList<Integer> mineLocations = new ArrayList<Integer>();
        Random rnd = new Random();
        int randInt;
        for (int x = 0; x < numMines; x++) {
            do {
                randInt = rnd.nextInt(numButtons);
            // make sure there's only 1 occurence of each number
            } while (Collections.frequency(mineLocations, randInt) > 0);
            mineLocations.add(randInt);
        }
        // sort the mine locations (not necessary but makes cheating easier)
        Collections.sort(mineLocations);
        // System.out.println(mineLocations);
        return mineLocations;
    }

    private static JPanel makeButtonGrid(JFrame window, int rows, int cols,
                                         int numMines) {
        JPanel butPan = new JPanel(new GridLayout(rows, cols));
        MINE_GRID = new MineButton[rows][cols];
        boolean isMine;
        int adjMines;

        // create and distribute mine tiles
        ArrayList<Integer> mineList = mineLocs(numMines, rows * cols);
        // cheat sheet:
        System.out.println("*****cheats!*****\n" + mineList);
        // make the buttons
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                // see if this tile should be a mine
                if (mineList.contains((rows * x) + y)) isMine = true;
                else isMine = false;
                // create the mine buttons
                MINE_GRID[x][y] = new MineButton(isMine, x, y);
                butPan.add(MINE_GRID[x][y]);
                MINE_GRID[x][y].setPreferredSize(new Dimension(25, 25));

                // set up mouse listeners for button clicks
                MINE_GRID[x][y].addMouseListener(new MouseAdapter(){
                    MOUSE_STATUS mouseStatus;

                    public void mousePressed(MouseEvent me) {
                        if (SwingUtilities.isLeftMouseButton(me)) {
                            mouseStatus = MOUSE_STATUS.PRESSED;
                            mouseLeftPressedHandler(me);
                        }
                        else if (SwingUtilities.isRightMouseButton(me)) {
                            mouseRightPressedHandler(me);
                        }
                    }
                    public void mouseExited(MouseEvent me) {
                        if (SwingUtilities.isLeftMouseButton(me)) {
                            mouseStatus = MOUSE_STATUS.EXITED;
                        }
                    }
                    public void mouseReleased(MouseEvent me) {
                        if (SwingUtilities.isLeftMouseButton(me)) {
                            if (mouseStatus != MOUSE_STATUS.EXITED) {
                                mouseReleasedHandler(me);
                            } else {
                                mouseReleasedFaceHandler(me);
                            }
                        }
                    }
                });
            }
        }

        // find all the adjacent mine numbers
        for (MineButton[] mineRow : MINE_GRID) {
            for (MineButton thisMine : mineRow) {
                // each minebutton:
                int[] pos = thisMine.getPosition();
                adjMines = 0;

                // check the ring around the minebutton
                // don't check positions below 0
                int loRow = pos[0] - 1;
                if (loRow < 0) loRow = 0;
                int loCol = pos[1] - 1;
                if (loCol < 0) loCol = 0;
                // don't check positions above upper limit
                int hiRow = pos[0] + 1;
                if (hiRow >= rows) hiRow = rows - 1;
                int hiCol = pos[1] + 1;
                if (hiCol >= cols) hiCol = cols - 1;
                // go around the ring
                for (int i = loRow; i <= hiRow; i++) {
                    for (int j = loCol; j <= hiCol; j++) {
                        // increase adjMines if mine is present
                        if (MINE_GRID[i][j].hasMine()) adjMines++;
                        // don't have to exclude thisButton's mine because if
                        // it's a mine, it won't display a number anyway. And
                        // if it's not a mine, it won't affect the adjMines
                        // count either.
                    }
                }
                thisMine.setAdjMines(adjMines);
            }
        }
        return butPan;
    }

    private static void mouseLeftPressedHandler(MouseEvent me) {
        MineButton thisButton = (MineButton)me.getSource();
        if (!(thisButton.getFlagged() || thisButton.getRevealed())) {
            FACEBUTTON.showFace(Face.NERVOUS);
        }
    }

    private static void mouseRightPressedHandler(MouseEvent me) {
        MineButton thisButton = (MineButton)me.getSource();
        thisButton.toggleFlag();

    }

    private static void mouseReleasedFaceHandler(MouseEvent me) {
        FACEBUTTON.showFace(Face.OK);
    }

    private static void mouseReleasedHandler(MouseEvent me) {
        MineButton thisButton = (MineButton)me.getSource();
        if (thisButton.getFlagged()) {
            return;
        }
        else {
            if (thisButton.hasMine()) {
                FACEBUTTON.showFace(Face.DEAD);
                gameOver();
            } else {
                FACEBUTTON.showFace(Face.OK);
            }
            thisButton.decorateClicked();
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

    private static void gameOver() {
        // This section modified from Java Docs dialog demo from:
        // https://docs.oracle.com/javase/tutorial/uiswing/examples/components/DialogDemoProject/src/components/DialogDemo.java

        String[] options = {"Retry", "Quit"};
        int n = JOptionPane.showOptionDialog(WM_WINDOW,
                        "Game over. Try again?\n",
                        "Game Over",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[0]);
        if (n == JOptionPane.YES_OPTION) {
            newGame();
        } else if (n == JOptionPane.NO_OPTION) {
            WM_WINDOW.dispose();
        } else {
            System.out.println("Please tell me what you want!");
        }
    }
}