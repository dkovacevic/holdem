package com.wire.bots.holdem;

import com.wire.bots.sdk.tools.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

class Images {
    private static final ConcurrentHashMap<Card, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final String URL = "https://raw.githubusercontent.com/hayeah/playing-cards-assets/master/png";
    private static final int SHIFT = 38;
    private static final int WIDTH = 222;
    private static final int HEIGHT = 323;

    static byte[] getImage(Collection<Card> collection) throws IOException {
        Card[] cards = new Card[collection.size()];
        collection.toArray(cards);
        ArrayList<BufferedImage> load = load(cards);
        BufferedImage combine = combine(load);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(combine, "png", output);
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

    private static BufferedImage getBufferedImage(Card card) throws IOException {
        return cache.computeIfAbsent(card, k -> {
            try (InputStream input = new URL(String.format("%s/%s.png", URL, card)).openStream()) {
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
