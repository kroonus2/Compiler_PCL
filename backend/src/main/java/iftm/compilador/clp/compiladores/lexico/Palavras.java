package iftm.compilador.clp.compiladores.lexico;


public class Palavras {
    private static String[] words = {"LD", "LDN","ST","STN", "S", "R", "AND",
     "ANDN", "OR", "ORN", "XOR", "XORN","ADD", "SUB", "MUL", "DIV", "GT", "GE", "EQ",
    "NE", "LT", "LE", "JMP", "JMPC", "JMPCN", "CAL", "CALC", "CALCN", "RET", "RETC", "RETCN", 
    "CTU", "BLK", "TON", "TOFF"};    

    private static String[] hasLeftParModifier = {"AND", "OR", "XOR", "GT", "GE", "EQ", "NE", "LT", "LE"};

    public static boolean hasModifier(String str, String mod){        
        if(mod.equals("(")){
            for (String string : hasLeftParModifier) {
                if(str.toUpperCase().equals(string)){
                    return true;
                }
            }            
        }
        return false;
    }

    public static boolean compare(String str){
        for (String string : words) {
            if(str.toUpperCase().equals(string)){
                return true;
            }
        }
        return false;
    }

    public static boolean isFlowBreakInstruction(String operation){
        switch (operation) {
            case "JMP":
            case "JMPC":
            case "JMPCN":
            case "CAL":
            case "CALC":
            case "CALCN":
            case "RET":
                return true;                                
            default:
                return false;
        }
    }
}
