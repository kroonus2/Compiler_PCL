package compiladores.lexico;

import java.io.BufferedReader;
import java.io.IOException;

public class Lexico {

    private BufferedReader br;
    private char caractere;
    private int linha;
    private int coluna; 
    private char buffer;       
    
    public Lexico(BufferedReader br) {
        this.br = br;
        linha = 1;
        coluna = 0;
        buffer = ' ';
        caractere = nextChar();
    }

    
    public Token nextToken() {
        StringBuilder lexema = new StringBuilder();
        Token token = null;
        
        //verifica o buffer
        if(buffer != ' '){ 
            if(buffer != '(')
                token = new Token(Classe.cMod, linha, coluna);           
            else
                token = new Token(Classe.cParEsq, linha, coluna); 
            token.setValor(new Valor(buffer+""));            
            buffer = ' ';                       
            Tokens.insert(token);
            return token;
        }
        while (caractere != 65535) { // 65535 fim da stream
            if (Character.isLetter(caractere)) {                
                token = processIdentifier(lexema);
            } else if (Character.isDigit(caractere)) {
                token = processNumber(lexema);
            } else if (Character.isWhitespace(caractere)) {
                processWhitespace();
            } else {
                token = processSymbol(lexema);                
            }
            if (token != null) {
                Tokens.insert(token);
                return token;
            }
        }
        return new Token(Classe.cEOF, linha, coluna - 1);
    }

    private Token processIdentifier(StringBuilder lexema) {
        Token token = new Token(Classe.cId, linha, coluna);
        while (Character.isLetter(caractere) || Character.isDigit(caractere) || caractere == '_') {
            lexema.append(caractere);
            caractere = nextChar();
        }
        if (Palavras.compare(lexema.toString())) {
            token.setClasse(Classe.cPalRes);
        //modificador N após o início de uma expressão adiada '('
        }else if(lexema.toString().equals("N")){
            token.setClasse(Classe.cMod);
        }
        token.setValor(new Valor(lexema.toString()));
        return token;
    }

    private Token processNumber(StringBuilder lexema) {
        Token token = new Token(Classe.cInt, linha, coluna);
        while (Character.isDigit(caractere)) {
            lexema.append(caractere);
            caractere = nextChar();
        }
        token.setValor(new Valor(Integer.parseInt(lexema.toString())));
        return token;
    }

    private void processWhitespace() {
        if (caractere == '\n') {
            linha++;
            coluna = 0;
        }
        caractere = nextChar();
    }

    private Token processSymbol(StringBuilder lexema) {
        Token token = null;        
        
        switch (caractere) {
            case '(':
                token = new Token(Classe.cParEsq, linha, coluna);
                buffer = nextChar();
                if (buffer == '*') {
                    token.setClasse(Classe.cComent);  
                    lexema.append(caractere);
                    caractere = buffer; 
                    buffer = ' ';                   
                }
                break;
            case ')':
                token = new Token(Classe.cParDir, linha, coluna);                
                break;
            case ',':
                token = new Token(Classe.cVirg, linha, coluna);
                break;            
            case '.':
                token = new Token(Classe.cPonto, linha, coluna);
                break;
            case '+':
                token = new Token(Classe.cAdicao, linha, coluna);
                break;
            case '-':
                token = new Token(Classe.cSubtracao, linha, coluna);
                break;
            case '*':
                token = new Token(Classe.cMultiplicacao, linha, coluna);
                buffer = nextChar();
                if (buffer == ')') {
                    token.setClasse(Classe.cFComent);  
                    lexema.append(caractere);
                    caractere = buffer;
                    buffer = ' ';
                }
                break;
            case '/':
                token = new Token(Classe.cDivisao, linha, coluna);
                break;
            case ':':
                token = new Token(Classe.cDoisPontos, linha, coluna);                
                break;
            case '<':
                token = new Token(Classe.cMenor, linha, coluna);
                buffer = nextChar();
                if (buffer == '=') {
                    token.setClasse(Classe.cMenorIgual);
                    lexema.append(caractere);
                    caractere = buffer;
                    buffer = ' ';
                } else if (buffer == '>') {
                    token.setClasse(Classe.cDiferente);
                    lexema.append(caractere);
                    caractere = buffer;
                    buffer = ' ';
                }
                break;
            case '>':
                token = new Token(Classe.cMaior, linha, coluna);
                buffer = nextChar();
                if (buffer == '=') {
                    token.setClasse(Classe.cMaiorIgual);
                    lexema.append(caractere);
                    caractere = buffer;
                    buffer = ' ';
                }
                break;
            case '=':
                token = new Token(Classe.cIgual, linha, coluna);
                break; 
            case '_':
                token = new Token(Classe.cUnder, linha, coluna);
                break; 
            default:                
                caractere = nextChar();
                break;
        }        
        if (token != null) {
            setValor(token, lexema);
        }        
        return token;
    }

    // private Token processString(StringBuilder lexema) {
    //     Token token = new Token(Classe.cString, linha, coluna);
    //     caractere = nextChar();        
    //     setValor(token, lexema);
        
    //     while (caractere != '\'' && caractere != 65535) {            
    //         setValor(token, lexema);            
    //     }                
    //     return token;
    // }

    private char nextChar() {
        try {
            coluna++;            
            return (char) br.read();
        } catch (IOException e) {
            return ' ';
        }
    }   

    private void setValor(Token token, StringBuilder lexema){
        lexema.append(caractere);
        token.setValor(new Valor(lexema.toString()));
        caractere = nextChar();
    }

}