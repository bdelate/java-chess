package chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StartGUI extends JFrame implements ActionListener
{
  GraphicsEnvironment ge;

  JButton singlePlayerButton = new JButton("Human vs Computer");
  JButton multiPlayerButton = new JButton("Human vs Human");
  JButton exitButton = new JButton("Exit");

  JLabel chessLabel = new JLabel(new ImageIcon("../images/chess.png"));

  public StartGUI()
  {
    super("Chess");
    setResizable(false);
    setSize(320, 300);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //get centre point to centre JFrame
    ge = ge.getLocalGraphicsEnvironment();
    double x = ge.getCenterPoint().getX();
    double y = ge.getCenterPoint().getY();
    setLocation((int)x - 160, (int)y - 150);

    JPanel pane = new JPanel();
    pane.setLayout(null);
    setContentPane(pane);
    pane.setBackground(Color.BLACK);

    //chess header
    chessLabel.setBounds(10, 10, 300, 100);
    pane.add(chessLabel);

    //single player
    singlePlayerButton.setBounds(85, 130, 150, 20);
    singlePlayerButton.setOpaque(false);
    singlePlayerButton.setForeground(Color.RED);
    singlePlayerButton.addActionListener(this);
    pane.add(singlePlayerButton);

    //multiplayer
    multiPlayerButton.setBounds(85, 160, 150, 20);
    multiPlayerButton.setOpaque(false);
    multiPlayerButton.setForeground(Color.RED);
    multiPlayerButton.addActionListener(this);
    pane.add(multiPlayerButton);

    //exit game
    exitButton.setBounds(110, 190, 100, 20);
    exitButton.setOpaque(false);
    exitButton.setForeground(Color.RED);
    exitButton.addActionListener(this);
    pane.add(exitButton);
  }

  public void actionPerformed(ActionEvent e)
  {
    Object src = e.getSource();

    if (src == singlePlayerButton)
    {
      GameWindow gw = new GameWindow(1);
      gw.show();
      this.dispose();
    }
    else if (src == multiPlayerButton)
    {
      GameWindow gw = new GameWindow(2);
      gw.show();
      this.dispose();
    }
    else if (src == exitButton)
      System.exit(0);
  }

  public static void main(String[] args)
  {
    StartGUI gui = new StartGUI();
    gui.show();
  }
}
