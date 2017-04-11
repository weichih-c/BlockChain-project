import java.io.File;
import java.io.FileInputStream;
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

public class KeyGenerator{
	
	public static void main(String[] args) throws InvalidKeySpecException, IOException{
		KeyGenerator kg = new KeyGenerator();
		try {
			kg.keyGenerate();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	
	public void keyGenerate() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
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
		saveKeyPair(keyStorePath, pair);
		
		//LoadFromFile
		KeyPair pair2 = loadKeyPair(keyStorePath, "EC");
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
	}
	
	private static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	// Saving the key pairs to key stores. Naming respectively.
	public void saveKeyPair(String path, KeyPair keyPair) throws IOException {
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
 
		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(path + "/public.key");
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();
 
		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		fos = new FileOutputStream(path + "/private.key");
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
	}
 
	
	// Load the keyPair from keyStore path
	public KeyPair loadKeyPair(String path, String algorithm)
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException {
		// Read Public Key.
		File filePublicKey = new File(path + "/public.key");
		FileInputStream fis = new FileInputStream(path + "/public.key");
		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
		fis.read(encodedPublicKey);
		fis.close();
 
		// Read Private Key.
		File filePrivateKey = new File(path + "/private.key");
		fis = new FileInputStream(path + "/private.key");
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
