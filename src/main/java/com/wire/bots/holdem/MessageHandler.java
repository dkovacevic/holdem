package com.wire.bots.holdem;

import com.wire.bots.holdem.game.Poker;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.tools.Logger;

import java.util.ArrayList;

public class MessageHandler extends MessageHandlerBase {
    // Commands
    private static final String RANKING = "ranking";
    private static final String PRINT = "print";
    private static final String ADD_BOT = "add bot";

    private static final String RAISE = "raise";
    private static final String R = "r";          // short for `raise`

    private static final String DEAL = "deal";    // deal cards
    private static final String D = "d";    // deal cards

    private static final String F = "f";          // short for `fold`
    private static final String FOLD = "fold";

    private static final String CALL = "call";
    private static final String C = "c";          // short for `call`
    private static final String CHECK = "check";  // equivalent to `call`

    private static final String BLIND = "blind";
    private static final String BET = "bet";
    private static final String B = "b";

    private static final String RESET = "reset";
    private static final String NEW = "new";


    private final Poker poker;

    MessageHandler(StorageFactory storageFactory) {
        poker = new Poker(storageFactory);
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        try {
            Action action = parseCommand(msg.getText());
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
                case DEAL:
                    poker.onDeal(client);
                    break;
                case RAISE:
                    poker.onRaise(client, msg);
                    break;
                case CALL:
                    poker.onCall(client, msg);
                    break;
                case FOLD:
                    poker.onFold(client, msg);
                    break;
                case ADD_BOT:
                    poker.onAddBot(client);
                    break;
                default:
                    return;
            }
            poker.onBots(client, action);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("onText: %s", e);
        }
    }

    @Override
    public void onNewConversation(WireClient client) {
        try {
            client.sendText("Add more participants. Type: `deal` to start. `call`, `raise`, `fold` when betting..." +
                    " If you feel lonely type: `add bot`");
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void onMemberJoin(WireClient client, ArrayList<String> userIds) {
        try {
            poker.onMemberJoin(client, userIds);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    @Override
    public void onMemberLeave(WireClient client, ArrayList<String> userIds) {
        try {
            poker.onMemberLeave(client, userIds);
        } catch (Exception e) {
            Logger.error(e.getMessage());
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
            default:
                return Action.UNKNOWN;
        }
    }
}
