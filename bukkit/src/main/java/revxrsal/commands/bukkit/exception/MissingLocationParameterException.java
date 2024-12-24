package revxrsal.commands.bukkit.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.InvalidValueException;

/**
 * Thrown when a parameter of {@link org.bukkit.Location} is not specified, such
 * as {@code x} or {@code y} or {@code z}.
 */
public class MissingLocationParameterException extends InvalidValueException {

    public enum MissingAxis {
        X, Y, Z
    }

    private final @NotNull MissingAxis missingAxis;

    public MissingLocationParameterException(@NotNull String input, @NotNull MissingAxis missingAxis) {
        super(input);
        this.missingAxis = missingAxis;
    }

    public @NotNull MissingAxis axis() {
        return missingAxis;
    }
}
