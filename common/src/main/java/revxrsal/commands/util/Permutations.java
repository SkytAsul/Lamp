package revxrsal.commands.util;

import revxrsal.commands.command.CommandActor;
import revxrsal.commands.node.ParameterNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static revxrsal.commands.util.Preconditions.cannotInstantiate;

/**
 * A utility class for generating permutations of unordered {@link ParameterNode}s
 * (i.e. flags and switches).
 * <p>
 * This is useful for certain platforms like Brigadier and Minestom.
 */
public final class Permutations {

    private Permutations() {
        cannotInstantiate(Permutations.class);
    }

    public static <A extends CommandActor> List<List<ParameterNode<A, Object>>> generatePermutations(List<ParameterNode<A, Object>> list) {
        List<ParameterNode<A, Object>> required = new ArrayList<>();
        List<ParameterNode<A, Object>> optional = new ArrayList<>();

        for (ParameterNode<A, Object> node : list) {
            if (node.isRequired()) {
                required.add(node);
            } else {
                optional.add(node);
            }
        }

        List<List<ParameterNode<A, Object>>> truePermutations = new ArrayList<>();
        List<List<ParameterNode<A, Object>>> falsePermutations = new ArrayList<>();

        permute(required, 0, truePermutations);
        permute(optional, 0, falsePermutations);

        List<List<ParameterNode<A, Object>>> result = new ArrayList<>();

        for (List<ParameterNode<A, Object>> tp : truePermutations) {
            for (List<ParameterNode<A, Object>> fp : falsePermutations) {
                List<ParameterNode<A, Object>> combined = new ArrayList<>(tp);
                combined.addAll(fp);
                result.add(combined);
            }
        }

        return result;
    }

    private static <A extends CommandActor> void permute(List<ParameterNode<A, Object>> list, int start, List<List<ParameterNode<A, Object>>> result) {
        if (start == list.size()) {
            result.add(new ArrayList<>(list));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            java.util.Collections.swap(list, start, i);
            permute(list, start + 1, result);
            Collections.swap(list, start, i);
        }
    }
}
