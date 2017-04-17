import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.util.encoders.Hex;

public class KeyGenerator{
	
//	public static void main(String[] args) throws InvalidKeySpecException, IOException{
//		KeyGenerator kg = new KeyGenerator();
//		try {
//			kg.keyGenerate("public3.key", "private3.key");
//		} catch (NoSuchAlgorithmException e) {
//			System.out.println("Error: " + e.getMessage());
//		}
//	}
	
	
	public void keyGenerate(String pubKeyName, String privateKeyName) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		
		keyGen.initialize(256, random);
		
		// Generate a Key Pair
		KeyPair pair = keyGen.generateKeyPair();
		
		System.out.println("Keys Before Saving");
		dumpKeyPair(pair);
		// SaveToFile
		String keyStorePath = System.getProperty("user.dir");
		keyStorePath = keyStorePath + "/keystore";
		createKeystore(keyStorePath);
		saveKeyPair(keyStorePath, pair, pubKeyName, privateKeyName);
		
		//LoadFromFile
		KeyPair pair2 = loadKeyPair(keyStorePath, "EC", pubKeyName, privateKeyName);
		System.out.println("Keys After Saving");
		dumpKeyPair(pair2);
		
		
//        /*
//         * Create a Signature object and initialize it with the private key
//         */
//
//        Signature dsa = Signature.getInstance("SHA256withECDSA");
//
//        dsa.initSign(priv);
//        
////        System.out.println("Sig Private = " + );
//
//        String str = "This is string to sign";
//        byte[] strByte = str.getBytes("UTF-8");
//        dsa.update(strByte);
//        
//        byte[] realSig = dsa.sign();
//        System.out.println("Signature: " + new BigInteger(1, realSig).toString(16));

	}
	
	// print out the key pairs
	private void dumpKeyPair(KeyPair keyPair) {
		PublicKey pub = keyPair.getPublic();
		System.out.println("Public Key: " + getHexString(pub.getEncoded()));
 
		PrivateKey priv = keyPair.getPrivate();
		System.out.println("Private Key: " + getHexString(priv.getEncoded()));
		System.out.println("test = " + new String(Hex.encode( priv.getEncoded())));
		System.out.println("test2 = " + new BigInteger(1, priv.getEncoded()).toString(16));
	}
	
	
	// encoding the byte[] to hex string
	private static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	// Saving the key pairs to key stores. Naming respectively.
	public void saveKeyPair(String path, KeyPair keyPair, String pubKeyName, String privateKeyName) throws IOException {
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
 
		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(path + File.separator + pubKeyName);
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();
 
		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		fos = new FileOutputStream(path + File.separator + privateKeyName);
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
	}
 
	
	// Load the keyPair from keyStore path
	public KeyPair loadKeyPair(String keystorePath, String algorithm, String pubKeyName, String privateKeyName)
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException {
		// Read Public Key.
		File filePublicKey = new File(keystorePath + File.separator + pubKeyName);
		FileInputStream fis = new FileInputStream(keystorePath + File.separator + pubKeyName);
		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
		fis.read(encodedPublicKey);
		fis.close();
 
		// Read Private Key.
		File filePrivateKey = new File(keystorePath + File.separator + privateKeyName);
		fis = new FileInputStream(keystorePath + File.separator + privateKeyName);
		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
		fis.read(encodedPrivateKey);
		fis.close();
 
		// Generate KeyPair.
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
 
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
 
		return new KeyPair(publicKey, privateKey);
	}
	
	public PublicKey loadPublicKey(String keyPath, String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException{
		
		// Read Public Key.
		File filePublicKey = new File(keyPath);
		FileInputStream fis = new FileInputStream(keyPath);
		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
		fis.read(encodedPublicKey);
		fis.close();
		
		// Generate public key
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		
		return publicKey;
	}
	
	public PrivateKey loadPrivateKey(String keyPath, String algorithm) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		
		// Read Private Key.
		File filePrivateKey = new File(keyPath);
		FileInputStream fis = new FileInputStream(keyPath);
		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
		fis.read(encodedPrivateKey);
		fis.close();
		
		// Generate private key
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		
		return privateKey;
	}
	
	/**
	 * Create the keystore directory
	 * @param path
	 */
	private void createKeystore(String path) {
	    File fl = new File(path);
	        if ( fl.exists() != true)
	            fl.mkdir();
	}
	
}
