
import java.util.ArrayList;
import java.util.Iterator;
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
    int sequencerNode;      // the current sequencer node
    int seqNumb;            // the local sequence number
    Queue<Touple> queue;     // fifo queue of touples for reliabillity, if a sequencer dies the messages might be lost and needs to be resent
    int[] otherSeqs;

    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
        alive = new boolean[hosts];
        otherSeqs = new int[hosts];

        for (int i = 0; i < alive.length; i++) {
            alive[i] = true;
        }

        for (int i = 0; i < otherSeqs.length; i++) {
            otherSeqs[i] = -1;
        }

        sequencerNode = 0;
        seqNumb = 0;

        queue = new LinkedList<>();
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        queue.add(new Touple(id, messagetext));
        if(sequencerNode != -1){
            bcom.basicsend(sequencerNode, new MessageToSequencer(id, messagetext));
        }
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        if(message instanceof IMessageToSequencer){
            mcui.debug("recived sequencerMessage");
            reciveSequencerMessage(peer, (MessageToSequencer) message);

        }else if(message instanceof IMsg){
            mcui.debug("recived IMsg");
            reciveDataMessages(peer, (Msg) message);

        }else if(message instanceof IUpdateNewSequencer){
            mcui.debug("recived updateNewSequencer");
            UpdateNewSequnecer m = (UpdateNewSequnecer) message;
            sequencerNode = -1;
            alive[m.deadNode] = false;

            if(m.newSequnecer != id){ //if new seqcuenser is this node, dont send to self
                bcom.basicsend(m.newSequnecer, new SendSeq(id, seqNumb)); 
            }

        }else if(message instanceof ISendSeq){
            mcui.debug("recived SendSeq");
            SendSeq m = (SendSeq) message;
            otherSeqs[peer] = m.seq;

            int responses = 0;
            int maxSeq = seqNumb;
            for (int i = 0; i < alive.length; i++) {
                if(alive[i] && otherSeqs[i] > -1){
                    responses++;
                    if(maxSeq < otherSeqs[i]){ // get the max seq from the other nodes
                        maxSeq = otherSeqs[i];
                    }
                }
            }

            if(responses == getAliveNodes() - 1){
                sequencerNode = id;
                for (int i = 0; i < alive.length; i++) {
                    if(alive[i] && i != id){
                        bcom.basicsend(i, new SetNewSequencer(id, id));
                    }
                }
            }

        }else if(message instanceof ISetNewSequencer){
            mcui.debug("recived setNewSequencer");
            SetNewSequencer m = (SetNewSequencer) message;
            sequencerNode = m.newSequencer;
            
            // some messages could be lost during the timeout, therefore check the queue and send again
            for (Touple touple : queue) {
                if(sequencerNode != -1){
                    bcom.basicsend(sequencerNode, new MessageToSequencer(id, touple.msg));
                }
            }
        }
        
        mcui.debug("seq: " + seqNumb + " | sequencer:" + sequencerNode + "");
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
            sequencerNode = -1;
            int newSequnecerNode = -1;
            for (int i = 0; i < hosts; i++) {
                if(alive[i]){
                    newSequnecerNode = i;
                    break;
                }
            }

            for (int i = 0; i < alive.length; i++) {
                if(alive[i] && i != id ){
                    bcom.basicsend(i, new UpdateNewSequnecer(id, peer, newSequnecerNode));
                }
            }

            //since we cant send messages to ourselves, send seq to newSequencer here.
            bcom.basicsend(newSequnecerNode, new SendSeq(id, seqNumb));
        }
    }

    private void reciveDataMessages(int peer, Msg message){
        //only care about relevant messages
        if(((Msg)message).seq > seqNumb){
                
            // if the sender of a sertain message recives it, the sender can be sure it has been distrebuted
            Iterator<Touple> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Touple touple = iterator.next();
                // the id is checked since two nodes could send the same msgtext, and so one would thing the otherones message was its own when in reality the sequencer might ahve droped the message
                if(touple.sender == id && touple.msg.equals(message.text)){
                    iterator.remove();
                }
            }

            this.seqNumb = message.seq;
            mcui.deliver(message.getSender(), message.text);
            
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

    private void reciveSequencerMessage(int peer, MessageToSequencer message){
  
        seqNumb++;

        for (int i = 0; i < hosts; i++) {
            if(i != id && alive[i]){
                bcom.basicsend(i, new Msg(peer, message.msg, seqNumb));
                mcui.debug("sequencer sending to " + i);
            }
        }
        
        //update the sequencer localy, becouse apparently a node cant send a message to itself :(
        mcui.deliver(peer, message.msg);

        // if the sender of a sertain message recives it, the sender can be sure it has been distrebuted
        Iterator<Touple> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Touple touple = iterator.next();
            // the id is checked since two nodes could send the same msgtext, and so one would thing the otherones message was its own when in reality the sequencer might ahve droped the message
            if(touple.sender == id && touple.msg.equals( message.msg)){
                iterator.remove();
            }
        }

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


