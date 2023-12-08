package iftm.compilador.clp.compiladores.lexico;

public class Token {

    private Classe classe;
    private Valor valor;
    private int linha;
    private int coluna;

    public Token(){
        
    }

    public Token(Classe c, int lin, int col){
        this.setClasse(c);
        this.setLinha(lin);
        this.setColuna(col);
    }

    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public Valor getValor() {
        return valor;
    }

    public void setValor(Valor valor) {
        this.valor = valor;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    @Override
    public String toString() {
        return "Classe: " + classe + "\nValor: " + valor + "\nPosicao: " + linha + ", " + coluna;
    }

}
