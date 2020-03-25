package com.wire.bots.holdem;

import com.waz.model.Messages;
import com.wire.bots.holdem.game.Game;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.ButtonActionConfirmation;
import com.wire.bots.sdk.assets.Poll;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.tools.Logger;

import java.util.UUID;

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

    private final Game poker;

    MessageHandler() {
        poker = new Game();
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
            client.sendText("Texas Hold'em");

            Poll poll = new Poll();
            poll.addButton("deal", "Deal");
            poll.addButton("add bot", "Add bot");

            client.send(poll, message.from);
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

    @Override
    public void onEvent(WireClient client, UUID userId, Messages.GenericMessage event) {
        UUID messageId = UUID.fromString(event.getMessageId());

        // User clicked on a Poll Button
        if (event.hasButtonAction()) {
            Messages.ButtonAction action = event.getButtonAction();

            final UUID pollId = UUID.fromString(action.getReferenceMessageId());
            final String buttonId = action.getButtonId();

            ButtonActionConfirmation confirmation = new ButtonActionConfirmation(
                    pollId,
                    buttonId);

            try {
                client.send(confirmation, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            TextMessage textMessage = new TextMessage(messageId, client.getConversationId(), client.getDeviceId(), userId);
            textMessage.setText(buttonId);
            onText(client, textMessage);
        }
    }

    @Override
    public String getName(NewBot newBot) {
        return "Poker";
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
