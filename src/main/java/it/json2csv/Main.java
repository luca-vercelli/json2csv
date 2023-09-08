package it.json2csv;

import java.io.IOException;
import java.util.Properties;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {

    public static void main(String[] args) {
        Properties prop = loadProperties();
        final String NAME = prop.getProperty("NAME");
        final String VERSION = prop.getProperty("VERSION"); 

        Options cliArgs = new Options();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(cliArgs)
                .build();
        jCommander.setProgramName(NAME);

        try {
            jCommander.parse(args);
        } catch (ParameterException exception) {
            System.err.println(exception.getMessage());
            jCommander.usage();
            System.exit(-1);
        }

        if (cliArgs.isHelp()) {
            jCommander.usage();
            System.exit(0);
        } else if (cliArgs.isVersion()) {
            System.err.println(NAME + " version " + VERSION);
            System.exit(0);
        } else {
            Converter convert = new Converter(cliArgs);
            convert.run();
            System.exit(convert.getRc());
        }
    }

    public static Properties loadProperties() {
        Properties prop = new Properties();
        try {
            prop.load(Main.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException ex) {
            System.err.println("WARN: application.properties not found or not readable");
        }
        return prop;
    }
}
