
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class MessageToSequencer extends Message implements IMessageToSequencer {
        
    public String msg;
        
    public MessageToSequencer(int sender, String msg) {
        super(sender);
        this.msg = msg;
    }
    
    public static final long serialVersionUID = 0;
}
