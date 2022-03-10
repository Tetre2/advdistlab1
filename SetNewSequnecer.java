import mcgui.*;

public class SetNewSequnecer extends Message implements ISequencer {
        
    public int deadNode;
    public int newSequnecer;
        
    public SetNewSequnecer(int sender, int deadNode, int newSequnecer) {
        super(sender);
        this.deadNode = deadNode;
        this.newSequnecer = newSequnecer;
    }
    
    public static final long serialVersionUID = 0;
}
