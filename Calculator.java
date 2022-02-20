// Eric Kugel

// The keyboard can be used to enter stuff instead of the buttons
// The button layout can be customized in layout.txt

// Follows pemdas, including parentheses and other grouping

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Calculator extends JFrame {
    public static final int WIDTH = 480;
    public static final int HEIGHT = 500;

    private JTextField screen = new JTextField();
    private JTextField lastInput = new JTextField();

    private ArrayList<Button> buttons = new ArrayList<Button>();

    private boolean isRadians = true;
    private boolean second = false;

    public Calculator() {
        Button.setCalculator(this);
        setTitle("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        initGUI();
        pack();
        setVisible(true);
    }

    private void initGUI() {
        // main panel
        ArrayList<ArrayList<String>> buttonLayout = getButtonLayout();
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(main);
        
        // menu bar for the mode dropdown
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu mode = new JMenu("Mode");
        menuBar.add(mode);
        ButtonGroup modeButtons = new ButtonGroup();
        JRadioButton rad = new JRadioButton("rad", true);
        JRadioButton deg = new JRadioButton("deg");
        rad.setFocusable(false);
        deg.setFocusable(false);
        modeButtons.add(rad);
        modeButtons.add(deg);
        mode.add(rad);
        mode.add(deg);
        ActionListener modeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIsRadians(rad.isSelected());
            }
        };
        rad.addActionListener(modeListener);
        deg.addActionListener(modeListener);

        // shows the last input
        lastInput.setPreferredSize(new Dimension(WIDTH, 20));
        lastInput.setHorizontalAlignment(SwingConstants.RIGHT);
        lastInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lastInput.setBackground(new Color(255, 255, 255));
        lastInput.setForeground(new Color(155, 155, 155));
        lastInput.setEditable(false);
        main.add(lastInput);
        
        // shows the input/output
        screen.setPreferredSize(new Dimension(WIDTH, 60));
        screen.setHorizontalAlignment(SwingConstants.RIGHT);
        screen.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 30));
        screen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonClicked(new Button("=", new Dimension(0,0)));
            }
        });
        main.add(screen);

        // holds the "2nd" button
        JPanel secondContainer = new JPanel();
        secondContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        secondContainer.setLayout(new BoxLayout(secondContainer, BoxLayout.X_AXIS));
        JButton second = new JButton("2nd");
        second.setFocusable(false);
        second.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleSecond();
            }
        });
        second.setBackground(new Color(175, 175, 175));
        secondContainer.add(second);
        secondContainer.add(Box.createHorizontalGlue());
        main.add(secondContainer);

        // holds the rest of the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        main.add(buttonPanel);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // using the button layout
        int rows = buttonLayout.size();
        for (ArrayList<String> buttonRowLayout : buttonLayout) {
            int cols = buttonRowLayout.size();
            JPanel buttonRowPanel = new JPanel();
            buttonRowPanel.setLayout(new GridLayout(1, cols));
            for (String buttonText : buttonRowLayout) {
                Button button = new Button(buttonText, new Dimension(WIDTH / cols, HEIGHT / rows));
                // set button secondary button functions
                if (buttonText.equals("sin")) {
                    button.setSecond("sin\u207b¹");
                } else if (buttonText.equals("cos")) {
                    button.setSecond("cos\u207b¹");
                } else if (buttonText.equals("tan")) {
                    button.setSecond("tan\u207b¹");
                } else if (buttonText.equals("ln")) {
                    button.setSecond("log\u2081\u2080");
                } else if (buttonText.equals("abs")) {
                    button.setSecond("fac");
                }
                this.buttons.add(button);
                buttonRowPanel.add(button);
            }
            buttonPanel.add(buttonRowPanel);
        }
    }

    // change between degrees and radians
    private void setIsRadians(boolean isRadians) {
        this.isRadians = isRadians;
    }

    // toggles second buttons (sin -> sin^-1)
    private void toggleSecond() {
        this.second = !second;
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            button.toggleSecond();
        }
    }

    // gets the button layout from layout.txt
    private ArrayList<ArrayList<String>> getButtonLayout() {
        ArrayList<ArrayList<String>> layout = new ArrayList<ArrayList<String>>();
        try {
            Scanner scanner = new Scanner(new File("layout.txt"), "UTF-8");
            while (scanner.hasNextLine()) {
                String lineString = scanner.nextLine();
                if (!lineString.startsWith("#")) {
                    ArrayList<String> line = new ArrayList<String>();
                    String[] buttons = lineString.split(":");
                    for (String button : buttons) {
                        if (button.length() > 0) {
                            line.add(button);
                        }
                    }
                    layout.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return layout;
    }

    public void buttonClicked(Button button) {
        String buttonText = button.getText();
        String screenText = screen.getText();
        if (this.second) {
            toggleSecond();
        }
        if (ParseMath.isFunction(0, buttonText)) {
            int position = screen.getCaretPosition();
            screen.setText(screenText.substring(0, position) + buttonText + "()" + screenText.substring(position));
            screen.setCaretPosition(position+buttonText.length()+1);
        } else if (buttonText.equals("=")) {
            try {
                screen.setText("" + ParseMath.solve(ParseMath.parseFormula(screenText, isRadians)));
                lastInput.setText(ParseMath.formatFormula(screenText));
            } catch (Exception e) {
                // Usually thrown by bad syntax
                JOptionPane.showMessageDialog(this, "Check your syntax", "Something went wrong", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else if (buttonText.equals("C")) {
            // clear
            screen.setText("");
        } else if (buttonText.equals("del")) {
            // backspace
            int position = screen.getCaretPosition();
            try {
                if (position < screenText.length()) {
                    screen.setText(screenText.substring(0, position - 1) + screenText.substring(position));
                } else {
                    screen.setText(screenText.substring(0, screenText.length() - 1));
                }
            } catch (Exception e) {
                // do nothing
            }
            screen.setCaretPosition(position - 1);
        } else if (buttonText.equals("->")) {
            int position = screen.getCaretPosition();
            try {
                screen.setCaretPosition(position + 1);
            } catch(IllegalArgumentException e) {
                //do nothing
            }
        } else if (buttonText.equals("<-")) {
            int position = screen.getCaretPosition();
            try {
                screen.setCaretPosition(position - 1);
            } catch(IllegalArgumentException e) {
                //do nothing
            }
        } else {
            int position = screen.getCaretPosition();
            screen.setText(screenText.substring(0, position) + buttonText + screenText.substring(position));
            screen.setCaretPosition(position+buttonText.length());
        }
    }

    public static void main(String[] arg0) {
        new Calculator();
    }
}