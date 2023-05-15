import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class JScrollablePanelTest extends JFrame {
   public JScrollablePanelTest() {
      setTitle("JScrollablePanel Test");
      setLayout(new BorderLayout());
      JPanel panel = createPanel();
      add(BorderLayout.CENTER, new JScrollPane(panel));
      setSize(375, 250);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      setVisible(true);
   }
   public static JPanel createPanel() {
      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(100, 4, 10, 10));
      for (int i=0; i < 100; i++) {
         for (int j=0; j < 4; j++) {
            JLabel label = new JLabel("label " + i + ", " + j);
            label.setFont(new Font("Arial", Font.PLAIN, 20));
            panel.add(label);
         }
      }
      return panel;
   }
   public static void main(String [] args) {
      new JScrollablePanelTest();
   }
}