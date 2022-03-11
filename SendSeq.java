
import mcgui.*;

public class SendSeq extends Message implements ISendSeq {
        
    public int seq;
        
    public SendSeq(int sender, int seq) {
        super(sender);
        this.seq = seq;
    }
    
}
