package iftm.compilador.clp.thread;

import java.util.Arrays;

public class Communication {
    public int[] INPUT = {0,0,0,0,0,0,0,0};
    public int[] OUTPUT = {0,0,0,0,0,0,0,0};

    @Override
    public String toString() {
        return "Communication {" +
                "INPUT=" + Arrays.toString(INPUT) +
                ", OUTPUT=" + Arrays.toString(OUTPUT);
    }
}