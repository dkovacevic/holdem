package com.wire.bots.holdem;

import com.wire.bots.holdem.game.Poker;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.tools.Logger;

public class MessageHandler extends MessageHandlerBase {
    // Commands
    private static final String RANKING = "ranking";
    private static final String PRINT = "print";
    private static final String ADD_BOT = "add bot";

    private static final String RAISE = "raise";
    private static final String R = "r";          // short for `raise`

    private static final String DEAL = "deal";    // deal cards
    private static final String D = "d";          // deal cards

    private static final String F = "f";          // short for `fold`
    private static final String FOLD = "fold";

    private static final String CALL = "call";
    private static final String C = "c";          // short for `call`
    private static final String CHECK = "check";  // equivalent to `call`

    private static final String BLIND = "blind";  // equivalent to `call`
    private static final String BET = "bet";      // equivalent to `raise`
    private static final String B = "b";          // equivalent to `raise`

    private static final String RESET = "reset";
    private static final String NEW = "new";
    private static final String KICK_OUT = "kick out";
    // Commands

    private final Poker poker;

    MessageHandler() {
        poker = new Poker();
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            String sCmd = msg.getText().toLowerCase();
            if (sCmd.startsWith(KICK_OUT)) {
                String name = sCmd.replace(KICK_OUT, "").trim();
                if (poker.onKickOut(client, name)) {
                    return;
                }
            }

            Action action = parseCommand(sCmd);
            switch (action) {
                case PRINT:
                    poker.onPrint(client);
                    return;
                case RANKING:
                    poker.onRanking(client);
                    return;
                case RESET:
                    poker.onReset(client);
                    break;
                case ADD_BOT:
                    poker.onAddBot(client);
                    break;
                case DEAL:
                    poker.onDeal(client);
                    break;
                case RAISE:
                    poker.onRaise(client, msg.getUserId());
                    break;
                case CALL:
                    poker.onCall(client, msg.getUserId());
                    break;
                case FOLD:
                    poker.onFold(client, msg.getUserId());
                    break;
                default: {
                    return;
                }
            }
            poker.onBots(client, action);
        } catch (Exception e) {
            Logger.error("onText: %s", e);
        }
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage message) {
        try {
            client.sendText("Add more participants. Type: `deal` to start. `call`, `raise`, `fold` when betting..." +
                    " If you feel lonely type: `add bot`");
        } catch (Exception e) {
            Logger.error("onNewConversation: %s", e);
        }
    }

    @Override
    public void onMemberJoin(WireClient client, SystemMessage message) {
        try {
            poker.onMemberJoin(client, message.users);
        } catch (Exception e) {
            Logger.error("onMemberJoin: %s", e);
        }
    }

    @Override
    public void onMemberLeave(WireClient client, SystemMessage message) {
        try {
            poker.onMemberLeave(client, message.users);
        } catch (Exception e) {
            Logger.error("onMemberLeave: %s", e);
        }
    }

    private Action parseCommand(String cmd) {
        switch (cmd.toLowerCase().trim()) {
            case PRINT:
                return Action.PRINT;
            case RESET:
            case NEW:
                return Action.RESET;
            case D:
            case DEAL:
                return Action.DEAL;
            case R:
            case RAISE:
            case BET:
            case B:
                return Action.RAISE;
            case C:
            case CHECK:
            case CALL:
            case BLIND:
                return Action.CALL;
            case FOLD:
            case F:
                return Action.FOLD;
            case ADD_BOT:
                return Action.ADD_BOT;
            case RANKING:
                return Action.RANKING;
            default: {
                return Action.UNKNOWN;
            }
        }
    }
}
