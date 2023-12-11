package iftm.compilador.clp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import iftm.compilador.clp.compiladores.lexico.Lexico;
import iftm.compilador.clp.semantico.Semantico;
import iftm.compilador.clp.sintatico.Sintatico;

public class App {

    public static void main(String[] args) throws Exception {
        String nomeArquivo;

        if (args.length == 0) {
            // Instruções 
			System.out.println("Modo de usar: java -jar NomePrograma NomeArquivoCodigo");
			nomeArquivo = "teste.pas";
		} else {
            nomeArquivo = args[0];
        }        

        // Abrindo o arquivo 
        Path caminhoArquivoAux = Paths.get(nomeArquivo);
        int numeroEspacosPorTab = 4;
        StringBuilder juntando = new StringBuilder();
        String espacos;
        // remove as tabulações 
        for (int cont = 0; cont < numeroEspacosPorTab; cont++) {
            juntando.append(" ");
        }
        espacos = juntando.toString();        

        String conteudo;
        try {
            conteudo = new String(Files.readAllBytes(caminhoArquivoAux), StandardCharsets.UTF_8);
            conteudo = conteudo.replace("\t", espacos);
            Files.write(caminhoArquivoAux, conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

		String caminhoArquivo = Paths.get(nomeArquivo).toAbsolutePath().toString();

        Lexico lexico;
        
		try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            
            lexico = new Lexico(br);

            Sintatico sintatico = new Sintatico(lexico);
            sintatico.analisar();   
            
            Semantico semantico = new Semantico();     
            semantico.run();   

		} catch (IOException e) {
			System.err.println("Não foi possível abrir o arquivo ou ler do arquivo: " + nomeArquivo);
			e.printStackTrace();
		}

    }
}