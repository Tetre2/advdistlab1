
import mcgui.*;

public class PeerDownMessage extends Message implements IPeerDownMessage{
        
    private int sender;
    private int deadPeer;
        
    public PeerDownMessage(int sender, int deadPeer) {
        super(sender);
    }

    /**
     * @return int return the sender
     */
    public int getSender() {
        return sender;
    }

    /**
     * @return int return the deadPeer
     */
    public int getDeadPeer() {
        return deadPeer;
    }

}
