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
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.exception.CooldownException;
import revxrsal.commands.hook.PostCommandExecutedHook;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.process.CommandCondition;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ApiStatus.Internal
public final class ThreadExecutorCooldownCondition implements CommandCondition<CommandActor>, PostCommandExecutedHook<CommandActor> {

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
}
