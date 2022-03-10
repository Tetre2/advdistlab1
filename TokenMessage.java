
import mcgui.*;
public class TokenMessage extends Message implements ITokenMessage{
        
    Token token;
        
    public TokenMessage(int sender, Token token) {
        super(sender);
        this.token = token;
    }

    @Override
    public Token getToken() {
        return token;
    }
    
}
