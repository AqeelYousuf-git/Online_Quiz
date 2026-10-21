package quiz;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

public class Quiz extends JFrame implements ActionListener {

    String name;
    int userId;

    ArrayList<String[]> questions = new ArrayList<>();   
    ArrayList<Integer> correct = new ArrayList<>();     
    int current = 0;
    int timeLeft = 15;
    boolean lifelineUsed = false;
    javax.swing.Timer timer;

    JLabel qNoLabel, questionLabel, timerLabel;
    JRadioButton[] options = new JRadioButton[4];
    ButtonGroup group = new ButtonGroup();
    JButton nextBtn, lifelineBtn, submitBtn;
    int[] selected;

    Quiz(String name, int userId) {
        this.name = name;
        this.userId = userId;
        loadQuestions();
        selected = new int[questions.size()];

        setTitle("Quiz");
        setSize(800, 480);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);

        qNoLabel = new JLabel();
        qNoLabel.setBounds(40, 40, 50, 30);
        qNoLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        add(qNoLabel);

        questionLabel = new JLabel();
        questionLabel.setBounds(90, 30, 660, 60);
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        add(questionLabel);

        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setBounds(90, 110 + i * 45, 600, 35);
            options[i].setFont(new Font("Arial", Font.PLAIN, 18));
            options[i].setBackground(Color.WHITE);
            group.add(options[i]);
            add(options[i]);
        }

        timerLabel = new JLabel();
        timerLabel.setBounds(90, 310, 400, 30);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(Color.RED);
        add(timerLabel);

        lifelineBtn = makeButton("50-50 Lifeline", 90, 370, 150);
        nextBtn = makeButton("Next", 260, 370, 110);
        submitBtn = makeButton("Submit", 390, 370, 110);

        showQuestion();

        timer = new javax.swing.Timer(1000, ev -> {
            timeLeft--;
            timerLabel.setText("Time left - " + timeLeft + " seconds");
            if (timeLeft <= 0) {
                goNext();           
            }
        });
        timer.start();
    }

    private JButton makeButton(String text, int x, int y, int w) {
        JButton b = new JButton(text);
        b.setBounds(x, y, w, 35);
        b.setBackground(new Color(30, 144, 254));
        b.setForeground(Color.WHITE);
        b.addActionListener(this);
        add(b);
        return b;
    }

    private void loadQuestions() {
        
        questions.clear();
        correct.clear();
         
        String sql = "SELECT question, opt1, opt2, opt3, opt4, correct_option "
                   + "FROM questions "
                   + "GROUP BY question,opt1,opt2,opt3,opt4,correct_option "
                   + "ORDER BY RAND() LIMIT 10";
        
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                questions.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3),
                                           rs.getString(4), rs.getString(5)});
                correct.add(rs.getInt(6));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
            System.exit(0);
        }
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No questions found in the database.");
            System.exit(0);
        }
    }

    private void showQuestion() {
        String[] q = questions.get(current);
        qNoLabel.setText((current + 1) + ".");
        questionLabel.setText("<html>" + q[0] + "</html>");
        group.clearSelection();
        for (int i = 0; i < 4; i++) {
            options[i].setText(q[i + 1]);
            options[i].setVisible(true);
        }
        boolean last = (current == questions.size() - 1);
        nextBtn.setEnabled(!last);
        submitBtn.setEnabled(last);
        lifelineBtn.setEnabled(!lifelineUsed);
        timeLeft = 15;
        timerLabel.setText("Time left - " + timeLeft + " seconds");
    }

    private void recordAnswer() {
        selected[current] = 0;
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selected[current] = i + 1;
            }
        }
    }

    private void goNext() {
        recordAnswer();
        if (current == questions.size() - 1) {
            finishQuiz();
        } else {
            current++;
            showQuestion();
        }
    }

    private void useLifeline() {
        int right = correct.get(current);
        int hidden = 0;
        for (int i = 0; i < 4 && hidden < 2; i++) {
            if (i + 1 != right) {
                if (options[i].isSelected()) {
                    group.clearSelection();
                }
                options[i].setVisible(false);
                hidden++;
            }
        }
        lifelineUsed = true;
        lifelineBtn.setEnabled(false);
    }

    private void finishQuiz() {
        timer.stop();
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selected[i] == correct.get(i)) {
                score++;
            }
        }
        saveResult(score);
        dispose();
        new Score(name, score, questions.size()).setVisible(true);
    }

    private void saveResult(int score) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO results (user_id, score, total) VALUES (?, ?, ?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, score);
            ps.setInt(3, questions.size());
            ps.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not save result: " + e.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == lifelineBtn) {
            useLifeline();
        } else {
            goNext();     
        }
    }
}