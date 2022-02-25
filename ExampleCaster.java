
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    boolean[] alive;        // array for dead/alive nodes
    int sequencerNode;      // the current sequencer node
    int seqNumb;            // the local sequence number
    List<Touple> queue;     // fifo queue of touples for reliabillity, if a sequencer dies the messages might be lost and needs to be resent

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
        seqNumb = 0;

        queue = new ArrayList<>();
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        queue.add(new Touple(id, messagetext));
        bcom.basicsend(sequencerNode, new SequencerMessage(id, messagetext));
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        if(message instanceof ISequencer){
            
            seqNumb++;

            for (int i = 0; i < hosts; i++) {
                if(i != id){
                    bcom.basicsend(i, new DataMessage(peer, ((SequencerMessage) message).msg, seqNumb));
                    mcui.debug("sequencer sending to " + i);
                }
            }
            
            //update the sequencer localy, becouse apparently a node cant send a message to itself :(
            mcui.deliver(peer, ((SequencerMessage)message).msg);

            // if the sender of a sertain message recives it, the sender can be sure it has been distrebuted
            Iterator<Touple> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Touple touple = iterator.next();
                // the id is checked since two nodes could send the same msgtext, and so one would thing the otherones message was its own when in reality the sequencer might ahve droped the message
                if(touple.sender == id && touple.msg.equals(((SequencerMessage) message).msg)){
                    iterator.remove();
                }
            }

        }else{
            //only care about relevant messages
            if(((DataMessage)message).seq > seqNumb){
                
                // if the sender of a sertain message recives it, the sender can be sure it has been distrebuted
                Iterator<Touple> iterator = queue.iterator();
                while (iterator.hasNext()) {
                    Touple touple = iterator.next();
                    // the id is checked since two nodes could send the same msgtext, and so one would thing the otherones message was its own when in reality the sequencer might ahve droped the message
                    if(touple.sender == id && touple.msg.equals(((DataMessage)message).text)){
                        iterator.remove();
                    }
                }

                this.seqNumb = ((DataMessage)message).seq;
                mcui.deliver(((DataMessage)message).getSender(), ((DataMessage)message).text);
                
                // flood the message if recived from the sequencer
                if(peer == sequencerNode){ 
                    for (int i = 0; i < hosts; i++) { 
                        if(i == id || i == sequencerNode){
                            continue;
                        }else{
                            bcom.basicsend(i, message);
                        }
                    }
                }
            }
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

        //update the local sequencer representation
        alive[peer] = false;
        if(peer == sequencerNode){
            for (int i = 0; i < hosts; i++) {
                if(alive[i]){
                    sequencerNode = i;
                    break;
                }
            }

            // some messages could be lost during the timeout, therefore check the queue and send again
            for (Touple touple : queue) {
                bcom.basicsend(sequencerNode, new SequencerMessage(id, touple.msg));
            }
        }
    }

    //used in the queue to make sure a message is broudcast
    private class Touple{
        public int sender;
        public String msg;
        Touple(int sender, String msg){
            this.sender = sender;
            this.msg = msg;
        }
    }
    
}


