package com.wire.bots.holdem.game;

import com.wire.xenon.backend.models.User;
import org.junit.Test;

import java.util.UUID;

public class TurnsTest {


    @Test
    public void test1() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("A"), false);

        assert a.isTurn();

        table.call(a);

        assert a.isTurn();
    }

    @Test
    public void test2() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);

        assert a.isTurn();
        assert !b.isTurn();

        table.call(a);
        assert !a.isTurn();
        assert b.isTurn();
    }

    @Test
    public void test3() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);
        Player c = table.addPlayer(newUser("C"), false);

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
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);
        Player c = table.addPlayer(newUser("C"), false);
        Player d = table.addPlayer(newUser("D"), false);

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
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);
        Player c = table.addPlayer(newUser("C"), false);
        Player d = table.addPlayer(newUser("D"), false);

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
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);
        Player c = table.addPlayer(newUser("C"), false);

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

    private User newUser(String name) {
        User u = new User();
        u.id = UUID.randomUUID();
        u.name = name;
        return u;
    }
}
