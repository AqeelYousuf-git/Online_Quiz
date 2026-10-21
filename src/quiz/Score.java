package quiz;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Score extends JFrame implements ActionListener {

    JButton playAgain, exitBtn;

    Score(String name, int score, int total) {
        setTitle("Score");
        setSize(600, 380);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);

        JLabel thanks = new JLabel("Thank you " + name + " for playing Simple Minds");
        thanks.setBounds(40, 40, 520, 35);
        thanks.setFont(new Font("Arial", Font.BOLD, 24));
        add(thanks);

        JLabel scoreLabel = new JLabel("Your score is " + score + " / " + total);
        scoreLabel.setBounds(40, 120, 500, 40);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 30));
        scoreLabel.setForeground(new Color(30, 144, 254));
        add(scoreLabel);

        playAgain = new JButton("Play Again");
        playAgain.setBounds(120, 230, 150, 35);
        playAgain.setBackground(new Color(30, 144, 254));
        playAgain.setForeground(Color.WHITE);
        playAgain.addActionListener(this);
        add(playAgain);

        exitBtn = new JButton("Exit");
        exitBtn.setBounds(300, 230, 150, 35);
        exitBtn.setBackground(new Color(30, 144, 254));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.addActionListener(this);
        add(exitBtn);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playAgain) {
            dispose();
            new Login().setVisible(true);
        } else {
            System.exit(0);
        }
    }
}