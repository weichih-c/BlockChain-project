package core;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

public class HashGenerator {
	
	public static byte[] hashingSHA256 (byte[] data) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);  // Can't happen.
		}
		md.update(data);	// update the digest
		byte[] byteData = md.digest();	// complete the hash computation
		
		return byteData;
	}
	
	public static byte[] hashingSHA256Twice(byte[] data){
		return hashingSHA256( hashingSHA256(data) );
	}
	
	public static byte[] hashingRIPEMD160 (byte[] data) {
		RIPEMD160Digest d = new RIPEMD160Digest();
		d.update(data, 0, data.length);
		byte[] out = new byte[d.getDigestSize()];	// the output of hash computation
        d.doFinal (out, 0);	// closing the digest
        
        return out;
	}
}
