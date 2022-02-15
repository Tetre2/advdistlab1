
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class AckMessage extends Message implements IAck {
        
    public AckTypes types;
    public int seq;
        
    public AckMessage(int sender, AckTypes type, int seq) {
        super(sender);
        this.types = type;
        this.seq = seq;
    }
    
    public static final long serialVersionUID = 0;
}
