package revxrsal.commands.bukkit.parameters;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.MissingLocationParameterException;
import revxrsal.commands.bukkit.exception.MissingLocationParameterException.MissingAxis;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.util.Lazy;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * A {@link ParameterType} that parses {@link Location} types
 * <p>
 * Credits to @SkytAsul
 */
public final class LocationParameterType implements ParameterType<BukkitCommandActor, Location> {

    @Override
    public Location parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        if (input.peek() == '^')
            return parseLocal(input, context.actor());
        else
            return parseWorld(input, context.actor());
    }

    private void consumeSpace(@NotNull MutableStringStream input, @NotNull MissingAxis missingAxis) {
        if (input.hasFinished())
            throw new MissingLocationParameterException(input.peekString(), missingAxis);
        if (input.peek() == ' ')
            input.moveForward();
    }

    // absolute or tilde notation ~
    private Location parseWorld(@NotNull MutableStringStream input, @NotNull BukkitCommandActor actor) {
        Supplier<Location> actorLocation = Lazy.of(() -> actor.requirePlayer().getLocation());
        double x;
        double y;
        double z;

        x = readWorldCoordinate(input, () -> actorLocation.get().getX());

        consumeSpace(input, MissingAxis.Y);
        y = readWorldCoordinate(input, () -> actorLocation.get().getY());

        consumeSpace(input, MissingAxis.Z);
        z = readWorldCoordinate(input, () -> actorLocation.get().getZ());

        World world = actor.isPlayer() ? actorLocation.get().getWorld() : Bukkit.getWorld("world");
        return new Location(world, x, y, z);
    }

    private double readWorldCoordinate(@NotNull MutableStringStream input, DoubleSupplier relativeToSupplier) {
        if (input.peek() == '~') {
            double relativeTo = relativeToSupplier.getAsDouble();
            input.moveForward();
            if (!input.hasFinished() && !Character.isWhitespace(input.peek()))
                relativeTo += input.readDouble();
            return relativeTo;
        } else
            return input.readDouble();
    }

    // caret notation ^
    private Location parseLocal(@NotNull MutableStringStream input, @NotNull BukkitCommandActor actor) {
        Location actorLocation = actor.requirePlayer().getLocation();
        double x = readLocalCoordinate(input);

        consumeSpace(input, MissingAxis.Y);
        double y = readLocalCoordinate(input);

        consumeSpace(input, MissingAxis.Z);
        double z = readLocalCoordinate(input);

        Vector vector = getLocal(actorLocation, new Vector(x, y, z));
        return new Location(actorLocation.getWorld(), vector.getX(), vector.getY(), vector.getZ());
    }

    private double readLocalCoordinate(@NotNull MutableStringStream input) {
        if (input.read() != '^')
            throw new CommandErrorException("Expected '^'.");
        if (input.hasFinished() || Character.isWhitespace(input.peek()))
            return 0;
        return input.readDouble();
    }

    // math from https://www.spigotmc.org/threads/local-coordinates.529011/#post-4280379
    private Vector getLocal(Location reference, Vector local) {
        // Firstly a vector facing YAW = 0, on the XZ plane as start base
        Vector axisBase = new Vector(0, 0, 1);
        // This one pointing YAW + 90° should be the relative "left" of the field of view, isn't it (since
        // ROLL always is 0°)?

        Vector axisLeft = rotateAroundY(axisBase.clone(), Math.toRadians(-reference.getYaw() + 90.0f));
        // Left axis should be the rotation axis for going up, too, since it's perpendicular...
        Vector axisUp = rotateAroundNonUnitAxis(reference.getDirection().clone(), axisLeft, Math.toRadians(-90f));

        // Based on these directions, we got all we need
        Vector sway = axisLeft.clone().normalize().multiply(local.getX());
        Vector heave = axisUp.clone().normalize().multiply(local.getY());
        Vector surge = reference.getDirection().clone().multiply(local.getZ());

        // Add up the global reference based result
        return new Vector(reference.getX(), reference.getY(), reference.getZ()).add(sway).add(heave).add(surge);
    }

    @NotNull
    private Vector rotateAroundY(Vector vector, double angle) {
        double angleCos = Math.cos(angle);
        double angleSin = Math.sin(angle);
        double x = angleCos * vector.getX() + angleSin * vector.getZ();
        double z = -angleSin * vector.getX() + angleCos * vector.getZ();
        return vector.setX(x).setZ(z);
    }

    @NotNull
    private Vector rotateAroundNonUnitAxis(@NotNull Vector vector, @NotNull Vector axis, double angle) throws IllegalArgumentException {
        Preconditions.checkArgument(axis != null, "The provided axis vector was null");
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();
        double x2 = axis.getX();
        double y2 = axis.getY();
        double z2 = axis.getZ();
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);
        double dotProduct = vector.dot(axis);
        double xPrime = x2 * dotProduct * ((double) 1.0F - cosTheta) + x * cosTheta + (-z2 * y + y2 * z) * sinTheta;
        double yPrime = y2 * dotProduct * ((double) 1.0F - cosTheta) + y * cosTheta + (z2 * x - x2 * z) * sinTheta;
        double zPrime = z2 * dotProduct * ((double) 1.0F - cosTheta) + z * cosTheta + (-y2 * x + x2 * y) * sinTheta;
        return vector.setX(xPrime).setY(yPrime).setZ(zPrime);
    }
}
