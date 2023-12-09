
package iftm.compilador.clp.sintatico;
import java.util.Scanner;
import iftm.compilador.clp.compiladores.lexico.Classe;
import iftm.compilador.clp.compiladores.lexico.Lexico;
import iftm.compilador.clp.compiladores.lexico.Token;

public class Sintatico {

    private Lexico lexico;
    private Token token;
    private Scanner scanner;

    public Sintatico(Lexico lexico) {
        this.lexico = lexico;
        scanner = new Scanner(System.in);         
    }

    public void analisar() {             
        token = lexico.nextToken();           
        programa();
    }

    private void programa(){
        while(token.getClasse() != Classe.cEOF){            
            if(token.getClasse() == Classe.cId){  
                if(token.getValor().getTexto().length() > 16){
                    erro("O comprimento do rotulo não deve exceder 16 caracteres.");
                }              
                token = lexico.nextToken();
                consumirToken(Classe.cDoisPontos);
            //operador    
            }else if(token.getClasse() == Classe.cPalRes){                               
                operador();
            //comentário    
            }else if(token.getClasse() == Classe.cComent){
                comentario(); 
            //parêntese direito
            }else if(token.getClasse() == Classe.cParDir){
                token = lexico.nextToken();                
            }else{
                erro("Expressao invalida.");
            }
        }                                              
    }

    private void comentario() {    
        token = lexico.nextToken();    
        while(token.getClasse() != Classe.cFComent && 
            token.getClasse() != Classe.cEOF &&
            token.getClasse() != Classe.cComent){
            token = lexico.nextToken();            
        }  
        if(token.getClasse() != Classe.cFComent){            
            erro("'*)' esperado.");
        }  
        int linhaComentario = token.getLinha();
        token = lexico.nextToken();
        if(token.getClasse() != Classe.cEOF){             
            if(token.getLinha() == linhaComentario){        
                erro("Quebra de linha esperado.");
            }          
        }
    }

    private void operador(){
        token = lexico.nextToken();
        //após o operador, o token pode ser um modificador "(" ou um operando
        if(token.getClasse() == Classe.cId || token.getClasse() == Classe.cInt){
            operando();
            
        }else if(token.getClasse() == Classe.cParEsq){
            token = lexico.nextToken();
            /* após o modificador "(", o token pode ser um dos outros 
            modificadores ou operandos*/
            if(token.getClasse() == Classe.cMod){
                token = lexico.nextToken();
                // após o modificador, o token pode ser um operando                
                if(token.getClasse() == Classe.cId){
                    operando();
                }            
            }else if(token.getClasse() == Classe.cId){                
                operando();                    
            }
        }else{            
            erro("Expressao invalida.");
        }
    }

    private void operando(){
        int linhaAtual = token.getLinha();
        // consumirToken(Classe.cId);
        token = lexico.nextToken();
        //Mais de um operando por linha (implementado somente para a função CTU, TOFF, TON)
        if(token.getClasse() == Classe.cVirg){
            token = lexico.nextToken();
            operando();
        }
        if(token.getClasse() == Classe.cComent)
            comentario();
        else if(token.getLinha() == linhaAtual){        
            erro("Quebra de linha esperado.");
        } 
    }
    
    private void consumirToken(Classe classeEsperada) {
        if (token.getClasse() == classeEsperada) {
            token = lexico.nextToken();            
        } else {
            erro(classeEsperada + " esperado.");
        }
    }

    private void erro(String mensagem) {
        System.out.println(token.getLinha() +","+ token.getColuna() + ": " + mensagem);
        System.exit(0);
    }
}
