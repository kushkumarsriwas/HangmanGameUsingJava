import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HangmanGame extends JFrame implements ActionListener {
    private static final String[] WORDS = {"computer", "science", "java", "hangman", "programming", "university", "technology"};
    private static final int MAX_ATTEMPTS = 6;

    private Trie trie;
    private PriorityQueue<PlayerScore> leaderboard;
    private String wordToGuess;
    private char[] guessedWord;
    private Set<Character> guessedLetters;
    private int attemptsLeft;

    private JLabel wordLabel, hangmanLabel, leaderboardLabel;
    private JTextField inputField;
    private JButton guessButton;
    private HangmanPanel hangmanPanel;
    private JPanel inputPanel;

    public HangmanGame() {
        trie = new Trie();
        for (String word : WORDS) trie.insert(word);

        leaderboard = new PriorityQueue<>((a, b) -> b.score - a.score);

        setupUI();
        resetGame();
    }

    private void setupUI() {
        setTitle("Hangman Game");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel credits = new JLabel("<html><div style='text-align:left; border:2px solid black; padding:10px; font-weight:bold;'>Project Completed by,<br>Kush Kumar Sriwas (UID: 23BCS14211)<br>Milan Dhiman (UID: 23BCS14208)<br>Under the Supervision of Er. Jyoti Arora (E-Code: 16716)</div></html>");
        credits.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(credits, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(3, 1));
        wordLabel = new JLabel("Word: ", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Courier New", Font.BOLD, 24));

        hangmanLabel = new JLabel("Attempts left: " + MAX_ATTEMPTS, SwingConstants.CENTER);
        hangmanLabel.setFont(new Font("Arial", Font.BOLD, 20));

        inputPanel = new JPanel();
        inputField = new JTextField(5);
        inputField.setFont(new Font("Arial", Font.BOLD, 20));
        guessButton = new JButton("Guess");
        guessButton.setFont(new Font("Arial", Font.BOLD, 20));
        guessButton.addActionListener(this);

        String buttons = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (char ch : buttons.toCharArray()) {
            JButton letterButton = new JButton(String.valueOf(ch));
            letterButton.setFont(new Font("Arial", Font.PLAIN, 16));
            letterButton.addActionListener(ev -> {
                inputField.setText(String.valueOf(ch).toLowerCase());
                guessButton.doClick();
            });
            inputPanel.add(letterButton);
        }

        inputPanel.add(new JLabel("Enter a letter: ")).setFont(new Font("Arial", Font.BOLD, 18));
        inputPanel.add(inputField);
        inputPanel.add(guessButton);

        leaderboardLabel = new JLabel("Leaderboard:", SwingConstants.CENTER);
        leaderboardLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        centerPanel.add(wordLabel);
        centerPanel.add(hangmanLabel);
        centerPanel.add(inputPanel);
        add(centerPanel, BorderLayout.CENTER);
        add(leaderboardLabel, BorderLayout.SOUTH);

        hangmanPanel = new HangmanPanel();
        hangmanPanel.setPreferredSize(new Dimension(200, 300));
        add(hangmanPanel, BorderLayout.WEST);

        setVisible(true);
    }

    private void resetGame() {
        Random rand = new Random();
        wordToGuess = WORDS[rand.nextInt(WORDS.length)];
        guessedWord = new char[wordToGuess.length()];
        guessedLetters = new HashSet<>();
        attemptsLeft = MAX_ATTEMPTS;

        int revealCount = Math.max(1, wordToGuess.length() / 4);
        Set<Integer> revealedIndexes = new HashSet<>();
        while (revealedIndexes.size() < revealCount) {
            int index = rand.nextInt(wordToGuess.length());
            revealedIndexes.add(index);
            guessedLetters.add(wordToGuess.charAt(index));
        }

        for (int i = 0; i < wordToGuess.length(); i++) {
            guessedWord[i] = revealedIndexes.contains(i) ? wordToGuess.charAt(i) : '_';
        }

        updateLabels();
        hangmanPanel.repaint();
    }

    private void updateLabels() {
        StringBuilder displayWord = new StringBuilder();
        for (int i = 0; i < wordToGuess.length(); i++) {
            char c = wordToGuess.charAt(i);
            if (guessedLetters.contains(c)) {
                displayWord.append(c).append(" ");
            } else {
                displayWord.append("_ ");
            }
        }
        wordLabel.setText("Word: " + displayWord.toString().trim());
        hangmanLabel.setText("Attempts left: " + attemptsLeft);

        StringBuilder lbText = new StringBuilder("<html><div style='text-align:center;'>Leaderboard:<br>");
        PriorityQueue<PlayerScore> copy = new PriorityQueue<>(leaderboard);
        while (!copy.isEmpty()) {
            PlayerScore ps = copy.poll();
            lbText.append(ps.name).append(" - ").append(ps.score).append("<br>");
        }
        lbText.append("</div></html>");
        leaderboardLabel.setText(lbText.toString());
        hangmanPanel.repaint();
    }

    private boolean isWordGuessed() {
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (!guessedLetters.contains(wordToGuess.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        String input = inputField.getText().toLowerCase();
        if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
            char guess = input.charAt(0);
            if (!guessedLetters.contains(guess)) {
                guessedLetters.add(guess);
                if (wordToGuess.indexOf(guess) >= 0) {
                    for (int i = 0; i < wordToGuess.length(); i++) {
                        if (wordToGuess.charAt(i) == guess) {
                            guessedWord[i] = guess;
                        }
                    }
                } else {
                    attemptsLeft--;
                }
            }
        }

        inputField.setText("");
        updateLabels();

        if (isWordGuessed()) {
            String name = JOptionPane.showInputDialog("You won! Enter your name:");
            if (name != null && !name.trim().isEmpty()) {
                leaderboard.add(new PlayerScore(name, attemptsLeft));
            }
            resetGame();
        } else if (attemptsLeft == 0) {
            JOptionPane.showMessageDialog(this, "Game Over! The word was: " + wordToGuess);
            resetGame();
        }
    }

    public static void main(String[] args) {
        new HangmanGame();
    }

    class HangmanPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));

            g2.drawLine(50, 250, 150, 250);
            g2.drawLine(100, 250, 100, 50);
            g2.drawLine(100, 50, 180, 50);
            g2.drawLine(180, 50, 180, 70);

            int parts = MAX_ATTEMPTS - attemptsLeft;
            if (parts > 0) g2.drawOval(160, 70, 40, 40);
            if (parts > 1) g2.drawLine(180, 110, 180, 170);
            if (parts > 2) g2.drawLine(180, 120, 150, 150);
            if (parts > 3) g2.drawLine(180, 120, 210, 150);
            if (parts > 4) g2.drawLine(180, 170, 150, 200);
            if (parts > 5) g2.drawLine(180, 170, 210, 200);
        }
    }

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }

    static class Trie {
        private TrieNode root = new TrieNode();

        void insert(String word) {
            TrieNode node = root;
            for (char ch : word.toCharArray()) {
                node = node.children.computeIfAbsent(ch, k -> new TrieNode());
            }
            node.isEndOfWord = true;
        }
    }

    static class PlayerScore {
        String name;
        int score;

        PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}
