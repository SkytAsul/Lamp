/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
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
package revxrsal.commands.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A utility to modify {@link CommandNode}s reflectively.
 */
final class Nodes {

    // CommandNode#command
    private static final Field COMMAND;

    // CommandNode#children
    private static final Field CHILDREN;

    // CommandNode#literals
    private static final Field LITERALS;

    // CommandNode#arguments
    private static final Field ARGUMENTS;

    // CommandNode#requirement
    private static final Field REQUIREMENT;

    // ArgumentCommandNode#customSuggestions
    private static final Field CUSTOM_SUGGESTIONS;

    static {
        try {
            COMMAND = CommandNode.class.getDeclaredField("command");
            COMMAND.setAccessible(true);

            REQUIREMENT = CommandNode.class.getDeclaredField("requirement");
            REQUIREMENT.setAccessible(true);

            CHILDREN = CommandNode.class.getDeclaredField("children");
            CHILDREN.setAccessible(true);

            LITERALS = CommandNode.class.getDeclaredField("literals");
            LITERALS.setAccessible(true);

            ARGUMENTS = CommandNode.class.getDeclaredField("arguments");
            ARGUMENTS.setAccessible(true);

            CUSTOM_SUGGESTIONS = ArgumentCommandNode.class.getDeclaredField("customSuggestions");
            CUSTOM_SUGGESTIONS.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Sets the action of the node
     *
     * @param node    Node to set action for
     * @param command The action to set
     */
    public static <T> void setCommand(CommandNode<T> node, Command<T> command) {
        try {
            Nodes.COMMAND.set(node, command);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the requirement (i.e. permission) for executing this
     * node
     *
     * @param node        Node to set requirement for
     * @param requirement Requirement to set
     */
    public static <T> void setRequirement(@NotNull CommandNode<T> node, @NotNull Predicate<T> requirement) {
        try {
            Nodes.REQUIREMENT.set(node, requirement);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the suggestions of this node
     *
     * @param node     Note to set for
     * @param provider The provider to set
     */
    public static <S, T> void setSuggestionProvider(ArgumentCommandNode<S, T> node, SuggestionProvider<S> provider) {
        try {
            CUSTOM_SUGGESTIONS.set(node, provider);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <S> @NotNull Map<String, CommandNode<S>> getChildren(@NotNull CommandNode<S> node) {
        try {
            //noinspection unchecked
            return (Map<String, CommandNode<S>>) CHILDREN.get(node);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <S> @NotNull Map<String, LiteralCommandNode<S>> getLiterals(@NotNull CommandNode<S> node) {
        try {
            //noinspection unchecked
            return (Map<String, LiteralCommandNode<S>>) LITERALS.get(node);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <S> @NotNull Map<String, ArgumentCommandNode<S, ?>> getArguments(@NotNull CommandNode<S> node) {
        try {
            //noinspection unchecked
            return (Map<String, ArgumentCommandNode<S, ?>>) ARGUMENTS.get(node);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
