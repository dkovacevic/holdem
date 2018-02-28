package test;

import com.wire.bots.holdem.Card;
import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class CompareHands {
    public static void main(String[] args) throws IOException {
        Card card1 = new Card(2, 10);
        Card card2 = new Card(1, 0);
        Card card3 = new Card(2, 0);
        Card card4 = new Card(1, 7);
        Card card5 = new Card(0, 3);

        ArrayList<Card> board = new ArrayList<>();
        board.add(card1);
        board.add(card2);
        board.add(card3);
        board.add(card4);
        board.add(card5);

        Player p1 = new Player("p1", "P1", board);
        p1.addCard(new Card(3, 0));
        p1.addCard(new Card(1, 11));

        Player p2 = new Player("p2", "P2", board);
        p2.addCard(new Card(1, 9));
        p2.addCard(new Card(3, 3));

        Collection<Hand> allHands1 = p1.getAllHands();
        assert allHands1.size() == 10;

        Collection<Hand> allHands2 = p2.getAllHands();
        assert allHands2.size() == 10;

        System.out.printf("%s: %s\n", p1.getName(), p1.getBestHand());
        System.out.printf("%s: %s\n", p2.getName(), p2.getBestHand());

        int res = p1.getBestHand().compareTo(p2.getBestHand());
        System.out.printf("%s stronger than %s: %d\n", p1.getName(), p2.getName(), res);
    }
}
