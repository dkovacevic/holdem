package com.wire.bots.holdem.strategies;

import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.game.Player;

public class LoosePassive extends BaseStrategy implements Strategy {

    public LoosePassive(Player bot) {
        super(bot);
    }

    @Override
    public Action action(Action cmd) {
        if (!flop()) {
            if (!playable())
                return Action.FOLD;
            else
                return Action.CALL;
        }

        if (call > 0) {
            if (getChance() > 25f)
                return Action.CALL;
            else
                return Action.FOLD;
        }

        // it was a call

        if (getChance() > 80f && able())
            return Action.RAISE;
        else
            return Action.CALL;
    }
}
