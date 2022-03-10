
import mcgui.*;

public class PeerDownResponse extends Message implements IPeerDownResponse{
        
    private int sender;
    private Boolean hasActiveToken;
    private Token localToken;
        
    public PeerDownResponse(int sender, Boolean hasActiveToken, Token localToken) {
        super(sender);
    }

    /**
     * @return int return the sender
     */
    public int getSender() {
        return sender;
    }

    /**
     * @return Boolean return the hasActiveToken
     */
    public Boolean hasActiveToken() {
        return hasActiveToken;
    }

    /**
     * @return Token return the localToken
     */
    public Token getLocalToken() {
        return localToken;
    }

}