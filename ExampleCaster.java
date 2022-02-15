
import java.util.concurrent.Semaphore;

import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    boolean recivedAckForNewMessage;
    int acks;
    int seq;
    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
        acks = 0;
        seq = 0;
        recivedAckForNewMessage = false;
    }
        
    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i, new AckMessage(id, AckTypes.AckRequest, seq));
            }
        }
        seq++;

        

        for(int i=0; i < hosts; i++) {
            if(i != id) {
                bcom.basicsend(i, new DataMessage(id, messagetext, seq));
                acks = 0;
            }
        }

        mcui.debug("Sent out: \""+messagetext+"\"");
        mcui.deliver(id, messagetext, "from myself!");  
    
        //cast(messagetext);
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        if(message instanceof IAck){

            if(((AckMessage) message).types == AckTypes.Ack){
                if(((AckMessage) message).seq >= seq){
                    acks++;
                    mcui.debug("Ack recived");

                    if(acks == hosts){
                        
                    }

                }
            }else if (((AckMessage) message).types == AckTypes.AckRequest) {
                if(((AckMessage) message).seq >= seq){
                    if(!recivedAckForNewMessage){
                        bcom.basicsend(peer, new AckMessage(id, AckTypes.Ack, seq));
                        recivedAckForNewMessage = true;
                        mcui.debug("Ack req revcived");
                    }
                }
            }
        }else{
            recivedAckForNewMessage = false;
            
            mcui.deliver(peer, ((DataMessage)message).text);
            mcui.debug("msg diliverd");
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
    }
}
