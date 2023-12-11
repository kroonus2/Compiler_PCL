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
    public String pathFile = System.getProperty("user.dir") + "\\storage\\communication.json";

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
            File fileOpened = new File(pathFile);
            Communication communicationReaded = objectMapper.readValue(fileOpened, Communication.class);

            if(!Arrays.toString(communicationData.OUTPUT).equals(Arrays.toString(communicationReaded.OUTPUT))){
                objectMapper.writeValue(fileOpened, communicationData);
            }

            if(!Arrays.toString(communicationData.INPUT).equals(Arrays.toString(communicationReaded.INPUT))){
                communicationData.INPUT = communicationReaded.INPUT;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
