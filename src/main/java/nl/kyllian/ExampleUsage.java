package nl.kyllian;

import nl.kyllian.models.Paste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExampleUsage {

    public static void main(String[] args) {
        Paste paste = new Paste("https://paste.kyllian.nl/")
                .setMessage("Hi, does this work?")
                .encrypt();

        try {
            String pasteUrl = paste.send();
            System.out.println("Paste URL: " + pasteUrl);
        } catch (IOException e) {
            System.out.println("Something went wrong.");

            e.printStackTrace();
        }
    }
}
