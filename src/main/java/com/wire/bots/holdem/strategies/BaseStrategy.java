package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;
import com.wire.bots.holdem.Probability;

public abstract class BaseStrategy {
    protected final Player bot;
    private final Hand hand;
    private final Probability probability;
    protected int call;

    BaseStrategy(Player bot) {
        this.bot = bot;
        this.hand = bot.getBestHand();
        probability = new Probability(bot.getBoard(), bot.getCards());
    }

    boolean able() {
        return call < bot.getChips();
    }

    boolean flop() {
        return !bot.getBoard().isEmpty();
    }

    float getChance() {
        return probability.chance(bot);
    }

    boolean playable() {
        return hand.onePair() != -1 || (hand.getCard(0).getRank() > 7 && hand.getCard(1).getRank() > 7);
    }

    HandStrength strength() {
        return hand.getStrength();
    }
}
