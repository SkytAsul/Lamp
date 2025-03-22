package revxrsal.commands.annotation;

import org.jetbrains.annotations.NotNull;

/**
 * A utility annotation that marks a field as <em>only</em> accepting certain
 * values. This will take care of validation, tab-completion and errors.
 * <p>
 * While Lamp encourages using standard Java {@code enum}s for closed-type
 * values, this annotation makes it easier when a quick and dirty solution is needed.
 * <p>
 * It is also usable for numerical types, which cannot be done using enums.
 * <p>
 * Values can be of any type, including strings, integers, enums, etc.
 */
public @interface Values {

    /**
     * The allowed values.
     *
     * @return The allowed values
     */
    @NotNull String[] value();

    /**
     * Should checks be case-sensitive?
     *
     * @return if checks should be case-sensitive.
     */
    boolean caseSensitive() default true;

}
