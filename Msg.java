import java.io.Serializable;

public class Msg implements Serializable{

    private int sender;
    private String msg;

    public Msg(int sender, String msg){
        this.sender = sender;
        this.msg = msg;
    }

    public int getSender(){
        return sender;
    }

    public String getMsg(){
        return msg;
    }
}