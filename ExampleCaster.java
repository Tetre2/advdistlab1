
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    boolean[] alive;        // array for dead/alive nodes
    Queue<Msg> queue;     // fifo queue
    int lastAddedSeq;
    Token localToken;
    boolean hasTheToken;
    List<PeerDownResponse> PeerDownResponses;


    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
        alive = new boolean[hosts];

        for (int i = 0; i < alive.length; i++) {
            alive[i] = true;
        }

        queue = new LinkedList<Msg>();
        PeerDownResponses = new ArrayList<>();
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        queue.add(new Msg(id, messagetext));
        mcui.debug("queue added: " + messagetext + " | total queue size: " + queue.size());

        if(localToken == null){ // cant think of a better way to create the initial token :(
            Token t = new Token();
            t.addMessage(queue.remove());
            lastAddedSeq = t.getSeq();
            t.setLock(true);
            bcom.basicsend(getNextNodeInRing(), new TokenMessage(id, t));
        }
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        if(message instanceof ITokenMessage){
            receiveToken(peer, (ITokenMessage) message);
            
        }else if(message instanceof ITokenResponse){
            hasTheToken = false;

        }else if(message instanceof IPeerDownMessage){
            PeerDownMessage msg = (PeerDownMessage) message;
            alive[msg.getDeadPeer()] = false;
            bcom.basicsend(msg.getSender(), new PeerDownResponse(id, hasTheToken, localToken));

        }else if(message instanceof IPeerDownResponse){
            recivePeerDownResponse(peer, (IPeerDownResponse) message);
        }
        
    }

    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer "+peer+" has been dead for a while now!");
        if(alive[peer]){ //if not updated by other nodes
            alive[peer] = false;
            for (int i = 0; i < alive.length; i++) { //notify all other nodes about the dead peer
                if(alive[i] && i != id){
                    bcom.basicsend(i, new PeerDownMessage(id, peer));
                }
            }
        }
    }

    private void recivePeerDownResponse(int peer, IPeerDownResponse message){
        PeerDownResponse msg = (PeerDownResponse) message;
            PeerDownResponses.add(msg);

            if(PeerDownResponses.size() >= getAliveNodes() - 1){ //exclude yourself
                boolean tokenExists = false;
                for (PeerDownResponse peerDownResponse : PeerDownResponses) {
                    if(peerDownResponse.hasActiveToken()){
                        tokenExists = true;
                        break;
                    }   
                }

                if( !tokenExists){ //if no one has the token meaning it has been lost, recreate it
                    Token tokenWithHighestSeq = PeerDownResponses.get(0).getLocalToken();
                    for (PeerDownResponse peerDownResponse : PeerDownResponses) {
                        if(peerDownResponse.getLocalToken().getSeq() > tokenWithHighestSeq.getSeq()){
                            tokenWithHighestSeq = peerDownResponse.getLocalToken();
                        }
                    }
                    bcom.basicsend(getNextNodeInRing(), new TokenMessage(id, tokenWithHighestSeq));
                }

                
            }
    }

    private void receiveToken(int peer, ITokenMessage message){

        bcom.basicsend(peer, new TokenResponse(id)); //ack the token
        hasTheToken = true;

        Token token = message.getToken();

        mcui.debug("islocked: " + token.isLocked() + " | mgs size: " + token.getMessages().size());

        if(localToken == null || token.getSeq() > (localToken).getSeq()){

            Msg latestMsg = token.getMessages().get(token.getMessages().size() - 1);
            mcui.deliver(latestMsg.getSender(), latestMsg.getMsg());
        }

        try {
            localToken = (Token) token.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if(token.isLocked() && lastAddedSeq == token.getSeq()){ //if we locked the tocken remove the lock after one round
            token.setLock(false);
            mcui.debug("unlock token");
        }else if( !token.isLocked()){ // if there is messages in the queue send one and lock then token
            if(!queue.isEmpty()){
                mcui.debug("send message from queue");
                token.addMessage(queue.remove());
                lastAddedSeq = token.getSeq();
                token.setLock(true);
            }
        }
        
        bcom.basicsend(getNextNodeInRing(), new TokenMessage(id, token));
    }

    //used in the queue to make sure a message is broudcast
    private int getNextNodeInRing(){
        int next = (id + 1) % hosts;
        while( !alive[next]){
            next = (next + 1) % hosts;
        }
        return next;
    }

    private int getAliveNodes(){
        int sum = 0;
        for (boolean b : alive) {
            if(b){
                sum++;
            }
        }
        return sum;
    }

}


