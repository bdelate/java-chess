package chess;

public class MoveList
{
  private int[] currentBoard;

  private final int WHITE_PAWN=1;
  private final int WHITE_ROOK=2;
  private final int WHITE_KNIGHT=3;
  private final int WHITE_BISHOP=4;
  private final int WHITE_QUEEN=5;
  private final int WHITE_KING=6;
  private final int BLACK_PAWN=-1;
  private final int BLACK_ROOK=-2;
  private final int BLACK_KNIGHT=-3;
  private final int BLACK_BISHOP=-4;
  private final int BLACK_QUEEN=-5;
  private final int BLACK_KING=-6;

  //current position for each sides king
  private int whiteKingPos=4;
  private int blackKingPos=60;

  // Pre-processed data structures containing all possible moves from all
  // possible squares, by piece type
  private int knightMoves[][];
  private int bishopMoves[][][];
  private int rookMoves[][][];
  private int kingMoves[][];

  private boolean whiteCanCastle=true;
  private boolean leftWhiteCastleValid=true;
  private boolean rightWhiteCastleValid=true;
  private boolean blackCanCastle=true;
  private boolean leftBlackCastleValid=true;
  private boolean rightBlackCastleValid=true;

  public MoveList()
  {
    initBoard();
    initPieceMoves();
  }

  //set up the initial board
  private void initBoard()
  {
    currentBoard = new int[64];
    for (int i=0; i<64; i++)
      currentBoard[i] = 0;

    currentBoard[0] = WHITE_ROOK;
    currentBoard[1] = WHITE_KNIGHT;
    currentBoard[2] = WHITE_BISHOP;
    currentBoard[3] = WHITE_QUEEN;
    currentBoard[4] = WHITE_KING;
    currentBoard[5] = WHITE_BISHOP;
    currentBoard[6] = WHITE_KNIGHT;
    currentBoard[7] = WHITE_ROOK;
    for (int i=8; i<16; i++)
      currentBoard[i] = WHITE_PAWN;

    currentBoard[56] = BLACK_ROOK;
    currentBoard[57] = BLACK_KNIGHT;
    currentBoard[58] = BLACK_BISHOP;
    currentBoard[59] = BLACK_QUEEN;
    currentBoard[60] = BLACK_KING;
    currentBoard[61] = BLACK_BISHOP;
    currentBoard[62] = BLACK_KNIGHT;
    currentBoard[63] = BLACK_ROOK;
    for (int i=48; i<56; i++)
      currentBoard[i] = BLACK_PAWN;
  }

  public void setCurrentBoard(int[] currentBoard)
  {
    for (int i=0; i<64; i++)
      this.currentBoard[i] = currentBoard[i];
  }

  public double getMoveWorth(int source, int dest, int currentPlayer)
  {
    double moveWorth = 0.0;

    //check if the current player has lost the game
    if (isCheckMate(source, dest, currentPlayer))
      moveWorth = 100;
    else
    {
      moveWorth += getCastleKingWorth(source, dest);

      moveWorth += getMoveViable(source, dest, currentPlayer);

      moveWorth += getBoardWorth(source, dest, currentPlayer);

      moveWorth += getPossibleAttacks(source, dest, currentPlayer);

      moveWorth -= getVulnerablePieces(source, dest, currentPlayer);

      int num;
      if (currentPlayer == 1) num = 1;
      else num = -1;

      //reduce chance that king will move
      if (currentBoard[source] == 6*num)
        moveWorth--;
    }

    return moveWorth;
  }

  //check if trying to castle to king, if so then add or subtract value
  //of king plus rook (ie: (10+5)/10) depending on if the rook will be
  //in danger
  private double getCastleKingWorth(int source, int dest)
  {
    double worth = 0.0;

    if (source == 4)
    {
      //white left castle to king
      if (dest == 2)
      {
        if (inCheck(3, 3))
          worth -= 1.5;
        else
          worth += 1.5;
      }
      //white right castle to king
      else if (dest == 6)
      {
        if (inCheck(5, 3))
          worth -= 1.5;
        else
          worth += 1.5;
      }
    }
    else if (source == 60)
    {
      //black left castle to king
      if (dest == 58)
      {
        if (inCheck(59, 3))
          worth -= 1.5;
        else
          worth += 1.5;
      }
      //black right castle to king
      else if (dest == 62)
      {
        if (inCheck(61, 3))
          worth -= 1.5;
        else
          worth += 1.5;
      }
    }

    return worth;
  }

  //check for a check mate with simulated next move
  private boolean isCheckMate(int source, int dest, int currentPlayer)
  {
    int tempSource = currentBoard[source];
    currentBoard[source] = 0;
    int tempDest = currentBoard[dest];
    currentBoard[dest] = tempSource;

    int kingPos;
    if (currentPlayer == 1)
      kingPos = getCurrentWhiteKingPos();
    else
      kingPos = getCurrentBlackKingPos();

    //check if the king has a valid block where it can move
    //which means its not checkMate
    if (checkValidKingMove(kingPos, kingPos+1, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-7, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-8, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-9, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-1, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos+7, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos+8, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos+9, currentPlayer))
    {
      currentBoard[source] = tempSource;
      currentBoard[dest] = tempDest;
      return false;
    }

    //check if any other pieces have valid moves which means another piece
    //can make a move which will get the king out of check ie: not checkMate
    if (checkMateSave(currentPlayer))
    {
      currentBoard[source] = tempSource;
      currentBoard[dest] = tempDest;
      return false;
    }

    currentBoard[source] = tempSource;
    currentBoard[dest] = tempDest;
    return true;
  }

  //check for a check mate with the currentBoard
  public boolean isCheckMate(int currentPlayer)
  {
    int kingPos;
    if (currentPlayer == 1)
      kingPos = getCurrentWhiteKingPos();
    else
      kingPos = getCurrentBlackKingPos();

    //check if the king has a valid block where it can move
    //which means its not checkMate
    if (checkValidKingMove(kingPos, kingPos+1, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-7, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-8, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-9, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos-1, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos+7, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos+8, currentPlayer) ||
        checkValidKingMove(kingPos, kingPos+9, currentPlayer))
    {
      return false;
    }

    //check if any other pieces have valid moves which means another piece
    //can make a move which will get the king out of check ie: not checkMate
    if (checkMateSave(currentPlayer))
      return false;

    return true;
  }

  //simulate a move to see if dest will be in check. If in check then
  //reverse move and see if block(piece) to be taken is worh more than own
  //piece
  private double getMoveViable(int source, int dest, int currentPlayer)
  {
    double total = 0.0;
    int tempSource = currentBoard[source];
    currentBoard[source] = 0;
    int tempDest = currentBoard[dest];
    currentBoard[dest] = tempSource;

    if (inCheck(dest, currentPlayer))
    {
      currentBoard[source] = tempSource;
      currentBoard[dest] = tempDest;
      if (checkCaptureWorth(source) < checkCaptureWorth(dest))
        total += (checkCaptureWorth(dest) / 10)*1.5;
      else
        total -= 10;
    }
    else
    {
      currentBoard[source] = tempSource;
      currentBoard[dest] = tempDest;
      total += checkCaptureWorth(dest) / 10*1.5;
    }

    currentBoard[source] = tempSource;
    currentBoard[dest] = tempDest;
    return total;
  }

  //simulate a move and then calculate the board worth
  //and return the board worth
  private double getBoardWorth(int source, int dest, int currentPlayer)
  {
    double boardWorth = 0.0;
    int tempSource = currentBoard[source];
    currentBoard[source] = 0;
    int tempDest = currentBoard[dest];
    currentBoard[dest] = tempSource;
    int num;
    if (currentPlayer == 1) num = 1;
    else num = -1;

    for (int i=0; i<64; i++)
    {
      //check for pawns that can be promoted
      //add the worth of a queen for potential promotion pawns
      if (i >= 56 && currentPlayer == 1)
      {
        if (currentBoard[i] == 1 * num)
          boardWorth += 0.9;
      }
      else if (i <= 7 && currentPlayer != 1)
      {
        if (currentBoard[i] == 1 * num)
          boardWorth += 0.9;
      }
      //check for pieces in the middle of the board
      if (i >= 16 && i <=47)
      {
        if (currentPlayer == 1)
        {
          if (currentBoard[i] > 0)
            boardWorth += checkCaptureWorth(i) / 10 + 0.1;
          else if (currentBoard[i] < 0)
            boardWorth -= checkCaptureWorth(i) / 10 + 0.1;
        }
        else
        {
          if (currentBoard[i] < 0)
            boardWorth += checkCaptureWorth(i) / 10 + 0.1;
          else if (currentBoard[i] > 0)
            boardWorth -= checkCaptureWorth(i) / 10 + 0.1;
        }
      }
      else
      {
        //check the rest of the board ie: not the middle
        if (currentPlayer == 1)
        {
          if (currentBoard[i] > 0)
            boardWorth += checkCaptureWorth(i) / 10;
          else if (currentBoard[i] < 0)
            boardWorth -= checkCaptureWorth(i) / 10;
        }
        else
        {
          if (currentBoard[i] < 0)
            boardWorth += checkCaptureWorth(i) / 10;
          else if (currentBoard[i] > 0)
            boardWorth -= checkCaptureWorth(i) / 10;
        }
      }
    }

    currentBoard[source] = tempSource;
    currentBoard[dest] = tempDest;

    return boardWorth;
  }

  //simulate a move then determine how many enemy pieces can potentially
  //be taken and add them together
  private double getPossibleAttacks(int source, int dest, int currentPlayer)
  {
    double possibleAttacks = 0.0;
    int tempSource = currentBoard[source];
    currentBoard[source] = 0;
    int tempDest = currentBoard[dest];
    currentBoard[dest] = tempSource;

    if (currentPlayer == 1)
    {
      for (int i = 0; i < 64; i++)
        if (currentBoard[i] < 0)
          if (inCheck(i, 3))
            possibleAttacks += checkCaptureWorth(i) / 10-0.1;
    }
    else
    {
      for (int i = 0; i < 64; i++)
        if (currentBoard[i] > 0)
          if (inCheck(i, 1))
            possibleAttacks += checkCaptureWorth(i) / 10-0.1;
    }

    currentBoard[source] = tempSource;
    currentBoard[dest] = tempDest;

    return possibleAttacks;
  }

  //simulate a move then
  //determine how many friendly pieces can potentially be taken
  private double getVulnerablePieces(int source, int dest, int currentPlayer)
  {
    double vulnerablePieces = 0.0;
    int tempSource = currentBoard[source];
    currentBoard[source] = 0;
    int tempDest = currentBoard[dest];
    currentBoard[dest] = tempSource;

    if (currentPlayer == 1)
    {
      for (int i = 0; i < 64; i++)
        if (currentBoard[i] > 0)
          if (inCheck(i, 1))
            vulnerablePieces += checkCaptureWorth(i) / 10;
    }
    else
    {
      for (int i = 0; i < 64; i++)
        if (currentBoard[i] < 0)
          if (inCheck(i, 3))
            vulnerablePieces += checkCaptureWorth(i) / 10;
    }

    currentBoard[source] = tempSource;
    currentBoard[dest] = tempDest;

    return vulnerablePieces;
  }

  public int[] getCurrentBoard()
  {
    return currentBoard;
  }

  public int getPiece(int block)
  {
    return currentBoard[block];
  }

  //reset the canCastle booleans after minimax has changed all of them
  public void setBlackCanCastle()
  {
    blackCanCastle=true;
    leftBlackCastleValid=true;
    rightBlackCastleValid=true;
  }

  public void movePiece(int sourceBlock, int destBlock, int piece)
  {
    currentBoard[sourceBlock] = 0;
    currentBoard[destBlock] = piece;

    //disable ability for specific direction castle to king
    if (sourceBlock == 0) leftWhiteCastleValid = false;
    else if (sourceBlock == 7) leftWhiteCastleValid = false;
    else if (sourceBlock == 56) leftBlackCastleValid = false;
    else if (sourceBlock == 63) leftBlackCastleValid = false;

    //check for castle to King (white)
    if (piece == 6 && whiteCanCastle)
    {
      whiteCanCastle = false;
      if (destBlock == 2)
      {
        currentBoard[0] = 0;
        currentBoard[3] = 2;
      }
      else if (destBlock == 6)
      {
        currentBoard[7] = 0;
        currentBoard[5] = 2;
      }
    }
    //check for castle to King (black)
    else if (piece == -6 && blackCanCastle)
    {
      blackCanCastle = false;
      if (destBlock == 58)
      {
        currentBoard[56] = 0;
        currentBoard[59] = -2;
      }
      else if (destBlock == 62)
      {
        currentBoard[63] = 0;
        currentBoard[61] = -2;
      }
    }
  }

  //check if the correct player side is playing (ie: if its white's turn
  //then a white piece must be moved)
  private boolean checkValidPlayerPiece(int sourceBlock, int destBlock,
                                        int currentPlayer)
  {
    if (currentPlayer == 1 && currentBoard[sourceBlock] < 0)
      return false;
    if ((currentPlayer == 2 || currentPlayer == 3) &&
        currentBoard[sourceBlock] > 0)
      return false;

    return true;
  }

  public void promotePawn(int currentPlayer, int response, int destBlock)
  {
    int num;
    if (currentPlayer == 1) num = 1;
    else num = -1;

    if (response == 0) currentBoard[destBlock] = 2*num;
    else if (response == 1) currentBoard[destBlock] = 3*num;
    else if (response == 2) currentBoard[destBlock] = 4*num;
    else if (response == 3) currentBoard[destBlock] = 5*num;
  }

  //check if any other pieces have valid moves which means another piece
  //can make a move which will get the king out of check ie: not checkMate
  ////Check every block for a friendly piece,...if a friendly piece is found
  ////check every block to see if the piece is able to move there
  public boolean checkMateSave(int currentPlayer)
  {
    int num;

    if (currentPlayer == 1)
      num=1;
    else
      num=-1;

    for (int i=0; i<64; i++)
    {
      //pawn
      if (currentBoard[i] == 1*num)
      {
        for (int j=0; j<64; j++)
        {
          if (currentPlayer == 1)
          {
            if (checkValidWhitePawnMove(i, j, currentPlayer))
              if (tempMoveValid(i, j, 2 * num, currentPlayer))
                return true;
          }
          else
          {
            if (checkValidBlackPawnMove(i, j, currentPlayer))
              if (tempMoveValid(i, j, 2 * num, currentPlayer))
                return true;
          }
        }
      }
      //rook
      else if (currentBoard[i] == 2*num)
      {
        for (int j=0; j<64; j++)
        {
          if (checkValidRookMove(i, j, currentPlayer))
            if (tempMoveValid(i, j, 2*num, currentPlayer))
              return true;
        }
      }
      //knight
      else if (currentBoard[i] == 3*num)
      {
        for (int j=0; j<64; j++)
        {
          if (checkValidKnightMove(i, j, currentPlayer))
            if (tempMoveValid(i, j, 2*num, currentPlayer))
              return true;
        }
      }
      //bishop
      else if (currentBoard[i] == 4*num)
      {
        for (int j=0; j<64; j++)
        {
          if (checkValidBishopMove(i, j, currentPlayer))
            if (tempMoveValid(i, j, 2*num, currentPlayer))
              return true;
        }
      }
      //queen
      else if (currentBoard[i] == 5*num)
      {
        for (int j=0; j<64; j++)
        {
          if (checkValidQueenMove(i, j, currentPlayer))
            if (tempMoveValid(i, j, 2*num, currentPlayer))
              return true;
        }
      }
    }

    return false;
  }

  //determine how much worth should be allocated to this move dependin on
  //which piece will be taken
  public double checkCaptureWorth(int destBlock)
  {
    double worth=0.0;
    int piece = (int)Math.sqrt(Math.pow(currentBoard[destBlock], 2));

    switch (piece)
    {
      case 1:
        worth = 1.0;
        break;
      case 2:
        worth = 5.0;
        break;
      case 3:
        worth = 3.0;
        break;
      case 4:
        worth = 3.0;
        break;
      case 5:
        worth = 9.0;
        break;
      case 6:
        worth = 10.0;
        break;
    }
    return worth;
  }

  public boolean checkValidWhitePawnMove(int sourceBlock, int destBlock,
                                         int currentPlayer)
  {
    if (!checkValidPlayerPiece(sourceBlock, destBlock, currentPlayer))
      return false;

    //check for single block move
    if (destBlock - sourceBlock == 8)
    {
      if (currentBoard[destBlock] == 0)
        return true;
    }

    //check for double block move
    if ((sourceBlock <= 15) && (sourceBlock - destBlock == -16))
      if (currentBoard[destBlock] == 0 && currentBoard[sourceBlock+8] == 0)
        return true;

    //check for capture
    if ((destBlock-sourceBlock == 7 && (sourceBlock != 16
                                            && sourceBlock != 24
                                            && sourceBlock != 32
                                            && sourceBlock != 40
                                            && sourceBlock != 48
                                            && sourceBlock != 56))
       || destBlock-sourceBlock == 9 && (sourceBlock != 15
                                            && sourceBlock != 23
                                            && sourceBlock != 31
                                            && sourceBlock != 39
                                            && sourceBlock != 47
                                            && sourceBlock != 55
                                            && sourceBlock != 63))
      if (currentBoard[sourceBlock]*currentBoard[destBlock]<0)
        return true;

    return false;
  }

  public boolean checkValidBlackPawnMove(int sourceBlock, int destBlock,
                                         int currentPlayer)
  {
    if (!checkValidPlayerPiece(sourceBlock, destBlock, currentPlayer))
      return false;

    //check for single block move
    if (destBlock - sourceBlock == -8)
    {
      if (currentBoard[destBlock] == 0)
        return true;
    }

    //check for double block move
    if (sourceBlock >= 48 && (sourceBlock - destBlock == 16))
      if (currentBoard[destBlock] == 0 && currentBoard[sourceBlock - 8] == 0)
        return true;

    //check for capture
    if ( (destBlock - sourceBlock == -7 && (sourceBlock != 15
                                            && sourceBlock != 23
                                            && sourceBlock != 31
                                            && sourceBlock != 39
                                            && sourceBlock != 47
                                            && sourceBlock != 55
                                            && sourceBlock != 63))
        || destBlock - sourceBlock == -9 && (sourceBlock != 16
                                             && sourceBlock != 24
                                             && sourceBlock != 32
                                             && sourceBlock != 40
                                             && sourceBlock != 48
                                             && sourceBlock != 56))
      if (currentBoard[sourceBlock] * currentBoard[destBlock] < 0)
        return true;

    return false;
  }

  //first check if the specified move is valid for the sourceBlock
  //then check if the destBlock is not a friendly piece
  //then check if there are no pieces in the ray
  public boolean checkValidRookMove(int sourceBlock, int destBlock, int currentPlayer)
  {
    if (!checkValidPlayerPiece(sourceBlock, destBlock, currentPlayer))
      return false;

    for (int i = 0; i < rookMoves[sourceBlock].length; i++)
    {
      for (int j = 0; j < rookMoves[sourceBlock][i].length; j++)
        if (rookMoves[sourceBlock][i][j] == destBlock)
        {
          if (currentBoard[destBlock] == 0 ||
              currentBoard[sourceBlock] * currentBoard[destBlock] < 0)
          {
            int total = 0;
            for (int k = 0; k < j; k++)
            {
              if (currentBoard[rookMoves[sourceBlock][i][k]] != 0)
                total += 1;
            }
            if (total == 0)
            {

              return true;
            }
          }
          else
            return false;
        }
      }
      return false;
  }

  //first check if the specified move is valid for the sourceBlock
  //then check if the destBlock is not a friendly piece
  public boolean checkValidKnightMove(int sourceBlock, int destBlock,
                                      int currentPlayer)
  {
    if (!checkValidPlayerPiece(sourceBlock, destBlock, currentPlayer))
      return false;

    for (int i=0; i<knightMoves[sourceBlock].length; i++)
    {
      if (knightMoves[sourceBlock][i] == destBlock)
      {
        if (currentBoard[destBlock] == 0 ||
            currentBoard[sourceBlock]*currentBoard[destBlock]<0)
        {
          return true;
        }
        else
          return false;
      }
    }
    return false;
  }

  //first check if the specified move is valid for the sourceBlock
  //then check if the destBlock is not a friendly piece
  //then check if there are no pieces in the ray
  public boolean checkValidBishopMove(int sourceBlock, int destBlock,
                                      int currentPlayer)
  {
    if (!checkValidPlayerPiece(sourceBlock, destBlock, currentPlayer))
      return false;

    for (int i=0; i<bishopMoves[sourceBlock].length; i++)
    {
      for (int j = 0; j < bishopMoves[sourceBlock][i].length; j++)
        if (bishopMoves[sourceBlock][i][j] == destBlock)
        {
          if (currentBoard[destBlock] == 0 ||
              currentBoard[sourceBlock] * currentBoard[destBlock] < 0)
          {
            int total = 0;
            for (int k=0; k<j; k++)
            {
              if (currentBoard[bishopMoves[sourceBlock][i][k]] != 0)
                total += 1;
            }
            if (total == 0)
            {
              return true;
            }
          }
        }
    }
    return false;
  }

  public boolean checkValidQueenMove(int sourceBlock, int destBlock,
                                     int currentPlayer)
  {
    if (!checkValidPlayerPiece(sourceBlock, destBlock, currentPlayer))
      return false;

    if (checkValidRookMove(sourceBlock, destBlock, currentPlayer))
      return true;
    if (checkValidBishopMove(sourceBlock, destBlock, currentPlayer))
      return true;
    return false;
  }

  private boolean castleKing(int sourceBlock, int destBlock, int currentPlayer)
  {
    //check if trying to castle (white)
    if (currentPlayer == 1)
    {
      //castle left
      if (sourceBlock == 4 && destBlock == 2 && currentBoard[0] == 2)
      {
        if (whiteCanCastle && leftWhiteCastleValid)
          if (currentBoard[1] == 0 && currentBoard[2] == 0 &&
              currentBoard[3] == 0)
            return true;
      }
      //castle right
      else if (sourceBlock == 4 && destBlock == 6 && currentBoard[7] == 2)
      {
        if (whiteCanCastle && rightWhiteCastleValid)
          if (currentBoard[5] == 0 && currentBoard[6] == 0)
            return true;
      }
    }
    //check if trying to castle (black)
    else
    {
      //castle left
      if (sourceBlock == 60 && destBlock == 58 && currentBoard[56] == -2)
      {
        if (blackCanCastle && leftBlackCastleValid)
          if (currentBoard[57] == 0 && currentBoard[58] == 0 &&
              currentBoard[59] == 0)
            return true;
      }
      //castle right
      else if (sourceBlock == 60 && destBlock == 62 && currentBoard[63] == -2)
      {
        if (blackCanCastle && rightBlackCastleValid)
          if (currentBoard[61] == 0 && currentBoard[62] == 0)
            return true;
      }
    }

    return false;
  }

  public boolean checkValidKingMove(int sourceBlock, int destBlock,
                                    int currentPlayer)
  {
    //check if an invalid destBlock has been given
    if (destBlock<0 || destBlock>63)
      return false;

    if (!checkValidPlayerPiece(sourceBlock, destBlock, currentPlayer))
      return false;

    if (castleKing(sourceBlock, destBlock, currentPlayer))
      if (!inCheck(destBlock, currentPlayer))
        return true;

    for (int i=0; i<kingMoves[sourceBlock].length; i++)
    {
      if (kingMoves[sourceBlock][i] == destBlock)
      {
        if (currentBoard[destBlock] == 0 ||
            currentBoard[sourceBlock]*currentBoard[destBlock]<0)
        {
          int tempSource = currentBoard[sourceBlock];
          currentBoard[sourceBlock] = 0;
          int tempDest = currentBoard[destBlock];
          currentBoard[destBlock] = 0;
          //check if the move will result in being in check ie: invalid move
          if (inCheck(destBlock, currentPlayer))
          {
            currentBoard[destBlock] = tempDest;
            currentBoard[sourceBlock] = tempSource;
            return false;
          }
          currentBoard[destBlock] = tempDest;
          currentBoard[sourceBlock] = tempSource;
          return true;
        }
        else
          return false;
      }
    }
    return false;
  }

  public void setCurrentKingPos(int destBlock, int currentPlayer)
  {
    if (currentPlayer == 1)
      whiteKingPos = destBlock;
    else
      blackKingPos = destBlock;
  }

  public int getCurrentWhiteKingPos()
  {
    return whiteKingPos;
  }

  public int getCurrentBlackKingPos()
  {
    return blackKingPos;
  }

  //simulate a piece move and then check if that move is not illegal
  //ie: does not place that pieces king in check. It then moves the
  //pieces back to how they were before the tempMove and returns
  public boolean tempMoveValid(int sourceBlock, int destBlock, int piece,
                               int currentPlayer)
  {
    int tempSource = currentBoard[sourceBlock];
    currentBoard[sourceBlock] = 0;
    int tempDest = currentBoard[destBlock];
    currentBoard[destBlock] = piece;
    int kingPos;

    if (currentPlayer == 1)
      kingPos = whiteKingPos;
    else
      kingPos = blackKingPos;

    if (!inCheck(kingPos, currentPlayer))
    {
      currentBoard[sourceBlock] = tempSource;
      currentBoard[destBlock] = tempDest;
      return true;
    }

    currentBoard[sourceBlock] = tempSource;
    currentBoard[destBlock] = tempDest;
    return false;
  }

  //check if the specified move will result in the inDangerBlock no longer
  //being in danger. return true if move is a successfull defend
  public boolean pawnDefendPiece(int source, int dest, int inDangerBlock)
  {
    int tempSource = currentBoard[source];
    currentBoard[source] = 0;
    int tempDest = currentBoard[dest];
    currentBoard[dest] = -1;

    if (inCheck(inDangerBlock, 3))
    {
      currentBoard[source] = tempSource;
      currentBoard[dest] = tempDest;
      return false;
    }
    else
    {
      currentBoard[source] = tempSource;
      currentBoard[dest] = tempDest;
      return true;
    }
  }

  //determine if a specific block where the king (or other piece) is placed means
  //that it is in check or can be taken
  public boolean inCheck(int kingPos, int currentPlayer)
  {
    int num;
    if (currentPlayer == 1)
      num=-1;
    else
      num=1;
    //check the pieces on every blocks
    for (int block=0; block<64; block++)
    {
      //check all pawns
      if (currentBoard[block] == 1*num)
      {
        if (isPawnCheckingKing(kingPos, currentPlayer, block))
          return true;
      }
      //check all rooks
      if (currentBoard[block] == 2*num)
      {
        if (isRookCheckingKing(kingPos, currentPlayer, block))
          return true;
      }

      //check all knights
      if (currentBoard[block] == 3*num)
      {
        if (isKnightCheckingKing(kingPos, currentPlayer, block))
          return true;
      }

      //check all bishops
      if (currentBoard[block] == 4*num)
      {
        if (isBishopCheckingKing(kingPos, currentPlayer, block))
          return true;
      }

      //check all queens
      if (currentBoard[block] == 5*num)
      {
        if (isRookCheckingKing(kingPos, currentPlayer, block))
          return true;
        if (isBishopCheckingKing(kingPos, currentPlayer, block))
          return true;
      }

      //check the king
      if (currentBoard[block] == 6*num)
      {
        if (isKingCheckingKing(kingPos, currentPlayer, block))
          return true;
      }
    }
    return false;
  }

  private boolean isPawnCheckingKing(int destBlock, int currentPlayer,
                                     int block)
  {
    int temp;
    temp = currentBoard[destBlock];
    if (currentPlayer == 1)
    {
      currentBoard[destBlock] = 6;
      if (checkValidBlackPawnMove(block, destBlock, 2))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    else
    {
      currentBoard[destBlock] = -6;
      if (checkValidWhitePawnMove(block, destBlock, 1))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    currentBoard[destBlock] = temp;
    return false;
  }

  //checks if the destination block will be in check by any of the valid rook
  //moves
  private boolean isRookCheckingKing(int destBlock, int currentPlayer,
                                     int block)
  {
    int temp;
    temp = currentBoard[destBlock];
    currentBoard[destBlock] = 0;
    if (currentPlayer == 1)
    {
      if (checkValidRookMove(block, destBlock, 2))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    else
    {
      if (checkValidRookMove(block, destBlock, 1))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    currentBoard[destBlock] = temp;
    return false;
  }

  //checks if the destination block will be in check by any of the valid knight
  //moves
  private boolean isKnightCheckingKing(int destBlock, int currentPlayer,
                                       int block)
  {
    int temp;
    temp = currentBoard[destBlock];
    currentBoard[destBlock] = 0;
    if (currentPlayer == 1)
    {
      if (checkValidKnightMove(block, destBlock, 2))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    else
    {
      if (checkValidKnightMove(block, destBlock, 1))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    currentBoard[destBlock] = temp;
    return false;
  }

  //checks if the destination block will be in check by any of the valid rook
  //moves
  private boolean isBishopCheckingKing(int destBlock, int currentPlayer,
                                     int block)
  {
    int temp = currentBoard[destBlock];
    currentBoard[destBlock] = 0;
    if (currentPlayer == 1)
    {
      if (checkValidBishopMove(block, destBlock, 2))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    else
    {
      if (checkValidBishopMove(block, destBlock, 1))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    currentBoard[destBlock] = temp;
    return false;
  }

  //checks if the destination block will be in check by any of the valid king
  //moves
  private boolean isKingCheckingKing(int destBlock, int currentPlayer,
                                     int block)
  {
    int temp = currentBoard[destBlock];
    currentBoard[destBlock] = 0;
    if (currentPlayer == 1)
    {
      if (checkValidKingMove(block, destBlock, 2))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    else
    {
      if (checkValidKingMove(block, destBlock, 1))
      {
        currentBoard[destBlock] = temp;
        return true;
      }
    }
    currentBoard[destBlock] = temp;
    return false;
  }

  //all of the legal moves ordered by piece type
  private void initPieceMoves()
  {
    rookMoves = new int[ 64 ][][];
    rookMoves[ 0 ] = new int[ 2 ][];
    rookMoves[ 0 ][ 0 ] = new int[ 7 ];
    rookMoves[ 0 ][ 1 ] = new int[ 7 ];
    rookMoves[ 0 ][ 0 ][ 0 ] = 1;
    rookMoves[ 0 ][ 0 ][ 1 ] = 2;
    rookMoves[ 0 ][ 0 ][ 2 ] = 3;
    rookMoves[ 0 ][ 0 ][ 3 ] = 4;
    rookMoves[ 0 ][ 0 ][ 4 ] = 5;
    rookMoves[ 0 ][ 0 ][ 5 ] = 6;
    rookMoves[ 0 ][ 0 ][ 6 ] = 7;
    rookMoves[ 0 ][ 1 ][ 0 ] = 8;
    rookMoves[ 0 ][ 1 ][ 1 ] = 16;
    rookMoves[ 0 ][ 1 ][ 2 ] = 24;
    rookMoves[ 0 ][ 1 ][ 3 ] = 32;
    rookMoves[ 0 ][ 1 ][ 4 ] = 40;
    rookMoves[ 0 ][ 1 ][ 5 ] = 48;
    rookMoves[ 0 ][ 1 ][ 6 ] = 56;

    rookMoves[ 1 ] = new int[ 3 ][];
    rookMoves[ 1 ][ 0 ] = new int[ 1 ];
    rookMoves[ 1 ][ 1 ] = new int[ 6 ];
    rookMoves[ 1 ][ 2 ] = new int[ 7 ];
    rookMoves[ 1 ][ 0 ][ 0 ] = 0;
    rookMoves[ 1 ][ 1 ][ 0 ] = 2;
    rookMoves[ 1 ][ 1 ][ 1 ] = 3;
    rookMoves[ 1 ][ 1 ][ 2 ] = 4;
    rookMoves[ 1 ][ 1 ][ 3 ] = 5;
    rookMoves[ 1 ][ 1 ][ 4 ] = 6;
    rookMoves[ 1 ][ 1 ][ 5 ] = 7;
    rookMoves[ 1 ][ 2 ][ 0 ] = 9;
    rookMoves[ 1 ][ 2 ][ 1 ] = 17;
    rookMoves[ 1 ][ 2 ][ 2 ] = 25;
    rookMoves[ 1 ][ 2 ][ 3 ] = 33;
    rookMoves[ 1 ][ 2 ][ 4 ] = 41;
    rookMoves[ 1 ][ 2 ][ 5 ] = 49;
    rookMoves[ 1 ][ 2 ][ 6 ] = 57;

    rookMoves[ 2 ] = new int[ 3 ][];
    rookMoves[ 2 ][ 0 ] = new int[ 2 ];
    rookMoves[ 2 ][ 1 ] = new int[ 5 ];
    rookMoves[ 2 ][ 2 ] = new int[ 7 ];
    rookMoves[ 2 ][ 0 ][ 0 ] = 1;
    rookMoves[ 2 ][ 0 ][ 1 ] = 0;
    rookMoves[ 2 ][ 1 ][ 0 ] = 3;
    rookMoves[ 2 ][ 1 ][ 1 ] = 4;
    rookMoves[ 2 ][ 1 ][ 2 ] = 5;
    rookMoves[ 2 ][ 1 ][ 3 ] = 6;
    rookMoves[ 2 ][ 1 ][ 4 ] = 7;
    rookMoves[ 2 ][ 2 ][ 0 ] = 10;
    rookMoves[ 2 ][ 2 ][ 1 ] = 18;
    rookMoves[ 2 ][ 2 ][ 2 ] = 26;
    rookMoves[ 2 ][ 2 ][ 3 ] = 34;
    rookMoves[ 2 ][ 2 ][ 4 ] = 42;
    rookMoves[ 2 ][ 2 ][ 5 ] = 50;
    rookMoves[ 2 ][ 2 ][ 6 ] = 58;

    rookMoves[ 3 ] = new int[ 3 ][];
    rookMoves[ 3 ][ 0 ] = new int[ 3 ];
    rookMoves[ 3 ][ 1 ] = new int[ 4 ];
    rookMoves[ 3 ][ 2 ] = new int[ 7 ];
    rookMoves[ 3 ][ 0 ][ 0 ] = 2;
    rookMoves[ 3 ][ 0 ][ 1 ] = 1;
    rookMoves[ 3 ][ 0 ][ 2 ] = 0;
    rookMoves[ 3 ][ 1 ][ 0 ] = 4;
    rookMoves[ 3 ][ 1 ][ 1 ] = 5;
    rookMoves[ 3 ][ 1 ][ 2 ] = 6;
    rookMoves[ 3 ][ 1 ][ 3 ] = 7;
    rookMoves[ 3 ][ 2 ][ 0 ] = 11;
    rookMoves[ 3 ][ 2 ][ 1 ] = 19;
    rookMoves[ 3 ][ 2 ][ 2 ] = 27;
    rookMoves[ 3 ][ 2 ][ 3 ] = 35;
    rookMoves[ 3 ][ 2 ][ 4 ] = 43;
    rookMoves[ 3 ][ 2 ][ 5 ] = 51;
    rookMoves[ 3 ][ 2 ][ 6 ] = 59;

    rookMoves[ 4 ] = new int[ 3 ][];
    rookMoves[ 4 ][ 0 ] = new int[ 4 ];
    rookMoves[ 4 ][ 1 ] = new int[ 3 ];
    rookMoves[ 4 ][ 2 ] = new int[ 7 ];
    rookMoves[ 4 ][ 0 ][ 0 ] = 3;
    rookMoves[ 4 ][ 0 ][ 1 ] = 2;
    rookMoves[ 4 ][ 0 ][ 2 ] = 1;
    rookMoves[ 4 ][ 0 ][ 3 ] = 0;
    rookMoves[ 4 ][ 1 ][ 0 ] = 5;
    rookMoves[ 4 ][ 1 ][ 1 ] = 6;
    rookMoves[ 4 ][ 1 ][ 2 ] = 7;
    rookMoves[ 4 ][ 2 ][ 0 ] = 12;
    rookMoves[ 4 ][ 2 ][ 1 ] = 20;
    rookMoves[ 4 ][ 2 ][ 2 ] = 28;
    rookMoves[ 4 ][ 2 ][ 3 ] = 36;
    rookMoves[ 4 ][ 2 ][ 4 ] = 44;
    rookMoves[ 4 ][ 2 ][ 5 ] = 52;
    rookMoves[ 4 ][ 2 ][ 6 ] = 60;

    rookMoves[ 5 ] = new int[ 3 ][];
    rookMoves[ 5 ][ 0 ] = new int[ 5 ];
    rookMoves[ 5 ][ 1 ] = new int[ 2 ];
    rookMoves[ 5 ][ 2 ] = new int[ 7 ];
    rookMoves[ 5 ][ 0 ][ 0 ] = 4;
    rookMoves[ 5 ][ 0 ][ 1 ] = 3;
    rookMoves[ 5 ][ 0 ][ 2 ] = 2;
    rookMoves[ 5 ][ 0 ][ 3 ] = 1;
    rookMoves[ 5 ][ 0 ][ 4 ] = 0;
    rookMoves[ 5 ][ 1 ][ 0 ] = 6;
    rookMoves[ 5 ][ 1 ][ 1 ] = 7;
    rookMoves[ 5 ][ 2 ][ 0 ] = 13;
    rookMoves[ 5 ][ 2 ][ 1 ] = 21;
    rookMoves[ 5 ][ 2 ][ 2 ] = 29;
    rookMoves[ 5 ][ 2 ][ 3 ] = 37;
    rookMoves[ 5 ][ 2 ][ 4 ] = 45;
    rookMoves[ 5 ][ 2 ][ 5 ] = 53;
    rookMoves[ 5 ][ 2 ][ 6 ] = 61;

    rookMoves[ 6 ] = new int[ 3 ][];
    rookMoves[ 6 ][ 0 ] = new int[ 6 ];
    rookMoves[ 6 ][ 1 ] = new int[ 1 ];
    rookMoves[ 6 ][ 2 ] = new int[ 7 ];
    rookMoves[ 6 ][ 0 ][ 0 ] = 5;
    rookMoves[ 6 ][ 0 ][ 1 ] = 4;
    rookMoves[ 6 ][ 0 ][ 2 ] = 3;
    rookMoves[ 6 ][ 0 ][ 3 ] = 2;
    rookMoves[ 6 ][ 0 ][ 4 ] = 1;
    rookMoves[ 6 ][ 0 ][ 5 ] = 0;
    rookMoves[ 6 ][ 1 ][ 0 ] = 7;
    rookMoves[ 6 ][ 2 ][ 0 ] = 14;
    rookMoves[ 6 ][ 2 ][ 1 ] = 22;
    rookMoves[ 6 ][ 2 ][ 2 ] = 30;
    rookMoves[ 6 ][ 2 ][ 3 ] = 38;
    rookMoves[ 6 ][ 2 ][ 4 ] = 46;
    rookMoves[ 6 ][ 2 ][ 5 ] = 54;
    rookMoves[ 6 ][ 2 ][ 6 ] = 62;

    rookMoves[ 7 ] = new int[ 2 ][];
    rookMoves[ 7 ][ 0 ] = new int[ 7 ];
    rookMoves[ 7 ][ 1 ] = new int[ 7 ];
    rookMoves[ 7 ][ 0 ][ 0 ] = 6;
    rookMoves[ 7 ][ 0 ][ 1 ] = 5;
    rookMoves[ 7 ][ 0 ][ 2 ] = 4;
    rookMoves[ 7 ][ 0 ][ 3 ] = 3;
    rookMoves[ 7 ][ 0 ][ 4 ] = 2;
    rookMoves[ 7 ][ 0 ][ 5 ] = 1;
    rookMoves[ 7 ][ 0 ][ 6 ] = 0;
    rookMoves[ 7 ][ 1 ][ 0 ] = 15;
    rookMoves[ 7 ][ 1 ][ 1 ] = 23;
    rookMoves[ 7 ][ 1 ][ 2 ] = 31;
    rookMoves[ 7 ][ 1 ][ 3 ] = 39;
    rookMoves[ 7 ][ 1 ][ 4 ] = 47;
    rookMoves[ 7 ][ 1 ][ 5 ] = 55;
    rookMoves[ 7 ][ 1 ][ 6 ] = 63;

    rookMoves[ 8 ] = new int[ 3 ][];
    rookMoves[ 8 ][ 0 ] = new int[ 7 ];
    rookMoves[ 8 ][ 1 ] = new int[ 1 ];
    rookMoves[ 8 ][ 2 ] = new int[ 6 ];
    rookMoves[ 8 ][ 0 ][ 0 ] = 9;
    rookMoves[ 8 ][ 0 ][ 1 ] = 10;
    rookMoves[ 8 ][ 0 ][ 2 ] = 11;
    rookMoves[ 8 ][ 0 ][ 3 ] = 12;
    rookMoves[ 8 ][ 0 ][ 4 ] = 13;
    rookMoves[ 8 ][ 0 ][ 5 ] = 14;
    rookMoves[ 8 ][ 0 ][ 6 ] = 15;
    rookMoves[ 8 ][ 1 ][ 0 ] = 0;
    rookMoves[ 8 ][ 2 ][ 0 ] = 16;
    rookMoves[ 8 ][ 2 ][ 1 ] = 24;
    rookMoves[ 8 ][ 2 ][ 2 ] = 32;
    rookMoves[ 8 ][ 2 ][ 3 ] = 40;
    rookMoves[ 8 ][ 2 ][ 4 ] = 48;
    rookMoves[ 8 ][ 2 ][ 5 ] = 56;

    rookMoves[ 9 ] = new int[ 4 ][];
    rookMoves[ 9 ][ 0 ] = new int[ 1 ];
    rookMoves[ 9 ][ 1 ] = new int[ 6 ];
    rookMoves[ 9 ][ 2 ] = new int[ 1 ];
    rookMoves[ 9 ][ 3 ] = new int[ 6 ];
    rookMoves[ 9 ][ 0 ][ 0 ] = 8;
    rookMoves[ 9 ][ 1 ][ 0 ] = 10;
    rookMoves[ 9 ][ 1 ][ 1 ] = 11;
    rookMoves[ 9 ][ 1 ][ 2 ] = 12;
    rookMoves[ 9 ][ 1 ][ 3 ] = 13;
    rookMoves[ 9 ][ 1 ][ 4 ] = 14;
    rookMoves[ 9 ][ 1 ][ 5 ] = 15;
    rookMoves[ 9 ][ 2 ][ 0 ] = 1;
    rookMoves[ 9 ][ 3 ][ 0 ] = 17;
    rookMoves[ 9 ][ 3 ][ 1 ] = 25;
    rookMoves[ 9 ][ 3 ][ 2 ] = 33;
    rookMoves[ 9 ][ 3 ][ 3 ] = 41;
    rookMoves[ 9 ][ 3 ][ 4 ] = 49;
    rookMoves[ 9 ][ 3 ][ 5 ] = 57;

    rookMoves[ 10 ] = new int[ 4 ][];
    rookMoves[ 10 ][ 0 ] = new int[ 2 ];
    rookMoves[ 10 ][ 1 ] = new int[ 5 ];
    rookMoves[ 10 ][ 2 ] = new int[ 1 ];
    rookMoves[ 10 ][ 3 ] = new int[ 6 ];
    rookMoves[ 10 ][ 0 ][ 0 ] = 9;
    rookMoves[ 10 ][ 0 ][ 1 ] = 8;
    rookMoves[ 10 ][ 1 ][ 0 ] = 11;
    rookMoves[ 10 ][ 1 ][ 1 ] = 12;
    rookMoves[ 10 ][ 1 ][ 2 ] = 13;
    rookMoves[ 10 ][ 1 ][ 3 ] = 14;
    rookMoves[ 10 ][ 1 ][ 4 ] = 15;
    rookMoves[ 10 ][ 2 ][ 0 ] = 2;
    rookMoves[ 10 ][ 3 ][ 0 ] = 18;
    rookMoves[ 10 ][ 3 ][ 1 ] = 26;
    rookMoves[ 10 ][ 3 ][ 2 ] = 34;
    rookMoves[ 10 ][ 3 ][ 3 ] = 42;
    rookMoves[ 10 ][ 3 ][ 4 ] = 50;
    rookMoves[ 10 ][ 3 ][ 5 ] = 58;

    rookMoves[ 11 ] = new int[ 4 ][];
    rookMoves[ 11 ][ 0 ] = new int[ 3 ];
    rookMoves[ 11 ][ 1 ] = new int[ 4 ];
    rookMoves[ 11 ][ 2 ] = new int[ 1 ];
    rookMoves[ 11 ][ 3 ] = new int[ 6 ];
    rookMoves[ 11 ][ 0 ][ 0 ] = 10;
    rookMoves[ 11 ][ 0 ][ 1 ] = 9;
    rookMoves[ 11 ][ 0 ][ 2 ] = 8;
    rookMoves[ 11 ][ 1 ][ 0 ] = 12;
    rookMoves[ 11 ][ 1 ][ 1 ] = 13;
    rookMoves[ 11 ][ 1 ][ 2 ] = 14;
    rookMoves[ 11 ][ 1 ][ 3 ] = 15;
    rookMoves[ 11 ][ 2 ][ 0 ] = 3;
    rookMoves[ 11 ][ 3 ][ 0 ] = 19;
    rookMoves[ 11 ][ 3 ][ 1 ] = 27;
    rookMoves[ 11 ][ 3 ][ 2 ] = 35;
    rookMoves[ 11 ][ 3 ][ 3 ] = 43;
    rookMoves[ 11 ][ 3 ][ 4 ] = 51;
    rookMoves[ 11 ][ 3 ][ 5 ] = 59;

    rookMoves[ 12 ] = new int[ 4 ][];
    rookMoves[ 12 ][ 0 ] = new int[ 4 ];
    rookMoves[ 12 ][ 1 ] = new int[ 3 ];
    rookMoves[ 12 ][ 2 ] = new int[ 1 ];
    rookMoves[ 12 ][ 3 ] = new int[ 6 ];
    rookMoves[ 12 ][ 0 ][ 0 ] = 11;
    rookMoves[ 12 ][ 0 ][ 1 ] = 10;
    rookMoves[ 12 ][ 0 ][ 2 ] = 9;
    rookMoves[ 12 ][ 0 ][ 3 ] = 8;
    rookMoves[ 12 ][ 1 ][ 0 ] = 13;
    rookMoves[ 12 ][ 1 ][ 1 ] = 14;
    rookMoves[ 12 ][ 1 ][ 2 ] = 15;
    rookMoves[ 12 ][ 2 ][ 0 ] = 4;
    rookMoves[ 12 ][ 3 ][ 0 ] = 20;
    rookMoves[ 12 ][ 3 ][ 1 ] = 28;
    rookMoves[ 12 ][ 3 ][ 2 ] = 36;
    rookMoves[ 12 ][ 3 ][ 3 ] = 44;
    rookMoves[ 12 ][ 3 ][ 4 ] = 52;
    rookMoves[ 12 ][ 3 ][ 5 ] = 60;

    rookMoves[ 13 ] = new int[ 4 ][];
    rookMoves[ 13 ][ 0 ] = new int[ 5 ];
    rookMoves[ 13 ][ 1 ] = new int[ 2 ];
    rookMoves[ 13 ][ 2 ] = new int[ 1 ];
    rookMoves[ 13 ][ 3 ] = new int[ 6 ];
    rookMoves[ 13 ][ 0 ][ 0 ] = 12;
    rookMoves[ 13 ][ 0 ][ 1 ] = 11;
    rookMoves[ 13 ][ 0 ][ 2 ] = 10;
    rookMoves[ 13 ][ 0 ][ 3 ] = 9;
    rookMoves[ 13 ][ 0 ][ 4 ] = 8;
    rookMoves[ 13 ][ 1 ][ 0 ] = 14;
    rookMoves[ 13 ][ 1 ][ 1 ] = 15;
    rookMoves[ 13 ][ 2 ][ 0 ] = 5;
    rookMoves[ 13 ][ 3 ][ 0 ] = 21;
    rookMoves[ 13 ][ 3 ][ 1 ] = 29;
    rookMoves[ 13 ][ 3 ][ 2 ] = 37;
    rookMoves[ 13 ][ 3 ][ 3 ] = 45;
    rookMoves[ 13 ][ 3 ][ 4 ] = 53;
    rookMoves[ 13 ][ 3 ][ 5 ] = 61;

    rookMoves[ 14 ] = new int[ 4 ][];
    rookMoves[ 14 ][ 0 ] = new int[ 6 ];
    rookMoves[ 14 ][ 1 ] = new int[ 1 ];
    rookMoves[ 14 ][ 2 ] = new int[ 1 ];
    rookMoves[ 14 ][ 3 ] = new int[ 6 ];
    rookMoves[ 14 ][ 0 ][ 0 ] = 13;
    rookMoves[ 14 ][ 0 ][ 1 ] = 12;
    rookMoves[ 14 ][ 0 ][ 2 ] = 11;
    rookMoves[ 14 ][ 0 ][ 3 ] = 10;
    rookMoves[ 14 ][ 0 ][ 4 ] = 9;
    rookMoves[ 14 ][ 0 ][ 5 ] = 8;
    rookMoves[ 14 ][ 1 ][ 0 ] = 15;
    rookMoves[ 14 ][ 2 ][ 0 ] = 6;
    rookMoves[ 14 ][ 3 ][ 0 ] = 22;
    rookMoves[ 14 ][ 3 ][ 1 ] = 30;
    rookMoves[ 14 ][ 3 ][ 2 ] = 38;
    rookMoves[ 14 ][ 3 ][ 3 ] = 46;
    rookMoves[ 14 ][ 3 ][ 4 ] = 54;
    rookMoves[ 14 ][ 3 ][ 5 ] = 62;

    rookMoves[ 15 ] = new int[ 3 ][];
    rookMoves[ 15 ][ 0 ] = new int[ 7 ];
    rookMoves[ 15 ][ 1 ] = new int[ 1 ];
    rookMoves[ 15 ][ 2 ] = new int[ 6 ];
    rookMoves[ 15 ][ 0 ][ 0 ] = 14;
    rookMoves[ 15 ][ 0 ][ 1 ] = 13;
    rookMoves[ 15 ][ 0 ][ 2 ] = 12;
    rookMoves[ 15 ][ 0 ][ 3 ] = 11;
    rookMoves[ 15 ][ 0 ][ 4 ] = 10;
    rookMoves[ 15 ][ 0 ][ 5 ] = 9;
    rookMoves[ 15 ][ 0 ][ 6 ] = 8;
    rookMoves[ 15 ][ 1 ][ 0 ] = 7;
    rookMoves[ 15 ][ 2 ][ 0 ] = 23;
    rookMoves[ 15 ][ 2 ][ 1 ] = 31;
    rookMoves[ 15 ][ 2 ][ 2 ] = 39;
    rookMoves[ 15 ][ 2 ][ 3 ] = 47;
    rookMoves[ 15 ][ 2 ][ 4 ] = 55;
    rookMoves[ 15 ][ 2 ][ 5 ] = 63;

    rookMoves[ 16 ] = new int[ 3 ][];
    rookMoves[ 16 ][ 0 ] = new int[ 2 ];
    rookMoves[ 16 ][ 1 ] = new int[ 5 ];
    rookMoves[ 16 ][ 2 ] = new int[ 7 ];
    rookMoves[ 16 ][ 0 ][ 0 ] = 8;
    rookMoves[ 16 ][ 0 ][ 1 ] = 0;
    rookMoves[ 16 ][ 1 ][ 0 ] = 24;
    rookMoves[ 16 ][ 1 ][ 1 ] = 32;
    rookMoves[ 16 ][ 1 ][ 2 ] = 40;
    rookMoves[ 16 ][ 1 ][ 3 ] = 48;
    rookMoves[ 16 ][ 1 ][ 4 ] = 56;
    rookMoves[ 16 ][ 2 ][ 0 ] = 17;
    rookMoves[ 16 ][ 2 ][ 1 ] = 18;
    rookMoves[ 16 ][ 2 ][ 2 ] = 19;
    rookMoves[ 16 ][ 2 ][ 3 ] = 20;
    rookMoves[ 16 ][ 2 ][ 4 ] = 21;
    rookMoves[ 16 ][ 2 ][ 5 ] = 22;
    rookMoves[ 16 ][ 2 ][ 6 ] = 23;

    rookMoves[ 17 ] = new int[ 4 ][];
    rookMoves[ 17 ][ 0 ] = new int[ 1 ];
    rookMoves[ 17 ][ 1 ] = new int[ 6 ];
    rookMoves[ 17 ][ 2 ] = new int[ 2 ];
    rookMoves[ 17 ][ 3 ] = new int[ 5 ];
    rookMoves[ 17 ][ 0 ][ 0 ] = 16;
    rookMoves[ 17 ][ 1 ][ 0 ] = 18;
    rookMoves[ 17 ][ 1 ][ 1 ] = 19;
    rookMoves[ 17 ][ 1 ][ 2 ] = 20;
    rookMoves[ 17 ][ 1 ][ 3 ] = 21;
    rookMoves[ 17 ][ 1 ][ 4 ] = 22;
    rookMoves[ 17 ][ 1 ][ 5 ] = 23;
    rookMoves[ 17 ][ 2 ][ 0 ] = 9;
    rookMoves[ 17 ][ 2 ][ 1 ] = 1;
    rookMoves[ 17 ][ 3 ][ 0 ] = 25;
    rookMoves[ 17 ][ 3 ][ 1 ] = 33;
    rookMoves[ 17 ][ 3 ][ 2 ] = 41;
    rookMoves[ 17 ][ 3 ][ 3 ] = 49;
    rookMoves[ 17 ][ 3 ][ 4 ] = 57;

    rookMoves[ 18 ] = new int[ 4 ][];
    rookMoves[ 18 ][ 0 ] = new int[ 2 ];
    rookMoves[ 18 ][ 1 ] = new int[ 5 ];
    rookMoves[ 18 ][ 2 ] = new int[ 2 ];
    rookMoves[ 18 ][ 3 ] = new int[ 5 ];
    rookMoves[ 18 ][ 0 ][ 0 ] = 17;
    rookMoves[ 18 ][ 0 ][ 1 ] = 16;
    rookMoves[ 18 ][ 1 ][ 0 ] = 19;
    rookMoves[ 18 ][ 1 ][ 1 ] = 20;
    rookMoves[ 18 ][ 1 ][ 2 ] = 21;
    rookMoves[ 18 ][ 1 ][ 3 ] = 22;
    rookMoves[ 18 ][ 1 ][ 4 ] = 23;
    rookMoves[ 18 ][ 2 ][ 0 ] = 10;
    rookMoves[ 18 ][ 2 ][ 1 ] = 2;
    rookMoves[ 18 ][ 3 ][ 0 ] = 26;
    rookMoves[ 18 ][ 3 ][ 1 ] = 34;
    rookMoves[ 18 ][ 3 ][ 2 ] = 42;
    rookMoves[ 18 ][ 3 ][ 3 ] = 50;
    rookMoves[ 18 ][ 3 ][ 4 ] = 58;

    rookMoves[ 19 ] = new int[ 4 ][];
    rookMoves[ 19 ][ 0 ] = new int[ 3 ];
    rookMoves[ 19 ][ 1 ] = new int[ 4 ];
    rookMoves[ 19 ][ 2 ] = new int[ 2 ];
    rookMoves[ 19 ][ 3 ] = new int[ 5 ];
    rookMoves[ 19 ][ 0 ][ 0 ] = 18;
    rookMoves[ 19 ][ 0 ][ 1 ] = 17;
    rookMoves[ 19 ][ 0 ][ 2 ] = 16;
    rookMoves[ 19 ][ 1 ][ 0 ] = 20;
    rookMoves[ 19 ][ 1 ][ 1 ] = 21;
    rookMoves[ 19 ][ 1 ][ 2 ] = 22;
    rookMoves[ 19 ][ 1 ][ 3 ] = 23;
    rookMoves[ 19 ][ 2 ][ 0 ] = 11;
    rookMoves[ 19 ][ 2 ][ 1 ] = 3;
    rookMoves[ 19 ][ 3 ][ 0 ] = 27;
    rookMoves[ 19 ][ 3 ][ 1 ] = 35;
    rookMoves[ 19 ][ 3 ][ 2 ] = 43;
    rookMoves[ 19 ][ 3 ][ 3 ] = 51;
    rookMoves[ 19 ][ 3 ][ 4 ] = 59;

    rookMoves[ 20 ] = new int[ 4 ][];
    rookMoves[ 20 ][ 0 ] = new int[ 4 ];
    rookMoves[ 20 ][ 1 ] = new int[ 3 ];
    rookMoves[ 20 ][ 2 ] = new int[ 2 ];
    rookMoves[ 20 ][ 3 ] = new int[ 5 ];
    rookMoves[ 20 ][ 0 ][ 0 ] = 19;
    rookMoves[ 20 ][ 0 ][ 1 ] = 18;
    rookMoves[ 20 ][ 0 ][ 2 ] = 17;
    rookMoves[ 20 ][ 0 ][ 3 ] = 16;
    rookMoves[ 20 ][ 1 ][ 0 ] = 21;
    rookMoves[ 20 ][ 1 ][ 1 ] = 22;
    rookMoves[ 20 ][ 1 ][ 2 ] = 23;
    rookMoves[ 20 ][ 2 ][ 0 ] = 12;
    rookMoves[ 20 ][ 2 ][ 1 ] = 4;
    rookMoves[ 20 ][ 3 ][ 0 ] = 28;
    rookMoves[ 20 ][ 3 ][ 1 ] = 36;
    rookMoves[ 20 ][ 3 ][ 2 ] = 44;
    rookMoves[ 20 ][ 3 ][ 3 ] = 52;
    rookMoves[ 20 ][ 3 ][ 4 ] = 60;

    rookMoves[ 21 ] = new int[ 4 ][];
    rookMoves[ 21 ][ 0 ] = new int[ 5 ];
    rookMoves[ 21 ][ 1 ] = new int[ 2 ];
    rookMoves[ 21 ][ 2 ] = new int[ 2 ];
    rookMoves[ 21 ][ 3 ] = new int[ 5 ];
    rookMoves[ 21 ][ 0 ][ 0 ] = 20;
    rookMoves[ 21 ][ 0 ][ 1 ] = 19;
    rookMoves[ 21 ][ 0 ][ 2 ] = 18;
    rookMoves[ 21 ][ 0 ][ 3 ] = 17;
    rookMoves[ 21 ][ 0 ][ 4 ] = 16;
    rookMoves[ 21 ][ 1 ][ 0 ] = 22;
    rookMoves[ 21 ][ 1 ][ 1 ] = 23;
    rookMoves[ 21 ][ 2 ][ 0 ] = 13;
    rookMoves[ 21 ][ 2 ][ 1 ] = 5;
    rookMoves[ 21 ][ 3 ][ 0 ] = 29;
    rookMoves[ 21 ][ 3 ][ 1 ] = 37;
    rookMoves[ 21 ][ 3 ][ 2 ] = 45;
    rookMoves[ 21 ][ 3 ][ 3 ] = 53;
    rookMoves[ 21 ][ 3 ][ 4 ] = 61;

    rookMoves[ 22 ] = new int[ 4 ][];
    rookMoves[ 22 ][ 0 ] = new int[ 6 ];
    rookMoves[ 22 ][ 1 ] = new int[ 1 ];
    rookMoves[ 22 ][ 2 ] = new int[ 2 ];
    rookMoves[ 22 ][ 3 ] = new int[ 5 ];
    rookMoves[ 22 ][ 0 ][ 0 ] = 21;
    rookMoves[ 22 ][ 0 ][ 1 ] = 20;
    rookMoves[ 22 ][ 0 ][ 2 ] = 19;
    rookMoves[ 22 ][ 0 ][ 3 ] = 18;
    rookMoves[ 22 ][ 0 ][ 4 ] = 17;
    rookMoves[ 22 ][ 0 ][ 5 ] = 16;
    rookMoves[ 22 ][ 1 ][ 0 ] = 23;
    rookMoves[ 22 ][ 2 ][ 0 ] = 14;
    rookMoves[ 22 ][ 2 ][ 1 ] = 6;
    rookMoves[ 22 ][ 3 ][ 0 ] = 30;
    rookMoves[ 22 ][ 3 ][ 1 ] = 38;
    rookMoves[ 22 ][ 3 ][ 2 ] = 46;
    rookMoves[ 22 ][ 3 ][ 3 ] = 54;
    rookMoves[ 22 ][ 3 ][ 4 ] = 62;

    rookMoves[ 23 ] = new int[ 3 ][];
    rookMoves[ 23 ][ 0 ] = new int[ 7 ];
    rookMoves[ 23 ][ 1 ] = new int[ 2 ];
    rookMoves[ 23 ][ 2 ] = new int[ 5 ];
    rookMoves[ 23 ][ 0 ][ 0 ] = 22;
    rookMoves[ 23 ][ 0 ][ 1 ] = 21;
    rookMoves[ 23 ][ 0 ][ 2 ] = 20;
    rookMoves[ 23 ][ 0 ][ 3 ] = 19;
    rookMoves[ 23 ][ 0 ][ 4 ] = 18;
    rookMoves[ 23 ][ 0 ][ 5 ] = 17;
    rookMoves[ 23 ][ 0 ][ 6 ] = 16;
    rookMoves[ 23 ][ 1 ][ 0 ] = 15;
    rookMoves[ 23 ][ 1 ][ 1 ] = 7;
    rookMoves[ 23 ][ 2 ][ 0 ] = 31;
    rookMoves[ 23 ][ 2 ][ 1 ] = 39;
    rookMoves[ 23 ][ 2 ][ 2 ] = 47;
    rookMoves[ 23 ][ 2 ][ 3 ] = 55;
    rookMoves[ 23 ][ 2 ][ 4 ] = 63;

    rookMoves[ 24 ] = new int[ 3 ][];
    rookMoves[ 24 ][ 0 ] = new int[ 3 ];
    rookMoves[ 24 ][ 1 ] = new int[ 4 ];
    rookMoves[ 24 ][ 2 ] = new int[ 7 ];
    rookMoves[ 24 ][ 0 ][ 0 ] = 16;
    rookMoves[ 24 ][ 0 ][ 1 ] = 8;
    rookMoves[ 24 ][ 0 ][ 2 ] = 8;
    rookMoves[ 24 ][ 1 ][ 0 ] = 32;
    rookMoves[ 24 ][ 1 ][ 1 ] = 40;
    rookMoves[ 24 ][ 1 ][ 2 ] = 48;
    rookMoves[ 24 ][ 1 ][ 3 ] = 56;
    rookMoves[ 24 ][ 2 ][ 0 ] = 25;
    rookMoves[ 24 ][ 2 ][ 1 ] = 26;
    rookMoves[ 24 ][ 2 ][ 2 ] = 27;
    rookMoves[ 24 ][ 2 ][ 3 ] = 28;
    rookMoves[ 24 ][ 2 ][ 4 ] = 29;
    rookMoves[ 24 ][ 2 ][ 5 ] = 30;
    rookMoves[ 24 ][ 2 ][ 6 ] = 31;

    rookMoves[ 25 ] = new int[ 4 ][];
    rookMoves[ 25 ][ 0 ] = new int[ 1 ];
    rookMoves[ 25 ][ 1 ] = new int[ 6 ];
    rookMoves[ 25 ][ 2 ] = new int[ 3 ];
    rookMoves[ 25 ][ 3 ] = new int[ 4 ];
    rookMoves[ 25 ][ 0 ][ 0 ] = 24;
    rookMoves[ 25 ][ 1 ][ 0 ] = 26;
    rookMoves[ 25 ][ 1 ][ 1 ] = 27;
    rookMoves[ 25 ][ 1 ][ 2 ] = 28;
    rookMoves[ 25 ][ 1 ][ 3 ] = 29;
    rookMoves[ 25 ][ 1 ][ 4 ] = 30;
    rookMoves[ 25 ][ 1 ][ 5 ] = 31;
    rookMoves[ 25 ][ 2 ][ 0 ] = 17;
    rookMoves[ 25 ][ 2 ][ 1 ] = 9;
    rookMoves[ 25 ][ 2 ][ 2 ] = 1;
    rookMoves[ 25 ][ 3 ][ 0 ] = 33;
    rookMoves[ 25 ][ 3 ][ 1 ] = 41;
    rookMoves[ 25 ][ 3 ][ 2 ] = 49;
    rookMoves[ 25 ][ 3 ][ 3 ] = 57;

    rookMoves[ 26 ] = new int[ 4 ][];
    rookMoves[ 26 ][ 0 ] = new int[ 2 ];
    rookMoves[ 26 ][ 1 ] = new int[ 5 ];
    rookMoves[ 26 ][ 2 ] = new int[ 3 ];
    rookMoves[ 26 ][ 3 ] = new int[ 4 ];
    rookMoves[ 26 ][ 0 ][ 0 ] = 25;
    rookMoves[ 26 ][ 0 ][ 1 ] = 24;
    rookMoves[ 26 ][ 1 ][ 0 ] = 27;
    rookMoves[ 26 ][ 1 ][ 1 ] = 28;
    rookMoves[ 26 ][ 1 ][ 2 ] = 29;
    rookMoves[ 26 ][ 1 ][ 3 ] = 30;
    rookMoves[ 26 ][ 1 ][ 4 ] = 31;
    rookMoves[ 26 ][ 2 ][ 0 ] = 18;
    rookMoves[ 26 ][ 2 ][ 1 ] = 10;
    rookMoves[ 26 ][ 2 ][ 2 ] = 2;
    rookMoves[ 26 ][ 3 ][ 0 ] = 34;
    rookMoves[ 26 ][ 3 ][ 1 ] = 42;
    rookMoves[ 26 ][ 3 ][ 2 ] = 50;
    rookMoves[ 26 ][ 3 ][ 3 ] = 58;

    rookMoves[ 27 ] = new int[ 4 ][];
    rookMoves[ 27 ][ 0 ] = new int[ 3 ];
    rookMoves[ 27 ][ 1 ] = new int[ 4 ];
    rookMoves[ 27 ][ 2 ] = new int[ 3 ];
    rookMoves[ 27 ][ 3 ] = new int[ 4 ];
    rookMoves[ 27 ][ 0 ][ 0 ] = 26;
    rookMoves[ 27 ][ 0 ][ 1 ] = 25;
    rookMoves[ 27 ][ 0 ][ 2 ] = 24;
    rookMoves[ 27 ][ 1 ][ 0 ] = 28;
    rookMoves[ 27 ][ 1 ][ 1 ] = 29;
    rookMoves[ 27 ][ 1 ][ 2 ] = 30;
    rookMoves[ 27 ][ 1 ][ 3 ] = 31;
    rookMoves[ 27 ][ 2 ][ 0 ] = 19;
    rookMoves[ 27 ][ 2 ][ 1 ] = 11;
    rookMoves[ 27 ][ 2 ][ 2 ] = 3;
    rookMoves[ 27 ][ 3 ][ 0 ] = 35;
    rookMoves[ 27 ][ 3 ][ 1 ] = 43;
    rookMoves[ 27 ][ 3 ][ 2 ] = 51;
    rookMoves[ 27 ][ 3 ][ 3 ] = 59;

    rookMoves[ 28 ] = new int[ 4 ][];
    rookMoves[ 28 ][ 0 ] = new int[ 4 ];
    rookMoves[ 28 ][ 1 ] = new int[ 3 ];
    rookMoves[ 28 ][ 2 ] = new int[ 3 ];
    rookMoves[ 28 ][ 3 ] = new int[ 4 ];
    rookMoves[ 28 ][ 0 ][ 0 ] = 27;
    rookMoves[ 28 ][ 0 ][ 1 ] = 26;
    rookMoves[ 28 ][ 0 ][ 2 ] = 25;
    rookMoves[ 28 ][ 0 ][ 3 ] = 24;
    rookMoves[ 28 ][ 1 ][ 0 ] = 29;
    rookMoves[ 28 ][ 1 ][ 1 ] = 30;
    rookMoves[ 28 ][ 1 ][ 2 ] = 31;
    rookMoves[ 28 ][ 2 ][ 0 ] = 20;
    rookMoves[ 28 ][ 2 ][ 1 ] = 12;
    rookMoves[ 28 ][ 2 ][ 2 ] = 4;
    rookMoves[ 28 ][ 3 ][ 0 ] = 36;
    rookMoves[ 28 ][ 3 ][ 1 ] = 44;
    rookMoves[ 28 ][ 3 ][ 2 ] = 52;
    rookMoves[ 28 ][ 3 ][ 3 ] = 60;

    rookMoves[ 29 ] = new int[ 4 ][];
    rookMoves[ 29 ][ 0 ] = new int[ 5 ];
    rookMoves[ 29 ][ 1 ] = new int[ 2 ];
    rookMoves[ 29 ][ 2 ] = new int[ 3 ];
    rookMoves[ 29 ][ 3 ] = new int[ 4 ];
    rookMoves[ 29 ][ 0 ][ 0 ] = 28;
    rookMoves[ 29 ][ 0 ][ 1 ] = 27;
    rookMoves[ 29 ][ 0 ][ 2 ] = 26;
    rookMoves[ 29 ][ 0 ][ 3 ] = 25;
    rookMoves[ 29 ][ 0 ][ 4 ] = 24;
    rookMoves[ 29 ][ 1 ][ 0 ] = 30;
    rookMoves[ 29 ][ 1 ][ 1 ] = 31;
    rookMoves[ 29 ][ 2 ][ 0 ] = 21;
    rookMoves[ 29 ][ 2 ][ 1 ] = 13;
    rookMoves[ 29 ][ 2 ][ 2 ] = 5;
    rookMoves[ 29 ][ 3 ][ 0 ] = 37;
    rookMoves[ 29 ][ 3 ][ 1 ] = 45;
    rookMoves[ 29 ][ 3 ][ 2 ] = 53;
    rookMoves[ 29 ][ 3 ][ 3 ] = 61;

    rookMoves[ 30 ] = new int[ 4 ][];
    rookMoves[ 30 ][ 0 ] = new int[ 6 ];
    rookMoves[ 30 ][ 1 ] = new int[ 1 ];
    rookMoves[ 30 ][ 2 ] = new int[ 3 ];
    rookMoves[ 30 ][ 3 ] = new int[ 4 ];
    rookMoves[ 30 ][ 0 ][ 0 ] = 29;
    rookMoves[ 30 ][ 0 ][ 1 ] = 28;
    rookMoves[ 30 ][ 0 ][ 2 ] = 27;
    rookMoves[ 30 ][ 0 ][ 3 ] = 26;
    rookMoves[ 30 ][ 0 ][ 4 ] = 25;
    rookMoves[ 30 ][ 0 ][ 5 ] = 24;
    rookMoves[ 30 ][ 1 ][ 0 ] = 31;
    rookMoves[ 30 ][ 2 ][ 0 ] = 22;
    rookMoves[ 30 ][ 2 ][ 1 ] = 14;
    rookMoves[ 30 ][ 2 ][ 2 ] = 6;
    rookMoves[ 30 ][ 3 ][ 0 ] = 38;
    rookMoves[ 30 ][ 3 ][ 1 ] = 46;
    rookMoves[ 30 ][ 3 ][ 2 ] = 54;
    rookMoves[ 30 ][ 3 ][ 3 ] = 62;

    rookMoves[ 31 ] = new int[ 3 ][];
    rookMoves[ 31 ][ 0 ] = new int[ 7 ];
    rookMoves[ 31 ][ 1 ] = new int[ 3 ];
    rookMoves[ 31 ][ 2 ] = new int[ 4 ];
    rookMoves[ 31 ][ 0 ][ 0 ] = 30;
    rookMoves[ 31 ][ 0 ][ 1 ] = 29;
    rookMoves[ 31 ][ 0 ][ 2 ] = 28;
    rookMoves[ 31 ][ 0 ][ 3 ] = 27;
    rookMoves[ 31 ][ 0 ][ 4 ] = 26;
    rookMoves[ 31 ][ 0 ][ 5 ] = 25;
    rookMoves[ 31 ][ 0 ][ 6 ] = 24;
    rookMoves[ 31 ][ 1 ][ 0 ] = 23;
    rookMoves[ 31 ][ 1 ][ 1 ] = 15;
    rookMoves[ 31 ][ 1 ][ 2 ] = 7;
    rookMoves[ 31 ][ 2 ][ 0 ] = 39;
    rookMoves[ 31 ][ 2 ][ 1 ] = 47;
    rookMoves[ 31 ][ 2 ][ 2 ] = 55;
    rookMoves[ 31 ][ 2 ][ 3 ] = 63;

    rookMoves[ 32 ] = new int[ 3 ][];
    rookMoves[ 32 ][ 0 ] = new int[ 7 ];
    rookMoves[ 32 ][ 1 ] = new int[ 3 ];
    rookMoves[ 32 ][ 2 ] = new int[ 4 ];
    rookMoves[ 32 ][ 0 ][ 0 ] = 33;
    rookMoves[ 32 ][ 0 ][ 1 ] = 34;
    rookMoves[ 32 ][ 0 ][ 2 ] = 35;
    rookMoves[ 32 ][ 0 ][ 3 ] = 36;
    rookMoves[ 32 ][ 0 ][ 4 ] = 37;
    rookMoves[ 32 ][ 0 ][ 5 ] = 38;
    rookMoves[ 32 ][ 0 ][ 6 ] = 39;
    rookMoves[ 32 ][ 1 ][ 0 ] = 40;
    rookMoves[ 32 ][ 1 ][ 1 ] = 48;
    rookMoves[ 32 ][ 1 ][ 2 ] = 56;
    rookMoves[ 32 ][ 2 ][ 0 ] = 24;
    rookMoves[ 32 ][ 2 ][ 1 ] = 16;
    rookMoves[ 32 ][ 2 ][ 2 ] = 8;
    rookMoves[ 32 ][ 2 ][ 3 ] = 0;

    rookMoves[ 33 ] = new int[ 4 ][];
    rookMoves[ 33 ][ 0 ] = new int[ 1 ];
    rookMoves[ 33 ][ 1 ] = new int[ 6 ];
    rookMoves[ 33 ][ 2 ] = new int[ 4 ];
    rookMoves[ 33 ][ 3 ] = new int[ 3 ];
    rookMoves[ 33 ][ 0 ][ 0 ] = 32;
    rookMoves[ 33 ][ 1 ][ 0 ] = 34;
    rookMoves[ 33 ][ 1 ][ 1 ] = 35;
    rookMoves[ 33 ][ 1 ][ 2 ] = 36;
    rookMoves[ 33 ][ 1 ][ 3 ] = 37;
    rookMoves[ 33 ][ 1 ][ 4 ] = 38;
    rookMoves[ 33 ][ 1 ][ 5 ] = 39;
    rookMoves[ 33 ][ 2 ][ 0 ] = 25;
    rookMoves[ 33 ][ 2 ][ 1 ] = 17;
    rookMoves[ 33 ][ 2 ][ 2 ] = 9;
    rookMoves[ 33 ][ 2 ][ 3 ] = 1;
    rookMoves[ 33 ][ 3 ][ 0 ] = 41;
    rookMoves[ 33 ][ 3 ][ 1 ] = 49;
    rookMoves[ 33 ][ 3 ][ 2 ] = 57;

    rookMoves[ 34 ] = new int[ 4 ][];
    rookMoves[ 34 ][ 0 ] = new int[ 2 ];
    rookMoves[ 34 ][ 1 ] = new int[ 5 ];
    rookMoves[ 34 ][ 2 ] = new int[ 4 ];
    rookMoves[ 34 ][ 3 ] = new int[ 3 ];
    rookMoves[ 34 ][ 0 ][ 0 ] = 33;
    rookMoves[ 34 ][ 0 ][ 1 ] = 32;
    rookMoves[ 34 ][ 1 ][ 0 ] = 35;
    rookMoves[ 34 ][ 1 ][ 1 ] = 36;
    rookMoves[ 34 ][ 1 ][ 2 ] = 37;
    rookMoves[ 34 ][ 1 ][ 3 ] = 38;
    rookMoves[ 34 ][ 1 ][ 4 ] = 39;
    rookMoves[ 34 ][ 2 ][ 0 ] = 26;
    rookMoves[ 34 ][ 2 ][ 1 ] = 18;
    rookMoves[ 34 ][ 2 ][ 2 ] = 10;
    rookMoves[ 34 ][ 2 ][ 3 ] = 2;
    rookMoves[ 34 ][ 3 ][ 0 ] = 42;
    rookMoves[ 34 ][ 3 ][ 1 ] = 50;
    rookMoves[ 34 ][ 3 ][ 2 ] = 58;

    rookMoves[ 35 ] = new int[ 4 ][];
    rookMoves[ 35 ][ 0 ] = new int[ 3 ];
    rookMoves[ 35 ][ 1 ] = new int[ 4 ];
    rookMoves[ 35 ][ 2 ] = new int[ 4 ];
    rookMoves[ 35 ][ 3 ] = new int[ 3 ];
    rookMoves[ 35 ][ 0 ][ 0 ] = 34;
    rookMoves[ 35 ][ 0 ][ 1 ] = 33;
    rookMoves[ 35 ][ 0 ][ 2 ] = 32;
    rookMoves[ 35 ][ 1 ][ 0 ] = 36;
    rookMoves[ 35 ][ 1 ][ 1 ] = 37;
    rookMoves[ 35 ][ 1 ][ 2 ] = 38;
    rookMoves[ 35 ][ 1 ][ 3 ] = 39;
    rookMoves[ 35 ][ 2 ][ 0 ] = 27;
    rookMoves[ 35 ][ 2 ][ 1 ] = 19;
    rookMoves[ 35 ][ 2 ][ 2 ] = 11;
    rookMoves[ 35 ][ 2 ][ 3 ] = 3;
    rookMoves[ 35 ][ 3 ][ 0 ] = 43;
    rookMoves[ 35 ][ 3 ][ 1 ] = 51;
    rookMoves[ 35 ][ 3 ][ 2 ] = 59;

    rookMoves[ 36 ] = new int[ 4 ][];
    rookMoves[ 36 ][ 0 ] = new int[ 4 ];
    rookMoves[ 36 ][ 1 ] = new int[ 3 ];
    rookMoves[ 36 ][ 2 ] = new int[ 4 ];
    rookMoves[ 36 ][ 3 ] = new int[ 3 ];
    rookMoves[ 36 ][ 0 ][ 0 ] = 35;
    rookMoves[ 36 ][ 0 ][ 1 ] = 34;
    rookMoves[ 36 ][ 0 ][ 2 ] = 33;
    rookMoves[ 36 ][ 0 ][ 3 ] = 32;
    rookMoves[ 36 ][ 1 ][ 0 ] = 37;
    rookMoves[ 36 ][ 1 ][ 1 ] = 38;
    rookMoves[ 36 ][ 1 ][ 2 ] = 39;
    rookMoves[ 36 ][ 2 ][ 0 ] = 28;
    rookMoves[ 36 ][ 2 ][ 1 ] = 20;
    rookMoves[ 36 ][ 2 ][ 2 ] = 12;
    rookMoves[ 36 ][ 2 ][ 3 ] = 4;
    rookMoves[ 36 ][ 3 ][ 0 ] = 44;
    rookMoves[ 36 ][ 3 ][ 1 ] = 52;
    rookMoves[ 36 ][ 3 ][ 2 ] = 60;

    rookMoves[ 37 ] = new int[ 4 ][];
    rookMoves[ 37 ][ 0 ] = new int[ 5 ];
    rookMoves[ 37 ][ 1 ] = new int[ 2 ];
    rookMoves[ 37 ][ 2 ] = new int[ 4 ];
    rookMoves[ 37 ][ 3 ] = new int[ 3 ];
    rookMoves[ 37 ][ 0 ][ 0 ] = 36;
    rookMoves[ 37 ][ 0 ][ 1 ] = 35;
    rookMoves[ 37 ][ 0 ][ 2 ] = 34;
    rookMoves[ 37 ][ 0 ][ 3 ] = 33;
    rookMoves[ 37 ][ 0 ][ 4 ] = 32;
    rookMoves[ 37 ][ 1 ][ 0 ] = 38;
    rookMoves[ 37 ][ 1 ][ 1 ] = 39;
    rookMoves[ 37 ][ 2 ][ 0 ] = 29;
    rookMoves[ 37 ][ 2 ][ 1 ] = 21;
    rookMoves[ 37 ][ 2 ][ 2 ] = 13;
    rookMoves[ 37 ][ 2 ][ 3 ] = 5;
    rookMoves[ 37 ][ 3 ][ 0 ] = 45;
    rookMoves[ 37 ][ 3 ][ 1 ] = 53;
    rookMoves[ 37 ][ 3 ][ 2 ] = 61;

    rookMoves[ 38 ] = new int[ 4 ][];
    rookMoves[ 38 ][ 0 ] = new int[ 6 ];
    rookMoves[ 38 ][ 1 ] = new int[ 1 ];
    rookMoves[ 38 ][ 2 ] = new int[ 4 ];
    rookMoves[ 38 ][ 3 ] = new int[ 3 ];
    rookMoves[ 38 ][ 0 ][ 0 ] = 37;
    rookMoves[ 38 ][ 0 ][ 1 ] = 36;
    rookMoves[ 38 ][ 0 ][ 2 ] = 35;
    rookMoves[ 38 ][ 0 ][ 3 ] = 34;
    rookMoves[ 38 ][ 0 ][ 4 ] = 33;
    rookMoves[ 38 ][ 0 ][ 5 ] = 32;
    rookMoves[ 38 ][ 1 ][ 0 ] = 39;
    rookMoves[ 38 ][ 2 ][ 0 ] = 30;
    rookMoves[ 38 ][ 2 ][ 1 ] = 22;
    rookMoves[ 38 ][ 2 ][ 2 ] = 14;
    rookMoves[ 38 ][ 2 ][ 3 ] = 6;
    rookMoves[ 38 ][ 3 ][ 0 ] = 46;
    rookMoves[ 38 ][ 3 ][ 1 ] = 54;
    rookMoves[ 38 ][ 3 ][ 2 ] = 62;

    rookMoves[ 39 ] = new int[ 3 ][];
    rookMoves[ 39 ][ 0 ] = new int[ 7 ];
    rookMoves[ 39 ][ 1 ] = new int[ 4 ];
    rookMoves[ 39 ][ 2 ] = new int[ 3 ];
    rookMoves[ 39 ][ 0 ][ 0 ] = 38;
    rookMoves[ 39 ][ 0 ][ 1 ] = 37;
    rookMoves[ 39 ][ 0 ][ 2 ] = 36;
    rookMoves[ 39 ][ 0 ][ 3 ] = 35;
    rookMoves[ 39 ][ 0 ][ 4 ] = 34;
    rookMoves[ 39 ][ 0 ][ 5 ] = 33;
    rookMoves[ 39 ][ 0 ][ 6 ] = 32;
    rookMoves[ 39 ][ 1 ][ 0 ] = 31;
    rookMoves[ 39 ][ 1 ][ 1 ] = 23;
    rookMoves[ 39 ][ 1 ][ 2 ] = 15;
    rookMoves[ 39 ][ 1 ][ 3 ] = 7;
    rookMoves[ 39 ][ 2 ][ 0 ] = 47;
    rookMoves[ 39 ][ 2 ][ 1 ] = 55;
    rookMoves[ 39 ][ 2 ][ 2 ] = 63;

    rookMoves[ 40 ] = new int[ 3 ][];
    rookMoves[ 40 ][ 0 ] = new int[ 7 ];
    rookMoves[ 40 ][ 1 ] = new int[ 5 ];
    rookMoves[ 40 ][ 2 ] = new int[ 2 ];
    rookMoves[ 40 ][ 0 ][ 0 ] = 41;
    rookMoves[ 40 ][ 0 ][ 1 ] = 42;
    rookMoves[ 40 ][ 0 ][ 2 ] = 43;
    rookMoves[ 40 ][ 0 ][ 3 ] = 44;
    rookMoves[ 40 ][ 0 ][ 4 ] = 45;
    rookMoves[ 40 ][ 0 ][ 5 ] = 46;
    rookMoves[ 40 ][ 0 ][ 6 ] = 47;
    rookMoves[ 40 ][ 1 ][ 0 ] = 32;
    rookMoves[ 40 ][ 1 ][ 1 ] = 24;
    rookMoves[ 40 ][ 1 ][ 2 ] = 16;
    rookMoves[ 40 ][ 1 ][ 3 ] = 8;
    rookMoves[ 40 ][ 1 ][ 4 ] = 0;
    rookMoves[ 40 ][ 2 ][ 0 ] = 48;
    rookMoves[ 40 ][ 2 ][ 1 ] = 56;

    rookMoves[ 41 ] = new int[ 4 ][];
    rookMoves[ 41 ][ 0 ] = new int[ 1 ];
    rookMoves[ 41 ][ 1 ] = new int[ 6 ];
    rookMoves[ 41 ][ 2 ] = new int[ 5 ];
    rookMoves[ 41 ][ 3 ] = new int[ 2 ];
    rookMoves[ 41 ][ 0 ][ 0 ] = 40;
    rookMoves[ 41 ][ 1 ][ 0 ] = 42;
    rookMoves[ 41 ][ 1 ][ 1 ] = 43;
    rookMoves[ 41 ][ 1 ][ 2 ] = 44;
    rookMoves[ 41 ][ 1 ][ 3 ] = 45;
    rookMoves[ 41 ][ 1 ][ 4 ] = 46;
    rookMoves[ 41 ][ 1 ][ 5 ] = 47;
    rookMoves[ 41 ][ 2 ][ 0 ] = 33;
    rookMoves[ 41 ][ 2 ][ 1 ] = 25;
    rookMoves[ 41 ][ 2 ][ 2 ] = 17;
    rookMoves[ 41 ][ 2 ][ 3 ] = 9;
    rookMoves[ 41 ][ 2 ][ 4 ] = 1;
    rookMoves[ 41 ][ 3 ][ 0 ] = 49;
    rookMoves[ 41 ][ 3 ][ 1 ] = 57;

    rookMoves[ 42 ] = new int[ 4 ][];
    rookMoves[ 42 ][ 0 ] = new int[ 2 ];
    rookMoves[ 42 ][ 1 ] = new int[ 5 ];
    rookMoves[ 42 ][ 2 ] = new int[ 5 ];
    rookMoves[ 42 ][ 3 ] = new int[ 2 ];
    rookMoves[ 42 ][ 0 ][ 0 ] = 41;
    rookMoves[ 42 ][ 0 ][ 1 ] = 40;
    rookMoves[ 42 ][ 1 ][ 0 ] = 43;
    rookMoves[ 42 ][ 1 ][ 1 ] = 44;
    rookMoves[ 42 ][ 1 ][ 2 ] = 45;
    rookMoves[ 42 ][ 1 ][ 3 ] = 46;
    rookMoves[ 42 ][ 1 ][ 4 ] = 47;
    rookMoves[ 42 ][ 2 ][ 0 ] = 34;
    rookMoves[ 42 ][ 2 ][ 1 ] = 26;
    rookMoves[ 42 ][ 2 ][ 2 ] = 18;
    rookMoves[ 42 ][ 2 ][ 3 ] = 10;
    rookMoves[ 42 ][ 2 ][ 4 ] = 2;
    rookMoves[ 42 ][ 3 ][ 0 ] = 50;
    rookMoves[ 42 ][ 3 ][ 1 ] = 58;

    rookMoves[ 43 ] = new int[ 4 ][];
    rookMoves[ 43 ][ 0 ] = new int[ 3 ];
    rookMoves[ 43 ][ 1 ] = new int[ 4 ];
    rookMoves[ 43 ][ 2 ] = new int[ 5 ];
    rookMoves[ 43 ][ 3 ] = new int[ 2 ];
    rookMoves[ 43 ][ 0 ][ 0 ] = 42;
    rookMoves[ 43 ][ 0 ][ 1 ] = 41;
    rookMoves[ 43 ][ 0 ][ 2 ] = 40;
    rookMoves[ 43 ][ 1 ][ 0 ] = 44;
    rookMoves[ 43 ][ 1 ][ 1 ] = 45;
    rookMoves[ 43 ][ 1 ][ 2 ] = 46;
    rookMoves[ 43 ][ 1 ][ 3 ] = 47;
    rookMoves[ 43 ][ 2 ][ 0 ] = 35;
    rookMoves[ 43 ][ 2 ][ 1 ] = 27;
    rookMoves[ 43 ][ 2 ][ 2 ] = 19;
    rookMoves[ 43 ][ 2 ][ 3 ] = 11;
    rookMoves[ 43 ][ 2 ][ 4 ] = 3;
    rookMoves[ 43 ][ 3 ][ 0 ] = 51;
    rookMoves[ 43 ][ 3 ][ 1 ] = 59;

    rookMoves[ 44 ] = new int[ 4 ][];
    rookMoves[ 44 ][ 0 ] = new int[ 4 ];
    rookMoves[ 44 ][ 1 ] = new int[ 3 ];
    rookMoves[ 44 ][ 2 ] = new int[ 5 ];
    rookMoves[ 44 ][ 3 ] = new int[ 2 ];
    rookMoves[ 44 ][ 0 ][ 0 ] = 43;
    rookMoves[ 44 ][ 0 ][ 1 ] = 42;
    rookMoves[ 44 ][ 0 ][ 2 ] = 41;
    rookMoves[ 44 ][ 0 ][ 3 ] = 40;
    rookMoves[ 44 ][ 1 ][ 0 ] = 45;
    rookMoves[ 44 ][ 1 ][ 1 ] = 46;
    rookMoves[ 44 ][ 1 ][ 2 ] = 47;
    rookMoves[ 44 ][ 2 ][ 0 ] = 36;
    rookMoves[ 44 ][ 2 ][ 1 ] = 28;
    rookMoves[ 44 ][ 2 ][ 2 ] = 20;
    rookMoves[ 44 ][ 2 ][ 3 ] = 12;
    rookMoves[ 44 ][ 2 ][ 4 ] = 4;
    rookMoves[ 44 ][ 3 ][ 0 ] = 52;
    rookMoves[ 44 ][ 3 ][ 1 ] = 60;

    rookMoves[ 45 ] = new int[ 4 ][];
    rookMoves[ 45 ][ 0 ] = new int[ 5 ];
    rookMoves[ 45 ][ 1 ] = new int[ 2 ];
    rookMoves[ 45 ][ 2 ] = new int[ 5 ];
    rookMoves[ 45 ][ 3 ] = new int[ 2 ];
    rookMoves[ 45 ][ 0 ][ 0 ] = 44;
    rookMoves[ 45 ][ 0 ][ 1 ] = 43;
    rookMoves[ 45 ][ 0 ][ 2 ] = 42;
    rookMoves[ 45 ][ 0 ][ 3 ] = 41;
    rookMoves[ 45 ][ 0 ][ 4 ] = 40;
    rookMoves[ 45 ][ 1 ][ 0 ] = 46;
    rookMoves[ 45 ][ 1 ][ 1 ] = 47;
    rookMoves[ 45 ][ 2 ][ 0 ] = 37;
    rookMoves[ 45 ][ 2 ][ 1 ] = 29;
    rookMoves[ 45 ][ 2 ][ 2 ] = 21;
    rookMoves[ 45 ][ 2 ][ 3 ] = 13;
    rookMoves[ 45 ][ 2 ][ 4 ] = 5;
    rookMoves[ 45 ][ 3 ][ 0 ] = 53;
    rookMoves[ 45 ][ 3 ][ 1 ] = 61;

    rookMoves[ 46 ] = new int[ 4 ][];
    rookMoves[ 46 ][ 0 ] = new int[ 6 ];
    rookMoves[ 46 ][ 1 ] = new int[ 1 ];
    rookMoves[ 46 ][ 2 ] = new int[ 5 ];
    rookMoves[ 46 ][ 3 ] = new int[ 2 ];
    rookMoves[ 46 ][ 0 ][ 0 ] = 45;
    rookMoves[ 46 ][ 0 ][ 1 ] = 44;
    rookMoves[ 46 ][ 0 ][ 2 ] = 43;
    rookMoves[ 46 ][ 0 ][ 3 ] = 42;
    rookMoves[ 46 ][ 0 ][ 4 ] = 41;
    rookMoves[ 46 ][ 0 ][ 5 ] = 40;
    rookMoves[ 46 ][ 1 ][ 0 ] = 47;
    rookMoves[ 46 ][ 2 ][ 0 ] = 38;
    rookMoves[ 46 ][ 2 ][ 1 ] = 30;
    rookMoves[ 46 ][ 2 ][ 2 ] = 22;
    rookMoves[ 46 ][ 2 ][ 3 ] = 14;
    rookMoves[ 46 ][ 2 ][ 4 ] = 6;
    rookMoves[ 46 ][ 3 ][ 0 ] = 54;
    rookMoves[ 46 ][ 3 ][ 1 ] = 62;

    rookMoves[ 47 ] = new int[ 3 ][];
    rookMoves[ 47 ][ 0 ] = new int[ 7 ];
    rookMoves[ 47 ][ 1 ] = new int[ 5 ];
    rookMoves[ 47 ][ 2 ] = new int[ 2 ];
    rookMoves[ 47 ][ 0 ][ 0 ] = 46;
    rookMoves[ 47 ][ 0 ][ 1 ] = 45;
    rookMoves[ 47 ][ 0 ][ 2 ] = 44;
    rookMoves[ 47 ][ 0 ][ 3 ] = 43;
    rookMoves[ 47 ][ 0 ][ 4 ] = 42;
    rookMoves[ 47 ][ 0 ][ 5 ] = 41;
    rookMoves[ 47 ][ 0 ][ 6 ] = 40;
    rookMoves[ 47 ][ 1 ][ 0 ] = 39;
    rookMoves[ 47 ][ 1 ][ 1 ] = 31;
    rookMoves[ 47 ][ 1 ][ 2 ] = 23;
    rookMoves[ 47 ][ 1 ][ 3 ] = 15;
    rookMoves[ 47 ][ 1 ][ 4 ] = 7;
    rookMoves[ 47 ][ 2 ][ 0 ] = 55;
    rookMoves[ 47 ][ 2 ][ 1 ] = 63;

    rookMoves[ 48 ] = new int[ 3 ][];
    rookMoves[ 48 ][ 0 ] = new int[ 7 ];
    rookMoves[ 48 ][ 1 ] = new int[ 6 ];
    rookMoves[ 48 ][ 2 ] = new int[ 1 ];
    rookMoves[ 48 ][ 0 ][ 0 ] = 49;
    rookMoves[ 48 ][ 0 ][ 1 ] = 50;
    rookMoves[ 48 ][ 0 ][ 2 ] = 51;
    rookMoves[ 48 ][ 0 ][ 3 ] = 52;
    rookMoves[ 48 ][ 0 ][ 4 ] = 53;
    rookMoves[ 48 ][ 0 ][ 5 ] = 54;
    rookMoves[ 48 ][ 0 ][ 6 ] = 55;
    rookMoves[ 48 ][ 1 ][ 0 ] = 40;
    rookMoves[ 48 ][ 1 ][ 1 ] = 32;
    rookMoves[ 48 ][ 1 ][ 2 ] = 24;
    rookMoves[ 48 ][ 1 ][ 3 ] = 16;
    rookMoves[ 48 ][ 1 ][ 4 ] = 8;
    rookMoves[ 48 ][ 1 ][ 5 ] = 0;
    rookMoves[ 48 ][ 2 ][ 0 ] = 56;

    rookMoves[ 49 ] = new int[ 4 ][];
    rookMoves[ 49 ][ 0 ] = new int[ 1 ];
    rookMoves[ 49 ][ 1 ] = new int[ 6 ];
    rookMoves[ 49 ][ 2 ] = new int[ 6 ];
    rookMoves[ 49 ][ 3 ] = new int[ 1 ];
    rookMoves[ 49 ][ 0 ][ 0 ] = 48;
    rookMoves[ 49 ][ 1 ][ 0 ] = 50;
    rookMoves[ 49 ][ 1 ][ 1 ] = 51;
    rookMoves[ 49 ][ 1 ][ 2 ] = 52;
    rookMoves[ 49 ][ 1 ][ 3 ] = 53;
    rookMoves[ 49 ][ 1 ][ 4 ] = 54;
    rookMoves[ 49 ][ 1 ][ 5 ] = 55;
    rookMoves[ 49 ][ 2 ][ 0 ] = 41;
    rookMoves[ 49 ][ 2 ][ 1 ] = 33;
    rookMoves[ 49 ][ 2 ][ 2 ] = 25;
    rookMoves[ 49 ][ 2 ][ 3 ] = 17;
    rookMoves[ 49 ][ 2 ][ 4 ] = 9;
    rookMoves[ 49 ][ 2 ][ 5 ] = 1;
    rookMoves[ 49 ][ 3 ][ 0 ] = 57;

    rookMoves[ 50 ] = new int[ 4 ][];
    rookMoves[ 50 ][ 0 ] = new int[ 2 ];
    rookMoves[ 50 ][ 1 ] = new int[ 5 ];
    rookMoves[ 50 ][ 2 ] = new int[ 6 ];
    rookMoves[ 50 ][ 3 ] = new int[ 1 ];
    rookMoves[ 50 ][ 0 ][ 0 ] = 49;
    rookMoves[ 50 ][ 0 ][ 1 ] = 48;
    rookMoves[ 50 ][ 1 ][ 0 ] = 51;
    rookMoves[ 50 ][ 1 ][ 1 ] = 52;
    rookMoves[ 50 ][ 1 ][ 2 ] = 53;
    rookMoves[ 50 ][ 1 ][ 3 ] = 54;
    rookMoves[ 50 ][ 1 ][ 4 ] = 55;
    rookMoves[ 50 ][ 2 ][ 0 ] = 42;
    rookMoves[ 50 ][ 2 ][ 1 ] = 34;
    rookMoves[ 50 ][ 2 ][ 2 ] = 26;
    rookMoves[ 50 ][ 2 ][ 3 ] = 18;
    rookMoves[ 50 ][ 2 ][ 4 ] = 10;
    rookMoves[ 50 ][ 2 ][ 5 ] = 2;
    rookMoves[ 50 ][ 3 ][ 0 ] = 58;

    rookMoves[ 51 ] = new int[ 4 ][];
    rookMoves[ 51 ][ 0 ] = new int[ 3 ];
    rookMoves[ 51 ][ 1 ] = new int[ 4 ];
    rookMoves[ 51 ][ 2 ] = new int[ 6 ];
    rookMoves[ 51 ][ 3 ] = new int[ 1 ];
    rookMoves[ 51 ][ 0 ][ 0 ] = 50;
    rookMoves[ 51 ][ 0 ][ 1 ] = 49;
    rookMoves[ 51 ][ 0 ][ 2 ] = 48;
    rookMoves[ 51 ][ 1 ][ 0 ] = 52;
    rookMoves[ 51 ][ 1 ][ 1 ] = 53;
    rookMoves[ 51 ][ 1 ][ 2 ] = 54;
    rookMoves[ 51 ][ 1 ][ 3 ] = 55;
    rookMoves[ 51 ][ 2 ][ 0 ] = 43;
    rookMoves[ 51 ][ 2 ][ 1 ] = 35;
    rookMoves[ 51 ][ 2 ][ 2 ] = 27;
    rookMoves[ 51 ][ 2 ][ 3 ] = 19;
    rookMoves[ 51 ][ 2 ][ 4 ] = 11;
    rookMoves[ 51 ][ 2 ][ 5 ] = 3;
    rookMoves[ 51 ][ 3 ][ 0 ] = 59;

    rookMoves[ 52 ] = new int[ 4 ][];
    rookMoves[ 52 ][ 0 ] = new int[ 4 ];
    rookMoves[ 52 ][ 1 ] = new int[ 3 ];
    rookMoves[ 52 ][ 2 ] = new int[ 6 ];
    rookMoves[ 52 ][ 3 ] = new int[ 1 ];
    rookMoves[ 52 ][ 0 ][ 0 ] = 51;
    rookMoves[ 52 ][ 0 ][ 1 ] = 50;
    rookMoves[ 52 ][ 0 ][ 2 ] = 49;
    rookMoves[ 52 ][ 0 ][ 3 ] = 48;
    rookMoves[ 52 ][ 1 ][ 0 ] = 53;
    rookMoves[ 52 ][ 1 ][ 1 ] = 54;
    rookMoves[ 52 ][ 1 ][ 2 ] = 55;
    rookMoves[ 52 ][ 2 ][ 0 ] = 44;
    rookMoves[ 52 ][ 2 ][ 1 ] = 36;
    rookMoves[ 52 ][ 2 ][ 2 ] = 28;
    rookMoves[ 52 ][ 2 ][ 3 ] = 20;
    rookMoves[ 52 ][ 2 ][ 4 ] = 12;
    rookMoves[ 52 ][ 2 ][ 5 ] = 4;
    rookMoves[ 52 ][ 3 ][ 0 ] = 60;

    rookMoves[ 53 ] = new int[ 4 ][];
    rookMoves[ 53 ][ 0 ] = new int[ 5 ];
    rookMoves[ 53 ][ 1 ] = new int[ 2 ];
    rookMoves[ 53 ][ 2 ] = new int[ 6 ];
    rookMoves[ 53 ][ 3 ] = new int[ 1 ];
    rookMoves[ 53 ][ 0 ][ 0 ] = 52;
    rookMoves[ 53 ][ 0 ][ 1 ] = 51;
    rookMoves[ 53 ][ 0 ][ 2 ] = 50;
    rookMoves[ 53 ][ 0 ][ 3 ] = 49;
    rookMoves[ 53 ][ 0 ][ 4 ] = 48;
    rookMoves[ 53 ][ 1 ][ 0 ] = 54;
    rookMoves[ 53 ][ 1 ][ 1 ] = 55;
    rookMoves[ 53 ][ 2 ][ 0 ] = 45;
    rookMoves[ 53 ][ 2 ][ 1 ] = 37;
    rookMoves[ 53 ][ 2 ][ 2 ] = 29;
    rookMoves[ 53 ][ 2 ][ 3 ] = 21;
    rookMoves[ 53 ][ 2 ][ 4 ] = 13;
    rookMoves[ 53 ][ 2 ][ 5 ] = 5;
    rookMoves[ 53 ][ 3 ][ 0 ] = 61;

    rookMoves[ 54 ] = new int[ 4 ][];
    rookMoves[ 54 ][ 0 ] = new int[ 6 ];
    rookMoves[ 54 ][ 1 ] = new int[ 1 ];
    rookMoves[ 54 ][ 2 ] = new int[ 6 ];
    rookMoves[ 54 ][ 3 ] = new int[ 1 ];
    rookMoves[ 54 ][ 0 ][ 0 ] = 53;
    rookMoves[ 54 ][ 0 ][ 1 ] = 52;
    rookMoves[ 54 ][ 0 ][ 2 ] = 51;
    rookMoves[ 54 ][ 0 ][ 3 ] = 50;
    rookMoves[ 54 ][ 0 ][ 4 ] = 49;
    rookMoves[ 54 ][ 0 ][ 5 ] = 48;
    rookMoves[ 54 ][ 1 ][ 0 ] = 55;
    rookMoves[ 54 ][ 2 ][ 0 ] = 46;
    rookMoves[ 54 ][ 2 ][ 1 ] = 38;
    rookMoves[ 54 ][ 2 ][ 2 ] = 30;
    rookMoves[ 54 ][ 2 ][ 3 ] = 22;
    rookMoves[ 54 ][ 2 ][ 4 ] = 14;
    rookMoves[ 54 ][ 2 ][ 5 ] = 6;
    rookMoves[ 54 ][ 3 ][ 0 ] = 62;

    rookMoves[ 55 ] = new int[ 3 ][];
    rookMoves[ 55 ][ 0 ] = new int[ 7 ];
    rookMoves[ 55 ][ 1 ] = new int[ 6 ];
    rookMoves[ 55 ][ 2 ] = new int[ 1 ];
    rookMoves[ 55 ][ 0 ][ 0 ] = 54;
    rookMoves[ 55 ][ 0 ][ 1 ] = 53;
    rookMoves[ 55 ][ 0 ][ 2 ] = 52;
    rookMoves[ 55 ][ 0 ][ 3 ] = 51;
    rookMoves[ 55 ][ 0 ][ 4 ] = 50;
    rookMoves[ 55 ][ 0 ][ 5 ] = 49;
    rookMoves[ 55 ][ 0 ][ 6 ] = 48;
    rookMoves[ 55 ][ 1 ][ 0 ] = 47;
    rookMoves[ 55 ][ 1 ][ 1 ] = 39;
    rookMoves[ 55 ][ 1 ][ 2 ] = 31;
    rookMoves[ 55 ][ 1 ][ 3 ] = 23;
    rookMoves[ 55 ][ 1 ][ 4 ] = 15;
    rookMoves[ 55 ][ 1 ][ 5 ] = 7;
    rookMoves[ 55 ][ 2 ][ 0 ] = 63;

    rookMoves[ 56 ] = new int[ 2 ][];
    rookMoves[ 56 ][ 0 ] = new int[ 7 ];
    rookMoves[ 56 ][ 1 ] = new int[ 7 ];
    rookMoves[ 56 ][ 0 ][ 0 ] = 57;
    rookMoves[ 56 ][ 0 ][ 1 ] = 58;
    rookMoves[ 56 ][ 0 ][ 2 ] = 59;
    rookMoves[ 56 ][ 0 ][ 3 ] = 60;
    rookMoves[ 56 ][ 0 ][ 4 ] = 61;
    rookMoves[ 56 ][ 0 ][ 5 ] = 62;
    rookMoves[ 56 ][ 0 ][ 6 ] = 63;
    rookMoves[ 56 ][ 1 ][ 0 ] = 48;
    rookMoves[ 56 ][ 1 ][ 1 ] = 40;
    rookMoves[ 56 ][ 1 ][ 2 ] = 32;
    rookMoves[ 56 ][ 1 ][ 3 ] = 24;
    rookMoves[ 56 ][ 1 ][ 4 ] = 16;
    rookMoves[ 56 ][ 1 ][ 5 ] = 8;
    rookMoves[ 56 ][ 1 ][ 6 ] = 0;

    rookMoves[ 57 ] = new int[ 3 ][];
    rookMoves[ 57 ][ 0 ] = new int[ 1 ];
    rookMoves[ 57 ][ 1 ] = new int[ 6 ];
    rookMoves[ 57 ][ 2 ] = new int[ 7 ];
    rookMoves[ 57 ][ 0 ][ 0 ] = 56;
    rookMoves[ 57 ][ 1 ][ 0 ] = 58;
    rookMoves[ 57 ][ 1 ][ 1 ] = 59;
    rookMoves[ 57 ][ 1 ][ 2 ] = 60;
    rookMoves[ 57 ][ 1 ][ 3 ] = 61;
    rookMoves[ 57 ][ 1 ][ 4 ] = 62;
    rookMoves[ 57 ][ 1 ][ 5 ] = 63;
    rookMoves[ 57 ][ 2 ][ 0 ] = 49;
    rookMoves[ 57 ][ 2 ][ 1 ] = 41;
    rookMoves[ 57 ][ 2 ][ 2 ] = 33;
    rookMoves[ 57 ][ 2 ][ 3 ] = 25;
    rookMoves[ 57 ][ 2 ][ 4 ] = 17;
    rookMoves[ 57 ][ 2 ][ 5 ] = 9;
    rookMoves[ 57 ][ 2 ][ 6 ] = 1;

    rookMoves[ 58 ] = new int[ 3 ][];
    rookMoves[ 58 ][ 0 ] = new int[ 2 ];
    rookMoves[ 58 ][ 1 ] = new int[ 5 ];
    rookMoves[ 58 ][ 2 ] = new int[ 7 ];
    rookMoves[ 58 ][ 0 ][ 0 ] = 57;
    rookMoves[ 58 ][ 0 ][ 1 ] = 56;
    rookMoves[ 58 ][ 1 ][ 0 ] = 59;
    rookMoves[ 58 ][ 1 ][ 1 ] = 60;
    rookMoves[ 58 ][ 1 ][ 2 ] = 61;
    rookMoves[ 58 ][ 1 ][ 3 ] = 62;
    rookMoves[ 58 ][ 1 ][ 4 ] = 63;
    rookMoves[ 58 ][ 2 ][ 0 ] = 50;
    rookMoves[ 58 ][ 2 ][ 1 ] = 42;
    rookMoves[ 58 ][ 2 ][ 2 ] = 34;
    rookMoves[ 58 ][ 2 ][ 3 ] = 26;
    rookMoves[ 58 ][ 2 ][ 4 ] = 18;
    rookMoves[ 58 ][ 2 ][ 5 ] = 10;
    rookMoves[ 58 ][ 2 ][ 6 ] = 2;

    rookMoves[ 59 ] = new int[ 3 ][];
    rookMoves[ 59 ][ 0 ] = new int[ 3 ];
    rookMoves[ 59 ][ 1 ] = new int[ 4 ];
    rookMoves[ 59 ][ 2 ] = new int[ 7 ];
    rookMoves[ 59 ][ 0 ][ 0 ] = 58;
    rookMoves[ 59 ][ 0 ][ 1 ] = 57;
    rookMoves[ 59 ][ 0 ][ 2 ] = 56;
    rookMoves[ 59 ][ 1 ][ 0 ] = 60;
    rookMoves[ 59 ][ 1 ][ 1 ] = 61;
    rookMoves[ 59 ][ 1 ][ 2 ] = 62;
    rookMoves[ 59 ][ 1 ][ 3 ] = 63;
    rookMoves[ 59 ][ 2 ][ 0 ] = 51;
    rookMoves[ 59 ][ 2 ][ 1 ] = 43;
    rookMoves[ 59 ][ 2 ][ 2 ] = 35;
    rookMoves[ 59 ][ 2 ][ 3 ] = 27;
    rookMoves[ 59 ][ 2 ][ 4 ] = 19;
    rookMoves[ 59 ][ 2 ][ 5 ] = 11;
    rookMoves[ 59 ][ 2 ][ 6 ] = 3;

    rookMoves[ 60 ] = new int[ 3 ][];
    rookMoves[ 60 ][ 0 ] = new int[ 4 ];
    rookMoves[ 60 ][ 1 ] = new int[ 3 ];
    rookMoves[ 60 ][ 2 ] = new int[ 7 ];
    rookMoves[ 60 ][ 0 ][ 0 ] = 59;
    rookMoves[ 60 ][ 0 ][ 1 ] = 58;
    rookMoves[ 60 ][ 0 ][ 2 ] = 57;
    rookMoves[ 60 ][ 0 ][ 3 ] = 56;
    rookMoves[ 60 ][ 1 ][ 0 ] = 61;
    rookMoves[ 60 ][ 1 ][ 1 ] = 62;
    rookMoves[ 60 ][ 1 ][ 2 ] = 63;
    rookMoves[ 60 ][ 2 ][ 0 ] = 52;
    rookMoves[ 60 ][ 2 ][ 1 ] = 44;
    rookMoves[ 60 ][ 2 ][ 2 ] = 36;
    rookMoves[ 60 ][ 2 ][ 3 ] = 28;
    rookMoves[ 60 ][ 2 ][ 4 ] = 20;
    rookMoves[ 60 ][ 2 ][ 5 ] = 12;
    rookMoves[ 60 ][ 2 ][ 6 ] = 4;

    rookMoves[ 61 ] = new int[ 3 ][];
    rookMoves[ 61 ][ 0 ] = new int[ 5 ];
    rookMoves[ 61 ][ 1 ] = new int[ 2 ];
    rookMoves[ 61 ][ 2 ] = new int[ 7 ];
    rookMoves[ 61 ][ 0 ][ 0 ] = 60;
    rookMoves[ 61 ][ 0 ][ 1 ] = 59;
    rookMoves[ 61 ][ 0 ][ 2 ] = 58;
    rookMoves[ 61 ][ 0 ][ 3 ] = 57;
    rookMoves[ 61 ][ 0 ][ 4 ] = 56;
    rookMoves[ 61 ][ 1 ][ 0 ] = 62;
    rookMoves[ 61 ][ 1 ][ 1 ] = 63;
    rookMoves[ 61 ][ 2 ][ 0 ] = 53;
    rookMoves[ 61 ][ 2 ][ 1 ] = 45;
    rookMoves[ 61 ][ 2 ][ 2 ] = 37;
    rookMoves[ 61 ][ 2 ][ 3 ] = 29;
    rookMoves[ 61 ][ 2 ][ 4 ] = 21;
    rookMoves[ 61 ][ 2 ][ 5 ] = 13;
    rookMoves[ 61 ][ 2 ][ 6 ] = 5;

    rookMoves[ 62 ] = new int[ 3 ][];
    rookMoves[ 62 ][ 0 ] = new int[ 6 ];
    rookMoves[ 62 ][ 1 ] = new int[ 1 ];
    rookMoves[ 62 ][ 2 ] = new int[ 7 ];
    rookMoves[ 62 ][ 0 ][ 0 ] = 61;
    rookMoves[ 62 ][ 0 ][ 1 ] = 60;
    rookMoves[ 62 ][ 0 ][ 2 ] = 59;
    rookMoves[ 62 ][ 0 ][ 3 ] = 58;
    rookMoves[ 62 ][ 0 ][ 4 ] = 57;
    rookMoves[ 62 ][ 0 ][ 5 ] = 56;
    rookMoves[ 62 ][ 1 ][ 0 ] = 63;
    rookMoves[ 62 ][ 2 ][ 0 ] = 54;
    rookMoves[ 62 ][ 2 ][ 1 ] = 46;
    rookMoves[ 62 ][ 2 ][ 2 ] = 38;
    rookMoves[ 62 ][ 2 ][ 3 ] = 30;
    rookMoves[ 62 ][ 2 ][ 4 ] = 22;
    rookMoves[ 62 ][ 2 ][ 5 ] = 14;
    rookMoves[ 62 ][ 2 ][ 6 ] = 6;

    rookMoves[ 63 ] = new int[ 2 ][];
    rookMoves[ 63 ][ 0 ] = new int[ 7 ];
    rookMoves[ 63 ][ 1 ] = new int[ 7 ];
    rookMoves[ 63 ][ 0 ][ 0 ] = 62;
    rookMoves[ 63 ][ 0 ][ 1 ] = 61;
    rookMoves[ 63 ][ 0 ][ 2 ] = 60;
    rookMoves[ 63 ][ 0 ][ 3 ] = 59;
    rookMoves[ 63 ][ 0 ][ 4 ] = 58;
    rookMoves[ 63 ][ 0 ][ 5 ] = 57;
    rookMoves[ 63 ][ 0 ][ 6 ] = 56;
    rookMoves[ 63 ][ 1 ][ 0 ] = 55;
    rookMoves[ 63 ][ 1 ][ 1 ] = 47;
    rookMoves[ 63 ][ 1 ][ 2 ] = 39;
    rookMoves[ 63 ][ 1 ][ 3 ] = 31;
    rookMoves[ 63 ][ 1 ][ 4 ] = 23;
    rookMoves[ 63 ][ 1 ][ 5 ] = 15;
    rookMoves[ 63 ][ 1 ][ 6 ] = 7;

    knightMoves = new int[ 64 ][];
    knightMoves[ 0 ] = new int[ 2 ];
    knightMoves[ 0 ][ 0 ] = 10;
    knightMoves[ 0 ][ 1 ] = 17;
    knightMoves[ 1 ] = new int[ 3 ];
    knightMoves[ 1 ][ 0 ] = 16;
    knightMoves[ 1 ][ 1 ] = 18;
    knightMoves[ 1 ][ 2 ] = 11;
    knightMoves[ 2 ] = new int[ 4 ];
    knightMoves[ 2 ][ 0 ] = 8;
    knightMoves[ 2 ][ 1 ] = 12;
    knightMoves[ 2 ][ 2 ] = 17;
    knightMoves[ 2 ][ 3 ] = 19;
    knightMoves[ 3 ] = new int[ 4 ];
    knightMoves[ 3 ][ 0 ] = 9;
    knightMoves[ 3 ][ 1 ] = 13;
    knightMoves[ 3 ][ 2 ] = 18;
    knightMoves[ 3 ][ 3 ] = 20;
    knightMoves[ 4 ] = new int[ 4 ];
    knightMoves[ 4 ][ 0 ] = 10;
    knightMoves[ 4 ][ 1 ] = 14;
    knightMoves[ 4 ][ 2 ] = 21;
    knightMoves[ 4 ][ 3 ] = 19;
    knightMoves[ 5 ] = new int[ 4 ];
    knightMoves[ 5 ][ 0 ] = 11;
    knightMoves[ 5 ][ 1 ] = 15;
    knightMoves[ 5 ][ 2 ] = 22;
    knightMoves[ 5 ][ 3 ] = 20;
    knightMoves[ 6 ] = new int[ 3 ];
    knightMoves[ 6 ][ 0 ] = 12;
    knightMoves[ 6 ][ 1 ] = 21;
    knightMoves[ 6 ][ 2 ] = 23;
    knightMoves[ 7 ] = new int[ 2 ];
    knightMoves[ 7 ][ 0 ] = 13;
    knightMoves[ 7 ][ 1 ] = 22;

    knightMoves[ 8 ] = new int[ 3 ];
    knightMoves[ 8 ][ 0 ] = 2;
    knightMoves[ 8 ][ 1 ] = 18;
    knightMoves[ 8 ][ 2 ] = 25;
    knightMoves[ 9 ] = new int[ 4 ];
    knightMoves[ 9 ][ 0 ] = 3;
    knightMoves[ 9 ][ 1 ] = 19;
    knightMoves[ 9 ][ 2 ] = 24;
    knightMoves[ 9 ][ 3 ] = 26;
    knightMoves[ 10 ] = new int[ 6 ];
    knightMoves[ 10 ][ 0 ] = 0;
    knightMoves[ 10 ][ 1 ] = 4;
    knightMoves[ 10 ][ 2 ] = 20;
    knightMoves[ 10 ][ 3 ] = 27;
    knightMoves[ 10 ][ 4 ] = 25;
    knightMoves[ 10 ][ 5 ] = 16;
    knightMoves[ 11 ] = new int[ 6 ];
    knightMoves[ 11 ][ 0 ] = 1;
    knightMoves[ 11 ][ 1 ] = 5;
    knightMoves[ 11 ][ 2 ] = 21;
    knightMoves[ 11 ][ 3 ] = 28;
    knightMoves[ 11 ][ 4 ] = 26;
    knightMoves[ 11 ][ 5 ] = 17;
    knightMoves[ 12 ] = new int[ 6 ];
    knightMoves[ 12 ][ 0 ] = 2;
    knightMoves[ 12 ][ 1 ] = 6;
    knightMoves[ 12 ][ 2 ] = 22;
    knightMoves[ 12 ][ 3 ] = 29;
    knightMoves[ 12 ][ 4 ] = 27;
    knightMoves[ 12 ][ 5 ] = 18;
    knightMoves[ 13 ] = new int[ 6 ];
    knightMoves[ 13 ][ 0 ] = 3;
    knightMoves[ 13 ][ 1 ] = 7;
    knightMoves[ 13 ][ 2 ] = 23;
    knightMoves[ 13 ][ 3 ] = 30;
    knightMoves[ 13 ][ 4 ] = 28;
    knightMoves[ 13 ][ 5 ] = 19;
    knightMoves[ 14 ] = new int[ 4 ];
    knightMoves[ 14 ][ 0 ] = 31;
    knightMoves[ 14 ][ 1 ] = 29;
    knightMoves[ 14 ][ 2 ] = 20;
    knightMoves[ 14 ][ 3 ] = 4;
    knightMoves[ 15 ] = new int[ 3 ];
    knightMoves[ 15 ][ 0 ] = 5;
    knightMoves[ 15 ][ 1 ] = 21;
    knightMoves[ 15 ][ 2 ] = 30;

    knightMoves[ 16 ] = new int[ 4 ];
    knightMoves[ 16 ][ 0 ] = 1;
    knightMoves[ 16 ][ 1 ] = 10;
    knightMoves[ 16 ][ 2 ] = 26;
    knightMoves[ 16 ][ 3 ] = 33;
    knightMoves[ 17 ] = new int[ 6 ];
    knightMoves[ 17 ][ 0 ] = 0;
    knightMoves[ 17 ][ 1 ] = 2;
    knightMoves[ 17 ][ 2 ] = 11;
    knightMoves[ 17 ][ 3 ] = 27;
    knightMoves[ 17 ][ 4 ] = 34;
    knightMoves[ 17 ][ 5 ] = 32;
    knightMoves[ 18 ] = new int[ 8 ];
    knightMoves[ 18 ][ 0 ] = 1;
    knightMoves[ 18 ][ 1 ] = 3;
    knightMoves[ 18 ][ 2 ] = 12;
    knightMoves[ 18 ][ 3 ] = 28;
    knightMoves[ 18 ][ 4 ] = 35;
    knightMoves[ 18 ][ 5 ] = 33;
    knightMoves[ 18 ][ 6 ] = 24;
    knightMoves[ 18 ][ 7 ] = 8;
    knightMoves[ 19 ] = new int[ 8 ];
    knightMoves[ 19 ][ 0 ] = 2;
    knightMoves[ 19 ][ 1 ] = 4;
    knightMoves[ 19 ][ 2 ] = 13;
    knightMoves[ 19 ][ 3 ] = 29;
    knightMoves[ 19 ][ 4 ] = 36;
    knightMoves[ 19 ][ 5 ] = 34;
    knightMoves[ 19 ][ 6 ] = 25;
    knightMoves[ 19 ][ 7 ] = 9;
    knightMoves[ 20 ] = new int[ 8 ];
    knightMoves[ 20 ][ 0 ] = 3;
    knightMoves[ 20 ][ 1 ] = 5;
    knightMoves[ 20 ][ 2 ] = 14;
    knightMoves[ 20 ][ 3 ] = 30;
    knightMoves[ 20 ][ 4 ] = 37;
    knightMoves[ 20 ][ 5 ] = 35;
    knightMoves[ 20 ][ 6 ] = 26;
    knightMoves[ 20 ][ 7 ] = 10;
    knightMoves[ 21 ] = new int[ 8 ];
    knightMoves[ 21 ][ 0 ] = 4;
    knightMoves[ 21 ][ 1 ] = 6;
    knightMoves[ 21 ][ 2 ] = 15;
    knightMoves[ 21 ][ 3 ] = 31;
    knightMoves[ 21 ][ 4 ] = 38;
    knightMoves[ 21 ][ 5 ] = 36;
    knightMoves[ 21 ][ 6 ] = 27;
    knightMoves[ 21 ][ 7 ] = 11;
    knightMoves[ 22 ] = new int[ 6 ];
    knightMoves[ 22 ][ 0 ] = 5;
    knightMoves[ 22 ][ 1 ] = 7;
    knightMoves[ 22 ][ 2 ] = 39;
    knightMoves[ 22 ][ 3 ] = 37;
    knightMoves[ 22 ][ 4 ] = 28;
    knightMoves[ 22 ][ 5 ] = 12;
    knightMoves[ 23 ] = new int[ 4 ];
    knightMoves[ 23 ][ 0 ] = 6;
    knightMoves[ 23 ][ 1 ] = 38;
    knightMoves[ 23 ][ 2 ] = 29;
    knightMoves[ 23 ][ 3 ] = 13;

    knightMoves[ 24 ] = new int[ 4 ];
    knightMoves[ 24 ][ 0 ] = 9;
    knightMoves[ 24 ][ 1 ] = 18;
    knightMoves[ 24 ][ 2 ] = 34;
    knightMoves[ 24 ][ 3 ] = 41;
    knightMoves[ 25 ] = new int[ 6 ];
    knightMoves[ 25 ][ 0 ] = 8;
    knightMoves[ 25 ][ 1 ] = 10;
    knightMoves[ 25 ][ 2 ] = 19;
    knightMoves[ 25 ][ 3 ] = 35;
    knightMoves[ 25 ][ 4 ] = 42;
    knightMoves[ 25 ][ 5 ] = 40;
    knightMoves[ 26 ] = new int[ 8 ];
    knightMoves[ 26 ][ 0 ] = 9;
    knightMoves[ 26 ][ 1 ] = 11;
    knightMoves[ 26 ][ 2 ] = 20;
    knightMoves[ 26 ][ 3 ] = 36;
    knightMoves[ 26 ][ 4 ] = 43;
    knightMoves[ 26 ][ 5 ] = 41;
    knightMoves[ 26 ][ 6 ] = 32;
    knightMoves[ 26 ][ 7 ] = 16;
    knightMoves[ 27 ] = new int[ 8 ];
    knightMoves[ 27 ][ 0 ] = 10;
    knightMoves[ 27 ][ 1 ] = 12;
    knightMoves[ 27 ][ 2 ] = 21;
    knightMoves[ 27 ][ 3 ] = 37;
    knightMoves[ 27 ][ 4 ] = 44;
    knightMoves[ 27 ][ 5 ] = 42;
    knightMoves[ 27 ][ 6 ] = 33;
    knightMoves[ 27 ][ 7 ] = 17;
    knightMoves[ 28 ] = new int[ 8 ];
    knightMoves[ 28 ][ 0 ] = 11;
    knightMoves[ 28 ][ 1 ] = 13;
    knightMoves[ 28 ][ 2 ] = 22;
    knightMoves[ 28 ][ 3 ] = 38;
    knightMoves[ 28 ][ 4 ] = 45;
    knightMoves[ 28 ][ 5 ] = 43;
    knightMoves[ 28 ][ 6 ] = 34;
    knightMoves[ 28 ][ 7 ] = 18;
    knightMoves[ 29 ] = new int[ 8 ];
    knightMoves[ 29 ][ 0 ] = 12;
    knightMoves[ 29 ][ 1 ] = 14;
    knightMoves[ 29 ][ 2 ] = 23;
    knightMoves[ 29 ][ 3 ] = 39;
    knightMoves[ 29 ][ 4 ] = 46;
    knightMoves[ 29 ][ 5 ] = 44;
    knightMoves[ 29 ][ 6 ] = 35;
    knightMoves[ 29 ][ 7 ] = 19;
    knightMoves[ 30 ] = new int[ 6 ];
    knightMoves[ 30 ][ 0 ] = 13;
    knightMoves[ 30 ][ 1 ] = 15;
    knightMoves[ 30 ][ 2 ] = 47;
    knightMoves[ 30 ][ 3 ] = 45;
    knightMoves[ 30 ][ 4 ] = 36;
    knightMoves[ 30 ][ 5 ] = 20;
    knightMoves[ 31 ] = new int[ 4 ];
    knightMoves[ 31 ][ 0 ] = 14;
    knightMoves[ 31 ][ 1 ] = 46;
    knightMoves[ 31 ][ 2 ] = 37;
    knightMoves[ 31 ][ 3 ] = 21;

    knightMoves[ 32 ] = new int[ 4 ];
    knightMoves[ 32 ][ 0 ] = 17;
    knightMoves[ 32 ][ 1 ] = 26;
    knightMoves[ 32 ][ 2 ] = 42;
    knightMoves[ 32 ][ 3 ] = 49;
    knightMoves[ 33 ] = new int[ 6 ];
    knightMoves[ 33 ][ 0 ] = 16;
    knightMoves[ 33 ][ 1 ] = 18;
    knightMoves[ 33 ][ 2 ] = 27;
    knightMoves[ 33 ][ 3 ] = 43;
    knightMoves[ 33 ][ 4 ] = 50;
    knightMoves[ 33 ][ 5 ] = 48;
    knightMoves[ 34 ] = new int[ 8 ];
    knightMoves[ 34 ][ 0 ] = 17;
    knightMoves[ 34 ][ 1 ] = 19;
    knightMoves[ 34 ][ 2 ] = 28;
    knightMoves[ 34 ][ 3 ] = 44;
    knightMoves[ 34 ][ 4 ] = 51;
    knightMoves[ 34 ][ 5 ] = 49;
    knightMoves[ 34 ][ 6 ] = 40;
    knightMoves[ 34 ][ 7 ] = 24;
    knightMoves[ 35 ] = new int[ 8 ];
    knightMoves[ 35 ][ 0 ] = 18;
    knightMoves[ 35 ][ 1 ] = 20;
    knightMoves[ 35 ][ 2 ] = 29;
    knightMoves[ 35 ][ 3 ] = 45;
    knightMoves[ 35 ][ 4 ] = 52;
    knightMoves[ 35 ][ 5 ] = 50;
    knightMoves[ 35 ][ 6 ] = 41;
    knightMoves[ 35 ][ 7 ] = 25;
    knightMoves[ 36 ] = new int[ 8 ];
    knightMoves[ 36 ][ 0 ] = 19;
    knightMoves[ 36 ][ 1 ] = 21;
    knightMoves[ 36 ][ 2 ] = 30;
    knightMoves[ 36 ][ 3 ] = 46;
    knightMoves[ 36 ][ 4 ] = 53;
    knightMoves[ 36 ][ 5 ] = 51;
    knightMoves[ 36 ][ 6 ] = 42;
    knightMoves[ 36 ][ 7 ] = 26;
    knightMoves[ 37 ] = new int[ 8 ];
    knightMoves[ 37 ][ 0 ] = 20;
    knightMoves[ 37 ][ 1 ] = 22;
    knightMoves[ 37 ][ 2 ] = 31;
    knightMoves[ 37 ][ 3 ] = 47;
    knightMoves[ 37 ][ 4 ] = 54;
    knightMoves[ 37 ][ 5 ] = 52;
    knightMoves[ 37 ][ 6 ] = 43;
    knightMoves[ 37 ][ 7 ] = 27;
    knightMoves[ 38 ] = new int[ 6 ];
    knightMoves[ 38 ][ 0 ] = 21;
    knightMoves[ 38 ][ 1 ] = 23;
    knightMoves[ 38 ][ 2 ] = 55;
    knightMoves[ 38 ][ 3 ] = 53;
    knightMoves[ 38 ][ 4 ] = 44;
    knightMoves[ 38 ][ 5 ] = 28;
    knightMoves[ 39 ] = new int[ 4 ];
    knightMoves[ 39 ][ 0 ] = 22;
    knightMoves[ 39 ][ 1 ] = 54;
    knightMoves[ 39 ][ 2 ] = 45;
    knightMoves[ 39 ][ 3 ] = 29;

    knightMoves[ 40 ] = new int[ 4 ];
    knightMoves[ 40 ][ 0 ] = 25;
    knightMoves[ 40 ][ 1 ] = 34;
    knightMoves[ 40 ][ 2 ] = 50;
    knightMoves[ 40 ][ 3 ] = 57;
    knightMoves[ 41 ] = new int[ 6 ];
    knightMoves[ 41 ][ 0 ] = 26;
    knightMoves[ 41 ][ 1 ] = 24;
    knightMoves[ 41 ][ 2 ] = 35;
    knightMoves[ 41 ][ 3 ] = 51;
    knightMoves[ 41 ][ 4 ] = 58;
    knightMoves[ 41 ][ 5 ] = 56;
    knightMoves[ 42 ] = new int[ 8 ];
    knightMoves[ 42 ][ 0 ] = 25;
    knightMoves[ 42 ][ 1 ] = 27;
    knightMoves[ 42 ][ 2 ] = 36;
    knightMoves[ 42 ][ 3 ] = 52;
    knightMoves[ 42 ][ 4 ] = 59;
    knightMoves[ 42 ][ 5 ] = 57;
    knightMoves[ 42 ][ 6 ] = 48;
    knightMoves[ 42 ][ 7 ] = 32;
    knightMoves[ 43 ] = new int[ 8 ];
    knightMoves[ 43 ][ 0 ] = 26;
    knightMoves[ 43 ][ 1 ] = 28;
    knightMoves[ 43 ][ 2 ] = 37;
    knightMoves[ 43 ][ 3 ] = 53;
    knightMoves[ 43 ][ 4 ] = 60;
    knightMoves[ 43 ][ 5 ] = 58;
    knightMoves[ 43 ][ 6 ] = 49;
    knightMoves[ 43 ][ 7 ] = 33;
    knightMoves[ 44 ] = new int[ 8 ];
    knightMoves[ 44 ][ 0 ] = 27;
    knightMoves[ 44 ][ 1 ] = 29;
    knightMoves[ 44 ][ 2 ] = 38;
    knightMoves[ 44 ][ 3 ] = 54;
    knightMoves[ 44 ][ 4 ] = 61;
    knightMoves[ 44 ][ 5 ] = 59;
    knightMoves[ 44 ][ 6 ] = 50;
    knightMoves[ 44 ][ 7 ] = 34;
    knightMoves[ 45 ] = new int[ 8 ];
    knightMoves[ 45 ][ 0 ] = 28;
    knightMoves[ 45 ][ 1 ] = 30;
    knightMoves[ 45 ][ 2 ] = 39;
    knightMoves[ 45 ][ 3 ] = 55;
    knightMoves[ 45 ][ 4 ] = 62;
    knightMoves[ 45 ][ 5 ] = 60;
    knightMoves[ 45 ][ 6 ] = 51;
    knightMoves[ 45 ][ 7 ] = 35;
    knightMoves[ 46 ] = new int[ 6 ];
    knightMoves[ 46 ][ 0 ] = 29;
    knightMoves[ 46 ][ 1 ] = 31;
    knightMoves[ 46 ][ 2 ] = 63;
    knightMoves[ 46 ][ 3 ] = 61;
    knightMoves[ 46 ][ 4 ] = 52;
    knightMoves[ 46 ][ 5 ] = 36;
    knightMoves[ 47 ] = new int[ 4 ];
    knightMoves[ 47 ][ 0 ] = 30;
    knightMoves[ 47 ][ 1 ] = 62;
    knightMoves[ 47 ][ 2 ] = 53;
    knightMoves[ 47 ][ 3 ] = 37;

    knightMoves[ 48 ] = new int[ 3 ];
    knightMoves[ 48 ][ 0 ] = 33;
    knightMoves[ 48 ][ 1 ] = 42;
    knightMoves[ 48 ][ 2 ] = 58;
    knightMoves[ 49 ] = new int[ 4 ];
    knightMoves[ 49 ][ 0 ] = 32;
    knightMoves[ 49 ][ 1 ] = 34;
    knightMoves[ 49 ][ 2 ] = 43;
    knightMoves[ 49 ][ 3 ] = 59;
    knightMoves[ 50 ] = new int[ 6 ];
    knightMoves[ 50 ][ 0 ] = 40;
    knightMoves[ 50 ][ 1 ] = 33;
    knightMoves[ 50 ][ 2 ] = 35;
    knightMoves[ 50 ][ 3 ] = 44;
    knightMoves[ 50 ][ 4 ] = 60;
    knightMoves[ 50 ][ 5 ] = 56;
    knightMoves[ 51 ] = new int[ 6 ];
    knightMoves[ 51 ][ 0 ] = 41;
    knightMoves[ 51 ][ 1 ] = 34;
    knightMoves[ 51 ][ 2 ] = 36;
    knightMoves[ 51 ][ 3 ] = 45;
    knightMoves[ 51 ][ 4 ] = 61;
    knightMoves[ 51 ][ 5 ] = 57;
    knightMoves[ 52 ] = new int[ 6 ];
    knightMoves[ 52 ][ 0 ] = 42;
    knightMoves[ 52 ][ 1 ] = 35;
    knightMoves[ 52 ][ 2 ] = 37;
    knightMoves[ 52 ][ 3 ] = 46;
    knightMoves[ 52 ][ 4 ] = 62;
    knightMoves[ 52 ][ 5 ] = 58;
    knightMoves[ 53 ] = new int[ 6 ];
    knightMoves[ 53 ][ 0 ] = 43;
    knightMoves[ 53 ][ 1 ] = 36;
    knightMoves[ 53 ][ 2 ] = 38;
    knightMoves[ 53 ][ 3 ] = 47;
    knightMoves[ 53 ][ 4 ] = 63;
    knightMoves[ 53 ][ 5 ] = 59;
    knightMoves[ 54 ] = new int[ 4 ];
    knightMoves[ 54 ][ 0 ] = 39;
    knightMoves[ 54 ][ 1 ] = 60;
    knightMoves[ 54 ][ 2 ] = 44;
    knightMoves[ 54 ][ 3 ] = 37;
    knightMoves[ 55 ] = new int[ 3 ];
    knightMoves[ 55 ][ 0 ] = 38;
    knightMoves[ 55 ][ 1 ] = 45;
    knightMoves[ 55 ][ 2 ] = 61;

    knightMoves[ 56 ] = new int[ 2 ];
    knightMoves[ 56 ][ 0 ] = 41;
    knightMoves[ 56 ][ 1 ] = 50;
    knightMoves[ 57 ] = new int[ 3 ];
    knightMoves[ 57 ][ 0 ] = 40;
    knightMoves[ 57 ][ 1 ] = 42;
    knightMoves[ 57 ][ 2 ] = 51;
    knightMoves[ 58 ] = new int[ 4 ];
    knightMoves[ 58 ][ 0 ] = 48;
    knightMoves[ 58 ][ 1 ] = 41;
    knightMoves[ 58 ][ 2 ] = 43;
    knightMoves[ 58 ][ 3 ] = 52;
    knightMoves[ 59 ] = new int[ 4 ];
    knightMoves[ 59 ][ 0 ] = 49;
    knightMoves[ 59 ][ 1 ] = 42;
    knightMoves[ 59 ][ 2 ] = 44;
    knightMoves[ 59 ][ 3 ] = 53;
    knightMoves[ 60 ] = new int[ 4 ];
    knightMoves[ 60 ][ 0 ] = 50;
    knightMoves[ 60 ][ 1 ] = 43;
    knightMoves[ 60 ][ 2 ] = 45;
    knightMoves[ 60 ][ 3 ] = 54;
    knightMoves[ 61 ] = new int[ 4 ];
    knightMoves[ 61 ][ 0 ] = 51;
    knightMoves[ 61 ][ 1 ] = 44;
    knightMoves[ 61 ][ 2 ] = 46;
    knightMoves[ 61 ][ 3 ] = 55;
    knightMoves[ 62 ] = new int[ 3 ];
    knightMoves[ 62 ][ 0 ] = 52;
    knightMoves[ 62 ][ 1 ] = 45;
    knightMoves[ 62 ][ 2 ] = 47;
    knightMoves[ 63 ] = new int[ 2 ];
    knightMoves[ 63 ][ 0 ] = 53;
    knightMoves[ 63 ][ 1 ] = 46;

    bishopMoves = new int[ 64 ][][];
    bishopMoves[ 0 ] = new int [ 1 ][ 7 ];
    bishopMoves[ 0 ][ 0 ][ 0 ] = 9;
    bishopMoves[ 0 ][ 0 ][ 1 ] = 18;
    bishopMoves[ 0 ][ 0 ][ 2 ] = 27;
    bishopMoves[ 0 ][ 0 ][ 3 ] = 36;
    bishopMoves[ 0 ][ 0 ][ 4 ] = 45;
    bishopMoves[ 0 ][ 0 ][ 5 ] = 54;
    bishopMoves[ 0 ][ 0 ][ 6 ] = 63;

    bishopMoves[ 1 ] = new int[ 2 ][];
    bishopMoves[ 1 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 1 ][ 0 ][ 0 ] = 8;
    bishopMoves[ 1 ][ 1 ] = new int[ 6 ];
    bishopMoves[ 1 ][ 1 ][ 0 ] = 10;
    bishopMoves[ 1 ][ 1 ][ 1 ] = 19;
    bishopMoves[ 1 ][ 1 ][ 2 ] = 28;
    bishopMoves[ 1 ][ 1 ][ 3 ] = 37;
    bishopMoves[ 1 ][ 1 ][ 4 ] = 46;
    bishopMoves[ 1 ][ 1 ][ 5 ] = 55;

    bishopMoves[ 2 ] = new int[ 2 ][];
    bishopMoves[ 2 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 2 ][ 0 ][ 0 ] = 9;
    bishopMoves[ 2 ][ 0 ][ 1 ] = 16;
    bishopMoves[ 2 ][ 1 ] = new int[ 5 ];
    bishopMoves[ 2 ][ 1 ][ 0 ] = 11;
    bishopMoves[ 2 ][ 1 ][ 1 ] = 20;
    bishopMoves[ 2 ][ 1 ][ 2 ] = 29;
    bishopMoves[ 2 ][ 1 ][ 3 ] = 38;
    bishopMoves[ 2 ][ 1 ][ 4 ] = 47;

    bishopMoves[ 3 ] = new int[ 2 ][];
    bishopMoves[ 3 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 3 ][ 0 ][ 0 ] = 10;
    bishopMoves[ 3 ][ 0 ][ 1 ] = 17;
    bishopMoves[ 3 ][ 0 ][ 2 ] = 24;
    bishopMoves[ 3 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 3 ][ 1 ][ 0 ] = 12;
    bishopMoves[ 3 ][ 1 ][ 1 ] = 21;
    bishopMoves[ 3 ][ 1 ][ 2 ] = 30;
    bishopMoves[ 3 ][ 1 ][ 3 ] = 39;

    bishopMoves[ 4 ] = new int[ 2 ][];
    bishopMoves[ 4 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 4 ][ 0 ][ 0 ] = 11;
    bishopMoves[ 4 ][ 0 ][ 1 ] = 18;
    bishopMoves[ 4 ][ 0 ][ 2 ] = 25;
    bishopMoves[ 4 ][ 0 ][ 3 ] = 32;
    bishopMoves[ 4 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 4 ][ 1 ][ 0 ] = 13;
    bishopMoves[ 4 ][ 1 ][ 1 ] = 22;
    bishopMoves[ 4 ][ 1 ][ 2 ] = 31;

    bishopMoves[ 5 ] = new int[ 2 ][];
    bishopMoves[ 5 ][ 0 ] = new int[ 5 ];
    bishopMoves[ 5 ][ 0 ][ 0 ] = 12;
    bishopMoves[ 5 ][ 0 ][ 1 ] = 19;
    bishopMoves[ 5 ][ 0 ][ 2 ] = 26;
    bishopMoves[ 5 ][ 0 ][ 3 ] = 33;
    bishopMoves[ 5 ][ 0 ][ 4 ] = 40;
    bishopMoves[ 5 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 5 ][ 1 ][ 0 ] = 14;
    bishopMoves[ 5 ][ 1 ][ 1 ] = 23;

    bishopMoves[ 6 ] = new int[ 2 ][];
    bishopMoves[ 6 ][ 0 ] = new int[ 6 ];
    bishopMoves[ 6 ][ 0 ][ 0 ] = 13;
    bishopMoves[ 6 ][ 0 ][ 1 ] = 20;
    bishopMoves[ 6 ][ 0 ][ 2 ] = 27;
    bishopMoves[ 6 ][ 0 ][ 3 ] = 34;
    bishopMoves[ 6 ][ 0 ][ 4 ] = 41;
    bishopMoves[ 6 ][ 0 ][ 5 ] = 48;
    bishopMoves[ 6 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 6 ][ 1 ][ 0 ] = 15;

    bishopMoves[ 7 ] = new int [ 1 ][ 7 ];
    bishopMoves[ 7 ][ 0 ][ 0 ] = 14;
    bishopMoves[ 7 ][ 0 ][ 1 ] = 21;
    bishopMoves[ 7 ][ 0 ][ 2 ] = 28;
    bishopMoves[ 7 ][ 0 ][ 3 ] = 35;
    bishopMoves[ 7 ][ 0 ][ 4 ] = 42;
    bishopMoves[ 7 ][ 0 ][ 5 ] = 49;
    bishopMoves[ 7 ][ 0 ][ 6 ] = 56;

    bishopMoves[ 8 ] = new int[ 2 ][];
    bishopMoves[ 8 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 8 ][ 0 ][ 0 ] = 1;
    bishopMoves[ 8 ][ 1 ] = new int[ 6 ];
    bishopMoves[ 8 ][ 1 ][ 0 ] = 17;
    bishopMoves[ 8 ][ 1 ][ 1 ] = 26;
    bishopMoves[ 8 ][ 1 ][ 2 ] = 35;
    bishopMoves[ 8 ][ 1 ][ 3 ] = 44;
    bishopMoves[ 8 ][ 1 ][ 4 ] = 53;
    bishopMoves[ 8 ][ 1 ][ 5 ] = 62;

    bishopMoves[ 9 ] = new int[ 4 ][];
    bishopMoves[ 9 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 9 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 9 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 9 ][ 3 ] = new int[ 6 ];
    bishopMoves[ 9 ][ 0 ][ 0 ] = 0;
    bishopMoves[ 9 ][ 1 ][ 0 ] = 2;
    bishopMoves[ 9 ][ 2 ][ 0 ] = 16;
    bishopMoves[ 9 ][ 3 ][ 0 ] = 18;
    bishopMoves[ 9 ][ 3 ][ 1 ] = 27;
    bishopMoves[ 9 ][ 3 ][ 2 ] = 36;
    bishopMoves[ 9 ][ 3 ][ 3 ] = 45;
    bishopMoves[ 9 ][ 3 ][ 4 ] = 54;
    bishopMoves[ 9 ][ 3 ][ 5 ] = 63;

    bishopMoves[ 10 ] = new int[ 4 ][];
    bishopMoves[ 10 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 10 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 10 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 10 ][ 3 ] = new int[ 5 ];
    bishopMoves[ 10 ][ 0 ][ 0 ] = 1;
    bishopMoves[ 10 ][ 1 ][ 0 ] = 3;
    bishopMoves[ 10 ][ 2 ][ 0 ] = 17;
    bishopMoves[ 10 ][ 2 ][ 1 ] = 24;
    bishopMoves[ 10 ][ 3 ][ 0 ] = 19;
    bishopMoves[ 10 ][ 3 ][ 1 ] = 28;
    bishopMoves[ 10 ][ 3 ][ 2 ] = 37;
    bishopMoves[ 10 ][ 3 ][ 3 ] = 46;
    bishopMoves[ 10 ][ 3 ][ 4 ] = 55;

    bishopMoves[ 11 ] = new int[ 4 ][];
    bishopMoves[ 11 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 11 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 11 ][ 2 ] = new int[ 3 ];
    bishopMoves[ 11 ][ 3 ] = new int[ 4 ];
    bishopMoves[ 11 ][ 0 ][ 0 ] = 2;
    bishopMoves[ 11 ][ 1 ][ 0 ] = 4;
    bishopMoves[ 11 ][ 2 ][ 0 ] = 18;
    bishopMoves[ 11 ][ 2 ][ 1 ] = 25;
    bishopMoves[ 11 ][ 2 ][ 2 ] = 32;
    bishopMoves[ 11 ][ 3 ][ 0 ] = 20;
    bishopMoves[ 11 ][ 3 ][ 1 ] = 29;
    bishopMoves[ 11 ][ 3 ][ 2 ] = 38;
    bishopMoves[ 11 ][ 3 ][ 3 ] = 47;

    bishopMoves[ 12 ] = new int[ 4 ][];
    bishopMoves[ 12 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 12 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 12 ][ 2 ] = new int[ 4 ];
    bishopMoves[ 12 ][ 3 ] = new int[ 3 ];
    bishopMoves[ 12 ][ 0 ][ 0 ] = 3;
    bishopMoves[ 12 ][ 1 ][ 0 ] = 5;
    bishopMoves[ 12 ][ 2 ][ 0 ] = 19;
    bishopMoves[ 12 ][ 2 ][ 1 ] = 26;
    bishopMoves[ 12 ][ 2 ][ 2 ] = 33;
    bishopMoves[ 12 ][ 2 ][ 3 ] = 40;
    bishopMoves[ 12 ][ 3 ][ 0 ] = 21;
    bishopMoves[ 12 ][ 3 ][ 1 ] = 30;
    bishopMoves[ 12 ][ 3 ][ 2 ] = 39;

    bishopMoves[ 13 ] = new int[ 4 ][];
    bishopMoves[ 13 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 13 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 13 ][ 2 ] = new int[ 5 ];
    bishopMoves[ 13 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 13 ][ 0 ][ 0 ] = 4;
    bishopMoves[ 13 ][ 1 ][ 0 ] = 6;
    bishopMoves[ 13 ][ 2 ][ 0 ] = 20;
    bishopMoves[ 13 ][ 2 ][ 1 ] = 27;
    bishopMoves[ 13 ][ 2 ][ 2 ] = 34;
    bishopMoves[ 13 ][ 2 ][ 3 ] = 41;
    bishopMoves[ 13 ][ 2 ][ 4 ] = 48;
    bishopMoves[ 13 ][ 3 ][ 0 ] = 22;
    bishopMoves[ 13 ][ 3 ][ 1 ] = 31;

    bishopMoves[ 14 ] = new int[ 4 ][];
    bishopMoves[ 14 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 14 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 14 ][ 2 ] = new int[ 6 ];
    bishopMoves[ 14 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 14 ][ 0 ][ 0 ] = 5;
    bishopMoves[ 14 ][ 1 ][ 0 ] = 7;
    bishopMoves[ 14 ][ 2 ][ 0 ] = 21;
    bishopMoves[ 14 ][ 2 ][ 1 ] = 28;
    bishopMoves[ 14 ][ 2 ][ 2 ] = 35;
    bishopMoves[ 14 ][ 2 ][ 3 ] = 42;
    bishopMoves[ 14 ][ 2 ][ 4 ] = 49;
    bishopMoves[ 14 ][ 2 ][ 5 ] = 56;
    bishopMoves[ 14 ][ 3 ][ 0 ] = 23;

    bishopMoves[ 15 ] = new int[ 2 ][];
    bishopMoves[ 15 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 15 ][ 1 ] = new int[ 6 ];
    bishopMoves[ 15 ][ 0 ][ 0 ] = 6;
    bishopMoves[ 15 ][ 1 ][ 0 ] = 22;
    bishopMoves[ 15 ][ 1 ][ 1 ] = 29;
    bishopMoves[ 15 ][ 1 ][ 2 ] = 36;
    bishopMoves[ 15 ][ 1 ][ 3 ] = 43;
    bishopMoves[ 15 ][ 1 ][ 4 ] = 50;
    bishopMoves[ 15 ][ 1 ][ 5 ] = 57;

    bishopMoves[ 16 ] = new int[ 2 ][];
    bishopMoves[ 16 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 16 ][ 1 ] = new int[ 5 ];
    bishopMoves[ 16 ][ 0 ][ 0 ] = 9;
    bishopMoves[ 16 ][ 0 ][ 1 ] = 2;
    bishopMoves[ 16 ][ 1 ][ 0 ] = 25;
    bishopMoves[ 16 ][ 1 ][ 1 ] = 34;
    bishopMoves[ 16 ][ 1 ][ 2 ] = 43;
    bishopMoves[ 16 ][ 1 ][ 3 ] = 52;
    bishopMoves[ 16 ][ 1 ][ 4 ] = 61;

    bishopMoves[ 17 ] = new int[ 4 ][];
    bishopMoves[ 17 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 17 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 17 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 17 ][ 3 ] = new int[ 5 ];
    bishopMoves[ 17 ][ 0 ][ 0 ] = 8;
    bishopMoves[ 17 ][ 1 ][ 0 ] = 10;
    bishopMoves[ 17 ][ 1 ][ 1 ] = 3;
    bishopMoves[ 17 ][ 2 ][ 0 ] = 24;
    bishopMoves[ 17 ][ 3 ][ 0 ] = 26;
    bishopMoves[ 17 ][ 3 ][ 1 ] = 35;
    bishopMoves[ 17 ][ 3 ][ 2 ] = 44;
    bishopMoves[ 17 ][ 3 ][ 3 ] = 53;
    bishopMoves[ 17 ][ 3 ][ 4 ] = 62;

    bishopMoves[ 18 ] = new int[ 4 ][];
    bishopMoves[ 18 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 18 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 18 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 18 ][ 3 ] = new int[ 5 ];
    bishopMoves[ 18 ][ 0 ][ 0 ] = 9;
    bishopMoves[ 18 ][ 0 ][ 1 ] = 0;
    bishopMoves[ 18 ][ 1 ][ 0 ] = 11;
    bishopMoves[ 18 ][ 1 ][ 1 ] = 4;
    bishopMoves[ 18 ][ 2 ][ 0 ] = 25;
    bishopMoves[ 18 ][ 2 ][ 1 ] = 32;
    bishopMoves[ 18 ][ 3 ][ 0 ] = 27;
    bishopMoves[ 18 ][ 3 ][ 1 ] = 36;
    bishopMoves[ 18 ][ 3 ][ 2 ] = 45;
    bishopMoves[ 18 ][ 3 ][ 3 ] = 54;
    bishopMoves[ 18 ][ 3 ][ 4 ] = 63;

    bishopMoves[ 19 ] = new int[ 4 ][];
    bishopMoves[ 19 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 19 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 19 ][ 2 ] = new int[ 3 ];
    bishopMoves[ 19 ][ 3 ] = new int[ 4 ];
    bishopMoves[ 19 ][ 0 ][ 0 ] = 10;
    bishopMoves[ 19 ][ 0 ][ 1 ] = 1;
    bishopMoves[ 19 ][ 1 ][ 0 ] = 12;
    bishopMoves[ 19 ][ 1 ][ 1 ] = 5;
    bishopMoves[ 19 ][ 2 ][ 0 ] = 26;
    bishopMoves[ 19 ][ 2 ][ 1 ] = 33;
    bishopMoves[ 19 ][ 2 ][ 2 ] = 40;
    bishopMoves[ 19 ][ 3 ][ 0 ] = 28;
    bishopMoves[ 19 ][ 3 ][ 1 ] = 37;
    bishopMoves[ 19 ][ 3 ][ 2 ] = 46;
    bishopMoves[ 19 ][ 3 ][ 3 ] = 55;

    bishopMoves[ 20 ] = new int[ 4 ][];
    bishopMoves[ 20 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 20 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 20 ][ 2 ] = new int[ 4 ];
    bishopMoves[ 20 ][ 3 ] = new int[ 3 ];
    bishopMoves[ 20 ][ 0 ][ 0 ] = 11;
    bishopMoves[ 20 ][ 0 ][ 1 ] = 2;
    bishopMoves[ 20 ][ 1 ][ 0 ] = 13;
    bishopMoves[ 20 ][ 1 ][ 1 ] = 6;
    bishopMoves[ 20 ][ 2 ][ 0 ] = 27;
    bishopMoves[ 20 ][ 2 ][ 1 ] = 34;
    bishopMoves[ 20 ][ 2 ][ 2 ] = 41;
    bishopMoves[ 20 ][ 2 ][ 3 ] = 48;
    bishopMoves[ 20 ][ 3 ][ 0 ] = 29;
    bishopMoves[ 20 ][ 3 ][ 1 ] = 38;
    bishopMoves[ 20 ][ 3 ][ 2 ] = 47;

    bishopMoves[ 21 ] = new int[ 4 ][];
    bishopMoves[ 21 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 21 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 21 ][ 2 ] = new int[ 5 ];
    bishopMoves[ 21 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 21 ][ 0 ][ 0 ] = 12;
    bishopMoves[ 21 ][ 0 ][ 1 ] = 3;
    bishopMoves[ 21 ][ 1 ][ 0 ] = 14;
    bishopMoves[ 21 ][ 1 ][ 1 ] = 7;
    bishopMoves[ 21 ][ 2 ][ 0 ] = 28;
    bishopMoves[ 21 ][ 2 ][ 1 ] = 35;
    bishopMoves[ 21 ][ 2 ][ 2 ] = 42;
    bishopMoves[ 21 ][ 2 ][ 3 ] = 49;
    bishopMoves[ 21 ][ 2 ][ 4 ] = 56;
    bishopMoves[ 21 ][ 3 ][ 0 ] = 30;
    bishopMoves[ 21 ][ 3 ][ 1 ] = 39;

    bishopMoves[ 22 ] = new int[ 4 ][];
    bishopMoves[ 22 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 22 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 22 ][ 2 ] = new int[ 5 ];
    bishopMoves[ 22 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 22 ][ 0 ][ 0 ] = 13;
    bishopMoves[ 22 ][ 0 ][ 1 ] = 4;
    bishopMoves[ 22 ][ 1 ][ 0 ] = 15;
    bishopMoves[ 22 ][ 2 ][ 0 ] = 29;
    bishopMoves[ 22 ][ 2 ][ 1 ] = 36;
    bishopMoves[ 22 ][ 2 ][ 2 ] = 43;
    bishopMoves[ 22 ][ 2 ][ 3 ] = 50;
    bishopMoves[ 22 ][ 2 ][ 4 ] = 57;
    bishopMoves[ 22 ][ 3 ][ 0 ] = 31;

    bishopMoves[ 23 ] = new int[ 2 ][];
    bishopMoves[ 23 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 23 ][ 1 ] = new int[ 5 ];
    bishopMoves[ 23 ][ 0 ][ 0 ] = 14;
    bishopMoves[ 23 ][ 0 ][ 1 ] = 5;
    bishopMoves[ 23 ][ 1 ][ 0 ] = 30;
    bishopMoves[ 23 ][ 1 ][ 1 ] = 37;
    bishopMoves[ 23 ][ 1 ][ 2 ] = 44;
    bishopMoves[ 23 ][ 1 ][ 3 ] = 51;
    bishopMoves[ 23 ][ 1 ][ 4 ] = 58;

    bishopMoves[ 24 ] = new int[ 2 ][];
    bishopMoves[ 24 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 24 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 24 ][ 0 ][ 0 ] = 17;
    bishopMoves[ 24 ][ 0 ][ 1 ] = 10;
    bishopMoves[ 24 ][ 0 ][ 2 ] = 3;
    bishopMoves[ 24 ][ 1 ][ 0 ] = 33;
    bishopMoves[ 24 ][ 1 ][ 1 ] = 42;
    bishopMoves[ 24 ][ 1 ][ 2 ] = 51;
    bishopMoves[ 24 ][ 1 ][ 3 ] = 60;

    bishopMoves[ 25 ] = new int[ 4 ][];
    bishopMoves[ 25 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 25 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 25 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 25 ][ 3 ] = new int[ 4 ];
    bishopMoves[ 25 ][ 0 ][ 0 ] = 16;
    bishopMoves[ 25 ][ 1 ][ 0 ] = 18;
    bishopMoves[ 25 ][ 1 ][ 1 ] = 11;
    bishopMoves[ 25 ][ 1 ][ 2 ] = 4;
    bishopMoves[ 25 ][ 2 ][ 0 ] = 32;
    bishopMoves[ 25 ][ 3 ][ 0 ] = 34;
    bishopMoves[ 25 ][ 3 ][ 1 ] = 43;
    bishopMoves[ 25 ][ 3 ][ 2 ] = 52;
    bishopMoves[ 25 ][ 3 ][ 3 ] = 61;

    bishopMoves[ 26 ] = new int[ 4 ][];
    bishopMoves[ 26 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 26 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 26 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 26 ][ 3 ] = new int[ 4 ];
    bishopMoves[ 26 ][ 0 ][ 0 ] = 17;
    bishopMoves[ 26 ][ 0 ][ 1 ] = 8;
    bishopMoves[ 26 ][ 1 ][ 0 ] = 19;
    bishopMoves[ 26 ][ 1 ][ 1 ] = 12;
    bishopMoves[ 26 ][ 1 ][ 2 ] = 5;
    bishopMoves[ 26 ][ 2 ][ 0 ] = 33;
    bishopMoves[ 26 ][ 2 ][ 1 ] = 40;
    bishopMoves[ 26 ][ 3 ][ 0 ] = 35;
    bishopMoves[ 26 ][ 3 ][ 1 ] = 44;
    bishopMoves[ 26 ][ 3 ][ 2 ] = 53;
    bishopMoves[ 26 ][ 3 ][ 3 ] = 62;

    bishopMoves[ 27 ] = new int[ 4 ][];
    bishopMoves[ 27 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 27 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 27 ][ 2 ] = new int[ 3 ];
    bishopMoves[ 27 ][ 3 ] = new int[ 4 ];
    bishopMoves[ 27 ][ 0 ][ 0 ] = 18;
    bishopMoves[ 27 ][ 0 ][ 1 ] = 9;
    bishopMoves[ 27 ][ 0 ][ 2 ] = 0;
    bishopMoves[ 27 ][ 1 ][ 0 ] = 20;
    bishopMoves[ 27 ][ 1 ][ 1 ] = 13;
    bishopMoves[ 27 ][ 1 ][ 2 ] = 6;
    bishopMoves[ 27 ][ 2 ][ 0 ] = 34;
    bishopMoves[ 27 ][ 2 ][ 1 ] = 41;
    bishopMoves[ 27 ][ 2 ][ 2 ] = 48;
    bishopMoves[ 27 ][ 3 ][ 0 ] = 36;
    bishopMoves[ 27 ][ 3 ][ 1 ] = 45;
    bishopMoves[ 27 ][ 3 ][ 2 ] = 54;
    bishopMoves[ 27 ][ 3 ][ 3 ] = 63;

    bishopMoves[ 28 ] = new int[ 4 ][];
    bishopMoves[ 28 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 28 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 28 ][ 2 ] = new int[ 4 ];
    bishopMoves[ 28 ][ 3 ] = new int[ 3 ];
    bishopMoves[ 28 ][ 0 ][ 0 ] = 19;
    bishopMoves[ 28 ][ 0 ][ 1 ] = 10;
    bishopMoves[ 28 ][ 0 ][ 2 ] = 1;
    bishopMoves[ 28 ][ 1 ][ 0 ] = 21;
    bishopMoves[ 28 ][ 1 ][ 1 ] = 14;
    bishopMoves[ 28 ][ 1 ][ 2 ] = 7;
    bishopMoves[ 28 ][ 2 ][ 0 ] = 35;
    bishopMoves[ 28 ][ 2 ][ 1 ] = 42;
    bishopMoves[ 28 ][ 2 ][ 2 ] = 49;
    bishopMoves[ 28 ][ 2 ][ 3 ] = 56;
    bishopMoves[ 28 ][ 3 ][ 0 ] = 37;
    bishopMoves[ 28 ][ 3 ][ 1 ] = 46;
    bishopMoves[ 28 ][ 3 ][ 2 ] = 55;

    bishopMoves[ 29 ] = new int[ 4 ][];
    bishopMoves[ 29 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 29 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 29 ][ 2 ] = new int[ 4 ];
    bishopMoves[ 29 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 29 ][ 0 ][ 0 ] = 20;
    bishopMoves[ 29 ][ 0 ][ 1 ] = 11;
    bishopMoves[ 29 ][ 0 ][ 2 ] = 2;
    bishopMoves[ 29 ][ 1 ][ 0 ] = 22;
    bishopMoves[ 29 ][ 1 ][ 1 ] = 15;
    bishopMoves[ 29 ][ 2 ][ 0 ] = 36;
    bishopMoves[ 29 ][ 2 ][ 1 ] = 43;
    bishopMoves[ 29 ][ 2 ][ 2 ] = 50;
    bishopMoves[ 29 ][ 2 ][ 3 ] = 57;
    bishopMoves[ 29 ][ 3 ][ 0 ] = 38;
    bishopMoves[ 29 ][ 3 ][ 1 ] = 47;

    bishopMoves[ 30 ] = new int[ 4 ][];
    bishopMoves[ 30 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 30 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 30 ][ 2 ] = new int[ 4 ];
    bishopMoves[ 30 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 30 ][ 0 ][ 0 ] = 21;
    bishopMoves[ 30 ][ 0 ][ 1 ] = 12;
    bishopMoves[ 30 ][ 0 ][ 2 ] = 3;
    bishopMoves[ 30 ][ 1 ][ 0 ] = 23;
    bishopMoves[ 30 ][ 2 ][ 0 ] = 37;
    bishopMoves[ 30 ][ 2 ][ 1 ] = 44;
    bishopMoves[ 30 ][ 2 ][ 2 ] = 51;
    bishopMoves[ 30 ][ 2 ][ 3 ] = 58;
    bishopMoves[ 30 ][ 3 ][ 0 ] = 39;

    bishopMoves[ 31 ] = new int[ 2 ][];
    bishopMoves[ 31 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 31 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 31 ][ 0 ][ 0 ] = 22;
    bishopMoves[ 31 ][ 0 ][ 1 ] = 13;
    bishopMoves[ 31 ][ 0 ][ 2 ] = 4;
    bishopMoves[ 31 ][ 1 ][ 0 ] = 38;
    bishopMoves[ 31 ][ 1 ][ 1 ] = 45;
    bishopMoves[ 31 ][ 1 ][ 2 ] = 52;
    bishopMoves[ 31 ][ 1 ][ 3 ] = 59;

    bishopMoves[ 32 ] = new int[ 2 ][];
    bishopMoves[ 32 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 32 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 32 ][ 0 ][ 0 ] = 25;
    bishopMoves[ 32 ][ 0 ][ 1 ] = 18;
    bishopMoves[ 32 ][ 0 ][ 2 ] = 11;
    bishopMoves[ 32 ][ 0 ][ 3 ] = 4;
    bishopMoves[ 32 ][ 1 ][ 0 ] = 41;
    bishopMoves[ 32 ][ 1 ][ 1 ] = 50;
    bishopMoves[ 32 ][ 1 ][ 2 ] = 59;

    bishopMoves[ 33 ] = new int[ 4 ][];
    bishopMoves[ 33 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 33 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 33 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 33 ][ 3 ] = new int[ 3 ];
    bishopMoves[ 33 ][ 0 ][ 0 ] = 24;
    bishopMoves[ 33 ][ 1 ][ 0 ] = 26;
    bishopMoves[ 33 ][ 1 ][ 1 ] = 19;
    bishopMoves[ 33 ][ 1 ][ 2 ] = 12;
    bishopMoves[ 33 ][ 1 ][ 3 ] = 5;
    bishopMoves[ 33 ][ 2 ][ 0 ] = 40;
    bishopMoves[ 33 ][ 3 ][ 0 ] = 42;
    bishopMoves[ 33 ][ 3 ][ 1 ] = 51;
    bishopMoves[ 33 ][ 3 ][ 2 ] = 60;

    bishopMoves[ 34 ] = new int[ 4 ][];
    bishopMoves[ 34 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 34 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 34 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 34 ][ 3 ] = new int[ 3 ];
    bishopMoves[ 34 ][ 0 ][ 0 ] = 25;
    bishopMoves[ 34 ][ 0 ][ 1 ] = 16;
    bishopMoves[ 34 ][ 1 ][ 0 ] = 27;
    bishopMoves[ 34 ][ 1 ][ 1 ] = 20;
    bishopMoves[ 34 ][ 1 ][ 2 ] = 13;
    bishopMoves[ 34 ][ 1 ][ 3 ] = 6;
    bishopMoves[ 34 ][ 2 ][ 0 ] = 41;
    bishopMoves[ 34 ][ 2 ][ 1 ] = 48;
    bishopMoves[ 34 ][ 3 ][ 0 ] = 43;
    bishopMoves[ 34 ][ 3 ][ 1 ] = 52;
    bishopMoves[ 34 ][ 3 ][ 2 ] = 61;

    bishopMoves[ 35 ] = new int[ 4 ][];
    bishopMoves[ 35 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 35 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 35 ][ 2 ] = new int[ 3 ];
    bishopMoves[ 35 ][ 3 ] = new int[ 3 ];
    bishopMoves[ 35 ][ 0 ][ 0 ] = 26;
    bishopMoves[ 35 ][ 0 ][ 1 ] = 17;
    bishopMoves[ 35 ][ 0 ][ 2 ] = 8;
    bishopMoves[ 35 ][ 1 ][ 0 ] = 28;
    bishopMoves[ 35 ][ 1 ][ 1 ] = 21;
    bishopMoves[ 35 ][ 1 ][ 2 ] = 14;
    bishopMoves[ 35 ][ 1 ][ 3 ] = 7;
    bishopMoves[ 35 ][ 2 ][ 0 ] = 42;
    bishopMoves[ 35 ][ 2 ][ 1 ] = 49;
    bishopMoves[ 35 ][ 2 ][ 2 ] = 56;
    bishopMoves[ 35 ][ 3 ][ 0 ] = 44;
    bishopMoves[ 35 ][ 3 ][ 1 ] = 53;
    bishopMoves[ 35 ][ 3 ][ 2 ] = 62;

    bishopMoves[ 36 ] = new int[ 4 ][];
    bishopMoves[ 36 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 36 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 36 ][ 2 ] = new int[ 3 ];
    bishopMoves[ 36 ][ 3 ] = new int[ 3 ];
    bishopMoves[ 36 ][ 0 ][ 0 ] = 27;
    bishopMoves[ 36 ][ 0 ][ 1 ] = 18;
    bishopMoves[ 36 ][ 0 ][ 2 ] = 9;
    bishopMoves[ 36 ][ 0 ][ 3 ] = 0;
    bishopMoves[ 36 ][ 1 ][ 0 ] = 29;
    bishopMoves[ 36 ][ 1 ][ 1 ] = 22;
    bishopMoves[ 36 ][ 1 ][ 2 ] = 15;
    bishopMoves[ 36 ][ 2 ][ 0 ] = 43;
    bishopMoves[ 36 ][ 2 ][ 1 ] = 50;
    bishopMoves[ 36 ][ 2 ][ 2 ] = 57;
    bishopMoves[ 36 ][ 3 ][ 0 ] = 45;
    bishopMoves[ 36 ][ 3 ][ 1 ] = 54;
    bishopMoves[ 36 ][ 3 ][ 2 ] = 63;

    bishopMoves[ 37 ] = new int[ 4 ][];
    bishopMoves[ 37 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 37 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 37 ][ 2 ] = new int[ 3 ];
    bishopMoves[ 37 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 37 ][ 0 ][ 0 ] = 28;
    bishopMoves[ 37 ][ 0 ][ 1 ] = 19;
    bishopMoves[ 37 ][ 0 ][ 2 ] = 10;
    bishopMoves[ 37 ][ 0 ][ 3 ] = 1;
    bishopMoves[ 37 ][ 1 ][ 0 ] = 30;
    bishopMoves[ 37 ][ 1 ][ 1 ] = 23;
    bishopMoves[ 37 ][ 2 ][ 0 ] = 44;
    bishopMoves[ 37 ][ 2 ][ 1 ] = 51;
    bishopMoves[ 37 ][ 2 ][ 2 ] = 58;
    bishopMoves[ 37 ][ 3 ][ 0 ] = 46;
    bishopMoves[ 37 ][ 3 ][ 1 ] = 55;

    bishopMoves[ 38 ] = new int[ 4 ][];
    bishopMoves[ 38 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 38 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 38 ][ 2 ] = new int[ 3 ];
    bishopMoves[ 38 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 38 ][ 0 ][ 0 ] = 29;
    bishopMoves[ 38 ][ 0 ][ 1 ] = 20;
    bishopMoves[ 38 ][ 0 ][ 2 ] = 11;
    bishopMoves[ 38 ][ 0 ][ 3 ] = 2;
    bishopMoves[ 38 ][ 1 ][ 0 ] = 31;
    bishopMoves[ 38 ][ 2 ][ 0 ] = 45;
    bishopMoves[ 38 ][ 2 ][ 1 ] = 52;
    bishopMoves[ 38 ][ 2 ][ 2 ] = 59;
    bishopMoves[ 38 ][ 3 ][ 0 ] = 47;

    bishopMoves[ 39 ] = new int[ 2 ][];
    bishopMoves[ 39 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 39 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 39 ][ 0 ][ 0 ] = 30;
    bishopMoves[ 39 ][ 0 ][ 1 ] = 21;
    bishopMoves[ 39 ][ 0 ][ 2 ] = 12;
    bishopMoves[ 39 ][ 0 ][ 3 ] = 3;
    bishopMoves[ 39 ][ 1 ][ 0 ] = 46;
    bishopMoves[ 39 ][ 1 ][ 1 ] = 53;
    bishopMoves[ 39 ][ 1 ][ 2 ] = 60;

    bishopMoves[ 40 ] = new int[ 2 ][];
    bishopMoves[ 40 ][ 0 ] = new int[ 5 ];
    bishopMoves[ 40 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 40 ][ 0 ][ 0 ] = 33;
    bishopMoves[ 40 ][ 0 ][ 1 ] = 26;
    bishopMoves[ 40 ][ 0 ][ 2 ] = 19;
    bishopMoves[ 40 ][ 0 ][ 3 ] = 12;
    bishopMoves[ 40 ][ 0 ][ 4 ] = 5;
    bishopMoves[ 40 ][ 1 ][ 0 ] = 49;
    bishopMoves[ 40 ][ 1 ][ 1 ] = 58;

    bishopMoves[ 41 ] = new int[ 4 ][];
    bishopMoves[ 41 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 41 ][ 1 ] = new int[ 5 ];
    bishopMoves[ 41 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 41 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 41 ][ 0 ][ 0 ] = 32;
    bishopMoves[ 41 ][ 1 ][ 0 ] = 34;
    bishopMoves[ 41 ][ 1 ][ 1 ] = 27;
    bishopMoves[ 41 ][ 1 ][ 2 ] = 20;
    bishopMoves[ 41 ][ 1 ][ 3 ] = 13;
    bishopMoves[ 41 ][ 1 ][ 4 ] = 6;
    bishopMoves[ 41 ][ 2 ][ 0 ] = 48;
    bishopMoves[ 41 ][ 3 ][ 0 ] = 50;
    bishopMoves[ 41 ][ 3 ][ 1 ] = 59;

    bishopMoves[ 42 ] = new int[ 4 ][];
    bishopMoves[ 42 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 42 ][ 1 ] = new int[ 5 ];
    bishopMoves[ 42 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 42 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 42 ][ 0 ][ 0 ] = 33;
    bishopMoves[ 42 ][ 0 ][ 1 ] = 24;
    bishopMoves[ 42 ][ 1 ][ 0 ] = 35;
    bishopMoves[ 42 ][ 1 ][ 1 ] = 28;
    bishopMoves[ 42 ][ 1 ][ 2 ] = 21;
    bishopMoves[ 42 ][ 1 ][ 3 ] = 14;
    bishopMoves[ 42 ][ 1 ][ 4 ] = 7;
    bishopMoves[ 42 ][ 2 ][ 0 ] = 49;
    bishopMoves[ 42 ][ 2 ][ 1 ] = 56;
    bishopMoves[ 42 ][ 3 ][ 0 ] = 51;
    bishopMoves[ 42 ][ 3 ][ 1 ] = 60;

    bishopMoves[ 43 ] = new int[ 4 ][];
    bishopMoves[ 43 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 43 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 43 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 43 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 43 ][ 0 ][ 0 ] = 34;
    bishopMoves[ 43 ][ 0 ][ 1 ] = 25;
    bishopMoves[ 43 ][ 0 ][ 2 ] = 16;
    bishopMoves[ 43 ][ 1 ][ 0 ] = 36;
    bishopMoves[ 43 ][ 1 ][ 1 ] = 29;
    bishopMoves[ 43 ][ 1 ][ 2 ] = 22;
    bishopMoves[ 43 ][ 1 ][ 3 ] = 15;
    bishopMoves[ 43 ][ 2 ][ 0 ] = 50;
    bishopMoves[ 43 ][ 2 ][ 1 ] = 57;
    bishopMoves[ 43 ][ 3 ][ 0 ] = 52;
    bishopMoves[ 43 ][ 3 ][ 1 ] = 61;

    bishopMoves[ 44 ] = new int[ 4 ][];
    bishopMoves[ 44 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 44 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 44 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 44 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 44 ][ 0 ][ 0 ] = 35;
    bishopMoves[ 44 ][ 0 ][ 1 ] = 26;
    bishopMoves[ 44 ][ 0 ][ 2 ] = 17;
    bishopMoves[ 44 ][ 0 ][ 3 ] = 8;
    bishopMoves[ 44 ][ 1 ][ 0 ] = 37;
    bishopMoves[ 44 ][ 1 ][ 1 ] = 30;
    bishopMoves[ 44 ][ 1 ][ 2 ] = 23;
    bishopMoves[ 44 ][ 2 ][ 0 ] = 51;
    bishopMoves[ 44 ][ 2 ][ 1 ] = 58;
    bishopMoves[ 44 ][ 3 ][ 0 ] = 53;
    bishopMoves[ 44 ][ 3 ][ 1 ] = 62;

    bishopMoves[ 45 ] = new int[ 4 ][];
    bishopMoves[ 45 ][ 0 ] = new int[ 5 ];
    bishopMoves[ 45 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 45 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 45 ][ 3 ] = new int[ 2 ];
    bishopMoves[ 45 ][ 0 ][ 0 ] = 36;
    bishopMoves[ 45 ][ 0 ][ 1 ] = 27;
    bishopMoves[ 45 ][ 0 ][ 2 ] = 18;
    bishopMoves[ 45 ][ 0 ][ 3 ] = 9;
    bishopMoves[ 45 ][ 0 ][ 4 ] = 0;
    bishopMoves[ 45 ][ 1 ][ 0 ] = 38;
    bishopMoves[ 45 ][ 1 ][ 1 ] = 31;
    bishopMoves[ 45 ][ 2 ][ 0 ] = 52;
    bishopMoves[ 45 ][ 2 ][ 1 ] = 59;
    bishopMoves[ 45 ][ 3 ][ 0 ] = 54;
    bishopMoves[ 45 ][ 3 ][ 1 ] = 63;

    bishopMoves[ 46 ] = new int[ 4 ][];
    bishopMoves[ 46 ][ 0 ] = new int[ 5 ];
    bishopMoves[ 46 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 46 ][ 2 ] = new int[ 2 ];
    bishopMoves[ 46 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 46 ][ 0 ][ 0 ] = 37;
    bishopMoves[ 46 ][ 0 ][ 1 ] = 28;
    bishopMoves[ 46 ][ 0 ][ 2 ] = 19;
    bishopMoves[ 46 ][ 0 ][ 3 ] = 10;
    bishopMoves[ 46 ][ 0 ][ 4 ] = 1;
    bishopMoves[ 46 ][ 1 ][ 0 ] = 39;
    bishopMoves[ 46 ][ 2 ][ 0 ] = 53;
    bishopMoves[ 46 ][ 2 ][ 1 ] = 60;
    bishopMoves[ 46 ][ 3 ][ 0 ] = 55;

    bishopMoves[ 47 ] = new int[ 2 ][];
    bishopMoves[ 47 ][ 0 ] = new int[ 5 ];
    bishopMoves[ 47 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 47 ][ 0 ][ 0 ] = 38;
    bishopMoves[ 47 ][ 0 ][ 1 ] = 29;
    bishopMoves[ 47 ][ 0 ][ 2 ] = 20;
    bishopMoves[ 47 ][ 0 ][ 3 ] = 11;
    bishopMoves[ 47 ][ 0 ][ 4 ] = 2;
    bishopMoves[ 47 ][ 1 ][ 0 ] = 54;
    bishopMoves[ 47 ][ 1 ][ 1 ] = 61;

    bishopMoves[ 48 ] = new int[ 2 ][];
    bishopMoves[ 48 ][ 0 ] = new int[ 6 ];
    bishopMoves[ 48 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 48 ][ 0 ][ 0 ] = 41;
    bishopMoves[ 48 ][ 0 ][ 1 ] = 34;
    bishopMoves[ 48 ][ 0 ][ 2 ] = 27;
    bishopMoves[ 48 ][ 0 ][ 3 ] = 20;
    bishopMoves[ 48 ][ 0 ][ 4 ] = 13;
    bishopMoves[ 48 ][ 0 ][ 5 ] = 6;
    bishopMoves[ 48 ][ 1 ][ 0 ] = 57;

    bishopMoves[ 49 ] = new int[ 4 ][];
    bishopMoves[ 49 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 49 ][ 1 ] = new int[ 6 ];
    bishopMoves[ 49 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 49 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 49 ][ 0 ][ 0 ] = 40;
    bishopMoves[ 49 ][ 1 ][ 0 ] = 42;
    bishopMoves[ 49 ][ 1 ][ 1 ] = 35;
    bishopMoves[ 49 ][ 1 ][ 2 ] = 28;
    bishopMoves[ 49 ][ 1 ][ 3 ] = 21;
    bishopMoves[ 49 ][ 1 ][ 4 ] = 14;
    bishopMoves[ 49 ][ 1 ][ 5 ] = 7;
    bishopMoves[ 49 ][ 2 ][ 0 ] = 56;
    bishopMoves[ 49 ][ 3 ][ 0 ] = 58;

    bishopMoves[ 50 ] = new int[ 4 ][];
    bishopMoves[ 50 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 50 ][ 1 ] = new int[ 5 ];
    bishopMoves[ 50 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 50 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 50 ][ 0 ][ 0 ] = 41;
    bishopMoves[ 50 ][ 0 ][ 1 ] = 32;
    bishopMoves[ 50 ][ 1 ][ 0 ] = 43;
    bishopMoves[ 50 ][ 1 ][ 1 ] = 36;
    bishopMoves[ 50 ][ 1 ][ 2 ] = 29;
    bishopMoves[ 50 ][ 1 ][ 3 ] = 22;
    bishopMoves[ 50 ][ 1 ][ 4 ] = 15;
    bishopMoves[ 50 ][ 2 ][ 0 ] = 57;
    bishopMoves[ 50 ][ 3 ][ 0 ] = 59;

    bishopMoves[ 51 ] = new int[ 4 ][];
    bishopMoves[ 51 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 51 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 51 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 51 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 51 ][ 0 ][ 0 ] = 42;
    bishopMoves[ 51 ][ 0 ][ 1 ] = 33;
    bishopMoves[ 51 ][ 0 ][ 2 ] = 24;
    bishopMoves[ 51 ][ 1 ][ 0 ] = 44;
    bishopMoves[ 51 ][ 1 ][ 1 ] = 37;
    bishopMoves[ 51 ][ 1 ][ 2 ] = 30;
    bishopMoves[ 51 ][ 1 ][ 3 ] = 23;
    bishopMoves[ 51 ][ 2 ][ 0 ] = 58;
    bishopMoves[ 51 ][ 3 ][ 0 ] = 60;

    bishopMoves[ 52 ] = new int[ 4 ][];
    bishopMoves[ 52 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 52 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 52 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 52 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 52 ][ 0 ][ 0 ] = 43;
    bishopMoves[ 52 ][ 0 ][ 1 ] = 34;
    bishopMoves[ 52 ][ 0 ][ 2 ] = 25;
    bishopMoves[ 52 ][ 0 ][ 3 ] = 16;
    bishopMoves[ 52 ][ 1 ][ 0 ] = 45;
    bishopMoves[ 52 ][ 1 ][ 1 ] = 38;
    bishopMoves[ 52 ][ 1 ][ 2 ] = 31;
    bishopMoves[ 52 ][ 2 ][ 0 ] = 59;
    bishopMoves[ 52 ][ 3 ][ 0 ] = 61;

    bishopMoves[ 53 ] = new int[ 4 ][];
    bishopMoves[ 53 ][ 0 ] = new int[ 5 ];
    bishopMoves[ 53 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 53 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 53 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 53 ][ 0 ][ 0 ] = 44;
    bishopMoves[ 53 ][ 0 ][ 1 ] = 35;
    bishopMoves[ 53 ][ 0 ][ 2 ] = 26;
    bishopMoves[ 53 ][ 0 ][ 3 ] = 17;
    bishopMoves[ 53 ][ 0 ][ 4 ] = 8;
    bishopMoves[ 53 ][ 1 ][ 0 ] = 46;
    bishopMoves[ 53 ][ 1 ][ 1 ] = 39;
    bishopMoves[ 53 ][ 2 ][ 0 ] = 60;
    bishopMoves[ 53 ][ 3 ][ 0 ] = 62;

    bishopMoves[ 54 ] = new int[ 4 ][];
    bishopMoves[ 54 ][ 0 ] = new int[ 6 ];
    bishopMoves[ 54 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 54 ][ 2 ] = new int[ 1 ];
    bishopMoves[ 54 ][ 3 ] = new int[ 1 ];
    bishopMoves[ 54 ][ 0 ][ 0 ] = 45;
    bishopMoves[ 54 ][ 0 ][ 1 ] = 36;
    bishopMoves[ 54 ][ 0 ][ 2 ] = 27;
    bishopMoves[ 54 ][ 0 ][ 3 ] = 18;
    bishopMoves[ 54 ][ 0 ][ 4 ] = 9;
    bishopMoves[ 54 ][ 0 ][ 5 ] = 0;
    bishopMoves[ 54 ][ 1 ][ 0 ] = 47;
    bishopMoves[ 54 ][ 2 ][ 0 ] = 61;
    bishopMoves[ 54 ][ 3 ][ 0 ] = 63;

    bishopMoves[ 55 ] = new int[ 2 ][];
    bishopMoves[ 55 ][ 0 ] = new int[ 6 ];
    bishopMoves[ 55 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 55 ][ 0 ][ 0 ] = 46;
    bishopMoves[ 55 ][ 0 ][ 1 ] = 37;
    bishopMoves[ 55 ][ 0 ][ 2 ] = 28;
    bishopMoves[ 55 ][ 0 ][ 3 ] = 19;
    bishopMoves[ 55 ][ 0 ][ 4 ] = 10;
    bishopMoves[ 55 ][ 0 ][ 5 ] = 1;
    bishopMoves[ 55 ][ 1 ][ 0 ] = 62;

    bishopMoves[ 56 ] = new int[ 1 ][ 7 ];
    bishopMoves[ 56 ][ 0 ][ 0 ] = 49;
    bishopMoves[ 56 ][ 0 ][ 1 ] = 42;
    bishopMoves[ 56 ][ 0 ][ 2 ] = 35;
    bishopMoves[ 56 ][ 0 ][ 3 ] = 28;
    bishopMoves[ 56 ][ 0 ][ 4 ] = 21;
    bishopMoves[ 56 ][ 0 ][ 5 ] = 14;
    bishopMoves[ 56 ][ 0 ][ 6 ] = 7;

    bishopMoves[ 57 ] = new int[ 2 ][];
    bishopMoves[ 57 ][ 0 ] = new int[ 1 ];
    bishopMoves[ 57 ][ 1 ] = new int[ 6 ];
    bishopMoves[ 57 ][ 0 ][ 0 ] = 48;
    bishopMoves[ 57 ][ 1 ][ 0 ] = 50;
    bishopMoves[ 57 ][ 1 ][ 1 ] = 43;
    bishopMoves[ 57 ][ 1 ][ 2 ] = 36;
    bishopMoves[ 57 ][ 1 ][ 3 ] = 29;
    bishopMoves[ 57 ][ 1 ][ 4 ] = 22;
    bishopMoves[ 57 ][ 1 ][ 5 ] = 15;

    bishopMoves[ 58 ] = new int[ 2 ][];
    bishopMoves[ 58 ][ 0 ] = new int[ 2 ];
    bishopMoves[ 58 ][ 1 ] = new int[ 5 ];
    bishopMoves[ 58 ][ 0 ][ 0 ] = 49;
    bishopMoves[ 58 ][ 0 ][ 1 ] = 40;
    bishopMoves[ 58 ][ 1 ][ 0 ] = 51;
    bishopMoves[ 58 ][ 1 ][ 1 ] = 44;
    bishopMoves[ 58 ][ 1 ][ 2 ] = 37;
    bishopMoves[ 58 ][ 1 ][ 3 ] = 30;
    bishopMoves[ 58 ][ 1 ][ 4 ] = 23;

    bishopMoves[ 59 ] = new int[ 2 ][];
    bishopMoves[ 59 ][ 0 ] = new int[ 3 ];
    bishopMoves[ 59 ][ 1 ] = new int[ 4 ];
    bishopMoves[ 59 ][ 0 ][ 0 ] = 50;
    bishopMoves[ 59 ][ 0 ][ 1 ] = 41;
    bishopMoves[ 59 ][ 0 ][ 2 ] = 32;
    bishopMoves[ 59 ][ 1 ][ 0 ] = 52;
    bishopMoves[ 59 ][ 1 ][ 1 ] = 45;
    bishopMoves[ 59 ][ 1 ][ 2 ] = 38;
    bishopMoves[ 59 ][ 1 ][ 3 ] = 31;

    bishopMoves[ 60 ] = new int[ 2 ][];
    bishopMoves[ 60 ][ 0 ] = new int[ 4 ];
    bishopMoves[ 60 ][ 1 ] = new int[ 3 ];
    bishopMoves[ 60 ][ 0 ][ 0 ] = 51;
    bishopMoves[ 60 ][ 0 ][ 1 ] = 42;
    bishopMoves[ 60 ][ 0 ][ 2 ] = 33;
    bishopMoves[ 60 ][ 0 ][ 3 ] = 24;
    bishopMoves[ 60 ][ 1 ][ 0 ] = 53;
    bishopMoves[ 60 ][ 1 ][ 1 ] = 46;
    bishopMoves[ 60 ][ 1 ][ 2 ] = 39;

    bishopMoves[ 61 ] = new int[ 2 ][];
    bishopMoves[ 61 ][ 0 ] = new int[ 5 ];
    bishopMoves[ 61 ][ 1 ] = new int[ 2 ];
    bishopMoves[ 61 ][ 0 ][ 0 ] = 52;
    bishopMoves[ 61 ][ 0 ][ 1 ] = 43;
    bishopMoves[ 61 ][ 0 ][ 2 ] = 34;
    bishopMoves[ 61 ][ 0 ][ 3 ] = 25;
    bishopMoves[ 61 ][ 0 ][ 4 ] = 16;
    bishopMoves[ 61 ][ 1 ][ 0 ] = 54;
    bishopMoves[ 61 ][ 1 ][ 1 ] = 47;

    bishopMoves[ 62 ] = new int[ 2 ][];
    bishopMoves[ 62 ][ 0 ] = new int[ 6 ];
    bishopMoves[ 62 ][ 1 ] = new int[ 1 ];
    bishopMoves[ 62 ][ 0 ][ 0 ] = 53;
    bishopMoves[ 62 ][ 0 ][ 1 ] = 44;
    bishopMoves[ 62 ][ 0 ][ 2 ] = 35;
    bishopMoves[ 62 ][ 0 ][ 3 ] = 26;
    bishopMoves[ 62 ][ 0 ][ 4 ] = 17;
    bishopMoves[ 62 ][ 0 ][ 5 ] = 8;
    bishopMoves[ 62 ][ 1 ][ 0 ] = 55;

    bishopMoves[ 63 ] = new int[ 1 ][ 7 ];
    bishopMoves[ 63 ][ 0 ][ 0 ] = 54;
    bishopMoves[ 63 ][ 0 ][ 1 ] = 45;
    bishopMoves[ 63 ][ 0 ][ 2 ] = 36;
    bishopMoves[ 63 ][ 0 ][ 3 ] = 27;
    bishopMoves[ 63 ][ 0 ][ 4 ] = 18;
    bishopMoves[ 63 ][ 0 ][ 5 ] = 9;
    bishopMoves[ 63 ][ 0 ][ 6 ] = 0;

    kingMoves = new int[ 64 ][];
    kingMoves[ 0 ] = new int[ 3 ];
    kingMoves[ 0 ][ 0 ] = 1;
    kingMoves[ 0 ][ 1 ] = 8;
    kingMoves[ 0 ][ 2 ] = 9;
    kingMoves[ 1 ] = new int[ 5 ];
    kingMoves[ 1 ][ 0 ] = 0;
    kingMoves[ 1 ][ 1 ] = 2;
    kingMoves[ 1 ][ 2 ] = 8;
    kingMoves[ 1 ][ 3 ] = 9;
    kingMoves[ 1 ][ 4 ] = 10;
    kingMoves[ 2 ] = new int[ 5 ];
    kingMoves[ 2 ][ 0 ] = 1;
    kingMoves[ 2 ][ 1 ] = 3;
    kingMoves[ 2 ][ 2 ] = 9;
    kingMoves[ 2 ][ 3 ] = 10;
    kingMoves[ 2 ][ 4 ] = 11;
    kingMoves[ 3 ] = new int[ 5 ];
    kingMoves[ 3 ][ 0 ] = 2;
    kingMoves[ 3 ][ 1 ] = 4;
    kingMoves[ 3 ][ 2 ] = 10;
    kingMoves[ 3 ][ 3 ] = 11;
    kingMoves[ 3 ][ 4 ] = 12;
    kingMoves[ 4 ] = new int[ 5 ];
    kingMoves[ 4 ][ 0 ] = 3;
    kingMoves[ 4 ][ 1 ] = 5;
    kingMoves[ 4 ][ 2 ] = 11;
    kingMoves[ 4 ][ 3 ] = 12;
    kingMoves[ 4 ][ 4 ] = 13;
    kingMoves[ 5 ] = new int[ 5 ];
    kingMoves[ 5 ][ 0 ] = 4;
    kingMoves[ 5 ][ 1 ] = 6;
    kingMoves[ 5 ][ 2 ] = 12;
    kingMoves[ 5 ][ 3 ] = 13;
    kingMoves[ 5 ][ 4 ] = 14;
    kingMoves[ 6 ] = new int[ 5 ];
    kingMoves[ 6 ][ 0 ] = 5;
    kingMoves[ 6 ][ 1 ] = 7;
    kingMoves[ 6 ][ 2 ] = 13;
    kingMoves[ 6 ][ 3 ] = 14;
    kingMoves[ 6 ][ 4 ] = 15;
    kingMoves[ 7 ] = new int[ 3 ];
    kingMoves[ 7 ][ 0 ] = 6;
    kingMoves[ 7 ][ 1 ] = 14;
    kingMoves[ 7 ][ 2 ] = 15;

    kingMoves[ 8 ] = new int[ 5 ];
    kingMoves[ 8 ][ 0 ] = 0;
    kingMoves[ 8 ][ 1 ] = 1;
    kingMoves[ 8 ][ 2 ] = 9;
    kingMoves[ 8 ][ 3 ] = 17;
    kingMoves[ 8 ][ 4 ] = 16;
    kingMoves[ 9 ] = new int[ 8 ];
    kingMoves[ 9 ][ 0 ] = 0;
    kingMoves[ 9 ][ 1 ] = 1;
    kingMoves[ 9 ][ 2 ] = 2;
    kingMoves[ 9 ][ 3 ] = 8;
    kingMoves[ 9 ][ 4 ] = 10;
    kingMoves[ 9 ][ 5 ] = 16;
    kingMoves[ 9 ][ 6 ] = 17;
    kingMoves[ 9 ][ 7 ] = 18;
    kingMoves[ 10 ] = new int[ 8 ];
    kingMoves[ 10 ][ 0 ] = 1;
    kingMoves[ 10 ][ 1 ] = 2;
    kingMoves[ 10 ][ 2 ] = 3;
    kingMoves[ 10 ][ 3 ] = 9;
    kingMoves[ 10 ][ 4 ] = 11;
    kingMoves[ 10 ][ 5 ] = 17;
    kingMoves[ 10 ][ 6 ] = 18;
    kingMoves[ 10 ][ 7 ] = 19;
    kingMoves[ 11 ] = new int[ 8 ];
    kingMoves[ 11 ][ 0 ] = 2;
    kingMoves[ 11 ][ 1 ] = 3;
    kingMoves[ 11 ][ 2 ] = 4;
    kingMoves[ 11 ][ 3 ] = 10;
    kingMoves[ 11 ][ 4 ] = 12;
    kingMoves[ 11 ][ 5 ] = 18;
    kingMoves[ 11 ][ 6 ] = 19;
    kingMoves[ 11 ][ 7 ] = 20;
    kingMoves[ 12 ] = new int[ 8 ];
    kingMoves[ 12 ][ 0 ] = 3;
    kingMoves[ 12 ][ 1 ] = 4;
    kingMoves[ 12 ][ 2 ] = 5;
    kingMoves[ 12 ][ 3 ] = 11;
    kingMoves[ 12 ][ 4 ] = 13;
    kingMoves[ 12 ][ 5 ] = 19;
    kingMoves[ 12 ][ 6 ] = 20;
    kingMoves[ 12 ][ 7 ] = 21;
    kingMoves[ 13 ] = new int[ 8 ];
    kingMoves[ 13 ][ 0 ] = 4;
    kingMoves[ 13 ][ 1 ] = 5;
    kingMoves[ 13 ][ 2 ] = 6;
    kingMoves[ 13 ][ 3 ] = 12;
    kingMoves[ 13 ][ 4 ] = 14;
    kingMoves[ 13 ][ 5 ] = 20;
    kingMoves[ 13 ][ 6 ] = 21;
    kingMoves[ 13 ][ 7 ] = 22;
    kingMoves[ 14 ] = new int[ 8 ];
    kingMoves[ 14 ][ 0 ] = 5;
    kingMoves[ 14 ][ 1 ] = 6;
    kingMoves[ 14 ][ 2 ] = 7;
    kingMoves[ 14 ][ 3 ] = 13;
    kingMoves[ 14 ][ 4 ] = 15;
    kingMoves[ 14 ][ 5 ] = 21;
    kingMoves[ 14 ][ 6 ] = 22;
    kingMoves[ 14 ][ 7 ] = 23;
    kingMoves[ 15 ] = new int[ 5 ];
    kingMoves[ 15 ][ 0 ] = 6;
    kingMoves[ 15 ][ 1 ] = 7;
    kingMoves[ 15 ][ 2 ] = 14;
    kingMoves[ 15 ][ 3 ] = 22;
    kingMoves[ 15 ][ 4 ] = 23;

    kingMoves[ 16 ] = new int[ 5 ];
    kingMoves[ 16 ][ 0 ] = 9;
    kingMoves[ 16 ][ 1 ] = 8;
    kingMoves[ 16 ][ 2 ] = 17;
    kingMoves[ 16 ][ 3 ] = 24;
    kingMoves[ 16 ][ 4 ] = 25;
    kingMoves[ 17 ] = new int[ 8 ];
    kingMoves[ 17 ][ 0 ] = 8;
    kingMoves[ 17 ][ 1 ] = 9;
    kingMoves[ 17 ][ 2 ] = 10;
    kingMoves[ 17 ][ 3 ] = 16;
    kingMoves[ 17 ][ 4 ] = 18;
    kingMoves[ 17 ][ 5 ] = 24;
    kingMoves[ 17 ][ 6 ] = 25;
    kingMoves[ 17 ][ 7 ] = 26;
    kingMoves[ 18 ] = new int[ 8 ];
    kingMoves[ 18 ][ 0 ] = 9;
    kingMoves[ 18 ][ 1 ] = 10;
    kingMoves[ 18 ][ 2 ] = 11;
    kingMoves[ 18 ][ 3 ] = 17;
    kingMoves[ 18 ][ 4 ] = 19;
    kingMoves[ 18 ][ 5 ] = 25;
    kingMoves[ 18 ][ 6 ] = 26;
    kingMoves[ 18 ][ 7 ] = 27;
    kingMoves[ 19 ] = new int[ 8 ];
    kingMoves[ 19 ][ 0 ] = 10;
    kingMoves[ 19 ][ 1 ] = 11;
    kingMoves[ 19 ][ 2 ] = 12;
    kingMoves[ 19 ][ 3 ] = 18;
    kingMoves[ 19 ][ 4 ] = 20;
    kingMoves[ 19 ][ 5 ] = 26;
    kingMoves[ 19 ][ 6 ] = 27;
    kingMoves[ 19 ][ 7 ] = 28;
    kingMoves[ 20 ] = new int[ 8 ];
    kingMoves[ 20 ][ 0 ] = 11;
    kingMoves[ 20 ][ 1 ] = 12;
    kingMoves[ 20 ][ 2 ] = 13;
    kingMoves[ 20 ][ 3 ] = 19;
    kingMoves[ 20 ][ 4 ] = 21;
    kingMoves[ 20 ][ 5 ] = 27;
    kingMoves[ 20 ][ 6 ] = 28;
    kingMoves[ 20 ][ 7 ] = 29;
    kingMoves[ 21 ] = new int[ 8 ];
    kingMoves[ 21 ][ 0 ] = 12;
    kingMoves[ 21 ][ 1 ] = 13;
    kingMoves[ 21 ][ 2 ] = 14;
    kingMoves[ 21 ][ 3 ] = 20;
    kingMoves[ 21 ][ 4 ] = 22;
    kingMoves[ 21 ][ 5 ] = 28;
    kingMoves[ 21 ][ 6 ] = 29;
    kingMoves[ 21 ][ 7 ] = 30;
    kingMoves[ 22 ] = new int[ 8 ];
    kingMoves[ 22 ][ 0 ] = 13;
    kingMoves[ 22 ][ 1 ] = 14;
    kingMoves[ 22 ][ 2 ] = 15;
    kingMoves[ 22 ][ 3 ] = 21;
    kingMoves[ 22 ][ 4 ] = 23;
    kingMoves[ 22 ][ 5 ] = 29;
    kingMoves[ 22 ][ 6 ] = 30;
    kingMoves[ 22 ][ 7 ] = 31;
    kingMoves[ 23 ] = new int[ 5 ];
    kingMoves[ 23 ][ 0 ] = 14;
    kingMoves[ 23 ][ 1 ] = 15;
    kingMoves[ 23 ][ 2 ] = 22;
    kingMoves[ 23 ][ 3 ] = 30;
    kingMoves[ 23 ][ 4 ] = 31;

    kingMoves[ 24 ] = new int[ 5 ];
    kingMoves[ 24 ][ 0 ] = 16;
    kingMoves[ 24 ][ 1 ] = 17;
    kingMoves[ 24 ][ 2 ] = 25;
    kingMoves[ 24 ][ 3 ] = 32;
    kingMoves[ 24 ][ 4 ] = 33;
    kingMoves[ 25 ] = new int[ 8 ];
    kingMoves[ 25 ][ 0 ] = 16;
    kingMoves[ 25 ][ 1 ] = 17;
    kingMoves[ 25 ][ 2 ] = 18;
    kingMoves[ 25 ][ 3 ] = 24;
    kingMoves[ 25 ][ 4 ] = 26;
    kingMoves[ 25 ][ 5 ] = 32;
    kingMoves[ 25 ][ 6 ] = 33;
    kingMoves[ 25 ][ 7 ] = 34;
    kingMoves[ 26 ] = new int[ 8 ];
    kingMoves[ 26 ][ 0 ] = 17;
    kingMoves[ 26 ][ 1 ] = 18;
    kingMoves[ 26 ][ 2 ] = 19;
    kingMoves[ 26 ][ 3 ] = 25;
    kingMoves[ 26 ][ 4 ] = 27;
    kingMoves[ 26 ][ 5 ] = 33;
    kingMoves[ 26 ][ 6 ] = 34;
    kingMoves[ 26 ][ 7 ] = 35;
    kingMoves[ 27 ] = new int[ 8 ];
    kingMoves[ 27 ][ 0 ] = 18;
    kingMoves[ 27 ][ 1 ] = 19;
    kingMoves[ 27 ][ 2 ] = 20;
    kingMoves[ 27 ][ 3 ] = 26;
    kingMoves[ 27 ][ 4 ] = 28;
    kingMoves[ 27 ][ 5 ] = 34;
    kingMoves[ 27 ][ 6 ] = 35;
    kingMoves[ 27 ][ 7 ] = 36;
    kingMoves[ 28 ] = new int[ 8 ];
    kingMoves[ 28 ][ 0 ] = 19;
    kingMoves[ 28 ][ 1 ] = 20;
    kingMoves[ 28 ][ 2 ] = 21;
    kingMoves[ 28 ][ 3 ] = 27;
    kingMoves[ 28 ][ 4 ] = 29;
    kingMoves[ 28 ][ 5 ] = 35;
    kingMoves[ 28 ][ 6 ] = 36;
    kingMoves[ 28 ][ 7 ] = 37;
    kingMoves[ 29 ] = new int[ 8 ];
    kingMoves[ 29 ][ 0 ] = 20;
    kingMoves[ 29 ][ 1 ] = 21;
    kingMoves[ 29 ][ 2 ] = 22;
    kingMoves[ 29 ][ 3 ] = 28;
    kingMoves[ 29 ][ 4 ] = 30;
    kingMoves[ 29 ][ 5 ] = 36;
    kingMoves[ 29 ][ 6 ] = 37;
    kingMoves[ 29 ][ 7 ] = 38;
    kingMoves[ 30 ] = new int[ 8 ];
    kingMoves[ 30 ][ 0 ] = 21;
    kingMoves[ 30 ][ 1 ] = 22;
    kingMoves[ 30 ][ 2 ] = 23;
    kingMoves[ 30 ][ 3 ] = 29;
    kingMoves[ 30 ][ 4 ] = 31;
    kingMoves[ 30 ][ 5 ] = 37;
    kingMoves[ 30 ][ 6 ] = 38;
    kingMoves[ 30 ][ 7 ] = 39;
    kingMoves[ 31 ] = new int[ 5 ];
    kingMoves[ 31 ][ 0 ] = 22;
    kingMoves[ 31 ][ 1 ] = 23;
    kingMoves[ 31 ][ 2 ] = 30;
    kingMoves[ 31 ][ 3 ] = 38;
    kingMoves[ 31 ][ 4 ] = 39;

    kingMoves[ 32 ] = new int[ 5 ];
    kingMoves[ 32 ][ 0 ] = 24;
    kingMoves[ 32 ][ 1 ] = 25;
    kingMoves[ 32 ][ 2 ] = 33;
    kingMoves[ 32 ][ 3 ] = 41;
    kingMoves[ 32 ][ 4 ] = 40;
    kingMoves[ 33 ] = new int[ 8 ];
    kingMoves[ 33 ][ 0 ] = 24;
    kingMoves[ 33 ][ 1 ] = 25;
    kingMoves[ 33 ][ 2 ] = 26;
    kingMoves[ 33 ][ 3 ] = 32;
    kingMoves[ 33 ][ 4 ] = 34;
    kingMoves[ 33 ][ 5 ] = 40;
    kingMoves[ 33 ][ 6 ] = 41;
    kingMoves[ 33 ][ 7 ] = 42;
    kingMoves[ 34 ] = new int[ 8 ];
    kingMoves[ 34 ][ 0 ] = 25;
    kingMoves[ 34 ][ 1 ] = 26;
    kingMoves[ 34 ][ 2 ] = 27;
    kingMoves[ 34 ][ 3 ] = 33;
    kingMoves[ 34 ][ 4 ] = 35;
    kingMoves[ 34 ][ 5 ] = 41;
    kingMoves[ 34 ][ 6 ] = 42;
    kingMoves[ 34 ][ 7 ] = 43;
    kingMoves[ 35 ] = new int[ 8 ];
    kingMoves[ 35 ][ 0 ] = 26;
    kingMoves[ 35 ][ 1 ] = 27;
    kingMoves[ 35 ][ 2 ] = 28;
    kingMoves[ 35 ][ 3 ] = 34;
    kingMoves[ 35 ][ 4 ] = 36;
    kingMoves[ 35 ][ 5 ] = 42;
    kingMoves[ 35 ][ 6 ] = 43;
    kingMoves[ 35 ][ 7 ] = 44;
    kingMoves[ 36 ] = new int[ 8 ];
    kingMoves[ 36 ][ 0 ] = 27;
    kingMoves[ 36 ][ 1 ] = 28;
    kingMoves[ 36 ][ 2 ] = 29;
    kingMoves[ 36 ][ 3 ] = 35;
    kingMoves[ 36 ][ 4 ] = 37;
    kingMoves[ 36 ][ 5 ] = 43;
    kingMoves[ 36 ][ 6 ] = 44;
    kingMoves[ 36 ][ 7 ] = 45;
    kingMoves[ 37 ] = new int[ 8 ];
    kingMoves[ 37 ][ 0 ] = 28;
    kingMoves[ 37 ][ 1 ] = 29;
    kingMoves[ 37 ][ 2 ] = 30;
    kingMoves[ 37 ][ 3 ] = 36;
    kingMoves[ 37 ][ 4 ] = 38;
    kingMoves[ 37 ][ 5 ] = 44;
    kingMoves[ 37 ][ 6 ] = 45;
    kingMoves[ 37 ][ 7 ] = 46;
    kingMoves[ 38 ] = new int[ 8 ];
    kingMoves[ 38 ][ 0 ] = 29;
    kingMoves[ 38 ][ 1 ] = 30;
    kingMoves[ 38 ][ 2 ] = 31;
    kingMoves[ 38 ][ 3 ] = 37;
    kingMoves[ 38 ][ 4 ] = 39;
    kingMoves[ 38 ][ 5 ] = 45;
    kingMoves[ 38 ][ 6 ] = 46;
    kingMoves[ 38 ][ 7 ] = 47;
    kingMoves[ 39 ] = new int[ 5 ];
    kingMoves[ 39 ][ 0 ] = 30;
    kingMoves[ 39 ][ 1 ] = 31;
    kingMoves[ 39 ][ 2 ] = 38;
    kingMoves[ 39 ][ 3 ] = 46;
    kingMoves[ 39 ][ 4 ] = 47;

    kingMoves[ 40 ] = new int[ 5 ];
    kingMoves[ 40 ][ 0 ] = 32;
    kingMoves[ 40 ][ 1 ] = 33;
    kingMoves[ 40 ][ 2 ] = 41;
    kingMoves[ 40 ][ 3 ] = 48;
    kingMoves[ 40 ][ 4 ] = 49;
    kingMoves[ 41 ] = new int[ 8 ];
    kingMoves[ 41 ][ 0 ] = 32;
    kingMoves[ 41 ][ 1 ] = 33;
    kingMoves[ 41 ][ 2 ] = 34;
    kingMoves[ 41 ][ 3 ] = 40;
    kingMoves[ 41 ][ 4 ] = 42;
    kingMoves[ 41 ][ 5 ] = 48;
    kingMoves[ 41 ][ 6 ] = 49;
    kingMoves[ 41 ][ 7 ] = 50;
    kingMoves[ 42 ] = new int[ 8 ];
    kingMoves[ 42 ][ 0 ] = 33;
    kingMoves[ 42 ][ 1 ] = 34;
    kingMoves[ 42 ][ 2 ] = 35;
    kingMoves[ 42 ][ 3 ] = 41;
    kingMoves[ 42 ][ 4 ] = 43;
    kingMoves[ 42 ][ 5 ] = 49;
    kingMoves[ 42 ][ 6 ] = 50;
    kingMoves[ 42 ][ 7 ] = 51;
    kingMoves[ 43 ] = new int[ 8 ];
    kingMoves[ 43 ][ 0 ] = 34;
    kingMoves[ 43 ][ 1 ] = 35;
    kingMoves[ 43 ][ 2 ] = 36;
    kingMoves[ 43 ][ 3 ] = 42;
    kingMoves[ 43 ][ 4 ] = 44;
    kingMoves[ 43 ][ 5 ] = 50;
    kingMoves[ 43 ][ 6 ] = 51;
    kingMoves[ 43 ][ 7 ] = 52;
    kingMoves[ 44 ] = new int[ 8 ];
    kingMoves[ 44 ][ 0 ] = 35;
    kingMoves[ 44 ][ 1 ] = 36;
    kingMoves[ 44 ][ 2 ] = 37;
    kingMoves[ 44 ][ 3 ] = 43;
    kingMoves[ 44 ][ 4 ] = 45;
    kingMoves[ 44 ][ 5 ] = 51;
    kingMoves[ 44 ][ 6 ] = 52;
    kingMoves[ 44 ][ 7 ] = 53;
    kingMoves[ 45 ] = new int[ 8 ];
    kingMoves[ 45 ][ 0 ] = 36;
    kingMoves[ 45 ][ 1 ] = 37;
    kingMoves[ 45 ][ 2 ] = 38;
    kingMoves[ 45 ][ 3 ] = 44;
    kingMoves[ 45 ][ 4 ] = 46;
    kingMoves[ 45 ][ 5 ] = 52;
    kingMoves[ 45 ][ 6 ] = 53;
    kingMoves[ 45 ][ 7 ] = 54;
    kingMoves[ 46 ] = new int[ 8 ];
    kingMoves[ 46 ][ 0 ] = 37;
    kingMoves[ 46 ][ 1 ] = 38;
    kingMoves[ 46 ][ 2 ] = 39;
    kingMoves[ 46 ][ 3 ] = 45;
    kingMoves[ 46 ][ 4 ] = 47;
    kingMoves[ 46 ][ 5 ] = 53;
    kingMoves[ 46 ][ 6 ] = 54;
    kingMoves[ 46 ][ 7 ] = 55;
    kingMoves[ 47 ] = new int[ 5 ];
    kingMoves[ 47 ][ 0 ] = 38;
    kingMoves[ 47 ][ 1 ] = 39;
    kingMoves[ 47 ][ 2 ] = 46;
    kingMoves[ 47 ][ 3 ] = 54;
    kingMoves[ 47 ][ 4 ] = 55;

    kingMoves[ 48 ] = new int[ 5 ];
    kingMoves[ 48 ][ 0 ] = 40;
    kingMoves[ 48 ][ 1 ] = 41;
    kingMoves[ 48 ][ 2 ] = 49;
    kingMoves[ 48 ][ 3 ] = 56;
    kingMoves[ 48 ][ 4 ] = 57;
    kingMoves[ 49 ] = new int[ 8 ];
    kingMoves[ 49 ][ 0 ] = 40;
    kingMoves[ 49 ][ 1 ] = 41;
    kingMoves[ 49 ][ 2 ] = 42;
    kingMoves[ 49 ][ 3 ] = 48;
    kingMoves[ 49 ][ 4 ] = 50;
    kingMoves[ 49 ][ 5 ] = 56;
    kingMoves[ 49 ][ 6 ] = 57;
    kingMoves[ 49 ][ 7 ] = 58;
    kingMoves[ 50 ] = new int[ 8 ];
    kingMoves[ 50 ][ 0 ] = 41;
    kingMoves[ 50 ][ 1 ] = 42;
    kingMoves[ 50 ][ 2 ] = 43;
    kingMoves[ 50 ][ 3 ] = 49;
    kingMoves[ 50 ][ 4 ] = 51;
    kingMoves[ 50 ][ 5 ] = 57;
    kingMoves[ 50 ][ 6 ] = 58;
    kingMoves[ 50 ][ 7 ] = 59;
    kingMoves[ 51 ] = new int[ 8 ];
    kingMoves[ 51 ][ 0 ] = 42;
    kingMoves[ 51 ][ 1 ] = 43;
    kingMoves[ 51 ][ 2 ] = 44;
    kingMoves[ 51 ][ 3 ] = 50;
    kingMoves[ 51 ][ 4 ] = 52;
    kingMoves[ 51 ][ 5 ] = 58;
    kingMoves[ 51 ][ 6 ] = 59;
    kingMoves[ 51 ][ 7 ] = 60;
    kingMoves[ 52 ] = new int[ 8 ];
    kingMoves[ 52 ][ 0 ] = 43;
    kingMoves[ 52 ][ 1 ] = 44;
    kingMoves[ 52 ][ 2 ] = 45;
    kingMoves[ 52 ][ 3 ] = 51;
    kingMoves[ 52 ][ 4 ] = 53;
    kingMoves[ 52 ][ 5 ] = 59;
    kingMoves[ 52 ][ 6 ] = 60;
    kingMoves[ 52 ][ 7 ] = 61;
    kingMoves[ 53 ] = new int[ 8 ];
    kingMoves[ 53 ][ 0 ] = 44;
    kingMoves[ 53 ][ 1 ] = 45;
    kingMoves[ 53 ][ 2 ] = 46;
    kingMoves[ 53 ][ 3 ] = 52;
    kingMoves[ 53 ][ 4 ] = 54;
    kingMoves[ 53 ][ 5 ] = 60;
    kingMoves[ 53 ][ 6 ] = 61;
    kingMoves[ 53 ][ 7 ] = 62;
    kingMoves[ 54 ] = new int[ 8 ];
    kingMoves[ 54 ][ 0 ] = 45;
    kingMoves[ 54 ][ 1 ] = 46;
    kingMoves[ 54 ][ 2 ] = 47;
    kingMoves[ 54 ][ 3 ] = 53;
    kingMoves[ 54 ][ 4 ] = 55;
    kingMoves[ 54 ][ 5 ] = 61;
    kingMoves[ 54 ][ 6 ] = 62;
    kingMoves[ 54 ][ 7 ] = 63;
    kingMoves[ 55 ] = new int[ 5 ];
    kingMoves[ 55 ][ 0 ] = 46;
    kingMoves[ 55 ][ 1 ] = 47;
    kingMoves[ 55 ][ 2 ] = 54;
    kingMoves[ 55 ][ 3 ] = 62;
    kingMoves[ 55 ][ 4 ] = 63;

    kingMoves[ 56 ] = new int[ 3 ];
    kingMoves[ 56 ][ 0 ] = 48;
    kingMoves[ 56 ][ 1 ] = 49;
    kingMoves[ 56 ][ 2 ] = 57;
    kingMoves[ 57 ] = new int[ 5 ];
    kingMoves[ 57 ][ 0 ] = 48;
    kingMoves[ 57 ][ 1 ] = 49;
    kingMoves[ 57 ][ 2 ] = 50;
    kingMoves[ 57 ][ 3 ] = 56;
    kingMoves[ 57 ][ 4 ] = 58;
    kingMoves[ 58 ] = new int[ 5 ];
    kingMoves[ 58 ][ 0 ] = 49;
    kingMoves[ 58 ][ 1 ] = 50;
    kingMoves[ 58 ][ 2 ] = 51;
    kingMoves[ 58 ][ 3 ] = 57;
    kingMoves[ 58 ][ 4 ] = 59;
    kingMoves[ 59 ] = new int[ 5 ];
    kingMoves[ 59 ][ 0 ] = 50;
    kingMoves[ 59 ][ 1 ] = 51;
    kingMoves[ 59 ][ 2 ] = 52;
    kingMoves[ 59 ][ 3 ] = 58;
    kingMoves[ 59 ][ 4 ] = 60;
    kingMoves[ 60 ] = new int[ 5 ];
    kingMoves[ 60 ][ 0 ] = 51;
    kingMoves[ 60 ][ 1 ] = 52;
    kingMoves[ 60 ][ 2 ] = 53;
    kingMoves[ 60 ][ 3 ] = 59;
    kingMoves[ 60 ][ 4 ] = 61;
    kingMoves[ 61 ] = new int[ 5 ];
    kingMoves[ 61 ][ 0 ] = 52;
    kingMoves[ 61 ][ 1 ] = 53;
    kingMoves[ 61 ][ 2 ] = 54;
    kingMoves[ 61 ][ 3 ] = 60;
    kingMoves[ 61 ][ 4 ] = 62;
    kingMoves[ 62 ] = new int[ 5 ];
    kingMoves[ 62 ][ 0 ] = 53;
    kingMoves[ 62 ][ 1 ] = 54;
    kingMoves[ 62 ][ 2 ] = 55;
    kingMoves[ 62 ][ 3 ] = 61;
    kingMoves[ 62 ][ 4 ] = 63;
    kingMoves[ 63 ] = new int[ 3 ];
    kingMoves[ 63 ][ 0 ] = 54;
    kingMoves[ 63 ][ 1 ] = 55;
    kingMoves[ 63 ][ 2 ] = 62;
 }
}