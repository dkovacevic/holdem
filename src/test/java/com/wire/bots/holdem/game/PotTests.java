package com.wire.bots.holdem.game;

import org.junit.Test;

import java.util.UUID;

public class PotTests {
    @Test
    public void testCallInsufficientFunds() {
        Table table = new Table(new Deck());
        table.setRaise(5);

        Player p1 = table.addPlayer(UUID.randomUUID(), "1", false);
        Player p2 = table.addPlayer(UUID.randomUUID(), "2", false);

        p1.setChips(20);
        p2.setChips(3);

        int raise = table.raise(p1);
        assert raise == 5;
        assert p1.getChips() == 15;

        int call = table.call(p2); //he has only 3 chips trying to call 5
        assert call == 3; //
        assert p2.getChips() == 0;
        assert !p2.isCalled();

        int debt = p2.getCall(); //
        table.refund(debt);
        assert p1.getChips() == 17;

        int pot = table.flushPot(p1);

        assert pot == 6;
        assert p1.getChips() == 23;
    }

    @Test
    public void testCallAll() {
        Table table = new Table(new Deck());
        table.setRaise(5);

        Player p1 = table.addPlayer(UUID.randomUUID(), "1", false);
        Player p2 = table.addPlayer(UUID.randomUUID(), "2", false);

        p1.setChips(100);
        p2.setChips(5);

        int raise = table.raise(p1);
        assert raise == 5;
        assert p1.getChips() == 95;

        int call = table.call(p2); //he has only 5 chips trying to call 5
        assert call == 5; //
        assert p2.getChips() == 0;
        assert p2.isCalled();
        assert p2.getCall() == 0;

        assert p1.getChips() == 95;

        int pot = table.flushPot(p1);
        assert pot == 10;
        assert p1.getChips() == 105;
    }

    @Test
    public void testRaiseInsufficientFunds() {
        Table table = new Table(new Deck());
        table.setRaise(5);

        Player p1 = table.addPlayer(UUID.randomUUID(), "1", false);
        Player p2 = table.addPlayer(UUID.randomUUID(), "2", false);

        p1.setChips(5);
        p2.setChips(100);

        int raise = table.raise(p1);
        assert raise == 5;
        assert p1.getChips() == 0;
        assert p1.isCalled();
        assert p1.getCall() == 0;

        int call = table.call(p2);
        assert call == 5;
        assert p2.isCalled();

        // try to raise again just for fun
        p1.setCalled(false);

        int raise2 = table.raise(p1);
        assert raise2 == -1;
        assert p1.getChips() == 0;
        assert !p1.isCalled();

        int pot = table.flushPot(p1);

        assert pot == 10;
        assert p1.getChips() == 10;
    }
}
