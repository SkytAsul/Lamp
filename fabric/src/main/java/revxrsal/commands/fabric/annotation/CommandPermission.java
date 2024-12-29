package revxrsal.commands.fabric.annotation;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.NotSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a command permission for the given command.
 */
@DistributeOnMethods
@NotSender.ImpliesNotSender
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {

    /**
     * The permission string. This is passed to {@link Permissions#check(CommandSource, String, int)} (CommandSource, String)}
     *
     * @return The permission value
     */
    String value();

    /**
     * The Vanilla permission value to be used as a fallback. This is passed to {@link Permissions#check(CommandSource, String, int)}
     *
     * @return The fallback Vanilla permission value
     */
    int vanilla() default 4;

}
