# PrivateBin-java

A simple utility to send plain text to a PrivateBin instance.

# Maven

You can use this library in your own project by adding the following dependency to your pom.xml:

```xml

<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.InstantlyMoist</groupId>
    <artifactId>privatebin-java-api</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

# Example usage

```java
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
```