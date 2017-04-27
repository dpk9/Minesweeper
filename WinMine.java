/*
David Kalish
CSCI-E10b
final project
Minesweeper
*/

import java.awt.*
import java.awt.event.*
import javax.swing.*

class MineButton extends JButton {
    int adj_mines;
    boolean isMine;

    MineButton(int myAdjMines, boolean myIsMine) {
        super("");
        this.adj_mines = myAdjMines;
        this.isMine = myIsMine;
    }
}

class WinMine {
    public static void main(String[] args) {
        System.out.println("hello");
    }
}