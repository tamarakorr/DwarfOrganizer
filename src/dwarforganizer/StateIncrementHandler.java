/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 * Allows incremented requests for incrementing states and doing
 * something at a threshold (such as accumulating requests to show/hide
 * component)
 * 
 * @author Tamara Orr
 */
public class StateIncrementHandler {
    protected enum DefaultState {
        NEGATIVE_STATE, POSITIVE_STATE
    }
    
    private static final int NEGATIVE_THRESHOLD = 0;
    private static final int POSITIVE_THRESHOLD = 1;        

    private int stateIncrement;
    private ThresholdFunctions thresholdFunctions;
    
    public StateIncrementHandler(DefaultState currentState) {
        
        if (currentState.equals(DefaultState.POSITIVE_STATE))
            stateIncrement = POSITIVE_THRESHOLD;
        else // (currentState.equals(DefaultState.NEGATIVE_STATE)
            stateIncrement = NEGATIVE_THRESHOLD;
    }
    public void initialize(ThresholdFunctions tf) {
        this.thresholdFunctions = tf;
    }
    protected interface ThresholdFunctions {
        public void doAtNegativeThreshold();
        public void doAtPositiveThreshold();
    }
    public void decrement() {
        
        if (thresholdFunctions == null) {
            System.err.println("[StateIncrementHandler] Must be initialized"
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
            System.err.println("[StateIncrementHandler] Must be initialized"
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
