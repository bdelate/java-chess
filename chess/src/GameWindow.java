package chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameWindow extends JFrame implements ActionListener, KeyListener
{
  GraphicsEnvironment ge;
  ChessBoard chessBoard;
  MoveList moveList;
  Minimax minimax;

  JButton endGameButton = new JButton("End Game");

  JTextArea historyArea = new JTextArea();

  //indicate how many players (2 or 1 with AI)
  private int numPlayers;
  //indictate whose turn it is (1=white[Human], 2=black[Human], 3=black[AI])
  private int currentPlayer=1;
  private int targetBlock=0;
  private int sourceBlock=64;
  private int destBlock=64;

  private boolean gameInPlay=true;
  private boolean calcPlayerMove=true;

  public GameWindow(int numPlayers)
  {
    super("Chess");
    setResizable(false);
    setSize(390, 440);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //get centre point to centre JFrame
    ge = ge.getLocalGraphicsEnvironment();
    double x = ge.getCenterPoint().getX();
    double y = ge.getCenterPoint().getY();
    setLocation((int)x - 195, (int)y - 220);

    JPanel pane = new JPanel();
    pane.setLayout(null);
    setContentPane(pane);
    pane.setBackground(Color.BLACK);

    this.numPlayers = numPlayers;

    //end game
    endGameButton.setBounds(280, 385, 100, 20);
    endGameButton.setFocusable(false);
    endGameButton.addActionListener(this);
    pane.add(endGameButton);

    //history
    historyArea.setBounds(0, 387, 250, 20);
    historyArea.setFocusable(false);
    historyArea.setEditable(false);
    historyArea.setBackground(new Color(181, 13, 32));
    historyArea.setForeground(Color.WHITE);
    historyArea.setText(" White Player Turn");
    pane.add(historyArea);

    moveList = new MoveList();

    //create chessBoard
    chessBoard = new ChessBoard();
    chessBoard.setBounds(0, 0, 390, 390);
    pane.add(chessBoard);
    chessBoard.setCurrentBoard(moveList.getCurrentBoard());

    addKeyListener(this);

    if (numPlayers == 1)
      minimax = new Minimax(moveList);
  }

  private boolean isCheckMate()
  {
    int kingPos;
    if (currentPlayer == 1)
      kingPos = moveList.getCurrentWhiteKingPos();
    else
      kingPos = moveList.getCurrentBlackKingPos();

    //check if the king has a valid block where it can move
    //which means its not checkMate
    if (moveList.checkValidKingMove(kingPos, kingPos+1, currentPlayer) ||
      moveList.checkValidKingMove(kingPos, kingPos-7, currentPlayer) ||
        moveList.checkValidKingMove(kingPos, kingPos-8, currentPlayer) ||
          moveList.checkValidKingMove(kingPos, kingPos-9, currentPlayer) ||
            moveList.checkValidKingMove(kingPos, kingPos-1, currentPlayer) ||
              moveList.checkValidKingMove(kingPos, kingPos+7, currentPlayer) ||
                moveList.checkValidKingMove(kingPos, kingPos+8, currentPlayer) ||
                  moveList.checkValidKingMove(kingPos, kingPos+9, currentPlayer))
                    return false;

    //check if any other pieces have valid moves which means another piece
    //can make a move which will get the king out of check ie: not checkMate
    if (moveList.checkMateSave(currentPlayer))
      return false;

    return true;
  }

  private void promotePawn()
  {
    if ((targetBlock >= 0 && targetBlock <= 7) ||
        (targetBlock >= 56 && targetBlock <= 63))
    {
      String[] choices = {"Rook", "Knight", "Bishop", "Queen"};

      int response = JOptionPane.showOptionDialog(null,
                                                  "Choose desired pawn promotion",
                                                  "Pawn Promotion", 0,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null,
                                                  choices, null);

      moveList.promotePawn(currentPlayer, response, targetBlock);
    }
  }

  private void movePlayerPiece()
  {
    int piece = moveList.getPiece(sourceBlock);
    switch (piece)
    {
      case -1:
        if (moveList.checkValidBlackPawnMove(sourceBlock, targetBlock,
                                             currentPlayer))
          if (moveList.tempMoveValid(sourceBlock, targetBlock, piece,
                                     currentPlayer))
          {
            moveList.movePiece(sourceBlock, targetBlock, piece);
            promotePawn();
          }
          else
            historyArea.setText(" Invalid Move - Move Leaves You in Check");
        else
          historyArea.setText(" Invalid Move - Try Again");
        break;
      case 1:
        if (moveList.checkValidWhitePawnMove(sourceBlock, targetBlock,
                                             currentPlayer))
          if (moveList.tempMoveValid(sourceBlock, targetBlock, piece,
                                     currentPlayer))
          {
            moveList.movePiece(sourceBlock, targetBlock, piece);
            promotePawn();
          }
          else
            historyArea.setText(" Invalid Move - Move Leaves You in Check");
        else
          historyArea.setText(" Invalid Move - Try Again");
        break;
      case -2:
      case 2:
        if (moveList.checkValidRookMove(sourceBlock, targetBlock,
                                        currentPlayer))
          if (moveList.tempMoveValid(sourceBlock, targetBlock, piece,
                                     currentPlayer))
            moveList.movePiece(sourceBlock, targetBlock, piece);
          else
            historyArea.setText(" Invalid Move - Move Leaves You in Check");
        else
          historyArea.setText(" Invalid Move - Try Again");
        break;
      case -3:
      case 3:
        if (moveList.checkValidKnightMove(sourceBlock, targetBlock,
                                          currentPlayer))
          if (moveList.tempMoveValid(sourceBlock, targetBlock, piece,
                                     currentPlayer))
            moveList.movePiece(sourceBlock, targetBlock, piece);
          else
            historyArea.setText(" Invalid Move - Move Leaves You in Check");
        else
          historyArea.setText(" Invalid Move - Try Again");
        break;
      case -4:
      case 4:
        if (moveList.checkValidBishopMove(sourceBlock, targetBlock,
                                          currentPlayer))
          if (moveList.tempMoveValid(sourceBlock, targetBlock, piece,
                                     currentPlayer))
            moveList.movePiece(sourceBlock, targetBlock, piece);
          else
            historyArea.setText(" Invalid Move - Move Leaves You in Check");
        else
          historyArea.setText(" Invalid Move - Try Again");
        break;
      case -5:
      case 5:
        if (moveList.checkValidQueenMove(sourceBlock, targetBlock,
                                         currentPlayer))
          if (moveList.tempMoveValid(sourceBlock, targetBlock, piece,
                                     currentPlayer))
            moveList.movePiece(sourceBlock, targetBlock, piece);
          else
            historyArea.setText(" Invalid Move - Move Leaves You in Check");
        else
          historyArea.setText(" Invalid Move - Try Again");
        break;
      case -6:
      case 6:
        if (moveList.checkValidKingMove(sourceBlock, targetBlock,
                                        currentPlayer))
        {
          moveList.setCurrentKingPos(targetBlock, currentPlayer);
          moveList.movePiece(sourceBlock, targetBlock, piece);
        }
        else
          historyArea.setText(" Invalid Move - Try Again");
        break;
    }

    //remove the sourceBlock from the board ie: dont display it
    sourceBlock=64;

    nextPlayer();
  }

  private void nextPlayer()
  {
    //next players move - iether human or AI
    if (!historyArea.getText().startsWith(" Invalid Move"))
    {
      if (numPlayers == 1)
      {
        if (currentPlayer == 1)
        {
          currentPlayer = 3;

          chessBoard.setDrawing(false);
          if (minimax.makeMove())
          {
            int source = minimax.getSourceBlock();
            int dest = minimax.getDestBlock();
            chessBoard.setDrawing(true);
            chessBoard.showMove(source, dest);
            nextPlayer();
          }
          else
          {
            historyArea.setText(" Check Mate  -  Game Over");
            currentPlayer = 4;
          }
        }
        else
        {
          currentPlayer = 1;
          if (moveList.inCheck(moveList.getCurrentWhiteKingPos(), currentPlayer))
            historyArea.setText(" White Player Move   (CHECK)");
          else
          historyArea.setText(" White Player Move");
        }
      }
      else if (numPlayers == 2)
      {
        if (currentPlayer == 1)
        {
          currentPlayer = 2;
          if (moveList.inCheck(moveList.getCurrentBlackKingPos(), currentPlayer))
            historyArea.setText(" Black Player Move   (CHECK)");
          else
            historyArea.setText(" Black Player Move");
        }
        else
        {
          currentPlayer = 1;
          if (moveList.inCheck(moveList.getCurrentWhiteKingPos(), currentPlayer))
            historyArea.setText(" White Player Move   (CHECK)");
          else
          historyArea.setText(" White Player Move");
        }
      }
    }

    //check for check mate
    if (historyArea.getText().endsWith("(CHECK)"))
      if (isCheckMate())
      {
        historyArea.setText(" Check Mate  -  Game Over");
        currentPlayer = 4;
      }
  }

  public void actionPerformed(ActionEvent e)
  {
    Object src = e.getSource();

    if (src == endGameButton)
    {
      int ans = JOptionPane.showConfirmDialog(null,
                                              "Are You Sure You Want To End This Game",
                                              "End Game",
                                              JOptionPane.YES_NO_OPTION);
      if (ans == 0)
      {
        StartGUI gui = new StartGUI();
        gui.show();
        chessBoard.stop();
        chessBoard = null;
        this.dispose();
      }
    }
  }

  public void keyPressed(KeyEvent evt)
  {
    //must be a human players turn in order to use the keys
    if ((currentPlayer == 1 || currentPlayer == 2) && gameInPlay)
    {
      int key = evt.getKeyCode();

      //move targetBlock and sourceBlock
      switch (key)
      {
        case KeyEvent.VK_LEFT:
          if (targetBlock - 1 >= 0)
            targetBlock -= 1;
          break;
        case KeyEvent.VK_RIGHT:
          if (targetBlock + 1 <= 63)
            targetBlock += 1;
          break;
        case KeyEvent.VK_UP:
          if (targetBlock + 8 <= 63)
            targetBlock += 8;
          break;
        case KeyEvent.VK_DOWN:
          if (targetBlock - 8 >= 0)
            targetBlock -= 8;
          break;
        case KeyEvent.VK_SPACE:
        case KeyEvent.VK_CONTROL:
          if (sourceBlock == targetBlock)
          {
            sourceBlock = 64;
            historyArea.setText(" Piece Released");
          }
          else
          {
            if (sourceBlock == 64)
            {
              sourceBlock = targetBlock;
              historyArea.setText(" Piece Selected");
            }
            else
              movePlayerPiece();
          }
          break;
      }
      chessBoard.setTarget(targetBlock);
      chessBoard.setSourceBlock(sourceBlock);
    }
  }

  public void keyReleased(KeyEvent evt)
  {
  }

  public void keyTyped(KeyEvent evt)
  {
  }
}