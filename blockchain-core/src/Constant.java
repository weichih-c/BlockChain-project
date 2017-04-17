import java.io.File;

public class Constant {
	
	
	public static String getPubKeyPath(String keyName){
		String keyStorePath = System.getProperty("user.dir");
		keyStorePath = keyStorePath + "/keystore";
		
		if(!keyName.endsWith(".key")){
//			System.out.println("add .key");
			keyName += ".key";
		}
		return keyStorePath + File.separator + keyName;
	}
}
