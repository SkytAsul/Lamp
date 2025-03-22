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
package revxrsal.commands.parameter.builtins;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Values;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.EnumNotFoundException;
import revxrsal.commands.exception.ValueNotAllowedException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

import java.lang.reflect.Type;
import java.util.*;

import static revxrsal.commands.util.Classes.getRawType;
import static revxrsal.commands.util.Collections.map;

@ApiStatus.Internal
public enum ValuesParameterTypeFactory implements ParameterType.Factory<CommandActor> {
    INSTANCE;

    @Override
    public <T> ParameterType<CommandActor, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<CommandActor> lamp) {
        Values values = annotations.get(Values.class);
        if (values == null)
            return null;
        ParameterType<CommandActor, Object> delegate = lamp.findNextResolver(parameterType, annotations, this)
                .requireParameterType();
        List<String> allowed = values.caseSensitive() ? Arrays.asList(values.value()) : map(values.value(), String::toUpperCase);
        if (allowed.isEmpty())
            throw new IllegalArgumentException("@Values() must contain at least 1 value!");
        return new ParameterType<CommandActor, T>() {
            @Override
            public T parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull CommandActor> context) {
                int start = input.position();
                @SuppressWarnings("unchecked")
                T value = (T) delegate.parse(input, context);
                int end = input.position();
                String consumed = input.peek(end - start);

                if ((values.caseSensitive() && allowed.contains(consumed))
                        || (!values.caseSensitive() && allowed.contains(consumed.toUpperCase())))
                    return value;
                throw new ValueNotAllowedException(
                        consumed,
                        Arrays.asList(values.value()),
                        values.caseSensitive()
                );
            }

            @Override public @NotNull SuggestionProvider<@NotNull CommandActor> defaultSuggestions() {
                return SuggestionProvider.of(values.value());
            }
        };
    }
}
