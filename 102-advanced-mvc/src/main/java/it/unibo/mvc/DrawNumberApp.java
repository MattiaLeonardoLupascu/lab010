package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Application entry point for DrawNumber game.
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private int MIN = 0;
    private int MAX = 0;
    private int ATTEMPTS = 0;

    private static final File config = new File(System.getProperty("user.dir")
        + File.separator + "src" + File.separator + "main" + File.separator
        + "resources" + File.separator + "config.yml"); 

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * Constructor.
     *
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(config))){
            String line = br.readLine();
            String[] splitLine = line.split(":");
            MIN = Integer.decode(splitLine[1].trim());
            line = br.readLine();
            splitLine = line.split(":");
            MAX = Integer.decode(splitLine[1].trim());
            line = br.readLine();
            splitLine = line.split(":");
            ATTEMPTS = Integer.decode(splitLine[1].trim());
    
        } catch (Exception e) {
            IO.println(e.getMessage());
        }
        this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (final IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    @SuppressFBWarnings(
        value = "DM_EXIT",
        justification = "Acceptable for exercising purposes."
    )
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * Application entry point.
     *
     * @throws FileNotFoundException if the configuration file cannot be fetched
     */
    public static void main() throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), 
        new DrawNumberViewImpl(), 
        new PrintStreamView(System.out), 
        new PrintStreamView("log.txt"));
    }

}
