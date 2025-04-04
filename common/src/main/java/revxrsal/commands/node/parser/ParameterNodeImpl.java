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
package revxrsal.commands.node.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.annotation.*;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.node.CommandAction;
import revxrsal.commands.node.CommandNode;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.MutableStringStreamImpl;
import revxrsal.commands.stream.StringStream;
import revxrsal.commands.util.Classes;

import java.util.Collection;
import java.util.Objects;

import static revxrsal.commands.reflect.ktx.KotlinConstants.defaultPrimitiveValue;
import static revxrsal.commands.reflect.ktx.KotlinConstants.isKotlinClass;

final class ParameterNodeImpl<A extends CommandActor, T> extends BaseCommandNode<A> implements ParameterNode<A, T> {

    private final @NotNull ParameterType<A, T> type;
    private final @NotNull SuggestionProvider<A> suggestions;
    private final @NotNull CommandParameter parameter;
    private final @NotNull CommandPermission<A> permission;
    private final boolean isOptional;
    private final @Nullable Switch switchAnn;
    private final @Nullable Flag flagAnn;

    public ParameterNodeImpl(
            @NotNull String name,
            @Nullable CommandAction<A> action,
            boolean isLast,
            @NotNull ParameterType<A, T> type,
            @NotNull SuggestionProvider<A> suggestions,
            @NotNull CommandParameter parameter,
            @NotNull CommandPermission<A> permission,
            boolean isOptional
    ) {
        super(name, action, isLast);
        this.type = type;
        this.suggestions = suggestions;
        this.parameter = parameter;
        this.permission = permission;
        this.isOptional = isOptional;
        this.switchAnn = parameter.getAnnotation(Switch.class);
        this.flagAnn = parameter.getAnnotation(Flag.class);
        if (isSwitch() && Classes.wrap(type()) != Boolean.class) {
            throw new IllegalArgumentException("@Switch can only be used on boolean types!");
        }
        if (isSwitch() && isFlag()) {
            throw new IllegalArgumentException("A parameter cannot have @Switch and @Flag at the same time!");
        }
    }

    private static @Nullable String getDefaultValue(AnnotationList annotations) {
        String defaultValue = annotations.map(Default.class, Default::value);
        if (defaultValue == null) {
            Sized sized = annotations.get(Sized.class);
            if (sized != null)
                return sized.min() == 0 ? "" : null;
            Length length = annotations.get(Length.class);
            if (length != null)
                return length.min() == 0 ? "" : null;
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T parse(MutableStringStream input, ExecutionContext<A> context) {
        checkForPermission(context);
        if (input.hasFinished()) {
            if (isOptional()) {
                String defaultValue = getDefaultValue(parameter.annotations());
                if (defaultValue != null)
                    ((MutableStringStreamImpl) input).extend(defaultValue);
                else {
                    if (isKotlinClass(context.command().function().method().getDeclaringClass()))
                        return null;
                    return (T) defaultPrimitiveValue(parameter.type());
                }
            } else {
                throw new MissingArgumentException(
                        (ParameterNode<CommandActor, Object>) this, (ExecutableCommand<CommandActor>) context.command()
                );
            }
        }
        return type.parse(input, context);
    }

    private void checkForPermission(ExecutionContext<A> context) {
        if (!permission.isExecutableBy(context.actor()))
            throw new NoPermissionException(this);
    }

    @Override
    public int compareTo(@NotNull CommandNode<A> o) {
        if (o instanceof LiteralNodeImpl)
            return 1;
        else {
            ParameterNodeImpl<?, A> n = (ParameterNodeImpl<?, A>) o;
            if (isOptional && !n.isOptional)
                return 1;
            else if (n.isOptional && !isOptional)
                return -1;
            int compare = type.parsePriority().comparator().compare(parameterType(), n.parameterType());
            if (compare != 0) {
                return compare;
            } else {
                compare = n.parameterType().parsePriority().comparator().compare(n.parameterType(), parameterType());
                return compare == 0 ? 0 : -compare;
            }
        }
    }

    @Override
    public @NotNull AnnotationList annotations() {
        return parameter.annotations();
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public @NotNull ParameterType<A, T> parameterType() {
        return this.type;
    }

    @Override
    public @NotNull SuggestionProvider<A> suggestions() {
        return suggestions;
    }

    @Override public boolean isGreedy() {
        return parameterType().isGreedy();
    }

    @Override
    public @NotNull CommandParameter parameter() {
        return this.parameter;
    }

    @Override
    public @NotNull CommandPermission<A> permission() {
        return permission;
    }

    @Override
    public @NotNull Collection<String> complete(@NotNull ExecutionContext<A> context) {
        return suggestions.getSuggestions(context);
    }

    @Override
    public @NotNull <L> ParameterNode<A, L> requireParameterNode() {
        //noinspection unchecked
        return (ParameterNode<A, L>) this;
    }

    @Override public @Nullable String description() {
        return annotations().map(Description.class, Description::value);
    }

    @Override public @Nullable String switchName() {
        if (switchAnn != null)
            return switchAnn.value().isEmpty() ? name() : switchAnn.value();
        return null;
    }

    @Override public @Nullable String flagName() {
        if (flagAnn != null)
            return flagAnn.value().isEmpty() ? name() : flagAnn.value();
        return null;
    }

    @Override public Character shorthand() {
        char shorthand;
        if (isFlag()) {
            shorthand = flagAnn.shorthand();
            if (shorthand == '\0')
                shorthand = flagName().charAt(0);
        } else if (isSwitch()) {
            shorthand = switchAnn.shorthand();
            if (shorthand == '\0')
                shorthand = switchName().charAt(0);
        } else {
            return null;
        }
        return shorthand;
    }

    @Override public @NotNull String representation() {
        if (isFlag() || isSwitch()) {
            char shorthand = Objects.requireNonNull(shorthand(), "shorthand() is null for a flag or switch. This is not supposed to happen!");
            if (isSwitch()) {
                return "[--" + switchName() + " | -" + shorthand + "]";
            } else if (isFlag()) {
                if (isOptional())
                    return "[--" + flagName() + " <" + name() + "> | -" + shorthand + " <" + name() + ">]";
                else
                    return "<--" + flagName() + " <" + name() + "> | -" + shorthand + " <" + name() + ">]";
            }
        }
        return isRequired() ? "<" + name() + ">" : "[" + name() + "]";
    }

    @Override public boolean isSwitch() {
        return switchAnn != null;
    }

    @Override public boolean isFlag() {
        return flagAnn != null;
    }

    @Override public String toString() {
        return "ParameterNode(name=" + name() + ')';
    }
}
