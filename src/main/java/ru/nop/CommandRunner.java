package ru.nop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandRunner {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static String run(String command) {

        try {

            StringBuilder sb = new StringBuilder();

            String[] commands = new String[]{"/bin/sh", "-c", command};



                Process proc = new ProcessBuilder(commands).start();
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(proc.getErrorStream()));

                System.out.println(true);
                String s;

                while ((s = stdInput.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }



                while ((s = stdError.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }


                return sb.toString();



        } catch (Exception e) {

            logger.error("",e);

        }

        return null;

    }


}



