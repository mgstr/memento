package org.memento.updatehandlers;

import com.google.common.base.Strings;
import org.memento.BotConfig;
import org.memento.client.SocketClient;
import org.memento.commands.HelpCommand;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * This handler mainly works with commands to demonstrate the Commands feature of the API
 *
 * @author Timo Schulz (Mit0x2)
 */
public class CommandHandlers extends TelegramLongPollingCommandBot {

    public static final String LOGTAG = "COMMANDSHANDLER";

    /**
     * Constructor.
     */
    public CommandHandlers() {
        HelpCommand helpCommand = new HelpCommand(this);
        register(helpCommand);

        registerDefaultAction((absSender, message) -> {
            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(message.getChatId());
            commandUnknownMessage.setText("The command '" + message.getText() + "' is not known by this bot. Here comes some help ");
            try {
                absSender.sendMessage(commandUnknownMessage);
            } catch (TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            }
            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[] {});
        });
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                SendMessage responseMessage = new SendMessage();
                responseMessage.setChatId(message.getChatId());
                String res = sendSearchRequest(message.getText());
                if (Strings.isNullOrEmpty(res)) {
                    res = "An error occured, please check bot logs";
                }
                responseMessage.setText(res);
                try {
                    sendMessage(responseMessage);
                } catch (TelegramApiException e) {
                    BotLogger.error(LOGTAG, e);
                }
            }
        }
    }

    public static String sendSearchRequest(String arguments) {
        return new SocketClient().send(arguments);
    }

    @Override
    public String getBotUsername() {
        return BotConfig.COMMANDS_USER;
    }

    @Override
    public String getBotToken() {
        String token = System.getenv("TELEGRAM_TOKEN");
        if (Strings.isNullOrEmpty(token)) {
            throw new RuntimeException("TELEGRAM_TOKEN is not set but required.");
        }
        return token;
    }
}