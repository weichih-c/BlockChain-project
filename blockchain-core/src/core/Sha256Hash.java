package core;

public class Sha256Hash {
	
	public static final int LENGTH = 32; // bytes
	private byte[] bytes;
	
	public Sha256Hash(byte[] rawHashBytes) {
        if(rawHashBytes.length == LENGTH)
        	this.bytes = rawHashBytes;
    }
	
	public Sha256Hash(String hexString) {
		if(hexString.length() == LENGTH * 2)
        	this.bytes = Utils.HEX.decode(hexString);
	}
	
	public String toString(){
		return Utils.HEX.encode(bytes);
	}
	
	public byte[] getBytes(){
		return bytes;
	}
}
