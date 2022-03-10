import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Token implements Serializable, Cloneable{
        
    private List<Msg> messages;
    private int seq;
    private boolean locked;
        
    public Token() {
        this.messages = new ArrayList<>();
        this.seq = 0;
        this.locked = false;
    }
    
    public List<Msg> getMessages(){
        return messages;
    }

    public void addMessage(Msg msg){
        if(locked){
            throw new Error("error. token is locked");
        }else{
            messages.add(msg);
            incSeq();
        }
    }

    public int getSeq(){
        return seq;
    }

    private void incSeq(){
        seq++;
    }

    public boolean isLocked(){
        return locked;
    }

    public void setLock(boolean b){
        locked = b;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
