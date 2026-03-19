package adris.altoclef;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class Debug {

    public static final int DEBUG_LOG_LEVEL = 0; // Standard info/debug messages
    public static final int WARN_LOG_LEVEL = 1;
    public static final int ERROR_LOG_LEVEL = 2;

    private static PrintWriter fileWriter;

    // --- Anti-Spam Configuration ---
    private static final int MAX_CACHE_SIZE = 100;
    private static final long DUPLICATE_TIMEOUT_MS = 1000; // 1 second timeout for duplicates

    // LRU Cache to store recent log messages and their timestamps
    private static final Map<String, Long> recentLogs = new LinkedHashMap<String, Long>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    // Initialize the file logger
    static {
        try {
            File tempFile = File.createTempFile("altoclef_log_", ".txt");
            fileWriter = new PrintWriter(new FileWriter(tempFile, true), true);
            System.out.println("ALTO CLEF: Plain text file logging initialized at: " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ALTO CLEF: Failed to initialize file logging.");
            e.printStackTrace();
        }
    }

    // Checks if the message was logged too recently
    private static boolean isDuplicateSpam(String message) {
        long now = System.currentTimeMillis();
        synchronized (recentLogs) {
            Long lastTime = recentLogs.get(message);
            if (lastTime != null && (now - lastTime) < DUPLICATE_TIMEOUT_MS) {
                return true; // It's spam, block it
            }
            recentLogs.put(message, now);
            return false; // Not spam, allow it
        }
    }

    // Helper to strip colors and prefixes, then write to the temp file
    private static void writeToFile(String message) {
        if (fileWriter != null && message != null) {
            // Regex to match and remove Minecraft formatting codes
            String plainText = message.replaceAll("\u00A7[0-9a-fk-orA-FK-OR]", "");
            
            // Remove the prefixes ONLY for the log file
            plainText = plainText.replace("ALTO CLEF: ", "").replace("[Alto Clef] ", "");
            
            fileWriter.println(plainText);
            fileWriter.flush(); // FIX: Explicit flush to guarantee immediate writing
        } else if (fileWriter == null) {
            System.err.println("ALTO CLEF: Attempted to write to null fileWriter");
        }
    }

    public static void logInternal(String message) {
        if (canLog(DEBUG_LOG_LEVEL) && !isDuplicateSpam(message)) {
            String msg = "ALTO CLEF: " + message;
            System.out.println(msg); // Console keeps the prefix
            writeToFile(msg);        // File gets it stripped
        }
    }

    public static void logInternal(String format, Object... args) {
        logInternal(String.format(format, args));
    }

    private static String getLogPrefix() {
        AltoClef altoClef = AltoClef.getInstance();
        if (altoClef != null && altoClef.getModSettings() != null) {
            String prefix = altoClef.getModSettings().getChatLogPrefix();
            return prefix != null ? prefix : "[Alto Clef] "; // Fallback if setting returns null
        }
        // Removed recursive Debug.logWarning here to prevent infinite loop spam
        return "[Alto Clef] ";
    }

    public static void logMessage(String message, boolean prefix) {
        if (prefix) {
            message = getLogPrefix() + message;
        }
        logInternal(message);
    }

    public static void logMessage(String message) {
        logMessage(message, true);
    }

    public static void logMessage(String format, Object... args) {
        logMessage(String.format(format, args));
    }

    public static void logWarning(String message) {
        if (canLog(WARN_LOG_LEVEL) && !isDuplicateSpam("WARN: " + message)) {
            String msg = "ALTO CLEF: WARNING: " + message;
            System.out.println(msg); // Console keeps the prefix
            writeToFile(msg);        // File gets it stripped
        }
    }

    public static void logWarning(String format, Object... args) {
        logWarning(String.format(format, args));
    }

    public static void logError(String message) {
        if (canLog(ERROR_LOG_LEVEL) && !isDuplicateSpam("ERROR: " + message)) {
            String stacktrace = getStack(2);
            String fullMessage = "[ERROR] " + message + "\nat:\n" + stacktrace;
            
            System.err.println("ALTO CLEF: " + fullMessage); // Console keeps the prefix
            writeToFile(fullMessage);                        // File gets it stripped
        }
    }

    public static void logError(String format, Object... args) {
        logError(String.format(format, args));
    }

    public static void logStack() {
        if (!isDuplicateSpam("STACKTRACE")) {
            logInternal("STACKTRACE: \n" + getStack(2));
        }
    }

    private static String getStack(int toSkip) {
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (toSkip-- <= 0) {
                stacktrace.append(ste.toString()).append("\n");
            }
        }
        return stacktrace.toString();
    }

    private static boolean canLog(int level) {
        AltoClef altoClef = AltoClef.getInstance();
        if (altoClef == null || altoClef.getModSettings() == null) {
            // Removed recursive Debug.logWarning here to prevent infinite loop spam
            return true;
        }

        String enabledLogLevel = altoClef.getModSettings().getLogLevel();
        
        if (enabledLogLevel == null) {
            return true;
        }

        return switch (enabledLogLevel.toUpperCase()) {
            case "NONE" -> false;
            case "ALL" -> true;
            case "NORMAL" -> level == DEBUG_LOG_LEVEL || level == WARN_LOG_LEVEL || level == ERROR_LOG_LEVEL;
            case "WARN" -> level == WARN_LOG_LEVEL || level == ERROR_LOG_LEVEL;
            case "ERROR" -> level == ERROR_LOG_LEVEL;
            default -> true; // Removed recursive Debug.logWarning here as well
        };
    }
}