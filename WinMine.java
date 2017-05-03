/*
David Kalish
CSCI-E10b
final project
Minesweeper
*/

/*
 *  TODO:   make a game menu
 *
 *  TODO:   Easy Medium and Hard game modes
 *
 *  TODO:   time elapsed display
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.swing.border.*;

// different looks for the face icon
enum Face {OK,
           NERVOUS,
           DEAD,
           WON}

// for keeping the mouseReleased from firing if the mouse has been moved off
// the tile before release
enum MouseStatus {PRESSED,
                  LR_PRESSED,
                  RELEASED,
                  LR_RELEASED,
                  EXITED}

// for breaking recursion in expandSafeZone. Otherwise, the next game loses
// immediately after pressing Retry on game over.
enum Recurs {GO,
             STOP}

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
        // set unclicked border
        Border raisedBorder = new BevelBorder(BevelBorder.RAISED);
        this.setBorder(raisedBorder);
    }

    MineButton(boolean myIsMine, int myRow, int myCol) {
        // MineButton constructor (no string first arg)
        this("", myIsMine, myRow, myCol);
    }

    public void decoratePressed() {
        // Decorate the tile to look pressed, but remain blank
        super.getModel().setPressed(true);
    }

    public void decorateUnpressed() {
        // Decorate the tile to look pressed, but remain blank
        super.getModel().setPressed(false);
    }

    public void decorateClicked() {
        // Decorate the tile when it has been clicked (mouseReleased).
        // Button look goes away
        super.setContentAreaFilled(false);
        super.setBorderPainted(true);
        super.setOpaque(false);
        this.revealed = true;
        this.flagged = false;
        // set clicked border
        Border lineBorder = new LineBorder(Color.LIGHT_GRAY, 1);
        this.setBorder(lineBorder);

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

    public int getAdjMines() {
        // return this tile's number of adjacent mines
        return this.adjMines;
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
        // only toggle if tile is not yet revealed
        if (!this.getRevealed()) {
            // toggle whether this mine has a flag or not
            if (this.flagged) this.flagged = false;
            else this.flagged = true;

            // display/remove the flag icon
            this.decorateFlag();
        } else {
            // in case a revealed tile is right clicked, always flagged = false
            this.flagged = false;
        }
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
        // from StackOverflow
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
        this.setIcon(faceImg);
    }
}

class WinMine {
    // Main class for the game

    // game window
    public static JFrame WM_WINDOW;
    // text field for remaining mines to be flagged
    public static JTextField MINES_LEFT_FIELD = new JTextField(2);
    // elapsed seconds timer display
    public static JTextField TIMER_FIELD = new JTextField(2);
    // face button
    public static FaceButton FACEBUTTON;
    // the main game grid
    public static MineButton[][] MINE_GRID;
    // rows for game board
    public static int ROWS;
    // cols for game board
    public static int COLS;
    // # of mines to generate
    public static int MINES;
    // mouse status
    public static MouseStatus MOUSE_STATUS;
    // recursion breaker init
    public static Recurs RECURSION = Recurs.GO;

    // main function
    public static void main(String[] args) {
        doLayout();
        System.out.println("main()");
    }

    // set up the whole game board
    private static void doLayout() {
        ROWS = 10;
        COLS = 10;
        MINES = 10;
        // set up JFrame window
        WM_WINDOW = new JFrame();
        WM_WINDOW.setResizable(false);
        WM_WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // make the layout.
        // make the upper panels
        JPanel timerDisplay = makeDisplay(MINES_LEFT_FIELD);
        updateMinesLeft(MINES);
        makeFaceButton();
        JPanel minesRemainingDisplay = makeDisplay(TIMER_FIELD);

        // put upper panel things into a panel
        JPanel upperPanel = new JPanel();
        BorderLayout bordLay = new BorderLayout(0,0);
        upperPanel.setLayout(bordLay);
        upperPanel.add(timerDisplay, bordLay.LINE_START);
        upperPanel.add(FACEBUTTON, bordLay.CENTER);
        upperPanel.add(minesRemainingDisplay, bordLay.LINE_END);

        // make the game board grid
        JPanel buttonPanel = makeButtonGrid(WM_WINDOW, ROWS, COLS, MINES);

        // build the window
        WM_WINDOW.add(upperPanel, BorderLayout.PAGE_START);
        WM_WINDOW.add(buttonPanel, BorderLayout.CENTER);

        // pack and display window
        WM_WINDOW.pack();
        WM_WINDOW.setVisible(true);
    }

    // make the face button and actionListener
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

    // start a new game
    private static void newGame() {
        // Start a new game by closing WM_WINDOW and opening a new one
        WM_WINDOW.dispose();
        doLayout();
    }

    // Randomly distribute the desired number of mines in a list
    private static ArrayList<Integer> mineLocs(int numMines, int numButtons) {
        // determine where all the mines will be
        ArrayList<Integer> mineLocations = new ArrayList<Integer>();
        // make a random int generator
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
        return mineLocations;
    }

    // Build the grid of buttons that make up the main game play area
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

                    public void mousePressed(MouseEvent me) {
                        // when left mouse is pressed. update mouse status to
                        // PRESSED

                        // L + R pressed
                        if (SwingUtilities.isLeftMouseButton(me)
                                 && SwingUtilities.isRightMouseButton(me)) {
                            MOUSE_STATUS = MouseStatus.LR_PRESSED;
                            mouseLRPressedHandler(me);
                        }
                        // RIGHT pressed
                        else if (SwingUtilities.isRightMouseButton(me)
                                 && MOUSE_STATUS != MouseStatus.LR_PRESSED) {
                            mouseRightPressedHandler(me);
                        }
                        // LEFT pressed
                        else if (SwingUtilities.isLeftMouseButton(me)
                                 && MOUSE_STATUS != MouseStatus.LR_PRESSED) {
                            MOUSE_STATUS = MouseStatus.PRESSED;
                            mouseLeftPressedHandler(me);
                        }
                    }
                    public void mouseExited(MouseEvent me) {
                        // when mouse leave the button before releasing, update
                        // mouse status to EXITED to be able to cancel the
                        // mouse released action from firing
                        MOUSE_STATUS = MouseStatus.EXITED;
                    }


                    public void mouseReleased(MouseEvent me) {
                        // fire if the mouse has not exited the button
                        if (MOUSE_STATUS != MouseStatus.EXITED) {
                            // don't fire if mouse has EXITED the button,
                            // but make sure the face returns to normal

                            // L + R release
                            if (SwingUtilities.isLeftMouseButton(me)
                                    && SwingUtilities.isRightMouseButton(me)) {
                                MOUSE_STATUS = MouseStatus.LR_RELEASED;
                                mouseLRReleasedHandler(me);
                            // Left release
                            } else if (SwingUtilities.isLeftMouseButton(me)
                                       && MOUSE_STATUS != MouseStatus.LR_PRESSED) {
                                MOUSE_STATUS = MouseStatus.RELEASED;
                                mouseLeftReleasedHandler(me);
                            // Right release
                            }
                        // let the face return to OK
                        } else mouseLeftReleasedFaceHandler(me);
                    }
                });
            }
        }

        // find all the adjacent mine numbers
        for (MineButton thisMine : mineGrid2D()) {
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
            // set the button's adjMines
            thisMine.setAdjMines(adjMines);
        }
        // return the panel containing the mine grid
        return butPan;
    }

    // left press makes the face nervous :o
    private static void mouseLeftPressedHandler(MouseEvent me) {
        RECURSION = Recurs.GO;
        MineButton thisButton = (MineButton)me.getSource();
        if (!(thisButton.getFlagged() || thisButton.getRevealed())) {
            FACEBUTTON.showFace(Face.NERVOUS);
        }
    }

    // right press toggles flag on unrevealed tile
    private static void mouseRightPressedHandler(MouseEvent me) {
        MineButton thisButton = (MineButton)me.getSource();
        thisButton.toggleFlag();

        System.out.println(getAllFlags());
        updateMinesLeft(MINES - getAllFlags());
        // see if you won
        checkWinCondition();
    }

    // LR press does something
    private static void mouseLRPressedHandler(MouseEvent me) {
        // make the surrounding mines appear pressed
        MineButton thisButton = (MineButton)me.getSource();
        for (MineButton checkButton : threeByThree(thisButton)) {
            checkButton.decoratePressed();
        }
    }

    // LR press does something
    private static void mouseLRReleasedHandler(MouseEvent me) {
        MineButton thisButton = (MineButton)me.getSource();
        int adjFlags = getAdjFlags(thisButton);
        int adjMines = thisButton.getAdjMines();
        for (MineButton checkButton : threeByThree(thisButton)) {
            checkButton.decorateUnpressed();
            if (adjFlags == adjMines && thisButton.getRevealed()) {
                if (! (checkButton.getRevealed()
                        || checkButton.getFlagged()) ) {
                    expandSafeZone(checkButton);
                }
            }
        }
        // see if you won
        checkWinCondition();
    }

    // return face to :) on exited mouse release
    private static void mouseLeftReleasedFaceHandler(MouseEvent me) {
        FACEBUTTON.showFace(Face.OK);
    }

    // reveal the tile's contents on mouse release
    private static void mouseLeftReleasedHandler(MouseEvent me) {
        MineButton thisButton = (MineButton)me.getSource();
        // do nothing if tile is flagged
        if (thisButton.getFlagged()) {
            return;
        }
        else {
            // dead face if tile had a mine
            if (thisButton.hasMine()) {
                gameOver();
            // ok face if tile is safe
            } else {
                FACEBUTTON.showFace(Face.OK);
                expandSafeZone(thisButton);
            }
        }
        // see if you won
        checkWinCondition();
    }

    private static void decorateAllMines() {
        for (MineButton thisButton : mineGrid2D()) {
            if (thisButton.hasMine() && !thisButton.getFlagged()) {
                thisButton.decorateClicked();
            }
        }
    }

    private static int getAdjFlags(MineButton thisButton) {
        int flagCounter = 0;

        for (MineButton checkButton : threeByThree(thisButton)) {
            if (checkButton.getFlagged()) flagCounter++;
        }
        return flagCounter;
    }

    private static MineButton[] mineGrid2D() {
        int counter = 0;
        MineButton[] allMines = new MineButton[ROWS * COLS];
        for (MineButton[] row : MINE_GRID) {
            for (MineButton thisButton : row) {
                allMines[counter] = thisButton;
                counter++;
            }
        }
        return allMines;
    }

    private static int getAllFlags() {
        int flagCounter = 0;
        for (MineButton thisButton : mineGrid2D()) {
            if (thisButton.getFlagged()) {
                flagCounter++;
            }
        }
        return flagCounter;
    }

    private static boolean checkWinCondition() {
        // win condition: all non-mine tiles are revealed
        for (MineButton thisButton : mineGrid2D()) {
            if (!thisButton.hasMine() && !thisButton.getRevealed()) {
                return false;
            }
        }
        gameWon();
        return true;
    }

    private static void expandSafeZone(MineButton thisButton) {
        if (thisButton.hasMine()) {
            RECURSION = Recurs.STOP;
            System.out.println("Found mine");
            gameOver();
            return;
        } else if (RECURSION == Recurs.GO) {
            // decorate the tile with mine or number
            thisButton.decorateClicked();

            // if this button has no mines and no adjacent mines (reveals a
            // blank tile), expand the safe area.
            if (thisButton.getAdjMines() == 0) {
                // NOTE: for some reason using threeByThree() doesn't work here
                int[] pos = thisButton.getPosition();
                // might find a mine coming from LR click
                // check the ring around the minebutton
                // don't check positions below 0
                int loRow = pos[0] - 1;
                if (loRow < 0) loRow = 0;
                int loCol = pos[1] - 1;
                if (loCol < 0) loCol = 0;
                // don't check positions above upper limit
                int hiRow = pos[0] + 1;
                if (hiRow >= ROWS) hiRow = ROWS - 1;
                int hiCol = pos[1] + 1;
                if (hiCol >= COLS) hiCol = COLS - 1;
                // go around the ring
                for (int i = loRow; i <= hiRow; i++) {
                    for (int j = loCol; j <= hiCol; j++) {
                        MineButton anotherButton = MINE_GRID[i][j];
                        // recursively click surrounding empty tiles to expand safe
                        // area.
                        if (! (anotherButton.getRevealed()
                                || anotherButton.getFlagged()) ) {
                            expandSafeZone(anotherButton);
                        }
                        // don't have to check for mine because blank tiles don't
                        // border these.
                    }
                }
            }
        } else if (RECURSION == Recurs.STOP) {
            return;
        }
    }

    private static MineButton[] threeByThree(MineButton thisButton) {
        // return the buttons contained in the 3x3 grid surrounding thisButton

        int counter = 0;
        int[] pos = thisButton.getPosition();
        // check the ring around the Minebutton
        // don't check positions below 0
        int loRow = pos[0] - 1;
        if (loRow < 0) loRow = 0;
        int loCol = pos[1] - 1;
        if (loCol < 0) loCol = 0;
        // don't check positions above upper limit
        int hiRow = pos[0] + 1;
        if (hiRow >= ROWS) hiRow = ROWS - 1;
        int hiCol = pos[1] + 1;
        if (hiCol >= COLS) hiCol = COLS - 1;

        // instantiate the buttonlist once the size has been determined
        int rowSize = hiRow - loRow + 1;
        int colSize = hiCol - loCol + 1;
        MineButton[] buttonList = new MineButton[(rowSize * colSize)];
        // go around the ring
        for (int i = loRow; i <= hiRow; i++) {
            for (int j = loCol; j <= hiCol; j++) {
                buttonList[counter] = MINE_GRID[i][j];
                counter++;
            }
        }

        return buttonList;
    }

    // make a textfield display panel of red text on black
    private static JPanel makeDisplay(JTextField displayField) {
        JPanel displayPanel = new JPanel();
        // display is the jtextfield
        displayField.setEditable(false);

        displayField.setBackground(Color.BLACK);
        Font font = new Font("Courier New", Font.BOLD, 36);
        displayField.setFont(font);
        displayField.setForeground(Color.RED);
        displayField.setHorizontalAlignment(SwingConstants.RIGHT);

        displayPanel.add(displayField);

        return displayPanel;
    }

    private static void updateMinesLeft(int minesLeft) {
        MINES_LEFT_FIELD.setText(Integer.toString(minesLeft));
    }

    // game won,  JOptionPane popup
    private static void gameWon() {
        RECURSION = Recurs.STOP;
        FACEBUTTON.showFace(Face.WON);

        Icon winface = new ImageIcon(WinMine.class.getResource("img/face-won.png"));
        // This section modified from Java Docs dialog demo from:
        // https://docs.oracle.com/javase/tutorial/uiswing/examples/components/DialogDemoProject/src/components/DialogDemo.java

        String[] options = {"Play Again", "Quit"};
        int n = JOptionPane.showOptionDialog(WM_WINDOW,
                        "You won! Play again or quit?\n",
                        "You Won",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        winface,
                        options,
                        options[0]);
        if (n == JOptionPane.YES_OPTION) {
            newGame();
        } else if (n == JOptionPane.NO_OPTION) {
            WM_WINDOW.dispose();
        } else {
            System.out.println("You aren't supposed to click X!");
        }
    }

    // game over, you lose JOptionPane popup
    private static void gameOver() {
        RECURSION = Recurs.STOP;
        FACEBUTTON.showFace(Face.DEAD);
        decorateAllMines();
        // This section modified from Java Docs dialog demo from:
        // https://docs.oracle.com/javase/tutorial/uiswing/examples/components/DialogDemoProject/src/components/DialogDemo.java

        String[] options = {"Retry", "Quit"};
        int n = JOptionPane.showOptionDialog(WM_WINDOW,
                        "Game over. Try again or quit?\n",
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
            System.out.println("You aren't supposed to click X!");
        }
    }
}