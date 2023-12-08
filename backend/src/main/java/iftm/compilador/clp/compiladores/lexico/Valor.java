package iftm.compilador.clp.compiladores.lexico;

public class Valor {

    public enum TipoValor {
        identificador,
        numero
    }

    private String texto;
    private int inteiro;
    private TipoValor tipo;

    public Valor(String texto) {
        this.texto = texto;
        this.tipo = TipoValor.identificador;
    }

    public Valor(int numero) {
        this.inteiro = numero;
        this.tipo = TipoValor.numero;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public int getInteiro() {
        return inteiro;
    }

    public void setInteiro(int inteiro) {
        this.inteiro = inteiro;
    }

    public TipoValor getTipo() {
        return tipo;
    }

    public void setTipo(TipoValor tipo) {
        this.tipo = tipo;
    }

    //toString que retorna o valor dependendo do tipo
    @Override
    public String toString() {
        if (tipo == TipoValor.identificador) {
            return texto;
        } else {
            return String.valueOf(inteiro);
        }
    }

}