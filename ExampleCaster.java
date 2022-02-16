
import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    boolean[] alive;
    int sequencerNode;
    int seq;

    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
        alive = new boolean[hosts];

        for (int i = 0; i < alive.length; i++) {
            alive[i] = true;
        }

        sequencerNode = 0;
        seq = 0;
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        bcom.basicsend(sequencerNode, new SequencerMessage(id, messagetext));
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        if(message instanceof ISequencer){

            for (int i = 0; i < hosts; i++) {
                bcom.basicsend(i, new DataMessage(id, ((SequencerMessage) message).msg, seq++));
            }

        }else{
            this.seq = ((DataMessage)message).seq;
            mcui.deliver(peer, ((DataMessage)message).text);
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
        alive[peer] = false;
        if(peer == sequencerNode){
            for (int i = 0; i < hosts; i++) {
                if(alive[i]){
                    sequencerNode = i;
                    break;
                }
            }
        }
    }
    
}


