/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.broadcast;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class BroadcastMessage {

    private String sourceMessage;
    private Object target;
    private String messageText;

    public BroadcastMessage(String sourceMessage, Object target
            , String messageText) { // JComponent
        super();

        this.sourceMessage = sourceMessage;
        this.target = target;
        this.messageText = messageText;

    }

    public String getMessageText() {
        return messageText;
    }

    public String getSource() {
        return sourceMessage;
    }

    public Object getTarget() {
        return target;
    }

}
