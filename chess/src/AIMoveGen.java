package chess;

import java.awt.*;
import java.util.*;

public class AIMoveGen
{
  MoveList moveList;
  private double moveWorth=-100.0;
  private int sourceBlock=64;
  private int destBlock=64;
  private int pieceToMove;
  private LinkedList possibleMoves;

  private int depth=0;

  public AIMoveGen(MoveList moveList)
  {
    this.moveList = moveList;
    possibleMoves = new LinkedList();
  }

  public int getSourceBlock()
  {
    return sourceBlock;
  }

  public int getDestBlock()
  {
    return destBlock;
  }

  public LinkedList getPossibleMoves()
  {
    return possibleMoves;
  }

  public int getPiece()
  {
    return pieceToMove;
  }

  public double getMoveWorth()
  {
    return moveWorth;
  }

  //if the current move is better than the one stored then this move becomes
  //the new best move
  private void setTarget(int source, int dest, double tempWorth, int piece,
                         int currentPlayer)
  {
    //only store all the moves if determining blacks moves
    if (currentPlayer == 3)
      possibleMoves.add(new String(source + " " + dest));

    if (tempWorth > moveWorth)
    {
      moveWorth = tempWorth;
      sourceBlock = source;
      destBlock = dest;
      pieceToMove = piece;
    }
  }

  private void calcPawnMoveWorth(int source, int currentPlayer, int num)
  {
    double tempWorth=0;

    if (currentPlayer == 1)
    {
      for (int dest=63; dest>-1; dest--)
        if (moveList.checkValidWhitePawnMove(source, dest, currentPlayer))
          if (moveList.tempMoveValid(source, dest, num*1, currentPlayer))
          {
            tempWorth = moveList.getMoveWorth(source, dest, currentPlayer);

            //3 rows from promotion
            if (dest >= 40) tempWorth++;
            setTarget(source, dest, tempWorth, num*1, currentPlayer);
          }
    }
    else
    {
      for (int dest=0; dest<64; dest++)
        if (moveList.checkValidBlackPawnMove(source, dest, currentPlayer))
          if (moveList.tempMoveValid(source, dest, num*1, currentPlayer))
          {
            tempWorth = moveList.getMoveWorth(source, dest, currentPlayer);

            //3 rows from promotion
            if (dest <= 23) tempWorth++;
            setTarget(source, dest, tempWorth, num*1, currentPlayer);
          }
    }
  }

  private void calcRookMoveWorth(int source, int currentPlayer, int num)
  {
    double tempWorth=0;

    for (int dest=0; dest<64; dest++)
      if (moveList.checkValidRookMove(source, dest, currentPlayer))
        if (moveList.tempMoveValid(source, dest, num*2, currentPlayer))
        {
          tempWorth = moveList.getMoveWorth(source, dest, currentPlayer);
          setTarget(source, dest, tempWorth, num*2, currentPlayer);
      }
  }

  private void calcKnightMoveWorth(int source, int currentPlayer, int num)
  {
    double tempWorth=0;

    for (int dest=0; dest<64; dest++)
      if (moveList.checkValidKnightMove(source, dest, currentPlayer))
        if (moveList.tempMoveValid(source, dest, num*3, currentPlayer))
        {
          tempWorth = moveList.getMoveWorth(source, dest, currentPlayer);
          setTarget(source, dest, tempWorth, num*3, currentPlayer);
        }
  }

  private void calcBishopMoveWorth(int source, int currentPlayer, int num)
  {
    double tempWorth=0;

    for (int dest=0; dest<64; dest++)
      if (moveList.checkValidBishopMove(source, dest, currentPlayer))
        if (moveList.tempMoveValid(source, dest, num*4, currentPlayer))
        {
          tempWorth = moveList.getMoveWorth(source, dest, currentPlayer);
          setTarget(source, dest, tempWorth, num*4, currentPlayer);
        }
  }

  private void calcQueenMoveWorth(int source, int currentPlayer, int num)
  {
    double tempWorth=0;

    for (int dest=0; dest<64; dest++)
      if (moveList.checkValidQueenMove(source, dest, currentPlayer))
        if (moveList.tempMoveValid(source, dest, num*5, currentPlayer))
        {
          tempWorth = moveList.getMoveWorth(source, dest, currentPlayer);
          setTarget(source, dest, tempWorth, num*5, currentPlayer);
        }
  }

  private void calcKingMoveWorth(int source, int currentPlayer, int num)
  {
    double tempWorth=0;

    for (int dest=0; dest<64; dest++)
      if (moveList.checkValidKingMove(source, dest, currentPlayer))
      {
        tempWorth = moveList.getMoveWorth(source, dest, currentPlayer);
        setTarget(source, dest, tempWorth, num*6, currentPlayer);
      }
  }

  public boolean move(int currentPlayer)
  {
    possibleMoves.clear();
    moveWorth = -100.0;
    int num;
    if (currentPlayer == 1) num = 1;
    else num = -1;

    //first check if currently in check mate
    if (moveList.isCheckMate(currentPlayer))
      return false;

    for (int source=63; source>-1; source--)
    {
      //pawn moves
      if (moveList.getPiece(source) == 1*num)
        calcPawnMoveWorth(source, currentPlayer, num);

      //rook moves
      if (moveList.getPiece(source) == 2*num)
        calcRookMoveWorth(source, currentPlayer, num);

      //knight moves
      if (moveList.getPiece(source) == 3*num)
        calcKnightMoveWorth(source, currentPlayer, num);

      //bishop moves
      if (moveList.getPiece(source) == 4*num)
        calcBishopMoveWorth(source, currentPlayer, num);

      //queen moves
      if (moveList.getPiece(source) == 5*num)
        calcQueenMoveWorth(source, currentPlayer, num);

      //king moves
      if (moveList.getPiece(source) == 6*num)
        calcKingMoveWorth(source, currentPlayer, num);
    }

    if (destBlock >= 0 && destBlock <= 63 && moveWorth >= -100)
    {
      if (currentPlayer == 1)
      {
        if (pieceToMove == 6 * num)
          moveList.setCurrentKingPos(destBlock, currentPlayer);
        moveList.movePiece(sourceBlock, destBlock, pieceToMove);
      }

      //promote pawn
      if ((destBlock <= 7 || destBlock >= 56) &&
          moveList.getPiece(destBlock) == 1 * num)
        moveList.promotePawn(currentPlayer, 3, destBlock);

      return true;
    }
    else
      return false;
  }
}
