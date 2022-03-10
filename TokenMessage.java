
import mcgui.*;
public class TokenMessage extends Message implements IToken{
        
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
