package test;

import com.wire.bots.holdem.Card;
import com.wire.bots.holdem.Hand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

public class CompareHands {
    public static void main(String[] args) throws IOException {
        Card card1 = new Card((short) 1, (short) 11);
        Card card2 = new Card((short) 3, (short) 12);
        Card card3 = new Card((short) 2, (short) 10);
        Card card4 = new Card((short) 1, (short) 3);
        Card card5 = new Card((short) 0, (short) 3);

        ArrayList<Card> cards = new ArrayList<>();
        cards.add(card1);
        cards.add(card2);
        cards.add(card3);
        cards.add(card4);
        cards.add(card5);

        Hand h1 = new Hand(cards);
        Hand h2 = new Hand(cards);
        HashSet<Hand> hands = new HashSet<>();
        hands.add(h1);
        hands.add(h2);

        Card a1 = new Card((short) 2, (short) 10);
        Card a2 = new Card((short) 1, (short) 11);

        Card b1 = new Card((short) 1, (short) 9);
        Card b2 = new Card((short) 3, (short) 3);

        Collection<Hand> allHandsA = getAllHands(a1, a2, cards);
        Collection<Hand> allHandsB = getAllHands(b1, b2, cards);

        Hand handA = allHandsA.stream().max(Comparator.naturalOrder()).get();
        Hand handB = allHandsB.stream().max(Comparator.naturalOrder()).get();

        System.out.printf("A: %s\n", handA);
        System.out.printf("B: %s\n", handB);

        int res = handA.compareTo(handB);
        System.out.printf("A bigger than B: %d\n", res);
    }

    private static Collection<Hand> getAllHands(Card c1, Card c2, ArrayList<Card> cards) {
        HashSet<Hand> ret = new HashSet<>();
        int n = cards.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (i != j && i != k && j != k) {
                        ArrayList<Card> tmp = new ArrayList<>();
                        tmp.add(c1);
                        tmp.add(c2);
                        tmp.add(cards.get(i));
                        tmp.add(cards.get(j));
                        tmp.add(cards.get(k));

                        ret.add(new Hand(tmp));
                    }
                }
            }
        }
        return ret;
    }

    /* arr[]  ---> Input Array
    data[] ---> Temporary array to store current combination
    start & end ---> Staring and Ending indexes in arr[]
    index  ---> Current index in data[]
    r ---> Size of a combination to be printed */
    static void combinationUtil(int arr[], int data[], int start, int end, int index, int r) {
        // Current combination is ready to be printed, print it
        if (index == r) {
            for (int j = 0; j < r; j++)
                System.out.print(data[j] + " ");
            System.out.println("");
            return;
        }

        // replace index with all possible elements. The condition
        // "end-i+1 >= r-index" makes sure that including one element
        // at index will make a combination with remaining elements
        // at remaining positions
        for (int i = start; i <= end && end - i + 1 >= r - index; i++) {
            data[index] = arr[i];
            combinationUtil(arr, data, i + 1, end, index + 1, r);
        }
    }

    // The main function that prints all combinations of size r
    // in arr[] of size n. This function mainly uses combinationUtil()
    static void printCombination(int arr[], int n, int r) {
        // A temporary array to store all combination one by one
        int data[] = new int[r];

        // Print all combination using temprary array 'data[]'
        combinationUtil(arr, data, 0, n - 1, 0, r);
    }

}
