package core;
import java.io.File;

public class Constant {
	
	// Constant value of SocketIO
	public static final String ServerIP = "https://127.0.0.1:3001";
	public static final String PAYLOAD = "payload";	// socketIO Payload
	public static final String SERVER_MESSAGE = "serverMessage"; // socketIO Server Message
	public static final String CLIENT_MESSAGE = "clientMessage"; // socketIO Client Message
	public static final String TYPE = "type";	// socketIO Type
	public static final String SYSTEM_CODE = "systemCode";	// socketIO system code
	public static final String EVENT = "event";	// socketIO event
	public static final String SOCKETIO_NAME = "name"; // socketIO Name
	public static final String PUBKEYHASHADDR = "pubKeyHashAddress";
	public static final String TOPIC_ID = "id";	// socketIO topic id
	public static final String TOPIC_CONNECTMESSAGE = "connectMessage"; // socketIO topic connectMessage;
	public static final String PEER_LIST = "peerList"; // socketIO string - peerList
	public static final String CLIENT_MESSAGE_TO = "to";
	public static final String CLIENT_MESSAGE_FROM = "from";
	public static final String CURRENT_BALANCE = "currentBalance";
	
	// for create utxo
	public static final String TRANSACTION_HASH = "transactionHash";
	public static final String OUTPUT_INDEX = "outputIndex";
	public static final String VALUE = "value";
	
	// event code
	public static final int TYPE_CONNECTION = 0x01;
	public static final int TYPE_EVENT = 0x02;
	public static final int EVENT_ASK_PEER = 0x101;
	public static final int EVENT_PROVIDE_INIT_PEER = 0x102;
	public static final int EVENT_CHECK_ACTIVE = 0x201;
	public static final int EVENT_CHECK_ACTIVE_RESPONSE = 0x202;
	public static final int EVENT_RAISE_TRANSACTION = 0x301;
	public static final int EVENT_UPDATE_BALANCE_ON_SERVER = 0x401;
	
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
