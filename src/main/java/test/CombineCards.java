package test;

import com.wire.bots.holdem.Images;
import com.wire.bots.holdem.game.Card;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CombineCards {

    private static final String PNG = "png";

    public static void main(String[] args) throws IOException {
        Card card1 = new Card(1, 11);
        Card card2 = new Card(3, 12);
        Card card3 = new Card(2, 10);
        Card card4 = new Card(0, 9);
        Card card5 = new Card(1, 8);
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
        ImageIO.write(read, PNG, new File("result.png"));

        image = Images.getImage(cards.subList(0, 2), cards.subList(2, 7));
        read = ImageIO.read(new ByteArrayInputStream(image));
        ImageIO.write(read, PNG, new File("combined.png"));

        image = Images.getImage(card1, card2);
        read = ImageIO.read(new ByteArrayInputStream(image));
        ImageIO.write(read, PNG, new File("attached.png"));

        BufferedImage image1 = Images.getBufferedImage(card1, 1f, Color.WHITE);
        BufferedImage image2 = Images.getBufferedImage(card2, 1f, Color.LIGHT_GRAY);
        BufferedImage image3 = Images.getBufferedImage(card3, 1f, Color.WHITE);
        BufferedImage image4 = Images.getBufferedImage(card4, 1f, Color.LIGHT_GRAY);
        BufferedImage image5 = Images.getBufferedImage(card5, 1f, Color.WHITE);

        ArrayList<BufferedImage> list = new ArrayList<>();
        list.add(image1);
        list.add(image2);
        list.add(image3);
        list.add(image4);
        list.add(image5);

        BufferedImage combine = Images.combine(list);
        ImageIO.write(combine, PNG, new File("board.png"));


    }
}
