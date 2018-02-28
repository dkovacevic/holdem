package test;

import com.wire.bots.holdem.Card;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class CombineCards {
    private static final String URL = "https://raw.githubusercontent.com/hayeah/playing-cards-assets/master/png";
    private static final int SHIFT = 35;
    private static final int WIDTH = 222;
    private static final int HEIGHT = 323;

    public static void main(String[] args) throws IOException {
        Card card1 = new Card(1, 11);
        Card card2 = new Card(3, 12);
        Card card3 = new Card(2, 10);
        Card card4 = new Card(1, 3);
        Card card5 = new Card(0, 0);

        ArrayList<BufferedImage> images = load(card1, card2, card3, card4, card5);

        BufferedImage result = combine(images);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(result, "png", output);
        byte[] bytes = output.toByteArray();

        ImageIO.write(result, "png", new File("result.png"));
    }

    private static ArrayList<BufferedImage> load(Card... cards) throws IOException {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (Card card : cards)
            images.add(getBufferedImage(card));
        return images;
    }

    private static BufferedImage combine(ArrayList<BufferedImage> images) {
        final int width = WIDTH + (images.size() - 1) * SHIFT;

        BufferedImage result = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        for (int i = 0; i < images.size() - 1; i++) {
            BufferedImage image = images.get(i);

            BufferedImage subimage = image.getSubimage(0, 0, SHIFT, HEIGHT);
            g.drawImage(subimage, i * SHIFT, 0, null);
        }
        BufferedImage image = images.get(images.size() - 1);
        g.drawImage(image, (images.size() - 1) * SHIFT, 0, null);
        return result;
    }

    private static BufferedImage getBufferedImage(Card card) throws IOException {
        try (InputStream input = new URL(String.format("%s/%s.png", URL, card)).openStream()) {
            return ImageIO.read(input);
        }
    }
}
