import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.FileInputStream;


public class PlaySound {
    private Player player;

    public PlaySound() {
    }

    public void playSound() throws Exception{
        try {
            FileInputStream fileInputStream = new FileInputStream("output.mp3");
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            player = new Player(bufferedInputStream);
        } catch (Exception e) {
            System.out.println("Problem playing mp3 file output.mp3");
            System.out.println(e.getMessage());
        }

        new Thread() {
            public void run() {
                try {
                    player.play();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }.start();

    }

    public void close() {
        if (player != null) player.close();
    }

}
