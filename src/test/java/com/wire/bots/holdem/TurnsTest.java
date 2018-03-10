package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;
import org.junit.Test;

public class TurnsTest {
    @Test
    public void test1() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);
        table.turn();

        assert a.isTurn();

        table.turn();

        assert a.isTurn();
    }

    @Test
    public void test2() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);
        Player b = table.addPlayer(newUser("b", "B"), false);

        assert a.isTurn();
        assert !b.isTurn();

        table.turn();
        assert !a.isTurn();
        assert b.isTurn();
    }

    @Test
    public void test3() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);
        Player b = table.addPlayer(newUser("b", "B"), false);
        Player c = table.addPlayer(newUser("c", "C"), false);

        assert a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();

        table.turn();
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();

        table.turn();
        assert !a.isTurn();
        assert !b.isTurn();
        assert c.isTurn();

        table.turn();
        assert a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();
    }

    @Test
    public void test4() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);
        Player b = table.addPlayer(newUser("b", "B"), false);
        Player c = table.addPlayer(newUser("c", "C"), false);
        Player d = table.addPlayer(newUser("d", "D"), false);

        assert a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.turn();
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.turn();
        assert !a.isTurn();
        assert !b.isTurn();
        assert c.isTurn();
        assert !d.isTurn();

        table.turn();
        assert !a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();
        assert d.isTurn();
    }

    @Test
    public void test4Fold() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);
        Player b = table.addPlayer(newUser("b", "B"), false);
        Player c = table.addPlayer(newUser("c", "C"), false);
        Player d = table.addPlayer(newUser("d", "D"), false);

        c.fold();
        d.fold();

        assert a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.turn();
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.turn();
        assert a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.turn();
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();
    }

    @Test
    public void test3Fold() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);
        Player b = table.addPlayer(newUser("b", "B"), false);
        Player c = table.addPlayer(newUser("c", "C"), false);

        a.fold();

        table.turn();
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();

        table.turn();
        assert !a.isTurn();
        assert !b.isTurn();
        assert c.isTurn();

        table.turn();
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();
    }

    private User newUser(String id, String name) {
        User u = new User();
        u.id = id;
        u.name = name;
        return u;
    }
}
