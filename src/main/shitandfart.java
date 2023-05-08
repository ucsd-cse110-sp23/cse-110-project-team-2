import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;

public class shitandfart {
    public static void main(String[] args) {
        String[] stuff = {
            "apple",
            "orange",
            "poop",
            "fart",
            "car"
        };

        JFrame frame = new JFrame();
        frame.setSize(700,500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLayout(new BorderLayout());

        JPanel bluePanel = new JPanel(new BorderLayout());
        bluePanel.setBackground(Color.BLUE);
        bluePanel.setPreferredSize(new Dimension(300, 500));

        JPanel list = new JPanel();
        list.setBackground(Color.GREEN);
        list.setPreferredSize(new Dimension(300,200));
        list.add(new JList<String>(stuff));
        bluePanel.add(list, BorderLayout.SOUTH);

        JPanel redPanel = new JPanel();
        redPanel.setBackground(Color.RED);
        redPanel.setPreferredSize(new Dimension(400, 500));

        frame.add(bluePanel, BorderLayout.WEST);
        frame.add(redPanel, BorderLayout.CENTER);
        
        frame.setVisible(true);
    }
}
