package iftm.compilador.clp.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.sound.sampled.Port;

import iftm.compilador.clp.compiladores.lexico.Classe;
import iftm.compilador.clp.compiladores.lexico.Palavras;
import iftm.compilador.clp.compiladores.lexico.Token;
import iftm.compilador.clp.compiladores.lexico.Tokens;
import iftm.compilador.clp.thread.JSONReaderThread;

public class Semantico {
    private Token token;
    private int i = 0;
    private Scanner scanner;
    JSONReaderThread jsonReader = new JSONReaderThread();

    // estruturas de dados
    public static List<Integer> labels = new ArrayList<Integer>();
    public static List<Token> stack = new ArrayList<Token>();
    public static List<Integer> ac_temp = new ArrayList<Integer>();
    public static List<Integer> callStack = new ArrayList<Integer>();
    Map<String, Runnable> operations = new HashMap<>();

    // contadores de parênteses
    int cParEsq_counter = 0;
    int cParDir_counter = 0;

    // registradores de entrada, saída e memória
    private int[] input_reg = { 0, 0, 0, 0, 0, 0, 0, 0 }; // I8, I7, I6, ...
    private int[] previous_input_reg = {0, 0, 0, 0, 0, 0, 0, 0}; // Estados anteriores das entradas
    private int[] output_reg = { 0, 0, 0, 0, 0, 0, 0, 0 }; // Q8, Q7, Q6, ...
    private int[] previous_output_reg = { 0, 0, 0, 0, 0, 0, 0, 0 };
    private int[] memory_reg = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // M0, M1, M2, ...
    private int[] counters = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};;
    private int[] previous_accumulator_counters = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};;
    private int[] timers = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // T0,
                                                                                                                         // T1,
                                                                                                                         // T2,
                                                                                                                         // ...

    private int[] counters_preset = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};;
    private int[] counters_output = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};;

    private int[] timers_preset = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // T0.PS, T1.PS, T2.PS, ...
    private int[] timers_output = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};// T0.Q, T1.Q, T2.Q, ...

    private int accumulator = 0;

    // Constantes
    private static final int TYPE_INPUT = 0;
    private static final int TYPE_OUTPUT = 1;
    private static final int TYPE_MEMORY = 2;
    private static final int TYPE_INTEGER = 3;
    private static final int TYPE_COUNTER = 4;
    private static final int TYPE_COUNTER_PRESET = 5;
    private static final int TYPE_COUNTER_OUTPUT = 6;
    private static final int TYPE_TIMER = 7;
    private static final int TYPE_TIMER_PRESET = 8;
    private static final int TYPE_TIMER_OUTPUT = 9;

    // utilizado para processar os identificadores
    private int type; // 0 = entrada, 1 = saída, 2 = memória
    private int port;
    private int value;

    public Semantico() {
        jsonReader.run();
        scanner = new Scanner(System.in);
        removeComments();
        processLabels();
        mapInstructions();
    }

    public void run() throws InterruptedException {
        while (true) {
            i = 0; // Indice que percorre a lista de Tokens
            while (i < Tokens.size()) {
                token = Tokens.get(i);
                if (token.getClasse() == Classe.cPalRes) {
                    instruction();
                } else if (token.getClasse() == Classe.cParDir) {
                    runStack();
                }
                i++;
            }
            // verifica a quantidade de parênteses do programa
            if (cParEsq_counter != 0 || cParDir_counter != 0) {
                error("Expressao invalida: abertura ou fechamento de parentese esperado.");
            }

            Thread.sleep(1);
        }
    }

    private void mapInstructions() {
        // Mapeamento de palavras reservadas para funções
        operations.put("LD", () -> load(false));
        operations.put("LDN", () -> load(true));
        operations.put("AND", () -> and(false));
        operations.put("ANDN", () -> and(true));
        operations.put("ST", () -> store(false));
        operations.put("STN", () -> store(true));
        operations.put("OR", () -> or(false));
        operations.put("ORN", () -> or(true));
        operations.put("XOR", () -> xor(false));
        operations.put("XORN", () -> xor(true));
        operations.put("GT", () -> calculateLogicAndArithmetic(0));
        operations.put("EQ", () -> calculateLogicAndArithmetic(1));
        operations.put("LT", () -> calculateLogicAndArithmetic(2));
        operations.put("GE", () -> calculateLogicAndArithmetic(3));
        operations.put("LE", () -> calculateLogicAndArithmetic(4));
        operations.put("NE", () -> calculateLogicAndArithmetic(5));
        operations.put("ADD", () -> calculateLogicAndArithmetic(6));
        operations.put("SUB", () -> calculateLogicAndArithmetic(7));
        operations.put("MUL", () -> calculateLogicAndArithmetic(8));
        operations.put("DIV", () -> calculateLogicAndArithmetic(9));
        operations.put("JMP", () -> jumpTo("JMP"));
        operations.put("JMPC", () -> jumpTo("JMPC"));
        operations.put("JMPCN", () -> jumpTo("JMPCN"));
        operations.put("CAL", () -> call("CAL"));
        operations.put("CALC", () -> call("CALC"));
        operations.put("CALCN", () -> call("CALCN"));
        operations.put("S", this::set);
        operations.put("R", this::reset);
        operations.put("RET", this::ret);
        operations.put("CTU", this::ctu);
        operations.put("CTD", this::ctd);
        operations.put("TON", this::ton);
        operations.put("TOFF", this::toff);
        operations.put("BLK", this::blk);
    }

    private void instruction() {
        for (String operation : operations.keySet()) {
            if (checkWord(token, operation)) {
                if (token.getValor().getTexto().equals("TON") || token.getValor().getTexto().equals("TOFF")) {
                    int x = i;
                    Token tokenCId = Tokens.get(++x);
                    if (tokenCId.getClasse() == Classe.cId) {
                        token = Tokens.get(++x);
                        if (token.getClasse() == Classe.cVirg) {
                            token = Tokens.get(++x);
                            if (token.getClasse() == Classe.cInt) {
                                int timer_preset_index = Integer
                                        .parseInt(tokenCId.getValor().getTexto().replaceAll("\\D", ""));
                                timers_preset[timer_preset_index-1] = token.getValor().getInteiro()/2;
                            }
                        }
                    }
                }

                else if (token.getValor().getTexto().equals("CTU") || token.getValor().getTexto().equals("CTD")) {
                    int x = i;
                    Token tokenCId = Tokens.get(++x);
                    if (tokenCId.getClasse() == Classe.cId) {
                        token = Tokens.get(++x);
                        if (token.getClasse() == Classe.cVirg) {
                            token = Tokens.get(++x);
                            if (token.getClasse() == Classe.cInt) {
                                int counter_preset_index = Integer
                                        .parseInt(tokenCId.getValor().getTexto().replaceAll("\\D", ""));
                                counters_preset[counter_preset_index-1] = token.getValor().getInteiro();
                            }
                        }
                    }
                }

                token = Tokens.get(++i);

                // expressão JMP, CAL ou RET
                if (Palavras.isFlowBreakInstruction(operation)) {
                    operations.get(operation).run();

                    // Expressão comum
                } else if (token.getClasse() == Classe.cId || token.getClasse() == Classe.cInt) {
                    processId(token);
                    operations.get(operation).run();

                    // nova expressão adiada
                } else if (token.getClasse() == Classe.cParEsq) {
                    cParEsq_counter++;

                    if (!Palavras.hasModifier(Tokens.get(i - 1).getValor().getTexto(), "(")) {
                        error("Instrucao nao permite o modificador '('.");
                    }

                    stack.add(Tokens.get(i - 1));
                    ac_temp.add(accumulator);
                    token = Tokens.get(++i);

                    if (token.getClasse() == Classe.cMod) {
                        token = Tokens.get(++i);
                        processId(token);
                        operations.get("LDN").run();

                    } else if (token.getClasse() == Classe.cId) {
                        processId(token);
                        operations.get("LD").run();

                    } else {
                        // Decrementa o índice i para o programa seguir para a próxima linha
                        i--;
                    }
                }
                break;
            }
        }
    }

    // executa a pilha de instrução adiada
    private void runStack() {
        cParDir_counter++;
        if (cParEsq_counter == cParDir_counter) {
            Token token;
            int j = stack.size() - 1;
            int operated;
            String operation;

            cParDir_counter = cParEsq_counter = 0;
            while (!stack.isEmpty()) {
                token = stack.remove(j--);
                operated = ac_temp.remove(ac_temp.size() - 1);
                operation = token.getValor().getTexto();
                switch (operation) {
                    case "OR":
                        accumulator = accumulator | operated;
                        break;
                    case "XOR":
                        accumulator = accumulator ^ operated;
                        break;
                    case "AND":
                        accumulator = accumulator & operated;
                        break;
                    case "GT":
                        accumulator = operated > accumulator ? 1 : 0;
                        break;
                    case "GE":
                        accumulator = operated >= accumulator ? 1 : 0;
                        break;
                    case "EQ":
                        accumulator = operated == accumulator ? 1 : 0;
                        break;
                    case "NE":
                        accumulator = operated != accumulator ? 1 : 0;
                        break;
                    case "LT":
                        accumulator = operated < accumulator ? 1 : 0;
                        break;
                    case "LE":
                        accumulator = operated <= accumulator ? 1 : 0;
                        break;
                }
            }
        }

    }

    // implementa a instrução R
    private void reset() {
        int[] register = getRegisterByType(type);
        if (register != null && accumulator >= 1) {
            register[port] = 0;
        }
    }

    // implementa a instrução S
    private void set() {
        int[] register = getRegisterByType(type);
        if (register != null && accumulator >= 1) {
            register[port] = 1;
        }
    }

    private void xor(boolean inverter) {
        int[] register = getRegisterByType(type);
        if (register != null) {
            int operated = register[port];
            if (inverter)
                operated = ~operated + 2;
            accumulator = accumulator ^ operated;
        }
    }

    private void or(boolean inverter) {
        int[] register = getRegisterByType(type);
        if (register != null) {
            int operando = register[port];

            if (inverter)
                operando = ~operando + 2;
            accumulator = operando | accumulator;

        }
    }

    private void and(boolean inverter) {
        int[] register = getRegisterByType(type);
        if (register != null) {
            int operando = register[port];

            if (inverter)
                operando = ~operando + 2;
            accumulator = operando & accumulator;

        }
    }

    // implementa a instrução ST
    private void store(boolean inverter) {
        int[] register = getRegisterByType(type);
        if (register != null) {
            if (inverter) {
                accumulator = ~accumulator + 2;
            }
            register[port] = accumulator;
        }
    }

    // implementa a instrução LD
    private void load(boolean inverter) {
        int[] register = getRegisterByType(type);
        if (register != null) {
            accumulator = register[port];
            if (inverter) {
                accumulator = ~accumulator + 2;
            }
        } else {
            accumulator = value;
        }
    }

    // implementa a instrução JMP
    private void jumpTo(String command) {
        if (command.equals("JMP") ||
                (command.equals("JMPC") && accumulator >= 1) ||
                (command.equals("JMPCN") && accumulator == 0)) {
            for (int indice : labels) {
                if (token.getValor().getTexto().equals(Tokens.get(indice).getValor().getTexto())) {
                    // vai para o label especificado
                    i = indice;
                    return;
                }
            }
            error("Label nao encontrado.");
        }
    }

    // implementa a instrução CAL
    private void call(String command) {
        if (command.equals("CAL") ||
                (command.equals("CALC") && accumulator >= 1) ||
                (command.equals("CALCN") && accumulator == 0)) {
            for (int indice : labels) {
                if (token.getValor().getTexto().equals(Tokens.get(indice).getValor().getTexto())) {
                    // salva o ponto de retorno
                    callStack.add(i);
                    // vai para o label especificado
                    i = indice;
                    return;
                }
            }
            error("Label nao encontrado.");
        }
    }

    private void ret() {
        if (callStack.size() > 0) {
            i = callStack.remove(callStack.size() - 1);
            if (token.getClasse() == Classe.cInt
                    || (token.getClasse() == Classe.cId && !token.getValor().getTexto().equals("NULL"))) {
                processId(token);
                int[] register = getRegisterByType(type);
                if (register != null) {
                    accumulator = register[port];
                } else {
                    accumulator = value;
                }
            }
        } else
            error("Não ha ponto de retorno.");
    }

    // Função CTU (Contador Crescente)
    private void ctu() {
        int[] register = getRegisterByType(type);
        if (register != null && type == TYPE_COUNTER) {
            if (accumulator >= 1) {
                // System.out.println(register[port]);
                if (previous_accumulator_counters[port] != accumulator) { // Verifica se houve mudança no accumulator
                    previous_accumulator_counters[port] = accumulator;
                    register[port] += 1;
                }
                
                if (register[port] >= counters_preset[port]) { // Verifica se atingiu o valor de preset
                    counters_output[port] = 1; // Define a saída do contador como 1
                } else {
                    counters_output[port] = 0;
                }
            }
            else{
                previous_accumulator_counters[port] = accumulator;
                if (register[port] >= counters_preset[port]) { // Verifica se atingiu o valor de preset
                    counters_output[port] = 1; // Define a saída do contador como 1
                } else {
                    counters_output[port] = 0;
                }
            }
        } else {
            error("Expressao invalida.");
        }
    }

    // Função CTD (Contador Decrescente)
    private void ctd() {
        int[] register = getRegisterByType(type);
        if (register != null && type == TYPE_COUNTER) {
            if (accumulator >= 1) {
                
                if (previous_accumulator_counters[port] != accumulator) {
                    previous_accumulator_counters[port] = accumulator;
                    register[port] -= 1;
                }
                
                if (register[port] + counters_preset[port] <= 0) { 
                    counters_output[port] = 1; 
                } else {
                    counters_output[port] = 0;
                }
            }
            else{
                previous_accumulator_counters[port] = accumulator;
                if (register[port] + counters_preset[port] <= 0) { 
                    counters_output[port] = 1; 
                } else {
                    counters_output[port] = 0;
                }
            }
        } else {
            error("Expressao invalida.");
        }
    }


    private void ton() {
        int[] register = getRegisterByType(type);
        if (register != null && type == TYPE_TIMER) {
            if (accumulator >= 1) {
                register[port] += 1;
                if (register[port] == timers_preset[port]) {
                    timers_output[port] = 1;
                }
                accumulator = register[port];
            } else {
                register[port] = 0;
                timers_output[port] = 0;
            }
        } else {
            error("Expressao invalida.");
        }
    }

    private void toff() {
        int[] register = getRegisterByType(type);
        if (register != null && type == TYPE_TIMER) {
            if (accumulator >= 1) {
                register[port] += 1;
                if (register[port] <= timers_preset[port]) {
                    timers_output[port] = 1;
                } else {
                    timers_output[port] = 0;
                }
                accumulator = register[port];
            } else {
                register[port] = 0;
                timers_output[port] = 0;
            }
        } else {
            error("Expressao invalida.");
        }
    }

    private void blk() {
        int[] register = getRegisterByType(type);
        if (register != null && type == TYPE_COUNTER) {
            register[port] = 0;
        } else {
            error("Expressao invalida.");
        }
    }

    private void calculateLogicAndArithmetic(int comparisonType) {
        int[] register = getRegisterByType(type);
        int operated;

        if (register != null) {
            operated = register[port];
        } else {
            operated = value;
        }

        switch (comparisonType) {
            case 0:
                accumulator = accumulator > operated ? 1 : 0;
                break;
            case 1:
                accumulator = accumulator == operated ? 1 : 0;
                break;
            case 2:
                accumulator = accumulator < operated ? 1 : 0;
                break;
            case 3:
                accumulator = accumulator >= operated ? 1 : 0;
                break;
            case 4:
                accumulator = accumulator <= operated ? 1 : 0;
                break;
            case 5:
                accumulator = accumulator != operated ? 1 : 0;
                break;
            case 6:
                accumulator = accumulator + operated;
                break;
            case 7:
                accumulator = accumulator - operated;
                break;
            case 8:
                accumulator = accumulator * operated;
                break;
            case 9:
                try {
                    accumulator = accumulator / operated;
                } catch (ArithmeticException e) {
                    error("Divisao por zero.");
                }
                break;
            default:
                break;
        }
    }

    private int[] getRegisterByType(int type) {
        switch (type) {
            case TYPE_INPUT:
                return jsonReader.communicationData.INPUT;
            case TYPE_OUTPUT:
                return jsonReader.communicationData.OUTPUT;
            case TYPE_MEMORY:
                return memory_reg;
            case TYPE_COUNTER:
                return counters;
            case TYPE_COUNTER_PRESET:
                return counters_preset;
            case TYPE_COUNTER_OUTPUT:
                return counters_output;
            case TYPE_TIMER:
                return timers;
            case TYPE_TIMER_PRESET:
                return timers_preset;
            case TYPE_TIMER_OUTPUT:
                return timers_output;
            default:
                return null;
        }
    }

    private void processId(Token token) {
        String id;
        String num = "";
        port = 0;

        if (token.getClasse() == Classe.cInt) {
            value = token.getValor().getInteiro();
            type = TYPE_INTEGER;
        } else {
            id = token.getValor().getTexto();

            for (int j = 0; j < id.length(); j++) {
                if (j == 0) {
                    if (id.charAt(0) == 'I') {
                        type = TYPE_INPUT;
                    } else if (id.charAt(0) == 'Q') {
                        type = TYPE_OUTPUT;
                    } else if (id.charAt(0) == 'M') {
                        type = TYPE_MEMORY;
                    } else if (id.charAt(0) == 'C' && id.charAt(id.length() - 1) == 'Q') {
                        type = TYPE_COUNTER_OUTPUT;
                    } else if (id.charAt(0) == 'C') {
                        type = TYPE_COUNTER;
                    } else if (id.charAt(0) == 'T' && id.charAt(id.length() - 1) == 'Q') {
                        type = TYPE_TIMER_OUTPUT;
                    } else if (id.charAt(0) == 'T') {
                        type = TYPE_TIMER;
                    } else {
                        error("Operando invalido.");
                    }
                } else {
                    // forma a parte numérica do identificador
                    num += id.charAt(j);
                }
            }
            try {
                port = Integer.parseInt(num.replaceAll("[^0-9]", "")) - 1;
                int maxLength = 0;
                if (type == TYPE_INPUT || type == TYPE_OUTPUT) {
                    maxLength = jsonReader.communicationData.INPUT.length - 1;
                } else {
                    maxLength = memory_reg.length - 1;
                }

                if (port == -1 || port > maxLength) {
                    error("Porta '" + num + "' invalida.");
                }
            } catch (NumberFormatException e) {
                System.out.println(e);
                error("Porta '" + num + "' invalida.");
            }
        }
    }

    private void processLabels() {
        int j = 0;
        while (j < Tokens.size() - 1) {
            if (Tokens.get(j).getClasse() == Classe.cId && Tokens.get(j + 1).getClasse() == Classe.cDoisPontos) {
                if (checkLabel(Tokens.get(j))) {
                    labels.add(j);
                } else {
                    token = Tokens.get(j);
                    error("Rotulo duplicado.");
                }
            }
            j++;
        }
    }

    // verifica se há rótulos duplicados
    private boolean checkLabel(Token tk) {
        // percorre cada índice na lista de rótulos e compara com o valor do token atual
        for (int indice : labels) {
            if (tk.getValor().getTexto().equals(Tokens.get(indice).getValor().getTexto())) {
                return false;
            }
        }
        return true;
    }

    // remove os comentários da lista de tokens
    private void removeComments() {
        int j = 0;
        while (j < Tokens.size()) {
            if (Tokens.get(j).getClasse() == Classe.cComent) {
                while (Tokens.get(j).getClasse() != Classe.cFComent) {
                    Tokens.remove(j);
                }
                Tokens.remove(j);
            }
            j++;
        }
    }

    private boolean checkWord(Token token, String palavra) {
        return token.getClasse() == Classe.cPalRes && token.getValor().getTexto().equals(palavra);
    }

    private void error(String mensagem) {
        System.out.println(token.getLinha() + "," + token.getColuna() + ": " + mensagem);
        System.exit(0);
    }
}
