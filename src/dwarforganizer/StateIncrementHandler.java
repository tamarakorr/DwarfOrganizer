/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.logging.Logger;

/**
 * Allows incremented requests for incrementing states and doing
 * something at a threshold (such as accumulating requests to show/hide
 * component)
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class StateIncrementHandler {
    private static final Logger logger = Logger.getLogger(
            StateIncrementHandler.class.getName());

    protected enum DefaultState {
        NEGATIVE_STATE, POSITIVE_STATE
    }

    private static final int NEGATIVE_THRESHOLD = 0;
    private static final int POSITIVE_THRESHOLD = 1;

    private int stateIncrement;
    private ThresholdFunctions thresholdFunctions;

    public StateIncrementHandler(final DefaultState currentState) {

        if (currentState.equals(DefaultState.POSITIVE_STATE))
            stateIncrement = POSITIVE_THRESHOLD;
        else // (currentState.equals(DefaultState.NEGATIVE_STATE)
            stateIncrement = NEGATIVE_THRESHOLD;
    }
    public void initialize(final ThresholdFunctions tf) {
        this.thresholdFunctions = tf;
    }
    protected interface ThresholdFunctions {
        public void doAtNegativeThreshold();
        public void doAtPositiveThreshold();
    }
    public void decrement() {

        if (thresholdFunctions == null) {
            logger.severe("[StateIncrementHandler] Must be initialized"
                    + " before incrementing.");
            return;
        }

        stateIncrement--;
        if (stateIncrement == NEGATIVE_THRESHOLD) {
            //System.out.println("Hiding");
            thresholdFunctions.doAtNegativeThreshold();
        }
    }
    public void increment() {
        if (thresholdFunctions == null) {
            logger.severe("[StateIncrementHandler] Must be initialized"
                    + " before incrementing.");
            return;
        }

        stateIncrement++;
        if (stateIncrement == POSITIVE_THRESHOLD) {
            //System.out.println("Showing");
            thresholdFunctions.doAtPositiveThreshold();
        }
    }
}
