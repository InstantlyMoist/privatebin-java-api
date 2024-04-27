package nl.kyllian.models;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import nl.kyllian.enums.Expire;
import nl.kyllian.enums.PasteFormat;

class PasteTest {

	public PasteTest() {
		super();
	}
	
	/**
	 * Privatebin.net upgraded recently to v7.1 of the privatebin
	 * software.  Since that upgrade, privatebin-java-api is failing
	 * when using java 1.8.
	 * 
	 * Maven, for this project, is setup to use java 8 (1.8), and 
	 * so this fails.
	 * 
	 * But when Maven is set to use java 17, then this works without
	 * having a SSLHandshakeException (see stack trace below).
	 * 
	 * So what I've been able to find, is that with Java 1.8, the
	 * TLS v1.3 is not supported directly.  So somehow it needs to
	 * be enabled for it to work.
	 * 
	 * 
	 * <pre>
javax.net.ssl.SSLHandshakeException: Received fatal alert: protocol_version
[01:33:36 WARN]:        at sun.security.ssl.Alert.createSSLException(Alert.java:131)
[01:33:36 WARN]:        at sun.security.ssl.Alert.createSSLException(Alert.java:117)
[01:33:36 WARN]:        at sun.security.ssl.TransportContext.fatal(TransportContext.java:356)
[01:33:36 WARN]:        at sun.security.ssl.Alert$AlertConsumer.consume(Alert.java:293)
[01:33:36 WARN]:        at sun.security.ssl.TransportContext.dispatch(TransportContext.java:202)
[01:33:36 WARN]:        at sun.security.ssl.SSLTransport.decode(SSLTransport.java:155)
[01:33:36 WARN]:        at sun.security.ssl.SSLSocketImpl.decode(SSLSocketImpl.java:1315)
[01:33:36 WARN]:        at sun.security.ssl.SSLSocketImpl.readHandshakeRecord(SSLSocketImpl.java:1228)
[01:33:36 WARN]:        at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:412)
[01:33:36 WARN]:        at sun.security.ssl.SSLSocketImpl.startHandshake(SSLSocketImpl.java:384)
[01:33:36 WARN]:        at sun.net.www.protocol.https.HttpsClient.afterConnect(HttpsClient.java:587)
[01:33:36 WARN]:        at sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection.connect(AbstractDelegateHttpsURLConnection.java:197)
[01:33:36 WARN]:        at sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1584)
[01:33:36 WARN]:        at sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1512)
[01:33:36 WARN]:        at sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:268)
[01:33:36 WARN]:        at nl.kyllian.models.Paste.send(Paste.java:207)
	 * </pre>
	 * 
	 */
	@Test
	void test() {
		
		String javaVersion = Paste.getJavaVersion();
		String tlsVersions = Paste.getTlsVersions();
		
        // Test to remove TLSv1.3:
        //tlsVersions = tlsVersions.replace("TLSv1.3", "TLSv0.1.3");
        
        System.out.println( "privatebin-java-api: Java version: " + javaVersion );
        System.out.println("Supported versions of TLS: " + tlsVersions);

        if ( javaVersion.contains("1.8") && !tlsVersions.contains("TLSv1.3")) {
        	
        	System.out.println( "WARNING: If running Java 1.8 and it is lacking TLSv1.3 then you "
        			+ "must upgrade to a more recent version of Java 1.8, such as 1.8.0_411.");
        }
		
		String url = "https://privatebin.net";
		String password = "pw";
		
		String text = "Test message.\nLine two.\n";
		
        Paste paste = new Paste( url )
                .setMessage( text )
                .setExpire( Expire.ONE_DAY )
                .setPasteFormat( PasteFormat.PLAINTEXT )
                .setUserPassword( password )
//                .setCompression( Compression.GZIP)
//                .removeIps()
                .encrypt();

        String pasteUrl = null;
        
        try {
			pasteUrl = paste.send();
		} 
        catch (Exception e) {
			String msg = String.format(
        			"Failure with paste.send(): %s  %s ",
        			e.getMessage(),
        			(e.getCause() == null ? "" : e.getCause().getMessage()) );
			System.err.println( msg );
		}

        System.out.println( String.format(
        		"PasteTest: \n  url: %s \n  pasteUrl: %s \n  password: %s", 
        		url, pasteUrl, password ) );
        
        assertNotNull( pasteUrl );
		
	}

}
