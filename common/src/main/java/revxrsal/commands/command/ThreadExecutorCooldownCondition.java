/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.exception.CooldownException;
import revxrsal.commands.hook.PostCommandExecutedHook;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ContextParameter;
import revxrsal.commands.process.CommandCondition;
import revxrsal.commands.util.Classes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
public final class ThreadExecutorCooldownCondition implements
        CommandCondition<CommandActor>,
        PostCommandExecutedHook<CommandActor>,
        ContextParameter.Factory<CommandActor> {

    private static final ScheduledExecutorService COOLDOWN_POOL = Executors.newSingleThreadScheduledExecutor();

    private final Map<UUID, Map<Integer, Long>> cooldowns = new ConcurrentHashMap<>();

    @Override
    public void onPostExecuted(@NotNull ExecutableCommand<CommandActor> command, @NotNull ExecutionContext<CommandActor> context) {
        Cooldown cooldown = command.annotations().get(Cooldown.class);
        if (cooldown == null || cooldown.value() == 0) return;
        Map<Integer, Long> spans = cooldowns.computeIfAbsent(context.actor().uniqueId(), u -> new ConcurrentHashMap<>());
        spans.put(command.hashCode(), System.currentTimeMillis());

        COOLDOWN_POOL.schedule(() -> spans.remove(command.hashCode()), cooldown.value(), cooldown.unit());
    }

    @Override public void test(@NotNull ExecutionContext<CommandActor> context) {
        @Nullable Cooldown cooldown = context.command().annotations().get(Cooldown.class);
        if (cooldown == null || cooldown.value() == 0)
            return;
        UUID uuid = context.actor().uniqueId();
        @Nullable Map<Integer, Long> spans = cooldowns.get(uuid);
        if (spans == null)
            return;
        @Nullable Long created = spans.get(context.command().hashCode());
        if (created == null)
            return;
        long passed = System.currentTimeMillis() - created;
        long left = cooldown.unit().toMillis(cooldown.value()) - passed;
        if (left > 0 && left < 1000)
            left = 1000L; // for formatting
        throw new CooldownException(left);
    }

    @Override
    public @Nullable <T> ContextParameter<CommandActor, T> create(@NotNull Type parameterType, @NotNull AnnotationList annotations, @NotNull Lamp<CommandActor> lamp) {
        Class<?> rawType = Classes.getRawType(parameterType);
        if (!CooldownHandle.class.isAssignableFrom(rawType))
            return null;
        @Nullable Cooldown cooldown = annotations.get(Cooldown.class);
        //noinspection unchecked
        return (ContextParameter<CommandActor, T>) (ContextParameter<CommandActor, CooldownHandle>) (
                parameter, context
        ) -> {
            if (context.command().annotations().contains(Cooldown.class))
                throw new IllegalArgumentException("Cannot have both @Cooldown and CooldownHandle in one command. Either put " +
                        "@Cooldown on the CooldownHandle parameter (@Cooldown(...) CooldownHandle handle), or " +
                        "remove @Cooldown entirely.");
            return new BasicHandle(
                    context.actor().uniqueId(),
                    context.command().hashCode(),
                    cooldown
            );
        };
    }

    private class BasicHandle implements CooldownHandle {

        private final UUID actor;
        private final int hashCode;
        private final @Nullable Cooldown cooldown;

        public BasicHandle(UUID actor, int hashCode, @Nullable Cooldown cooldown) {
            this.actor = actor;
            this.hashCode = hashCode;
            this.cooldown = cooldown;
        }

        @Override public @NotNull CooldownHandle withCooldown(long cooldownValue, @NotNull TimeUnit unit) {
            return new BasicHandle(actor, hashCode, new DynamicCooldown(cooldownValue, unit));
        }

        @Override public boolean isOnCooldown() {
            @Nullable Map<Integer, Long> spans = cooldowns.get(actor);
            if (spans == null)
                return false;
            @Nullable Long created = spans.get(hashCode);
            return created != null;
        }

        @Override public long elapsedMillis() {
            @Nullable Map<Integer, Long> spans = cooldowns.get(actor);
            if (spans == null)
                return 0L;
            @Nullable Long created = spans.get(hashCode);
            if (created == null)
                return 0L;
            return System.currentTimeMillis() - created;
        }

        @Override public void cooldown() {
            if (cooldown == null) {
                throw new IllegalArgumentException("cooldown() can only be used if the parameter " +
                        "has @Cooldown on it, otherwise use cooldown(duration, unit) or other " +
                        "overloads.");
            }
            cooldown(cooldown.value(), cooldown.unit());
        }

        @Override public void requireNotOnCooldown() {
            if (cooldown == null) {
                throw new IllegalArgumentException("requireNotOnCooldown() can only be used if the parameter " +
                        "has @Cooldown on it, otherwise use requireNotOnCooldown(duration, unit) or other " +
                        "overloads.");
            }
            requireNotOnCooldown(cooldown.value(), cooldown.unit());
        }

        @Override public void requireNotOnCooldown(long cooldownValue, @NotNull TimeUnit cooldownUnit) {
            long elapsed = elapsedMillis();
            if (elapsed == 0L)
                return;
            long left = cooldownUnit.toMillis(cooldownValue) - elapsed;
            if (left > 0 && left < 1000)
                left = 1000L; // for formatting
            throw new CooldownException(left);
        }

        @Override public void removeCooldown() {
            @Nullable Map<Integer, Long> spans = cooldowns.get(actor);
            if (spans == null)
                return;
            spans.remove(hashCode);
        }

        @Override public void cooldown(@Range(from = 1, to = Long.MAX_VALUE) long duration, @NotNull TimeUnit unit) {
            Map<Integer, Long> spans = cooldowns.computeIfAbsent(actor, u -> new ConcurrentHashMap<>());
            spans.put(hashCode, System.currentTimeMillis());

            COOLDOWN_POOL.schedule(() -> spans.remove(hashCode), duration, unit);
        }

        @Override public long remainingTimeMillis() {
            if (cooldown == null) {
                throw new IllegalArgumentException("remainingTimeMillis() can only be used if the parameter " +
                        "has @Cooldown on it, otherwise use remainingTimeMillis(duration, unit) or other " +
                        "overloads.");
            }
            return remainingTime(cooldown.value(), cooldown.unit(), TimeUnit.MILLISECONDS);
        }

        @Override public long remainingTime(@NotNull TimeUnit outputUnit) {
            return outputUnit.convert(remainingTimeMillis(), TimeUnit.MILLISECONDS);
        }

        private class DynamicCooldown implements Cooldown {

            private final long value;
            private final TimeUnit unit;

            public DynamicCooldown(long value, TimeUnit unit) {
                this.value = value;
                this.unit = unit;
            }

            @Override public long value() {
                return value;
            }

            @Override public TimeUnit unit() {
                return unit;
            }

            @Override public Class<? extends Annotation> annotationType() {
                return Cooldown.class;
            }
        }

    }
}
