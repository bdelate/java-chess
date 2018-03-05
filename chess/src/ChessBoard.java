package chess;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

public class ChessBoard extends JPanel implements Runnable
{
  Thread boardThread = new Thread(this);
  BufferedImage buffIm;
  Graphics2D buffImGraphics;

  private Color redColor;
  private Color greyColor;

  private int[] currentBoard;
  private int targetBlock=0;
  private int sourceBlock=64;
  private int aiSource;
  private int aiDest;
  private int sourceI, sourceJ, destI, destJ;

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

  //chess piece images
  private Image wPawnImage;
  private Image wRookImage;
  private Image wKnightImage;
  private Image wBishopImage;
  private Image wQueenImage;
  private Image wKingImage;
  private Image bPawnImage;
  private Image bRookImage;
  private Image bKnightImage;
  private Image bBishopImage;
  private Image bQueenImage;
  private Image bKingImage;

  private boolean showMove=false;
  private boolean running = true;
  private boolean drawing = true;

  public ChessBoard()
  {
    currentBoard = new int[64];
    redColor = new Color(121, 13, 42);
    greyColor = new Color(206, 198, 201);
    loadImages();
    boardThread.start();
  }

  public void setCurrentBoard(int[] currentBoard)
  {
    //point to the same currentBoard in memory that MoveList points to
    this.currentBoard = currentBoard;
  }

  public void update()
  {
    buffIm = (BufferedImage)createImage(384, 384);
    buffImGraphics = buffIm.createGraphics();
    buffImGraphics.setColor(greyColor);
    buffImGraphics.fillRect(0, 0, 384, 384);

    //draw pale grid
    buffImGraphics.setColor(redColor);
    for (int i=0; i<8; i++)
    {
      for (int j=0; j<8; j+=2)
      {
        if (i%2 == 0 && j == 0)
          j++;
        buffImGraphics.fillRect(j*48, i*48, 48, 48);
      }
    }

    //draw pieces (starting at bottom left)
    //and draw targetBlock and sourceBlock
    int num=0;
    for (int i=7; i>=0; i--)
    {
      for (int j=0; j<8; j++)
      {
        if (currentBoard[num] == WHITE_PAWN)
          buffImGraphics.drawImage(wPawnImage, j * 48, i * 48, null);
        else if (currentBoard[num] == WHITE_ROOK)
          buffImGraphics.drawImage(wRookImage, j * 48, i * 48, null);
        else if (currentBoard[num] == WHITE_KNIGHT)
          buffImGraphics.drawImage(wKnightImage, j * 48, i * 48, null);
        else if (currentBoard[num] == WHITE_BISHOP)
          buffImGraphics.drawImage(wBishopImage, j * 48, i * 48, null);
        else if (currentBoard[num] == WHITE_QUEEN)
          buffImGraphics.drawImage(wQueenImage, j * 48, i * 48, null);
        else if (currentBoard[num] == WHITE_KING)
          buffImGraphics.drawImage(wKingImage, j * 48, i * 48, null);
        else if (currentBoard[num] == BLACK_PAWN)
          buffImGraphics.drawImage(bPawnImage, j * 48, i * 48, null);
        else if (currentBoard[num] == BLACK_ROOK)
          buffImGraphics.drawImage(bRookImage, j * 48, i * 48, null);
        else if (currentBoard[num] == BLACK_KNIGHT)
          buffImGraphics.drawImage(bKnightImage, j * 48, i * 48, null);
        else if (currentBoard[num] == BLACK_BISHOP)
          buffImGraphics.drawImage(bBishopImage, j * 48, i * 48, null);
        else if (currentBoard[num] == BLACK_QUEEN)
          buffImGraphics.drawImage(bQueenImage, j * 48, i * 48, null);
        else if (currentBoard[num] == BLACK_KING)
          buffImGraphics.drawImage(bKingImage, j * 48, i * 48, null);

        //draw dest/target block
        if (num == targetBlock)
        {
          buffImGraphics.setColor(new Color(240, 240, 0, 190));
          buffImGraphics.fillRect(j * 48, i * 48, 48, 48);
        }

        if (num == sourceBlock)
        {
          buffImGraphics.setColor(new Color(240, 240, 0, 190));
          buffImGraphics.fillRect(j * 48, i * 48, 48, 48);
        }

        if (num == aiSource)
        {
          sourceI = i;
          sourceJ = j;
        }

        if (num == aiDest)
        {
          destI = i;
          destJ = j;
        }

        num+=1;
      }
    }

    //display the ai move that is being made
    if (showMove)
    {
      buffImGraphics.setColor(new Color(240, 0, 0, 190));
      buffImGraphics.fillRect(sourceJ*48, sourceI*48, 48, 48);
      buffImGraphics.fillRect(destJ*48, destI*48, 48, 48);
      repaint();
      try
      {
        Thread.sleep(1200);
      }
      catch (InterruptedException e) {}
      showMove = false;
    }

    repaint();
  }

  public void paint(Graphics g)
  {
    //OS might call paint at random times, therefore just redraw what is
    //currently on screen
    if (buffIm != null)
      g.drawImage(buffIm, 0, 0, null);
  }

  public void run()
  {
    while(running)
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e) {}

      if (drawing)
        update();
    }
  }

  public void showMove(int aiSource, int aiDest)
  {
    this.aiSource = aiSource;
    this.aiDest = aiDest;
    showMove = true;
  }

  private void loadImages()
  {
    wPawnImage = new ImageIcon("../images/wpawn.png").getImage();
    wRookImage = new ImageIcon("../images/wrook.png").getImage();
    wKnightImage = new ImageIcon("../images/wknight.png").getImage();
    wBishopImage = new ImageIcon("../images/wbishop.png").getImage();
    wQueenImage = new ImageIcon("../images/wqueen.png").getImage();
    wKingImage = new ImageIcon("../images/wking.png").getImage();
    bPawnImage = new ImageIcon("../images/bpawn.png").getImage();
    bRookImage = new ImageIcon("../images/brook.png").getImage();
    bKnightImage = new ImageIcon("../images/bknight.png").getImage();
    bBishopImage = new ImageIcon("../images/bbishop.png").getImage();
    bQueenImage = new ImageIcon("../images/bqueen.png").getImage();
    bKingImage = new ImageIcon("../images/bking.png").getImage();
  }

  public void setTarget(int targetBlock)
  {
    this.targetBlock = targetBlock;
  }

  public void setSourceBlock(int sourceBlock)
  {
    this.sourceBlock = sourceBlock;
  }

  public void stop()
  {
    running = false;
    drawing = false;
  }

  public void setDrawing(boolean drawing)
  {
    this.drawing = drawing;
  }
}
