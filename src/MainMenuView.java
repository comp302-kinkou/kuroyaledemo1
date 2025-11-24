import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuView {

    public MainMenuView() {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        JLabel label = new JLabel("KU Royale"); // Title
        label.setFont(new Font("Arial", Font.BOLD, 30));
        label.setHorizontalAlignment(SwingConstants.CENTER); // Title finished

        panel.setBackground(new Color(70, 210, 230)); //background color

        panel.setBorder(BorderFactory.createEmptyBorder(30,30,10,30));
        panel.setLayout(new GridLayout(0,1 ));
        panel.add(label);

        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JButton btn_ngame = new JButton("New Game");
        panel.add(btn_ngame);

        JButton btn_bdeck = new JButton("Build Deck");
        panel.add(btn_bdeck);

        JButton btn_darena = new JButton("Design Arena");
        panel.add(btn_darena);

        JButton btn_quit = new JButton("Quit");
        panel.add(btn_quit);
        btn_quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // quits game
                System.exit(0);
            }
        });

        frame.setTitle("Main Menu");
        frame.setSize(500, 800);
        frame.setResizable(false);
        frame.setVisible(true);


    }

}
