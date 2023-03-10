package nl.kyllian.models;

import nl.kyllian.enums.Compression;
import nl.kyllian.enums.Expire;
import nl.kyllian.enums.PasteFormat;
import nl.kyllian.utils.Base58;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;

import static java.nio.file.Files.readAllBytes;

public class Paste {
    private final String pasteUrl;

    private Expire expire = Expire.ONE_WEEK;
    private PasteFormat pasteFormat = PasteFormat.PLAINTEXT;
    private Compression compression = Compression.NONE;

    private int burnAfterReading = 0;
    private int openDiscussion = 0;

    private String message = "";

    private String userPassword = "";

    private String hash = "";
    private JSONObject payload = null;

    public Paste(String pasteUrl) {
        this.pasteUrl = pasteUrl;
    }

    public Paste setExpire(Expire expire) {
        this.expire = expire;
        return this;
    }

    public Paste setUserPassword(String userPassword) {
        this.userPassword = userPassword;
        return this;
    }

    public Paste setPasteFormat(PasteFormat pasteFormat) {
        this.pasteFormat = pasteFormat;
        return this;
    }

    public Paste setCompression(Compression compression) {
        this.compression = compression;
        return this;
    }

    public Paste setBurnAfterReading(boolean burnAfterReading) {
        this.burnAfterReading = burnAfterReading ? 1 : 0;
        // You can't open a discussion if you burn after reading.
        if (burnAfterReading) this.openDiscussion = 0;
        return this;
    }

    public Paste setOpenDiscussion(boolean openDiscussion) {
        this.openDiscussion = openDiscussion ? 1 : 0;
        // You can't burn after reading if you open a discussion.
        if (openDiscussion) this.burnAfterReading = 0;
        return this;
    }

    public Paste setMessage(String message) {
        this.message = message;
        return this;
    }

    public Paste setMessageFromFile(String filePath) {
        Path parsedFilePath = Paths.get(filePath);
        try {
            byte[] fileBytes = readAllBytes(parsedFilePath);
            this.message = new String(fileBytes);
        } catch (IOException exception) {
            exception.printStackTrace();
            return this;
        }
        return this;
    }

    public Paste encrypt() {
        try {
            payload = new JSONObject();

            // Generate random password
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(192);
            byte[] randomPasswordBytes = keyGenerator.generateKey().getEncoded();
            String randomPassword = Base64.getEncoder().encodeToString(randomPasswordBytes);
            hash = randomPassword;
            String customPassword = randomPassword + userPassword;


            // Generate IV
            byte[] cipherIVBytes = new byte[16];
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            secureRandom.nextBytes(cipherIVBytes);
            String cipherIVEncoded = Base64.getEncoder().encodeToString(cipherIVBytes);

            // Generate salt
            byte[] kdfSaltBytes = new byte[8];
            secureRandom.nextBytes(kdfSaltBytes);
            String kdfSaltEncoded = Base64.getEncoder().encodeToString(kdfSaltBytes);

            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("paste", message);

            byte[] parsedDataBytes;
            try {
                parsedDataBytes = attemptCompression(jsonMessage.toString().getBytes());
            } catch (IOException e) {
                System.out.println("Failed to compress data, falling back to uncompressed data.");
                setCompression(Compression.NONE);
                parsedDataBytes = jsonMessage.toString().getBytes();
            }

            // Generate secret key for cipher
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec passwordBasedEncryptionKeySpec = new PBEKeySpec(customPassword.toCharArray(), kdfSaltBytes, 100000, 256);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(passwordBasedEncryptionKeySpec).getEncoded(), "AES");

            // Generate aData array
            JSONArray aData = new JSONArray();
            JSONArray aDataInner = new JSONArray();
            aDataInner.put(cipherIVEncoded);
            aDataInner.put(kdfSaltEncoded);
            aDataInner.put(100000);
            aDataInner.put(256);
            aDataInner.put(128);
            aDataInner.put("aes");
            aDataInner.put("gcm");
            aDataInner.put("none");
            aData.put(aDataInner);
            aData.put(pasteFormat.getPasteFormat());
            aData.put(openDiscussion);
            aData.put(burnAfterReading);

            payload.put("adata", aData);

            // Generate cipher
            byte[] gcmBytes = aData.toString().getBytes();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, cipherIVBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secret, spec);
            cipher.updateAAD(gcmBytes);
            byte[] cipherTextBytes = cipher.doFinal(parsedDataBytes);
            String cipherTextEncoded = Base64.getEncoder().encodeToString(cipherTextBytes);

            payload.put("v", 2);
            payload.put("ct", cipherTextEncoded);
            payload.put("meta", new JSONObject().put("expire", expire.getExpire()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public String send() throws IOException {
        if (payload == null || hash.isEmpty()) throw new RuntimeException("You must encrypt the paste before sending it.");

        String payload = this.payload.toString();
        byte[] payloadBytes = payload.getBytes();

        URL url = new URL(pasteUrl);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Requested-With", "JSONHttpRequest");
        connection.setRequestProperty("Content-Length", Integer.toString(payloadBytes.length));

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(payloadBytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String responseString = responseBuilder.toString();
        JSONObject jsonResponse = new JSONObject(responseString);
        if (!jsonResponse.has("url")) throw new RuntimeException("Failed to send paste: " + jsonResponse);
        String receivedID = jsonResponse.getString("url");
        if (receivedID == null) throw new RuntimeException("Failed to get paste ID from response: " + responseString);
        return pasteUrl + receivedID + "#" + Base58.encode(hash.getBytes());
    }

    public byte[] attemptCompression(byte[] data) throws IOException {
        if (compression == Compression.NONE) return data;
        // Compression is GZIP, attempt compression
        Deflater deflater = new Deflater();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            stream.write(buffer, 0, count);
        }

        byte[] output = stream.toByteArray();
        stream.close();
        deflater.end();

        return Arrays.copyOfRange(output, 2, output.length - 4);
    }
}
