import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;


public class Receiver {
	public String publicKeyHashAddress;
	
	public static void main(String[] args){
		Receiver c = new Receiver();
		try {
			System.out.println(c.generatePublicHashKey() );
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Receiver(  ){
		try {
			publicKeyHashAddress = generatePublicHashKey();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public String getPublicKeyHashAddress(){
		return publicKeyHashAddress;
	}
	
	
	private String generatePublicHashKey() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		String keyStorePath = System.getProperty("user.dir");
		keyStorePath = keyStorePath + "/keystore";
		String keyName = "public2.key";
		String pubKeyPath = keyStorePath + File.separator + keyName;
		String algorithmName = "EC";
		
		// get public key from keyStore
		KeyGenerator keyGen = new KeyGenerator();
		PublicKey pubKey = keyGen.loadPublicKey(pubKeyPath, algorithmName);
		
		// generate a pubKeyHash
		byte[] pubKeyHash = HashGenerator.hashingRIPEMD160(HashGenerator.hashingSHA256(pubKey.getEncoded()));
		
		byte[] pkhPrefix = { 0x00 };	// prefix 1 byte (0x00)
		byte[] pkhChecksum = HashGenerator.hashingSHA256Twice(pubKeyHash);	// truncate first 4 bytes
		pkhChecksum = Arrays.copyOfRange(pkhChecksum, 0, 4);
		
		
		// concatenate arrays including prefix, pubKeyHash, checksum
		byte[] p2pkhAddress = concatenateByteArrays(pkhPrefix, pubKeyHash);
		p2pkhAddress = concatenateByteArrays(p2pkhAddress, pkhChecksum);

		return Base58.encode( p2pkhAddress );	// return a string encoded by Base58
		
	}
	
	
	// concatenate two Byte arrays
	private byte[] concatenateByteArrays(byte[] a, byte[] b) {
	    byte[] result = new byte[a.length + b.length]; 
	    System.arraycopy(a, 0, result, 0, a.length); 
	    System.arraycopy(b, 0, result, a.length, b.length); 
	    return result;
	} 
}
