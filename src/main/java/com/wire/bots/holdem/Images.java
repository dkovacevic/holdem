package com.wire.bots.holdem;

import com.wire.bots.sdk.tools.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Images {
    private static final ConcurrentHashMap<Card, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final String URL = "https://raw.githubusercontent.com/hayeah/playing-cards-assets/master/png";
    private static final int SHIFT = 42;
    private static final int WIDTH = 222;
    private static final int HEIGHT = 323;

    public static byte[] getImage(Collection<Card> cards) throws IOException {
        ArrayList<BufferedImage> load = getBufferedImages(cards);
        BufferedImage combine = combine(load);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(combine, "png", output);
        return output.toByteArray();
    }

    public static byte[] getImage(Card c1, Card c2) throws IOException {
        BufferedImage a = load(c1).get(0);
        BufferedImage b = load(c2).get(0);
        BufferedImage attached = attach(a, b);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(attached, "png", output);
        return output.toByteArray();
    }

    public static byte[] getImage(Collection<Card> coll1, Collection<Card> coll2) throws IOException {
        ArrayList<BufferedImage> load1 = getBufferedImages(coll1);
        ArrayList<BufferedImage> load2 = getBufferedImages(coll2);

        BufferedImage attached = attach(load1, load2);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(attached, "png", output);
        return output.toByteArray();
    }

    private static BufferedImage combine(ArrayList<BufferedImage> images) {
        int size = images.size() - 1;
        final int width = WIDTH + size * SHIFT;

        BufferedImage result = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        for (int i = 0; i < size; i++) {
            BufferedImage image = images.get(i);

            BufferedImage sub = image.getSubimage(0, 0, SHIFT, HEIGHT);
            g.drawImage(sub, i * SHIFT, 0, null);
        }
        BufferedImage image = images.get(size);
        g.drawImage(image, size * SHIFT, 0, null);
        return result;
    }

    private static BufferedImage attach(ArrayList<BufferedImage> images1, ArrayList<BufferedImage> images2) {
        BufferedImage a = combine(images1);
        BufferedImage b = combine(images2);

        int shift = 3 * SHIFT;
        final int width = a.getWidth() + b.getWidth() + shift;

        BufferedImage result = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        g.drawImage(a, 0, 0, null);
        g.drawImage(b, a.getWidth() + shift, 0, null);
        return result;
    }

    private static BufferedImage attach(BufferedImage a, BufferedImage b) {
        final int width = a.getWidth() + b.getWidth();

        BufferedImage result = new BufferedImage(width, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        g.drawImage(a, 0, 0, null);
        g.drawImage(b, a.getWidth(), 0, null);
        return result;
    }

    private static ArrayList<BufferedImage> getBufferedImages(Collection<Card> collection) throws IOException {
        Card[] cards = new Card[collection.size()];
        collection.toArray(cards);
        return load(cards);
    }

    private static BufferedImage getBufferedImage(Card card) throws IOException {
        return cache.computeIfAbsent(card, k -> {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            String path = String.format("%s/%s.png", "cards", card);
            try (InputStream input = classloader.getResourceAsStream(path)) {
                return ImageIO.read(input);
            } catch (IOException e) {
                Logger.error(e.toString());
                return null;
            }
        });
    }

    private static ArrayList<BufferedImage> load(Card... cards) throws IOException {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (Card card : cards)
            images.add(getBufferedImage(card));
        return images;
    }
}
