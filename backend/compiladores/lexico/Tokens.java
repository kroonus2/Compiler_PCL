package compiladores.lexico;

import java.util.ArrayList;
import java.util.List;

public class Tokens {
    public static List<Token> tokens = new ArrayList<Token>();
    
    public static void insert(Token token){
        tokens.add(token);
    }

    public static Token get(int index){
        return tokens.get(index);
    }

    public static int size(){
        return tokens.size();        
    }

    public static void remove(int index){
        tokens.remove(index);
    }
}
