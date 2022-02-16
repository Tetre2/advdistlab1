
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class SequencerMessage extends Message implements ISequencer {
        
    public String msg;
        
    public SequencerMessage(int sender, String msg) {
        super(sender);
        this.msg = msg;
    }
    
    public static final long serialVersionUID = 0;
}
