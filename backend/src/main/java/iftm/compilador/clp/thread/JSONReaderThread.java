package iftm.compilador.clp.thread;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONReaderThread extends Thread {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public Communication communicationData = new Communication();

    public JSONReaderThread() {
        this.setDaemon(true);
    }

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(this::readJSON, 0, 1, TimeUnit.MILLISECONDS);
    }

    private void readJSON() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Communication newCommunication = objectMapper.readValue(new File("C:\\Users\\Rafael\\Desktop\\CLP\\TrabalhoFinalCLP\\backend\\src\\main\\java\\iftm\\compilador\\clp\\storage\\communication.json"), Communication.class);

            if(!Arrays.toString(communicationData.OUTPUT).equals(Arrays.toString(newCommunication.OUTPUT))){
                System.out.println(Arrays.toString(communicationData.OUTPUT).equals(Arrays.toString(newCommunication.OUTPUT)));
                System.out.println(Arrays.toString(newCommunication.OUTPUT));
                System.out.println(Arrays.toString(communicationData.OUTPUT));

                objectMapper.writeValue(new File("C:\\Users\\Rafael\\Desktop\\CLP\\TrabalhoFinalCLP\\backend\\src\\main\\java\\iftm\\compilador\\clp\\storage\\communication.json"), communicationData);
            }

            if(!Arrays.toString(communicationData.INPUT).equals(Arrays.toString(newCommunication.INPUT))){
                communicationData.INPUT = newCommunication.INPUT;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
