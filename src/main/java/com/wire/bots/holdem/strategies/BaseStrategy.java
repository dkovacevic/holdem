package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Hand;
import com.wire.bots.holdem.HandStrength;
import com.wire.bots.holdem.Player;

public abstract class BaseStrategy {
    protected final Player bot;
    protected final HandStrength strength;
    protected final Hand hand;
    protected int call;

    public BaseStrategy(Player bot) {
        this.bot = bot;
        this.hand = bot.getBestHand();
        this.strength = hand.getStrength();
    }

    protected boolean able() {
        return call < bot.getChips();
    }

    protected boolean flop() {
        return !bot.getBoard().isEmpty();
    }
}
