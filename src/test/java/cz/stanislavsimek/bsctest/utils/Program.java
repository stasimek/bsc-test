package cz.stanislavsimek.bsctest.utils;

import cz.stanislavsimek.bsctest.Main;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

public class Program {

    private String firstArgument;
    private String secondArgument;
    private String input;
    private String err;
    private String out;
    private Thread thread;

    public Program(String firstArgument, String secondArgument) {
        this.firstArgument = firstArgument;
        this.secondArgument = secondArgument;
    }

    public Program(String firstArgument) {
        this.firstArgument = firstArgument;
    }

    public Program() {
    }

    public Thread start() throws InterruptedException {
        ConsoleErrorCapturer errorCapturer = new ConsoleErrorCapturer();
        ConsoleOutputCapturer outputCapturer = new ConsoleOutputCapturer();
        errorCapturer.start();
        outputCapturer.start();
        if (input != null) {
            System.setIn(new ByteArrayInputStream((input + System.lineSeparator()).getBytes()));
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (firstArgument == null) {
                    Main.main(new String[0]);
                } else if (secondArgument != null) {
                    Main.main(new String[]{firstArgument, secondArgument});
                } else {
                    Main.main(new String[]{firstArgument});
                }
            }
        });
        thread.start();
        TimeUnit.MILLISECONDS.sleep(1000);
        err = errorCapturer.stop();
        out = outputCapturer.stop();
        return thread;
    }

    public void stop() throws InterruptedException {
        thread.stop();
    }

    public void setInput(String command) {
        this.input = command;
    }

    public String getErr() {
        return err;
    }

    public String getOut() {
        return out;
    }

    public String getData() {
        return Main.getOutput();
    }

}
