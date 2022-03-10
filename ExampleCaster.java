
import java.util.LinkedList;
import java.util.Queue;

import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    boolean[] alive;        // array for dead/alive nodes
    Queue<Msg> queue = new LinkedList<Msg>();;     // fifo queue
    int lastAddedSeq;
    Object localToken;
    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
        alive = new boolean[hosts];

        for (int i = 0; i < alive.length; i++) {
            alive[i] = true;
        }

        
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

        Token token = ((IToken) message).getToken();

        //mcui.debug("islocked: " + token.isLocked() + " | mgs size: " + token.getMessages().size());

        if(localToken == null || token.getSeq() > ((Token) localToken).getSeq()){

            Msg latestMsg = token.getMessages().get(token.getMessages().size() - 1);
            mcui.deliver(latestMsg.getSender(), latestMsg.getMsg());
        }

        try {
            localToken = token.clone();
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

    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer "+peer+" has been dead for a while now!");
    }

    //used in the queue to make sure a message is broudcast
    private int getNextNodeInRing(){
        int next = (id + 1) % hosts;
        while( !alive[next]){
            next = (next + 1) % hosts;
        }
        return next;
    }

}


