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
        this.bestHand = null;
        Hand hand = getBestHand();

        if (hand == null)
            return Action.CALL;

        //Logger.info("Betman best hand: %s", hand.toString());
        
        if (cmd == Action.RAISE) {
            if (hand.getStrength() == HandStrength.HighCard && getBoard().size() >= 4)
                return Action.FOLD;
            else if (hand.getStrength().ordinal() > HandStrength.TwoPair.ordinal())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        if (hand.getStrength().ordinal() >= HandStrength.TwoPair.ordinal())
            return Action.RAISE;

        return Action.CALL;
    }
}
