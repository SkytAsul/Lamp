/*
 * This file is part of sweeper, licensed under the MIT License.
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
package revxrsal.commands.annotation;

import revxrsal.commands.command.CooldownHandle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Adds a cooldown to the command.
 * <p>
 * Note that this annotation can be used in two ways:
 * <ol>
 *     <li>On methods and on classes, as follows: <pre>
 * {@code
 *     @Command("foo")
 *     @Cooldown(value = 10, unit = TimeUnit.SECONDS)
 *     public void foo(...) {
 *         // ... command action here
 *     }
 *     }
 *     </pre>
 *     Which will create a simple cooldown of 10 seconds on the user. The user
 *     will always be put on cooldown if the command successfully executes, and will
 *     receive a {@link revxrsal.commands.exception.CooldownException} if they attempt to execute
 *     it during the cooldown.</li>
 *
 *     <li>On parameters combined with {@link CooldownHandle}: <pre>
 * {@code
 *
 *     @Command("foo")
 *     public void foo(
 *          ...,
 *          @Cooldown(value = 10, unit = TimeUnit.SECONDS) CooldownHandle cooldownHandle
 *     ) {
 *         if (cooldownHandle.isOnCooldown()) {
 *             long secondsLeft = cooldownHandle.remainingTime(TimeUnit.SECONDS);
 *             actor.reply("You must wait " + secondsLeft + " second(s) before using this commnd again!");
 *             return;
 *         }
 *         // ... command action here
 *
 *         // then put the user on cooldown:
 *         cooldownHandle.cooldown();
 *     }
 *     }
 *     </pre>
 *     or:
 * <pre>
 * {@code
 *
 *     @Command("foo")
 *     public void foo(
 *          ...,
 *          @Cooldown(value = 10, unit = TimeUnit.SECONDS) CooldownHandle cooldownHandle
 *     ) {
 *         cooldownHandle.requireNotOnCooldown();
 *         // ^^^ will throw a CooldownException if the user is on cooldown
 *
 *         // ... command action here
 *
 *         // then put the user on cooldown:
 *         cooldownHandle.cooldown();
 *     }
 *     }
 *     </pre>
 *     A {@link CooldownHandle} requires more code, however it gives you a lot more flexibility on deciding
 *     when to cooldown the user. It also provides useful methods such as {@link CooldownHandle#removeCooldown()}
 *     and {@link CooldownHandle#remainingTime(TimeUnit)}, allowing for better customization of the cooldown
 *     behavior.</li>
 * </ol>
 *
 * <strong>Note: </strong> {@link CooldownHandle} can be used without a {@link Cooldown @Cooldown} annotation.
 * See the documentation of {@link CooldownHandle} for more details.
 *
 * @see CooldownHandle
 */
@DistributeOnMethods
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
public @interface Cooldown {

    /**
     * The cooldown value
     *
     * @return The command cooldown value
     */
    long value();

    /**
     * The time unit in which the cooldown goes for.
     *
     * @return The time unit for the cooldown
     */
    TimeUnit unit() default TimeUnit.SECONDS;

}
