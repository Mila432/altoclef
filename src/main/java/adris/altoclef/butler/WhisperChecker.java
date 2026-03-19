package adris.altoclef.butler;

import adris.altoclef.util.time.TimerGame;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import adris.altoclef.AltoClef;

public class WhisperChecker {

    private static final TimerGame _repeatTimer = new TimerGame(0.1);

    private static String _lastMessage = null;

    // this didn't work correctly, so I rewrote it without fancy regex stuff -miran
    public static MessageResult tryParse(String ourUsername, String whisperFormat, String message) {
        List<String> parts = new ArrayList<>(List.of("{from}", "{to}", "{message}"));

        // Sort by the order of appearance in whisperFormat.
        parts.sort(Comparator.comparingInt(whisperFormat::indexOf));
        parts.removeIf(part -> !whisperFormat.contains(part));

        ArrayList<String> messageParts = new ArrayList<>(Arrays.stream(message.split(" ")).toList());
        MessageResult result = new MessageResult();
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (messageParts.isEmpty()) return null;

            if (part.equals("{from}")) {
                result.from = messageParts.remove(0);
            } else if (part.equals("{to}")) {
                String toUser = messageParts.remove(0);
                if (!toUser.equals(ourUsername)) {
                    Debug.logInternal("Rejected message since it is sent to " + toUser + " and not " + ourUsername);
                    return null;
                }
            } else if (part.equals("{message}")) {
                List<String> messageList = messageParts.subList(0,messageParts.size()-(parts.size()-i-1));

                StringBuilder msg = new StringBuilder(messageList.get(0));

                for (int j = 1; j < messageList.size(); j++) {
                    msg.append(" ").append(messageList.get(j));
                }

                result.message = msg.toString();
            } else {
                throw new IllegalArgumentException("Unknown part: "+part);
            }

        }

        return result;
    }

    public MessageResult receiveMessage(AltoClef mod, String ourUsername, String msg) {
        String foundMiddlePart = "";
        int index = -1;

        boolean duplicate = (msg.equals(_lastMessage));
        if (duplicate && !_repeatTimer.elapsed()) {
            _repeatTimer.reset();
            return null;
        }

        _lastMessage = msg;

        for (String format : ButlerConfig.getInstance().whisperFormats) {
            MessageResult check = tryParse(ourUsername, format, msg);
            if (check != null) {
                String user = check.from;
                String message = check.message;
                if (user == null || message == null) {
                    break;
                }
                return check;
            }
        }

        return null;
    }

    public static class MessageResult {
        public String from;
        public String message;

        @Override
        public String toString() {
            return "MessageResult{" +
                    "from='" + from + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
