package com.wire.bots.holdem;

import com.wire.bots.sdk.server.model.User;
import org.junit.Test;

public class ShiftTest {
    @Test
    public void test1() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"));

        assert a.getNameWithRole().equals("A(SB)");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A(SB)");
    }

    @Test
    public void test2() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"));
        Player b = table.addPlayer(newUser("b", "B"));

        assert a.getNameWithRole().equals("A(SB)");
        assert b.getNameWithRole().equals("B(BB)");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A(BB)");
        assert b.getNameWithRole().equals("B(SB)");
    }

    @Test
    public void test3() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"));
        Player b = table.addPlayer(newUser("b", "B"));
        Player c = table.addPlayer(newUser("c", "C"));

        assert a.getNameWithRole().equals("A(SB)");
        assert b.getNameWithRole().equals("B(BB)");
        assert c.getNameWithRole().equals("C");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A");
        assert b.getNameWithRole().equals("B(SB)");
        assert c.getNameWithRole().equals("C(BB)");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A(BB)");
        assert b.getNameWithRole().equals("B");
        assert c.getNameWithRole().equals("C(SB)");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A(SB)");
        assert b.getNameWithRole().equals("B(BB)");
        assert c.getNameWithRole().equals("C");
    }

    @Test
    public void test4() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("a", "A"));
        Player b = table.addPlayer(newUser("b", "B"));
        Player c = table.addPlayer(newUser("c", "C"));
        Player d = table.addPlayer(newUser("d", "D"));

        assert a.getNameWithRole().equals("A(SB)");
        assert b.getNameWithRole().equals("B(BB)");
        assert c.getNameWithRole().equals("C");
        assert d.getNameWithRole().equals("D");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A");
        assert b.getNameWithRole().equals("B(SB)");
        assert c.getNameWithRole().equals("C(BB)");
        assert d.getNameWithRole().equals("D");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A");
        assert b.getNameWithRole().equals("B");
        assert c.getNameWithRole().equals("C(SB)");
        assert d.getNameWithRole().equals("D(BB)");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A(BB)");
        assert b.getNameWithRole().equals("B");
        assert c.getNameWithRole().equals("C");
        assert d.getNameWithRole().equals("D(SB)");

        table.shiftPlayers();
        assert a.getNameWithRole().equals("A(SB)");
        assert b.getNameWithRole().equals("B(BB)");
        assert c.getNameWithRole().equals("C");
        assert d.getNameWithRole().equals("D");

    }

    private User newUser(String id, String name) {
        User u = new User();
        u.id = id;
        u.name = name;
        return u;
    }
}
