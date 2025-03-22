package revxrsal.commands.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.annotation.Values;

import java.util.List;

/**
 * Thrown when an invalid value is given for a parameter annotated with
 * {@link revxrsal.commands.annotation.Values @Values}.
 */
public class ValueNotAllowedException extends InvalidValueException {

    private final @Unmodifiable List<String> allowedValues;
    private final boolean caseSensitive;

    public ValueNotAllowedException(@NotNull String input, @NotNull @Unmodifiable List<String> allowedValues, boolean caseSensitive) {
        super(input);
        this.allowedValues = allowedValues;
        this.caseSensitive = caseSensitive;
    }

    /**
     * Tests whether the parameter is case-sensitive or not
     *
     * @return If the parameter is case-sensitive or not
     * @see Values#caseSensitive()
     */
    public boolean caseSensitive() {
        return caseSensitive;
    }

    /**
     * Returns an immutable list of the allowed values
     *
     * @return The allowed values
     * @see Values#value()
     */
    public @Unmodifiable @NotNull List<String> allowedValues() {
        return allowedValues;
    }

}
