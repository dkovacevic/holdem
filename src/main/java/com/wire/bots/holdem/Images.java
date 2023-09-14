package com.wire.bots.holdem;

import com.wire.bots.holdem.game.Card;
import com.wire.xenon.tools.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class Images {
    private static final int SHIFT = 30;
    private static final String PNG = "png";
    private static final float FACTOR = 0.7f;

    public static BufferedImage getImage(Collection<Card> cards) throws IOException {
        ArrayList<BufferedImage> load = load(cards);
        return combine(load);
    }

    public static BufferedImage getImage(Card c1, Card c2) throws IOException {
        BufferedImage a = load(c1).get(0);
        BufferedImage b = load(c2).get(0);
        return attach(a, b);
    }

    public static BufferedImage getImage(Collection<Card> coll1, Collection<Card> coll2) throws IOException {
        ArrayList<BufferedImage> load1 = load(coll1);
        ArrayList<BufferedImage> load2 = load(coll2);

        return attach(load1, load2);
    }

    public static BufferedImage combine(ArrayList<BufferedImage> images) {
        final BufferedImage first = images.get(0);
        final int size = images.size() - 1;
        final int width = first.getWidth() + (size * SHIFT);

        BufferedImage result = new BufferedImage(width, first.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        for (int i = 0; i < size; i++) {
            BufferedImage image = images.get(i);

            BufferedImage sub = image.getSubimage(0, 0, SHIFT, image.getHeight());
            g.drawImage(sub, i * SHIFT, 0, null);
        }
        BufferedImage image = images.get(size);
        g.drawImage(image, size * SHIFT, 0, null);
        g.dispose();
        return result;
    }

    public static BufferedImage combine(BufferedImage img1, BufferedImage img2) {
        final int width = Math.max(img1.getWidth(), img2.getWidth()) + SHIFT;
        final int height = Math.max(img1.getHeight(), img2.getHeight());

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        BufferedImage sub = img1.getSubimage(0, 0, SHIFT, img1.getHeight());
        g.drawImage(img1, 0, 0, null);

        g.drawImage(img2, SHIFT, 0, null);
        g.dispose();
        return result;
    }

    public static BufferedImage attach(ArrayList<BufferedImage> images1, ArrayList<BufferedImage> images2) {
        BufferedImage a = combine(images1);
        BufferedImage b = combine(images2);

        int shift = 3 * SHIFT;
        final int width = a.getWidth() + b.getWidth() + shift;

        BufferedImage result = new BufferedImage(width, a.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        g.drawImage(a, 0, 0, null);
        g.drawImage(b, a.getWidth() + shift, 0, null);
        g.dispose();
        return result;
    }

    public static byte[] getBytes(BufferedImage combine) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(combine, PNG, output);
            return output.toByteArray();
        }
    }

    private static BufferedImage attach(BufferedImage a, BufferedImage b) {
        int shift = 5 * SHIFT;
        final int width = a.getWidth() + b.getWidth() + shift;

        BufferedImage result = new BufferedImage(width, a.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        g.drawImage(a, 0, 0, null);
        g.drawImage(b, a.getWidth() + shift, 0, null);
        g.dispose();
        return result;
    }

    private static BufferedImage getBufferedImage(Card card) {
        return getBufferedImage(card, FACTOR, Color.WHITE);
    }

    private static BufferedImage getBufferedImage(Card card, float scale, Color color) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String path = String.format("%s/%s.png", "cards", card);
        try (InputStream input = new FileInputStream(path)) {
            BufferedImage read = ImageIO.read(input);
            BufferedImage image = replaceAlpha(read, color);
            return scale(image, scale);
        } catch (IOException e) {
            Logger.error(e.toString());
            return null;
        }
    }

    public static BufferedImage getBufferedImage(Card card, Color color) {
        return getBufferedImage(card, FACTOR, color);
    }

    public static BufferedImage getOriginalBufferedImage(Card card) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String path = String.format("%s/%s.png", "cards", card);
        try (InputStream input = new FileInputStream(path)) {
            return ImageIO.read(input);
        } catch (IOException e) {
            Logger.error(e.toString());
            return null;
        }
    }

    private static BufferedImage scale(BufferedImage image, float scale) {
        int w = (int) (image.getWidth() * scale);
        int h = (int) (image.getHeight() * scale);
        Image scaled = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = newImage.getGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return newImage;
    }

    private static ArrayList<BufferedImage> load(Card... cards) throws IOException {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (Card card : cards) {
            BufferedImage bufferedImage = getBufferedImage(card);
            images.add(bufferedImage);
        }
        return images;
    }

    private static ArrayList<BufferedImage> load(Collection<Card> collection) {
        Card[] cards = new Card[collection.size()];
        collection.toArray(cards);
        try {
            return load(cards);
        } catch (IOException e) {
            Logger.error(e.toString());
            return null;
        }
    }

    private static BufferedImage replaceAlpha(BufferedImage image, Color color) {
        BufferedImageOp lookup = new LookupOp(new LookupTable(0, 4) {
            @Override
            public int[] lookupPixel(int[] src, int[] dest) {
                if (src[3] == 0) {
                    dest[0] = color.getRed();
                    dest[1] = color.getGreen();
                    dest[2] = color.getBlue();
                    dest[3] = color.getAlpha();
                }
                return dest;
            }
        }, null);
        return lookup.filter(image, null);
    }
}
