package org.apache.zookeeper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandOptionParser {

    private Map<String, String> options = new HashMap<String, String>();
    private List<String> cmdArgs = null;
    private String command = null;
    public static final Pattern ARGS_PATTERN = Pattern.compile("\\s*([^\"\']\\S*|\"[^\"]*\"|'[^']*')\\s*");
    public static final Pattern QUOTED_PATTERN = Pattern.compile("^([\'\"])(.*)(\\1)$");

    public CommandOptionParser() {
        options.put("server", "localhost:2181"); //default port for zookeeper
        options.put("timeout", "30000");
    }


    public boolean parseCommand(String cmdstring) {
        Matcher matcher = ARGS_PATTERN.matcher(cmdstring);

        List<String> args = new LinkedList<>();
        while (matcher.find()) {
            String value = matcher.group(1);
            if (QUOTED_PATTERN.matcher(value).matches()) {
                // Strip off the surrounding quotes
                value = value.substring(1, value.length() - 1);
            }
            args.add(value);
        }
        if (args.isEmpty()) {
            return false;
        }
        command = args.get(0);
        cmdArgs = args;
        return true; //if parsing succeeded
    }

    public boolean parseOptions(String[] args) {
        List<String> argList = Arrays.asList(args);
        Iterator<String> it = argList.iterator();

        while (it.hasNext()) {
            String opt = it.next();
            try {
                if (opt.equals("-server")) {
                    options.put("server", it.next());
                } else if (opt.equals("-timeout")) {
                    options.put("timeout", it.next());
                } else if (opt.equals("-r")) {
                    options.put("readonly", "true");
                } else if (opt.equals("-client-configuration")) {
                    options.put("client-configuration", it.next());
                }
            } catch (NoSuchElementException e) {
                System.err.println("Error: no argument found for option " + opt);
                return false;
            }

            if (!opt.startsWith("-")) {
                command = opt;
                cmdArgs = new ArrayList<>();
                cmdArgs.add(command);
                while (it.hasNext()) {
                    cmdArgs.add(it.next());
                }
                return true;
            }
        }
        return true;
    }


}
