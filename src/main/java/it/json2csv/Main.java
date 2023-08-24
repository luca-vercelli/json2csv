package it.json2csv;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
    
    public static final String PROGRAM_NAME = "json2csv";
    public static final String VERSION = "1.0";

	public static void main(String[] args) {
		Options cliArgs  = new Options();
		JCommander jCommander = JCommander.newBuilder()
			.addObject(cliArgs)
			.build();
        jCommander.setProgramName(PROGRAM_NAME);

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
            System.err.println(PROGRAM_NAME + " version " + VERSION);
            System.exit(0);
        } else {
            Converter convert = new Converter(cliArgs);
            convert.run();
            System.exit(convert.getRc());
        }
	}
}
