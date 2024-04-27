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

# Notice about Java 1.8 and TLSv1.3 protocol

In order for this library to work under Java 1.8, and with the latest version of
privatebin (v1.7.1), Java must support TLSv1.3, which may not be supported in older
releases of 1.8.

When running the junit test case, it will display the java version and the available 
TLS versions.  Here is an example of a successful run:

```
privatebin-java-api: Java version: 1.8.0_411
Supported versions of TLS: [TLSv1.3, TLSv1.2]
PasteTest: 
  url: https://privatebin.net 
  pasteUrl: https://privatebin.net/?2592ad1b5d4a3906#6gSjqzyVQjLBWf7gq6ASkqPiLkSxmT1TiR3CEXoZgKmZ 
  password: pw
```


It's been confirmed that Java v1.8.0_411 supports TLSv1.3, so if you are seeing
'protocol_version' errors, then try upgrading to the latest version of 1.8.

When running Java v1.8 release 411, the use of `-Dhttps.protocols=TLSv1.2` can 
force the use of `TLSv1.2` and the protocol will fail.  Removing that setting, or
setting it to `-Dhttps.protocols=TLSv1.3` will be successful. This could be
used to test possible failures.  At this time, I cannot dynamically control 
the failure from one unit test to another.

An example of setting '-Dhttps.protocols=TLSv1.2` and then running the junit test 
results in the following output.  Note that `TLSv1.3` is still listed as being
available, but it's not being used with the transactions. Everything else would
be similar if it failed without that protocol being present.

```
privatebin-java-api: Java version: 1.8.0_411
Supported versions of TLS: [TLSv1.3, TLSv1.2]
Failure in generating the output stream and sending the payload: Received fatal alert: protocol_version   
Failure in reading the response: Received fatal alert: protocol_version   
Failure with response: No response.
Warning: There was a Paste failure that identified that there was a 'protocol_version' issue.  This may be related to an out of date use of a TLS protocol.
1.8.0_411
[TLSv1.3, TLSv1.2]
PasteTest: 
  url: https://privatebin.net 
  pasteUrl: null 
  password: pw
```

