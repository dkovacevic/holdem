package test;

import com.wire.bots.holdem.Card;
import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class CompareHands {
    public static void main(String[] args) throws IOException {
        Card card1 = new Card((short) 2, (short) 10);
        Card card2 = new Card((short) 1, (short) 0);
        Card card3 = new Card((short) 2, (short) 0);
        Card card4 = new Card((short) 1, (short) 7);
        Card card5 = new Card((short) 0, (short) 3);

        ArrayList<Card> board = new ArrayList<>();
        board.add(card1);
        board.add(card2);
        board.add(card3);
        board.add(card4);
        board.add(card5);

        Player p1 = new Player("p1", "P1", board);
        p1.addCard(new Card((short) 3, (short) 0));
        p1.addCard(new Card((short) 1, (short) 11));

        Player p2 = new Player("p2", "P2", board);
        p2.addCard(new Card((short) 1, (short) 9));
        p2.addCard(new Card((short) 3, (short) 3));

        Collection<Hand> allHands1 = p1.getAllHands(board);
        assert allHands1.size() == 10;

        Collection<Hand> allHands2 = p2.getAllHands(board);
        assert allHands2.size() == 10;

        System.out.printf("%s: %s\n", p1.getName(), p1.getBestHand());
        System.out.printf("%s: %s\n", p2.getName(), p2.getBestHand());

        int res = p1.getBestHand().compareTo(p2.getBestHand());
        System.out.printf("%s stronger than %s: %d\n", p1.getName(), p2.getName(), res);
    }
}
