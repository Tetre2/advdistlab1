import mcgui.*;

public class UpdateNewSequnecer extends Message implements IUpdateNewSequencer {
        
    public int deadNode;
    public int newSequnecer;
        
    public UpdateNewSequnecer(int sender, int deadNode, int newSequnecer) {
        super(sender);
        this.deadNode = deadNode;
        this.newSequnecer = newSequnecer;
    }
    
    public static final long serialVersionUID = 0;
}
