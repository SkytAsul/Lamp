/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.SecretCommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.command.trait.CommandAnnotationHolder;
import revxrsal.commands.command.trait.PermissionHolder;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.process.ResponseHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a command which can be executed with specific arguments,
 * flags or switches.
 */
public interface ExecutableCommand extends CommandAnnotationHolder, PermissionHolder, Comparable<ExecutableCommand> {

    /**
     * Returns the name of the command
     *
     * @return The command name
     */
    @NotNull String getName();

    /**
     * Returns a unique ID of this command. Note that this ID
     * will be shared by commands that are defined together in
     * the same method through aliases.
     * <p>
     * For example:
     * <pre>
     * &#064;Command({"foo", "bar"})
     * public void foo(CommandActor actor) {
     *     actor.reply("Way to go!");
     * }
     * </pre>
     * <p>
     * The above code will result in 2 separate {@link ExecutableCommand}s:
     * foo and bar. Because foo and bar are defined in the same scope and behave exactly
     * the same, they will share the same ID. This will also make sure that if an actor is
     * on cooldown on foo, they will also be on cooldown on bar.
     *
     * @return The unique ID.
     */
    @Range(from = 0, to = Long.MAX_VALUE) int getId();

    /**
     * Returns the command usage. This can be explicitly defined by
     * annotating the method with {@link Usage}, otherwise will be
     * auto-generated by marking required parameters with angled
     * brackets, and optional ones with squared brackets.
     *
     * @return The command usage.
     */
    @NotNull String getUsage();

    /**
     * Returns the command's description. This can be explicitly defined
     * with {@link Description}, otherwise will return null.
     *
     * @return The command description
     */
    @Nullable String getDescription();

    /**
     * Returns the full path of the command
     *
     * @return The full command path
     */
    @NotNull CommandPath getPath();

    /**
     * Returns the parent category of this command. Can be null in case
     * of root commands.
     *
     * @return The parent category of this command.
     */
    @Nullable CommandCategory getParent();

    /**
     * Returns all the parameters of this command
     *
     * @return The command parameters
     */
    @NotNull @Unmodifiable List<CommandParameter> getParameters();

    /**
     * Returns all the parameters of this command that are resolveable from
     * command arguments (i.e. not context)
     *
     * @return The command parameters
     */
    @NotNull @Unmodifiable Map<Integer, CommandParameter> getValueParameters();

    /**
     * Returns the command handler of this command
     *
     * @return The command handler
     */
    @NotNull CommandHandler getCommandHandler();

    /**
     * Returns the response handler of this command.
     *
     * @param <T> The return type
     * @return The command's response handler
     */
    @NotNull <T> ResponseHandler<T> getResponseHandler();

    /**
     * Executes the command on the behalf of the given sender
     *
     * @param actor Actor to execute as
     * @param input The command input
     */
    void execute(@NotNull CommandActor actor, @Nullable Collection<String> input);

    /**
     * Executes the command on the behalf of the given sender
     *
     * @param actor Actor to execute as
     * @param input The command input
     */
    void execute(@NotNull CommandActor actor, @Nullable String... input);

    /**
     * Returns whether is this command marked as secret or not
     * <p>
     * Specified by {@link SecretCommand}.
     *
     * @return is secret or not.
     */
    boolean isSecret();

}
