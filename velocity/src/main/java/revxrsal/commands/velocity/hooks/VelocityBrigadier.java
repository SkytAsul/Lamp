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
package revxrsal.commands.velocity.hooks;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampVisitor;
import revxrsal.commands.brigadier.BrigadierConverter;
import revxrsal.commands.brigadier.BrigadierParser;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.velocity.VelocityLampConfig;
import revxrsal.commands.velocity.actor.ActorFactory;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

/**
 * A hook that registers Lamp commands into Velocity
 *
 * @param <A> The actor type
 */
public final class VelocityBrigadier<A extends VelocityCommandActor> implements LampVisitor<A>, BrigadierConverter<A, CommandSource> {

    private final ProxyServer server;
    private final ActorFactory<A> actorFactory;
    private final ArgumentTypes<A> argumentTypes;
    private final BrigadierParser<CommandSource, A> parser = new BrigadierParser<>(this);

    public VelocityBrigadier(@NotNull VelocityLampConfig<A> config) {
        this.server = config.server();
        this.actorFactory = config.actorFactory();
        this.argumentTypes = config.argumentTypes();
    }

    public VelocityBrigadier(@NotNull ProxyServer server, @NotNull ActorFactory<A> actorFactory, @NotNull ArgumentTypes<A> argumentTypes) {
        this.server = server;
        this.actorFactory = actorFactory;
        this.argumentTypes = argumentTypes;
    }

    @Override
    public @NotNull ArgumentType<?> getArgumentType(@NotNull ParameterNode<A, ?> parameter) {
        return argumentTypes.type(parameter);
    }

    @Override
    public @NotNull A createActor(@NotNull CommandSource sender, @NotNull Lamp<A> lamp) {
        return actorFactory.create(sender, lamp);
    }

    @Override
    public void visit(@NotNull Lamp<A> lamp) {
        RootCommandNode<CommandSource> root = new RootCommandNode<>();
        for (ExecutableCommand<A> command : lamp.registry()) {
            LiteralCommandNode<CommandSource> node = parser.createNode(command);
            BrigadierParser.addChild(root, node);
        }
        for (CommandNode<CommandSource> node : root.getChildren()) {
            BrigadierCommand brigadierCommand = new BrigadierCommand((LiteralCommandNode<CommandSource>) node);
            CommandMeta meta = server.getCommandManager().metaBuilder(node.getName()).build();
            server.getCommandManager().register(meta, brigadierCommand);
        }
    }
}
