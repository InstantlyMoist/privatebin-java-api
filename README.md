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
    <version>{TAG}</version>
</dependency>
```

To build this library, run `maven package` to generate the jar file, where the version
can be set within the project's `pom.xml` file under `project.version`.
`/privatebin-java-api/target/PrivateBin-java-<version>.jar


# Example usage

```java
 public static void main(String[] args) {
        Paste paste = new Paste("https://paste.kyllian.nl/")
                .setMessage("Hi, does this work?")
                .encrypt();

        String pasteUrl = paste.send();
        if (pasteUrl != null) {
            System.out.println("Paste URL: " + pasteUrl);
        } 
        else {
            System.out.println("Something went wrong.");
        }
        
        if ( paste.getErrors().size() > 0 ) {
            for ( String err : getErrors() ) {
                System.err.println( err );
            }
        }
    }
```

# Notice about Java 1.8

In order for this library to work under Java 1.8, and with the latest version of
privatebin (v1.7.1), Java must support TLSv1.3, which may not be supported in older
releases of 1.8.  

It's been confirmed that Java v1.8.0_411 supports TLSv1.3, so if you are seeing
'protocol_version' errors, then try upgrading to the latest version of 1.8.

