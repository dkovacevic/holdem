package test;

import com.wire.bots.holdem.Card;
import com.wire.bots.holdem.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CombineCards {
    public static void main(String[] args) throws IOException {
        Card card1 = new Card(1, 11);
        Card card2 = new Card(3, 12);
        Card card3 = new Card(2, 10);
        Card card4 = new Card(0, 0);
        Card card5 = new Card(1, 0);
        Card card6 = new Card(2, 0);
        Card card7 = new Card(3, 0);

        ArrayList<Card> cards = new ArrayList<>();
        cards.add(card1);
        cards.add(card2);
        cards.add(card3);
        cards.add(card4);
        cards.add(card5);
        cards.add(card6);
        cards.add(card7);

        byte[] image = Images.getImage(cards);
        BufferedImage read = ImageIO.read(new ByteArrayInputStream(image));
        ImageIO.write(read, "png", new File("result.png"));

        image = Images.getImage(cards.subList(0, 2), cards.subList(2, 7));
        read = ImageIO.read(new ByteArrayInputStream(image));
        ImageIO.write(read, "png", new File("combined.png"));
    }
}
