package revxrsal.commands.velocity.exception;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.exception.*;
import revxrsal.commands.node.ParameterNode;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import static revxrsal.commands.velocity.util.VelocityUtils.legacyColorize;

public class VelocityExceptionHandler extends DefaultExceptionHandler<VelocityCommandActor> {

    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, VelocityCommandActor actor) {
        actor.error(legacyColorize("&cInvalid player: &e" + e.input() + "&c."));
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, VelocityCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }

    @HandleException
    public void onSenderNotConsole(SenderNotConsoleException e, VelocityCommandActor actor) {
        actor.error(legacyColorize("&cYou must be a player to execute this command!"));
    }

    @Override public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cInvalid choice: &e" + e.input() + "&c. Please enter a valid option from the available values."));
    }

    @Override public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cExpected &e" + e.node().name() + "&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInputParse(@NotNull InputParseException e, @NotNull VelocityCommandActor actor) {
        switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER:
                actor.error(legacyColorize("&cInvalid input. Use &e\\\\ &cto include a backslash."));
                break;
            case UNCLOSED_QUOTE:
                actor.error(legacyColorize("&cUnclosed quote. Make sure to close all quotes."));
                break;
            case EXPECTED_WHITESPACE:
                actor.error(legacyColorize("&cExpected whitespace to end one argument, but found trailing data."));
                break;
        }
    }

    @Override
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull VelocityCommandActor actor, @NotNull ParameterNode<VelocityCommandActor, ?> parameter) {
        if (e.inputSize() < e.minimum())
            actor.error(legacyColorize("&cYou must input at least &e" + fmt(e.minimum()) + " &centries for &e" + parameter.name() + "&c."));
        if (e.inputSize() > e.maximum())
            actor.error(legacyColorize("&cYou must input at most &e" + fmt(e.maximum()) + " &centries for &e" + parameter.name() + "&c."));
    }

    @Override
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e, @NotNull VelocityCommandActor actor, @NotNull ParameterNode<VelocityCommandActor, ?> parameter) {
        if (e.input().length() < e.minimum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &cmust be at least &e" + fmt(e.minimum()) + " &ccharacters long."));
        if (e.input().length() > e.maximum())
            actor.error(legacyColorize("&cParameter &e" + parameter.name() + " &ccan be at most &e" + fmt(e.maximum()) + " &ccharacters long."));
    }

    @Override public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cExpected &etrue &cor &efalse&c, found &e" + e.input() + "&c."));
    }

    @Override public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cInvalid number: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cInvalid integer: &e" + e.input() + "&c."));
    }

    @Override public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cInvalid UUID: " + e.input() + "&c."));
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull VelocityCommandActor actor, @NotNull ParameterNode<VelocityCommandActor, ?> parameter) {
        actor.error(legacyColorize("&cRequired parameter is missing: &e" + parameter.name() + "&c. Usage: &e/" + parameter.command().usage() + "&c."));
    }

    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cYou do not have permission to execute this command!"));
    }

    @Override
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull VelocityCommandActor actor, @NotNull ParameterNode<VelocityCommandActor, Number> parameter) {
        if (e.input().doubleValue() < e.minimum())
            actor.error(legacyColorize("&c" + parameter.name() + " too small &e(" + fmt(e.input()) + ")&c. Must be at least &e" + fmt(e.minimum()) + "&c."));
        if (e.input().doubleValue() > e.maximum())
            actor.error(legacyColorize("&c" + parameter.name() + " too large &e(" + fmt(e.input()) + ")&c. Must be at most &e" + fmt(e.maximum()) + "&c."));
    }

    @Override public void onInvalidHelpPage(@NotNull InvalidHelpPageException e, @NotNull VelocityCommandActor actor) {
        if (e.numberOfPages() == 1)
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be 1."));
        else
            actor.error(legacyColorize("Invalid help page: &e" + e.page() + "&c. Must be between &e1 &cand &e" + e.numberOfPages()));
    }

    @Override public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull VelocityCommandActor actor) {
        actor.error(legacyColorize("&cUnknown command: &e" + e.input() + "&c."));
    }

    @Override public void onValueNotAllowed(@NotNull ValueNotAllowedException e, @NotNull VelocityCommandActor actor) {
        String allowedValues = String.join("&c, &e", e.allowedValues());
        actor.error(legacyColorize("Received an invalid value: &e" + e.input() + "&c. Allowed values: &e" + allowedValues + "&c."));
    }
}
