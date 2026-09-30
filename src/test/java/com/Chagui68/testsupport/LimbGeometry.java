package com.Chagui68.testsupport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Reads a limb's second joint out of the export, independently of the code that hardcodes it.
 *
 * <p>Each dressed model names which pieces hang from an elbow or a knee, and each computes where that
 * joint is. Those two decisions are the ones a re-export can silently invalidate, so the model tests
 * derive the joint from the geometry instead: stack the limb's pieces by height and cut at the
 * biggest gap between two of them, which is where the export leaves the joint. The code's answer and
 * the export's answer are then compared, so a mis-named piece or a moved segment fails the suite
 * instead of folding the wrong half of a limb in game.
 */
public final class LimbGeometry {

    /** A limb cut at its joint: the pieces above it, the pieces below it, and the two edges of the cut. */
    public record Split<P>(List<P> upper, List<P> lower, double upperEnd, double lowerEnd) {

        /** The height the joint sits at: halfway between the two pieces it separates. */
        public double joint() {
            return (upperEnd + lowerEnd) * 0.5;
        }
    }

    private LimbGeometry() {
    }

    /**
     * Cuts a limb at the biggest vertical gap between two of its pieces.
     *
     * @param limb   the pieces of one limb
     * @param height where each piece rests, in model space
     */
    public static <P> Split<P> largestGap(List<P> limb, ToDoubleFunction<P> height) {
        if (limb.size() < 2) throw new IllegalArgumentException("a limb needs two pieces to have a joint");

        List<P> stacked = new ArrayList<>(limb);
        stacked.sort(Comparator.comparingDouble(height).reversed());

        int cut = 0;
        double biggest = Double.NEGATIVE_INFINITY;
        for (int i = 0; i + 1 < stacked.size(); i++) {
            double gap = height.applyAsDouble(stacked.get(i)) - height.applyAsDouble(stacked.get(i + 1));
            if (gap > biggest) {
                biggest = gap;
                cut = i;
            }
        }
        return new Split<>(List.copyOf(stacked.subList(0, cut + 1)),
                List.copyOf(stacked.subList(cut + 1, stacked.size())),
                height.applyAsDouble(stacked.get(cut)), height.applyAsDouble(stacked.get(cut + 1)));
    }
}
