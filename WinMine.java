/*
David Kalish
CSCI-E10b
final project
Minesweeper
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;
import java.net.URI;

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

// A game starts when the first mouse press happens. Keep track of it.
enum GameStatus {BEFORE,
                 IN_PROGRESS,
                 ENDED}

// Game difficulty
enum Difficulty {BEGINNER,
                 INTERMEDIATE,
                 EXPERT}

// Class for the mine button grid, extending JButton.
class MineButton extends JButton {
    private int adjMines;       // number of adjacent mines
    private boolean isMine;     // if this tile is a mine
    private boolean flagged;    // if this tile is flagged
    private boolean revealed;   // if this tile has been revealed
    private int row;            // row position of this tile
    private int col;            // column position of this tile

    // MineButton constructor
    MineButton(String s, boolean myIsMine, int myRow, int myCol) {
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

    // MineButton constructor (no string first arg)
    MineButton(boolean myIsMine, int myRow, int myCol) {
        this("", myIsMine, myRow, myCol);
    }

    // Decorate the tile to look pressed, but remain blank
    public void decoratePressed() {
        super.getModel().setPressed(true);
    }

    // Decorate the tile to look pressed, but remain blank
    public void decorateUnpressed() {
        super.getModel().setPressed(false);
    }

    // Decorate the tile when it has been clicked (mouseReleased).
    public void decorateClicked() {
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

    // set this tile's number of adjacent mines
    public void setAdjMines(int n) {
        this.adjMines = n;
    }
    // return this tile's number of adjacent mines
    public int getAdjMines() {
        return this.adjMines;
    }
    // return the {row, column} position of this mine
    public int[] getPosition() {
        int[] position = {this.row, this.col};
        return position;
    }
    // return whether or not this tile has a mine
    public boolean hasMine() {
        return this.isMine;
    }
    // return whether or not this tile has already been revealed
    public boolean getRevealed() {
        return this.revealed;
    }

    // toggle this tile's flag status
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

    // return whether or not this tile is currently flagged
    public boolean getFlagged() {
        return this.flagged;
    }

    // show a flag if this.flagged == true, remove it if its == false
    private void decorateFlag() {
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

    // show the number of mines adjacent to this tile. leave blank if no
    private void decorateAdjMineNum() {
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

// Class for the fac button at the top of the window.
class FaceButton extends JButton {
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
    public static JTextField MINES_REMAINING_FIELD = new JTextField(3);
    // elapsed seconds timer display
    public static JTextField TIMER_FIELD = new JTextField(3);
    // elapsed seconds timer Timer
    public static javax.swing.Timer TIMER;
    // elapsed seconds timer Timer
    public static int TIME;
    // face button
    public static FaceButton FACEBUTTON;
    // the main game grid
    public static MineButton[][] MINE_GRID;
    // difficulty tracker
    public static Difficulty DIFFICULTY;
    // rows for game board
    public static int ROWS;
    // cols for game board
    public static int COLS;
    // # of mines to generate
    public static int MINES;
    // mouse status
    public static MouseStatus MOUSE_STATUS;
    // game status
    public static GameStatus GAME_STATUS;
    // recursion breaker init
    public static Recurs RECURSION = Recurs.GO;

    // main function
    public static void main(String[] args) {
        setDifficulty(Difficulty.EXPERT);
        doLayout();
    }

    // set up the whole game board
    private static void doLayout() {
        // use difficulty to set the rows/cols/mines
        // System.out.println("" + DIFFICULTY);
        switch (DIFFICULTY) {
            case BEGINNER:
                ROWS = 9;
                COLS = 9;
                MINES = 10;
                break;
            case INTERMEDIATE:
                ROWS = 16;
                COLS = 16;
                MINES = 40;
                break;
            case EXPERT:
                ROWS = 16;
                COLS = 30;
                MINES = 99;
                break;
            default:
                ROWS = 16;
                COLS = 30;
                MINES = 99;
                break;
        }

        GAME_STATUS = GameStatus.BEFORE;
        // set up JFrame window
        WM_WINDOW = new JFrame();
        WM_WINDOW.setResizable(false);
        WM_WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // make the layout.
        // Make the menu
        JMenuBar menuBar = doMenuLayout();

        // make the upper panels
        JPanel minesRemainingDisplay = makeDisplay(MINES_REMAINING_FIELD);
        updateMinesLeft(MINES);
        makeFaceButton();
        JPanel timerDisplay = makeDisplay(TIMER_FIELD);
        initTimer();


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
        WM_WINDOW.setJMenuBar(menuBar);
        WM_WINDOW.add(upperPanel, BorderLayout.PAGE_START);
        WM_WINDOW.add(buttonPanel, BorderLayout.CENTER);

        // pack and display window
        WM_WINDOW.pack();
        WM_WINDOW.setVisible(true);
    }

    // Set up the menu bar
    private static JMenuBar doMenuLayout() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenu helpMenu = new JMenu("Help");
        // init game menu items
        JMenuItem newGameMenuItem = new JMenuItem("New Game");
        JMenuItem difficultyMenuItem = new JMenuItem("Difficulty");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        // game menu item action listeners:
        // New Game menu item actionlistener
        newGameMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Start a new game
                newGame();
            }
        });

        // Difficulty menu item actionlistener
        difficultyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Make an Option Dialog to choose difficulty; start new game
                String[] options = {"Beginner", "Intermediate", "Expert"};
                int n = JOptionPane.showOptionDialog(
                        WM_WINDOW,
                        "Choose difficulty:\n"
                        + "Beginner: 9 x 9, 10 mines\n"
                        + "Intermediate: 16 x 16, 40 mines \n"
                        + "Expert: 16 x 30, 99 mines \n",
                        "Choose Difficulty",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,   // use standard Q Message icon
                        options,
                        options[2]);

                switch (n) {
                    case 0:
                        setDifficulty(Difficulty.BEGINNER);
                        newGame();
                        break;
                    case 1:
                        setDifficulty(Difficulty.INTERMEDIATE);
                        newGame();
                        break;
                    case 2:
                        setDifficulty(Difficulty.EXPERT);
                        newGame();
                        break;
                    default: break;
                }
            }
        });

        // Exit menu item actionlistener
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Quit the game
                WM_WINDOW.dispose();
            }
        });

        // init help menu items
        JMenuItem aboutMenuItem = new JMenuItem("About");
        // About menu item actionlistener
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JEditorPane aboutPane;
                try {
                    // have to make a JEditorPane to make the URL clickable
                    aboutPane = new JEditorPane("text/html",
                            "<html>"
                                + "<head>"
                                    + "<style>"
                                        + "body {background-color: #EEEEEE;}"
                                    + "</style>"
                                + "</head>"
                                + "<body>"
                                    + "This MineSweeper clone was made by<br/>"
                                    + "David Kalish for Harvard Extension<br/>"
                                    + "School, course CSCI-E10b.<br/><br/>"
                                    + "Code is maintained at:<br/>"
                                    + "<a href=\"https://github.com/dpk9/WinMine\">https://github.com/dpk9/WinMine</a><br/><br/>"
                                    + "The Minesweeper found on the Windows<br/>"
                                    + "Store in Windows 8+ is a travesty,<br/>"
                                    + "so that's why I made this clone of<br/>"
                                    + "the older versions."
                                + "</body>"
                            + "</html>");
                    aboutPane.setEditable(false);
                    aboutPane.setBorder(null);
                    // listen for URL click
                    aboutPane.addHyperlinkListener(new HyperlinkListener() {
                        public void hyperlinkUpdate(HyperlinkEvent e) {
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                try {
                                    if(Desktop.isDesktopSupported())
                                    {
                                        Desktop.getDesktop().browse(new URI(e.getURL().toString()));
                                    }
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                        }
                    });
                    JOptionPane.showMessageDialog(WM_WINDOW, aboutPane);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        // construct menus
        gameMenu.add(newGameMenuItem);
        gameMenu.add(difficultyMenuItem);
        gameMenu.add(exitMenuItem);

        helpMenu.add(aboutMenuItem);

        // Construct menu bar
        menuBar.add(gameMenu);
        menuBar.add(helpMenu);

        return menuBar;
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
        /*
        // UNCOMMENT THIS SECTION TO CHEAT!


        System.out.println("*****cheats!*****\n" + mineList);


        */
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
                        if (GAME_STATUS == GameStatus.BEFORE) {
                            TIMER.start();
                            GAME_STATUS = GameStatus.IN_PROGRESS;
                        }

                        // L + R pressed - update mouse status to LR_PRESSED
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
                        // LEFT pressed - update mouse status to PRESSED
                        else if (SwingUtilities.isLeftMouseButton(me)
                                 && MOUSE_STATUS != MouseStatus.LR_PRESSED) {
                            MOUSE_STATUS = MouseStatus.PRESSED;
                            mouseLeftPressedHandler(me);
                        }
                    }
                    // Mouse left the button - update mouse status to EXITED
                    public void mouseExited(MouseEvent me) {
                        // when mouse leave the button before releasing, update
                        // mouse status to EXITED to be able to cancel the
                        // mouse released action from firing
                        MOUSE_STATUS = MouseStatus.EXITED;
                    }


                    public void mouseReleased(MouseEvent me) {
                        // fire if the mouse has not exited the button
                        if (MOUSE_STATUS != MouseStatus.EXITED) {
                            // L + R release
                            if (SwingUtilities.isLeftMouseButton(me)
                                    && SwingUtilities.isRightMouseButton(me)) {
                                MOUSE_STATUS = MouseStatus.LR_RELEASED;
                                mouseLRReleasedHandler(me);
                            // Left release (don't fire if mouse has EXITED the
                            // button, but make sure the face returns to normal
                            } else if (SwingUtilities.isLeftMouseButton(me)
                                    && MOUSE_STATUS != MouseStatus.LR_PRESSED) {
                                MOUSE_STATUS = MouseStatus.RELEASED;
                                mouseLeftReleasedHandler(me);
                            }
                        // let the face return to OK if the mouse EXITED
                        } else mouseLeftReleasedFaceHandler(me);
                    }
                });
            }
        }

        // set each mine's Adjacent Mines:
        // go through all tiles
        for (MineButton thisButton : mineGrid1D()) {
            adjMines = 0;
            // for each tile, check its surrounding tile
            for (MineButton checkButton : threeByThree(thisButton)) {
                // record if the tile has a mine
                if (checkButton.hasMine()) adjMines++;
            }
            // set the button's adjMines
            thisButton.setAdjMines(adjMines);
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

        // update remaining mines display
        updateMinesLeft(MINES - getAllFlags());
        // see if you won
        checkWinCondition();
    }

    // LR press decorates the adjacent unflagged and unrevealed tiles
    private static void mouseLRPressedHandler(MouseEvent me) {
        // make the surrounding mines appear pressed
        MineButton thisButton = (MineButton)me.getSource();
        for (MineButton checkButton : threeByThree(thisButton)) {
            FACEBUTTON.showFace(Face.NERVOUS);
            checkButton.decoratePressed();
        }
    }

    // LR release undecorates the adjacent unflagged and unrevealed tiles if
    // flags don't match displayed number
    private static void mouseLRReleasedHandler(MouseEvent me) {
        FACEBUTTON.showFace(Face.OK);
        MineButton thisButton = (MineButton)me.getSource();
        int adjFlags = getAdjFlags(thisButton);
        int adjMines = thisButton.getAdjMines();
        // surrounding tiles
        for (MineButton checkButton : threeByThree(thisButton)) {
            // return appearance to unclicked
            checkButton.decorateUnpressed();
            // if this tile is already revealed and its number matches the
            // number of adjacent flags...
            if (adjFlags == adjMines && thisButton.getRevealed()) {
                // if the adjacent tile is neither revealed nor flagged...
                if (! (checkButton.getRevealed()
                        || checkButton.getFlagged()) ) {
                    // reveal the adjacent tile! and expand if appropriate
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

    // Set the game difficulty
    private static void setDifficulty(Difficulty diff) {
        DIFFICULTY = diff;
    }

    // When the game is lost, show where all the unflagged mines were
    private static void decorateAllMines() {
        for (MineButton thisButton : mineGrid1D()) {
            if (thisButton.hasMine() && !thisButton.getFlagged()) {
                thisButton.decorateClicked();
            }
        }
    }

    // Get the number of flags adjacent to this button
    private static int getAdjFlags(MineButton thisButton) {
        int flagCounter = 0;

        for (MineButton checkButton : threeByThree(thisButton)) {
            if (checkButton.getFlagged()) flagCounter++;
        }
        return flagCounter;
    }

    // Convert the 2D MINE_GRID to 1D array. For easier for-looping sometimes
    private static MineButton[] mineGrid1D() {
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

    // get the total number of flagged tiles on the board
    private static int getAllFlags() {
        int flagCounter = 0;
        for (MineButton thisButton : mineGrid1D()) {
            if (thisButton.getFlagged()) {
                flagCounter++;
            }
        }
        return flagCounter;
    }

    // Check for win condition
    private static boolean checkWinCondition() {
        // win condition: all non-mine tiles are revealed
        for (MineButton thisButton : mineGrid1D()) {
            if (!thisButton.hasMine() && !thisButton.getRevealed()) {
                return false;
            }
        }
        gameWon();
        return true;
    }

    // Uncover a tile. If the tile has no adjacent mines, uncover all of its
    // adjacent tiles recursively.
    private static void expandSafeZone(MineButton thisButton) {
        // You uncovered a mine (from an L+R release most likely)
        if (thisButton.hasMine()) {
            RECURSION = Recurs.STOP;
            gameOver();
            return;
        // check RECURSION flag. Without this check, it will keep clicking a
        // new game board after a Game Over, as the recursion unravels.
        } else if (RECURSION == Recurs.GO) {
            // decorate the tile with mine or number
            thisButton.decorateClicked();

            // if this button has no mines and no adjacent mines (reveals a
            // blank tile), expand the safe area.
            if (thisButton.getAdjMines() == 0) {
                //
                // NOTE: for some reason, threeByThree() doesn't work here
                //
                int[] pos = thisButton.getPosition();
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
                        // recursively click surrounding empty tiles to expand
                        // area.
                        if (! (anotherButton.getRevealed()
                                || anotherButton.getFlagged()) ) {
                            expandSafeZone(anotherButton);
                        }
                    }
                }
            }
        // on Game Over, skip the recursive clicking
        } else if (RECURSION == Recurs.STOP) return;
    }

    // return the buttons contained in the 3x3 grid surrounding thisButton
    private static MineButton[] threeByThree(MineButton thisButton) {
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

        // give the buttonlist a size once the size has been determined
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
        // return the list
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

    // update the Mines Remaining display whenever flags are toggled
    private static void updateMinesLeft(int minesLeft) {
        MINES_REMAINING_FIELD.setText(Integer.toString(minesLeft));
    }

    // Initialize the elapsed time timer
    private static void initTimer() {
        TIME = 0;
        TIMER = new javax.swing.Timer(1000, new ActionListener() {
            // increase timer display each second
            public void actionPerformed(ActionEvent e) {
                timerElapse();
            }
        });
        TIMER_FIELD.setText(String.format("%03d", TIME));
    }

    // increase timer each second
    private static void timerElapse() {
        TIME++;
        TIMER_FIELD.setText(String.format("%03d", TIME));
    }

    // game won,  JOptionPane popup
    private static void gameWon() {
        GAME_STATUS = GameStatus.ENDED;
        TIMER.stop();
        RECURSION = Recurs.STOP;
        FACEBUTTON.showFace(Face.WON);

        Icon winface = new ImageIcon(WinMine.class.getResource("img/face-won.png"));

        String[] options = {"Play Again", "Quit"};
        int n = JOptionPane.showOptionDialog(
                WM_WINDOW,
                "You won in " + TIME + " seconds!\n"
                + "Play again or quit?\n",
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
        GAME_STATUS = GameStatus.ENDED;
        TIMER.stop();
        RECURSION = Recurs.STOP;
        FACEBUTTON.showFace(Face.DEAD);
        // show all the unflagged mines
        decorateAllMines();

        String[] options = {"Retry", "Quit"};
        int n = JOptionPane.showOptionDialog(
                        WM_WINDOW,
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