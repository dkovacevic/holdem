package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public class LooseAggressive implements Strategy {

    private final Player bot;

    public LooseAggressive(Player bot) {
        this.bot = bot;
    }

    @Override
    public Action action(Action cmd) {
        Hand hand = bot.getBestHand();
        if (bot.getBoard().isEmpty()) {
            if (hand.onePair() != -1 || hand.strongestCard() > 7) // One pair or High card stronger than 10
                return Action.RAISE;
            else
                return Action.CALL;
        }

        HandStrength strength = hand.getStrength();

        // it was a raise
        if (bot.getCall() > 0) {
            if (strength.ordinal() >= HandStrength.TwoPair.ordinal())
                return Action.RAISE;
            else
                return Action.CALL;
        }

        // it was a check or a fold

        if (strength.ordinal() >= HandStrength.OnePair.ordinal())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
