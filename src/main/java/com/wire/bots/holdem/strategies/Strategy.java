package com.wire.bots.holdem.strategies;


import com.wire.bots.holdem.Action;

public interface Strategy {
    Action action(Action cmd);
}
