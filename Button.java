import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Button extends JButton {
    private String first;
    private String second = "";
    private boolean secondDisplayed = false;
    private Color color;
    private static Calculator calculator;

    public Button(String text, Dimension size) {
        this.first = text;
        setText(first);
        // there might be a better way to test if a string is an integer
        try {
            Integer.parseInt(this.first);
            this.color = new Color(200, 200, 200);
        } catch (NumberFormatException e) {
            this.color = new Color(175, 175, 175);
        }
        setBackground(this.color);
        setFocusable(false);
        setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        setPreferredSize(size);

        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClick();
            }
        });
    }

    public String getFirst() {
        return first;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public void toggleSecond() {
        this.secondDisplayed = !this.secondDisplayed;
        if (second.length() > 0 && secondDisplayed) {
            this.setText(second);
        } else {
            this.setText(first);
        }
    }

    private void onClick() {
        calculator.buttonClicked(this);
    }

    public static void setCalculator(Calculator calculator) {
        Button.calculator = calculator;
    }
}