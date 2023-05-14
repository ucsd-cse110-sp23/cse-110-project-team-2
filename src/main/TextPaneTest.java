/*
 * Adapted from here: https://stackoverflow.com/questions/9650992/how-to-change-text-color-in-the-jtextarea
 * 
 */

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

import javax.swing.border.*;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class TextPaneTest extends JFrame
{
    private JPanel topPanel;
    private JTextPane tPane;

    public TextPaneTest()
    {
        topPanel = new JPanel();        

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);            

        EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));

        tPane = new JTextPane();                
        tPane.setBorder(eb);
        tPane.setEditable(false);
        tPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        tPane.setMargin(new Insets(5, 5, 5, 5));
        tPane.setBackground(Color.LIGHT_GRAY);

        topPanel.add(tPane);
        topPanel.setBackground(Color.ORANGE);

        appendToPane(tPane, "My Name is Too Good.\n", Color.RED);
        appendToPane(tPane, "I wish I could be ONE of THE BEST on ", Color.BLUE);
        appendToPane(tPane, "Stack", Color.DARK_GRAY);
        appendToPane(tPane, "Over", Color.MAGENTA);
        appendToPane(tPane, "flow", Color.ORANGE);

        tPane.setText("bruh");

        getContentPane().add(topPanel);

        pack();
        setVisible(true);   
    }

    private void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tPane.setEditable(true);
        tp.replaceSelection(msg);
        tPane.setEditable(false);
    }

    public static void main(String... args)
    {
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    new TextPaneTest();
                }
            });
    }
}