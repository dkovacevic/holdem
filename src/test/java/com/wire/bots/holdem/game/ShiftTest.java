package com.wire.bots.holdem.game;

import com.wire.bots.sdk.server.model.User;
import org.junit.Test;

import java.util.UUID;

public class ShiftTest {
    @Test
    public void test1() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("A"), false);

        assert a.getRole() == Role.SB;

        table.shiftRoles();

        assert a.getRole() == Role.SB;
    }

    @Test
    public void test2() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);

        assert a.getRole() == Role.SB;
        assert b.getRole() == Role.BB;

        table.shiftRoles();
        assert a.getRole() == Role.BB;
        assert b.getRole() == Role.SB;
    }

    @Test
    public void test3() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);
        Player c = table.addPlayer(newUser("C"), false);

        assert a.getRole() == Role.SB;
        assert b.getRole() == Role.BB;
        assert c.getRole() == Role.Player;

        table.shiftRoles();
        assert a.getRole() == Role.Player;
        assert b.getRole() == Role.SB;
        assert c.getRole() == Role.BB;

        table.shiftRoles();
        assert a.getRole() == Role.BB;
        assert b.getRole() == Role.Player;
        assert c.getRole() == Role.SB;

        table.shiftRoles();
        assert a.getRole() == Role.SB;
        assert b.getRole() == Role.BB;
        assert c.getRole() == Role.Player;
    }

    @Test
    public void test4() {
        Table table = new Table(new Deck());
        Player a = table.addPlayer(newUser("A"), false);
        Player b = table.addPlayer(newUser("B"), false);
        Player c = table.addPlayer(newUser("C"), false);
        Player d = table.addPlayer(newUser("D"), false);

        assert a.getRole() == Role.SB;
        assert b.getRole() == Role.BB;
        assert c.getRole() == Role.Player;
        assert d.getRole() == Role.Player;

        table.shiftRoles();
        assert a.getRole() == Role.Player;
        assert b.getRole() == Role.SB;
        assert c.getRole() == Role.BB;
        assert d.getRole() == Role.Player;

        table.shiftRoles();
        assert a.getRole() == Role.Player;
        assert b.getRole() == Role.Player;
        assert c.getRole() == Role.SB;
        assert d.getRole() == Role.BB;

        table.shiftRoles();
        assert a.getRole() == Role.BB;
        assert b.getRole() == Role.Player;
        assert c.getRole() == Role.Player;
        assert d.getRole() == Role.SB;

        table.shiftRoles();
        assert a.getRole() == Role.SB;
        assert b.getRole() == Role.BB;
        assert c.getRole() == Role.Player;
        assert d.getRole() == Role.Player;
    }

    private User newUser(String name) {
        User u = new User();
        u.id = UUID.randomUUID();
        u.name = name;
        return u;
    }
}
