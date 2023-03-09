package nl.kyllian;

import nl.kyllian.models.Paste;

public class ExampleUsage {

    public static void main(String[] args) {
        Paste paste = new Paste("https://paste.kyllian.nl/")
                .setMessage("Hi, does this work?")
                .encrypt();

        String pasteUrl = paste.send();
        if (pasteUrl != null) {
            System.out.println("Paste URL: " + pasteUrl);
        } else {
            System.out.println("Something went wrong.");
        }
    }
}
