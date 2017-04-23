package core;
import java.io.File;

public class Constant {
	
	// Constant value of SocketIO
	public static final String ServerIP = "https://127.0.0.1:3001";
	public static final String PAYLOAD = "payload";	// socketIO Payload
	public static final String SERVER_MESSAGE = "serverMessage"; // socketIO Server Message
	public static final String TYPE = "type";	// socketIO Type
	public static final String SYSTEM_CODE = "systemCode";	// socketIO system code
	public static final String EVENT = "event";	// socketIO event
	public static final String SOCKETIO_NAME = "name"; // socketIO Name
	public static final String TOPIC_ID = "id";	// socketIO topic id
	public static final String TOPIC_CONNECTMESSAGE = "connectMessage"; // socketIO topic connectMessage;
	
	public static int TYPE_CONNECTION = 0x01;
	public static int TYPE_EVENT = 0x02;
	public static int EVENT_ASK_PEER = 0x101;
	
	public static String getKeyPath(String keyName){
		String keyStorePath = System.getProperty("user.dir");
		keyStorePath = keyStorePath + "/keystore";
		
		if(!keyName.endsWith(".key")){
//			System.out.println("add .key");
			keyName += ".key";
		}
		return keyStorePath + File.separator + keyName;
	}
	
}
