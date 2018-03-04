package com.wire.bots.holdem;

import java.util.ArrayList;

class BotPlayer extends Player {
    BotPlayer(String userId, String name, ArrayList<Card> board) {
        super(userId, name, board);
        bot = true;
    }

    @Override
    boolean isBot() {
        return bot;
    }

    @Override
    Action action(Action cmd) {
        Hand hand = getBestHand();
        if (hand == null)
            return Action.CALL;

        int call = getCall();

        if (cmd == Action.RAISE) {
            if (hand.getStrenght() == HandStrength.HighCard && getBoard().size() >= 5)
                return Action.FOLD;
            else if (hand.getStrenght().ordinal() > HandStrength.TwoPair.ordinal())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        if (hand.getStrenght().ordinal() >= HandStrength.TwoPair.ordinal())
            return Action.RAISE;

        return Action.CALL;
    }
}
