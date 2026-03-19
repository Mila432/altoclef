package adris.altoclef.commandsystem.exception;


public abstract class CommandException extends Exception {

    public CommandException(String message) {
        super(message);
        if (message == null) {
        }
    }

    public CommandException(String message, Exception child) {
        super(message, child);
        if (message == null) {
        }
        if (child == null) {
        }
    }
}
