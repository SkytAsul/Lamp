package revxrsal.commands.command;

import org.jetbrains.annotations.*;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.exception.CooldownException;

import java.util.concurrent.TimeUnit;

/**
 * Represents a handle that allows for fine-grained control for the cooldown of a command.
 *
 * <p>A cooldown handle can be used in two main ways:</p>
 *
 * <ol>
 *     <li>
 *         <p>Combined with {@link Cooldown @Cooldown} annotation:</p>
 *         <pre>{@code
 * @Command("foo")
 * public void foo(
 *      ...,
 *      @Cooldown(value = 10, unit = TimeUnit.SECONDS) CooldownHandle cooldownHandle
 * ) {
 *     cooldownHandle.requireNotOnCooldown();
 *     // ^^^ will throw a CooldownException if the user is on cooldown
 *
 *     // ... command action here
 *
 *     // then put the user on cooldown:
 *     cooldownHandle.cooldown();
 * }
 *         }</pre>
 *         <p>This allows you to use shortcut methods such as {@link #requireNotOnCooldown()},
 *         {@link #remainingTimeMillis()}, and {@link #cooldown()} without having to specify
 *         the duration in each call, as it will use the values specified in {@link Cooldown @Cooldown}.</p>
 *
 *         <p>This has the drawback that the duration must be a compile-time constant and cannot use
 *         dynamic values for cooldowns (e.g., cooldowns from configuration).</p>
 *     </li>
 *
 *     <li>
 *         <p>Without the {@link Cooldown @Cooldown} annotation:</p>
 *         <pre>{@code
 * @Command("foo")
 * public void foo(
 *      ...,
 *      CooldownHandle cooldownHandle
 * ) {
 *     cooldownHandle.requireNotOnCooldown(10, TimeUnit.SECONDS);
 *     // ^^^ will throw a CooldownException if the user is on cooldown
 *
 *     // ... command action here
 *
 *     // then put the user on cooldown:
 *     cooldownHandle.cooldown(10, TimeUnit.SECONDS);
 * }
 *         }</pre>
 *         <p>This allows you to specify the cooldown duration and unit manually to any arbitrary value you want.</p>
 *
 *         <p>This has the drawback that the duration has to be specified in each function call.</p>
 *
 *         <p>You can avoid this by using {@link #withCooldown(long, TimeUnit)} which will return a new
 *         {@link CooldownHandle} that can use the shortcut form of functions:</p>
 *         <pre>{@code
 * @Command("foo")
 * public void foo(
 *      ...,
 *      CooldownHandle cooldownHandle
 * ) {
 *     // creates a cooldown handle with a specific cooldown value
 *     cooldownHandle = cooldownHandle.withCooldown(10, TimeUnit.SECONDS);
 *
 *     cooldownHandle.requireNotOnCooldown();
 *     // ^^^ will throw a CooldownException if the user is on cooldown
 *
 *     // ... command action here
 *
 *     // then put the user on cooldown:
 *     cooldownHandle.cooldown();
 * }
 *         }</pre>
 *     </li>
 * </ol>
 *
 * <strong>Note: </strong> You cannot declare {@link Cooldown @Cooldown} on the method
 * and then use {@link CooldownHandle} on it! The annotation must be declared on the
 * parameter, otherwise you will receive an error.
 *
 * @see Cooldown
 */
@ApiStatus.Experimental
public interface CooldownHandle {

    /**
     * Creates a new {@link CooldownHandle} that is identical to this handle,
     * except that it has a specified cooldown value and unit.
     * <p>
     * This handle allows calling shortcut methods such as {@link #cooldown()},
     * {@link #remainingTimeMillis()}, etc.
     *
     * @param cooldownValue Cooldown value
     * @param unit          The time unit of the cooldown
     * @return A newly created {@link CooldownHandle}
     */
    @CheckReturnValue
    @Contract(pure = true, value = "_, _ -> new")
    @NotNull CooldownHandle withCooldown(long cooldownValue, @NotNull TimeUnit unit);

    /**
     * Checks if the cooldown is currently active.
     *
     * @return {@code true} if on cooldown, {@code false} otherwise.
     */
    boolean isOnCooldown();

    /**
     * Returns the elapsed time since the cooldown started in milliseconds.
     * <p>
     * If the user is not on cooldown, this will return zero.
     *
     * @return Elapsed time in milliseconds.
     */
    long elapsedMillis();

    /**
     * Starts the cooldown with the duration specified in {@link Cooldown @Cooldown}.
     *
     * @throws IllegalArgumentException if the {@link CooldownHandle} parameter does not
     *                                  have {@link Cooldown @Cooldown} on it
     */
    void cooldown();

    /**
     * Throws an exception if currently on cooldown.
     *
     * @throws CooldownException        if on cooldown.
     * @throws IllegalArgumentException if the {@link CooldownHandle} parameter does not
     *                                  have {@link Cooldown @Cooldown} on it
     */
    void requireNotOnCooldown() throws CooldownException;

    /**
     * Throws an exception if on cooldown based on the specified duration.
     *
     * @param cooldownValue Cooldown duration.
     * @param cooldownUnit  Time unit for the cooldown duration.
     * @throws CooldownException if on cooldown.
     */
    void requireNotOnCooldown(long cooldownValue, @NotNull TimeUnit cooldownUnit) throws CooldownException;

    /**
     * Removes the active cooldown immediately.
     */
    void removeCooldown();

    /**
     * Returns the elapsed cooldown time in the specified time unit.
     *
     * @param outputUnit Time unit for the result.
     * @return Elapsed time in the specified unit.
     */
    default long elapsed(@NotNull TimeUnit outputUnit) {
        return outputUnit.convert(elapsedMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Starts the cooldown for a specific duration.
     *
     * @param duration Cooldown duration.
     * @param unit     Time unit for the duration.
     */
    void cooldown(@Range(from = 1, to = Long.MAX_VALUE) long duration, @NotNull TimeUnit unit);

    /**
     * Gets the remaining cooldown time in milliseconds.
     *
     * @return Remaining time in milliseconds.
     * @throws IllegalArgumentException if the {@link CooldownHandle} parameter does not
     *                                  have {@link Cooldown @Cooldown} on it
     */
    long remainingTimeMillis();

    /**
     * Gets the remaining cooldown time in the specified unit.
     *
     * @param outputUnit Time unit for the result.
     * @return Remaining time in the specified unit.
     * @throws IllegalArgumentException if the {@link CooldownHandle} parameter does not
     *                                  have {@link Cooldown @Cooldown} on it
     */
    long remainingTime(@NotNull TimeUnit outputUnit);

    /**
     * Calculates the remaining time for a specific cooldown in milliseconds.
     *
     * @param cooldownValue Cooldown duration.
     * @param cooldownUnit  Time unit for the duration.
     * @return Remaining time in milliseconds.
     */
    default long remainingTimeMillis(long cooldownValue, @NotNull TimeUnit cooldownUnit) {
        long cooldownMillis = cooldownUnit.toMillis(cooldownValue);
        return cooldownMillis - elapsedMillis();
    }

    /**
     * Calculates the remaining cooldown time in the same unit as the cooldown.
     *
     * @param cooldownValue Cooldown duration.
     * @param cooldownUnit  Time unit for the cooldown.
     * @return Remaining time in the cooldown unit.
     */
    default long remainingTime(long cooldownValue, @NotNull TimeUnit cooldownUnit) {
        return remainingTime(cooldownValue, cooldownUnit, cooldownUnit);
    }

    /**
     * Calculates the remaining cooldown time in a specified output unit.
     *
     * @param cooldownValue Cooldown duration.
     * @param cooldownUnit  Time unit for the cooldown.
     * @param outputUnit    Desired time unit for the result.
     * @return Remaining time in the output unit.
     */
    default long remainingTime(long cooldownValue, @NotNull TimeUnit cooldownUnit, @NotNull TimeUnit outputUnit) {
        long cooldownMillis = cooldownUnit.toMillis(cooldownValue);
        return outputUnit.convert(cooldownMillis - elapsedMillis(), TimeUnit.MILLISECONDS);
    }
}
