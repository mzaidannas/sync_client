package sftp;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


public final class Creds {
    public String HOST;
    public Integer PORT;
    public String USER;
    public String PASSWORD;
    public String DIRECTORY;

    public void readFromUser() {
        System.out.print("HOST = ");
        HOST = System.console().readLine();
        System.out.print("PORT = ");
        PORT = Integer.parseInt(System.console().readLine());
        System.out.print("USER = ");
        USER = System.console().readLine();
        System.out.print("PASSWORD = ");
        PASSWORD = System.console().readLine();
        System.out.print("DIRECTORY = ");
        DIRECTORY = System.console().readLine();
    }

    public static void writeToFile(Creds creds, String path) {
        try {
            System.out.println("Trying to write creds to YAML file");
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.writeValue(new File(path), creds);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Installation finished");
    }

    public static Creds readFromFile(String path) {
        Creds creds = null;
        try {
            System.out.println("Trying to read creds from YAML file");
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            creds = mapper.readValue(new File(path), Creds.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return creds;
    }
}
