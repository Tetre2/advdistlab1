
import mcgui.*;


public class SetNewSequencer extends Message implements ISetNewSequencer {
        
    public int newSequencer;
        
    public SetNewSequencer(int sender, int newSequencer) {
        super(sender);
        this.newSequencer = newSequencer;
    }
    
}
