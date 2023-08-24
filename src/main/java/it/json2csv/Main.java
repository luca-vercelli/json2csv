package it.json2csv;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Main {
    
	public static void main(String[] args) {
		Options cliArgs  = new Options();
		JCommander jCommander = JCommander.newBuilder()
			.addObject(cliArgs)
			.build();
        try {
            jCommander.parse(args);
        } catch (ParameterException exception) {
            System.err.println(exception.getMessage());
            showUsage(jCommander, -1);
        }

        if (cliArgs.isHelp()) {
            showUsage(jCommander, 0);
        }

		Converter convert = new Converter(cliArgs);
		convert.run();
        System.exit(convert.getRc());
	}
    
    public static void showUsage(JCommander jCommander, int rc) {
        jCommander.usage();
        System.exit(rc);
    }
}
