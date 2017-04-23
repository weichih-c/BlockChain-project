package network;

public class NetworkPeer {
	private String name;
	private String socketIO_ID;
	private String pubKeyHashAddress;
	private boolean isOnline;
	
	public NetworkPeer(String name, String socketIO_ID, String pubKeyHashAddr, boolean isOnline){
		this.name = name;
		this.socketIO_ID = socketIO_ID;
		this.pubKeyHashAddress = pubKeyHashAddr;
		this.isOnline = isOnline;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSocketIO_ID() {
		return socketIO_ID;
	}
	
	public void setSocketIO_ID(String socketIO_ID) {
		this.socketIO_ID = socketIO_ID;
	}
	
	public String getPubKeyHashAddress() {
		return pubKeyHashAddress;
	}
	
	public void setPubKeyHashAddress(String pubKeyHashAddress) {
		this.pubKeyHashAddress = pubKeyHashAddress;
	}
	
	public boolean isOnline() {
		return isOnline;
	}
	
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}
