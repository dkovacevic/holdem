package com.wire.bots.holdem.game;

import com.wire.bots.sdk.server.model.User;
import org.junit.Test;

public class TurnsTest {
    @Test
    public void test1() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);

        assert a.isTurn();

        table.call(a);

        assert a.isTurn();
    }

    @Test
    public void test2() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"), false);
        Player b = table.addPlayer(newUser("b", "B"), false);

        assert a.isTurn();
        assert !b.isTurn();

        table.call(a);
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

        table.call(a);
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();

        table.call(b);
        assert !a.isTurn();
        assert !b.isTurn();
        assert c.isTurn();

        table.call(c);
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

        table.call(a);
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.call(b);
        assert !a.isTurn();
        assert !b.isTurn();
        assert c.isTurn();
        assert !d.isTurn();

        table.call(c);
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

        table.fold(c);
        table.fold(d);

        assert a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.call(a);
        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.raise(b);
        assert a.isTurn();
        assert !b.isTurn();
        assert !c.isTurn();
        assert !d.isTurn();

        table.raise(a);
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

        table.fold(a);

        assert !a.isTurn();
        assert b.isTurn();
        assert !c.isTurn();

        table.call(b);
        assert !a.isTurn();
        assert !b.isTurn();
        assert c.isTurn();

        table.raise(c);
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
