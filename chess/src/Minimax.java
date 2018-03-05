package chess;

import java.util.*;

public class Minimax
{
  AIMoveGen aiMoveGen;
  MoveList moveList;
  private double moveWorth;
  private double initialBestMoveWorth;
  private int moveNum;
  private int finalSource;
  private int finalDest;
  private int initialBestSource;
  private int initialBestDest;
  private LinkedList allMoves;

  public Minimax(MoveList moveList)
  {
    this.moveList = moveList;
    aiMoveGen = new AIMoveGen(moveList);
    allMoves = new LinkedList();
  }

  //return an array of the currentBoard that moveList is using
  private int[] getCurrentBoard()
  {
    int[] returnBoard = new int[64];
    int tmpBoard[] = moveList.getCurrentBoard();
    for (int i=0; i<64; i++)
      returnBoard[i] = tmpBoard[i];

    return returnBoard;
  }

  public boolean makeMove()
  {
    moveWorth = -100.0;
    moveNum = -100;
    allMoves.clear();
    LinkedList blackChildren = new LinkedList();
    LinkedList possibleMoves = new LinkedList();
    //get the currentBoard to return to after minimax is finished
    int[] currentBoard = getCurrentBoard();

    //generate all children for this board and store them
    if (aiMoveGen.move(3))
    {
      possibleMoves = aiMoveGen.getPossibleMoves();
      initialBestMoveWorth=aiMoveGen.getMoveWorth();
      initialBestSource = aiMoveGen.getSourceBlock();
      initialBestDest = aiMoveGen.getDestBlock();
      for (int i=0; i<possibleMoves.size(); i++)
        allMoves.add(possibleMoves.get(i));

      //transform all possible moves into board positions
      for (int i=0; i<allMoves.size(); i++)
      {
        String posMove = (String)possibleMoves.get(i);
        int spacePos = posMove.indexOf(" ");
        int source = Integer.parseInt(posMove.substring(0, spacePos));
        int dest = Integer.parseInt(posMove.substring(spacePos+1,
            posMove.length()));
        int piece = moveList.getPiece(source);

        //make the move, store the child board position, restore the board
        moveList.movePiece(source, dest, piece);
        int[] childBoard = getCurrentBoard();
        blackChildren.add(childBoard);
        moveList.setCurrentBoard(currentBoard);

        //if move is for checkMate then make move now and return
        if (moveList.getMoveWorth(source, dest, 3) == 100)
        {
          moveNum = i;
          move();
          return true;
        }
      }

      LinkedList whiteChildren = getWhiteMoves(blackChildren);
      //reset the board
      moveList.setCurrentBoard(currentBoard);

      getFinalBlackBoard(whiteChildren);
      //reset the board
      moveList.setCurrentBoard(currentBoard);

      if (moveNum != -100)
      {
        move();
        return true;
      }
    }

    return false;
  }

  private void move()
  {
    int source=64;
    int dest=64;
    int piece;
    //determine if the direct child move is better than the planned move
    if (moveWorth > initialBestMoveWorth)
    {
      String posMove = (String) allMoves.get(moveNum);
      int spacePos = posMove.indexOf(" ");
      source = Integer.parseInt(posMove.substring(0, spacePos));
      dest = Integer.parseInt(posMove.substring(spacePos + 1,
          posMove.length()));
      piece = moveList.getPiece(source);
    }
    else
    {
      piece = moveList.getPiece(initialBestSource);
      source = initialBestSource;
      dest = initialBestDest;
    }

    //make sure that all the canCastle values are true ie: havent been
    //changed by minimax
    moveList.setBlackCanCastle();

    moveList.movePiece(source, dest, piece);
    //set final move so chessBoard can display the highlighted move
    finalSource = source;
    finalDest = dest;

    //promote pawn
    if (dest <= 7 && moveList.getPiece(dest) == -1)
      moveList.promotePawn(3, 3, dest);

    //make sure game knows where the kings are
    int[] tmpBoard = getCurrentBoard();
    for (int k=0; k<64; k++)
    {
      if (tmpBoard[k] == 6)
        moveList.setCurrentKingPos(k, 1);

      if (tmpBoard[k] == -6)
        moveList.setCurrentKingPos(k, 3);
    }
  }

  private LinkedList getWhiteMoves(LinkedList blackChildren)
  {
    LinkedList whiteChildren = new LinkedList();

    for (int i=0; i<blackChildren.size(); i++)
    {
      //iteratively make every move in blackChildren
      int[] blackChildBoard = (int[])blackChildren.get(i);
      moveList.setCurrentBoard(blackChildBoard);

      //make sure game knows where the kings are
      for (int k=0; k<64; k++)
      {
        if (blackChildBoard[k] == 6)
          moveList.setCurrentKingPos(k, 1);

        if (blackChildBoard[k] == -6)
          moveList.setCurrentKingPos(k, 3);
      }

      //get the best white move based on the blackChildBoard
      if (aiMoveGen.move(1))
      {
        int source = aiMoveGen.getSourceBlock();
        int dest = aiMoveGen.getDestBlock();
        int piece = aiMoveGen.getPiece();
        //make the move and store the board position
        moveList.movePiece(source, dest, piece);
        int[] childBoard = getCurrentBoard();
        whiteChildren.add(childBoard);
      }
    }

    return whiteChildren;
  }

  private void getFinalBlackBoard(LinkedList whiteChildren)
  {
    for (int i=0; i<whiteChildren.size(); i++)
    {
      //iteratively make every move in whiteChildren
      int[] whiteChildBoard = (int[])whiteChildren.get(i);
      moveList.setCurrentBoard(whiteChildBoard);

      //get the best board based on the current whiteBoard position
      if (aiMoveGen.move(3))
      {
        setBestMove(i);
      }
    }
  }

  private void setBestMove(int moveNum)
  {
    if (aiMoveGen.getMoveWorth() > moveWorth)
    {
      moveWorth = aiMoveGen.getMoveWorth();
      this.moveNum = moveNum;
    }
  }

  public int getSourceBlock()
  {
    return finalSource;
  }

  public int getDestBlock()
  {
    return finalDest;
  }
}