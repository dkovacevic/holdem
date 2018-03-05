package com.wire.bots.holdem.strategies;


import com.wire.bots.holdem.Action;
import com.wire.bots.holdem.Hand;

public interface Strategy {
    Action action(Hand hand, int call);
}
