
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import mcgui.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    public volatile boolean recivedAckForNewMessage;
    public volatile boolean[] alive;
    public volatile boolean[] acks;
    public volatile int seq;

    public volatile Semaphore sem;
    public volatile List<String> queue;

    public Thread reliableMessageCaster = new Thread(){
        public void run(){
            try {
                while(true){
                    if (queue.size() > 0) {
                        mcui.debug("1");
                        for(int i=0; i < hosts; i++) {
                            /* Sends to everyone except itself */
                            if(i != id) {
                                bcom.basicsend(i, new AckMessage(id, AckTypes.AckRequest, seq));
                            }
                        }

                        sem.acquire();

                        if (allHasAcked()) {
                            seq++;
                            String messageText = queue.get(0);
                            queue.remove(0);

                            for(int i=0; i < hosts; i++) {
                                if(i != id) {
                                    bcom.basicsend(i, new DataMessage(id, messageText, seq));
                                }
                                acks[id] = false;
                            }

                            mcui.debug("Sent out: \""+messageText+"\"");
                            mcui.deliver(id, messageText, "from myself!");  
                        }


                    }else{
                        sem.acquire();
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };


    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
        alive = new boolean[hosts];
        acks = new boolean[hosts];

        for (int i = 0; i < alive.length; i++) {
            alive[i] = true;
        }

        for (int i = 0; i < acks.length; i++) {
            acks[i] = false;
        }
        
        seq = 0;
        recivedAckForNewMessage = false;
        sem = new Semaphore(0);
        queue = new ArrayList<String>();

        reliableMessageCaster.start();


        /*Thread t = new Thread() {
            public void run() {
                while(true){
                    mcui.debug("Timeout started");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mcui.debug("Timeour done!");
                    mcui.debug(Arrays.toString(alive) + " | " + Arrays.toString(acks));
                    if(sem.availablePermits() == 0){
                        sem.release();
                    }
                }
            }
        };
        t.start();*/

    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        queue.add(messagetext);
        if(sem.availablePermits() == 0){
            sem.release();
        }
        mcui.debug("Cast message");
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {
        if(message instanceof IAck){

            if(((AckMessage) message).types == AckTypes.Ack){
                if(((AckMessage) message).seq >= seq){
                    acks[peer] = true;
                    mcui.debug("Ack recived");


                    mcui.debug(Arrays.toString(alive) + " | " + Arrays.toString(acks));

                    if(allHasAcked()){
                        if(sem.availablePermits() == 0){
                            sem.release();
                        }
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

    private boolean allHasAcked(){
        for (int node = 0; node < hosts; node++) {
            if(node == id){
                continue;
            }
            if(alive[node] && !acks[node]){ //if node is alive and has not acked, return false
                return false;
            }
        }
        mcui.debug(Arrays.toString(alive) + " | " + Arrays.toString(acks));
        return true;
    }

    
}


