package network;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import core.Constant;
import core.OpCode;
import core.Sha256Hash;
import core.UnspentTXO;
import core.Utils;
import core.Wallet;;



public class SocketIOClient {
	private Socket socket;
	private String clientName;
	private ArrayList<NetworkPeer> peerList;
	private ConnectionListener mListener;
	private final int MAX_PEERLIST_SIZE = 3;
	private Wallet wallet;
	
	public interface ConnectionListener {
		void onConnectionReady();
		void onInitialReady();
	}
	
	/**
     * 設定信任所有憑證(用於SSL加密)
     */
    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws java.security.cert.CertificateException {
			}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
				throws java.security.cert.CertificateException {
		}
    }};


    /**
     * 設定HTTPS 的 HostName
     */
    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    
    public SocketIOClient(ConnectionListener listener, String clientName, Wallet wallet){
    	this.mListener = listener;
    	this.clientName = clientName;
    	this.wallet = wallet;
    	peerList = new ArrayList<>();
    	
    	MessageHandler m = new MessageHandler();
    	try {			
			SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
			IO.setDefaultSSLContext(sc);
			HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());
            IO.Options opts = new IO.Options();     // 設定socketIO選項
            opts.sslContext = sc;
            opts.secure = true;
            opts.forceNew = true;
			
			socket = IO.socket(Constant.ServerIP, opts);	// establish a ssl connect

			// 註冊監聽主題
			socket.on(Constant.TOPIC_ID, m.onID);
			socket.on(Constant.TOPIC_CONNECTMESSAGE, m.onConnectListener);
			socket.on(Constant.SERVER_MESSAGE, m.onServerMessage);
			socket.on(Constant.CLIENT_MESSAGE, m.onClientMessage);
			socket.connect();
			
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Send message to server
     */
    public void sendServerMessage(JSONObject payload) throws JSONException{
    	JSONObject message = new JSONObject();
        message.put(Constant.PAYLOAD, payload);
//        System.out.println("Message content in payload = " + message);
        socket.emit(Constant.SERVER_MESSAGE, message);
    }
    
    public void sendClientMessage(JSONObject payload) throws JSONException{
    	JSONObject message = new JSONObject();
    	message.put(Constant.PAYLOAD, payload);
//    	System.out.println("Message content in payload = " + message);
    	socket.emit(Constant.CLIENT_MESSAGE, message);
    }
    
    /**
     * 當第一次執行時會發送連線訊息, 向socketIO server註冊
     */
    private void start(){
        try {
            JSONObject message = new JSONObject();
            message.put(Constant.SOCKETIO_NAME, clientName);
            message.put(Constant.PUBKEYHASHADDR, wallet.showPubKeyAddress(0));
            message.put(Constant.SYSTEM_CODE, Constant.TYPE_CONNECTION);
            sendServerMessage(message);
            System.out.println("emit connected");
            
        } catch (JSONException e) {
            System.out.println( e.getMessage());
        }
    }
    
    public void askPeersForInit() throws JSONException{
    	JSONObject message = new JSONObject();
    	message.put(Constant.SYSTEM_CODE, Constant.TYPE_EVENT);
    	message.put(Constant.EVENT, Constant.EVENT_ASK_PEER);
    	sendServerMessage(message);
    	System.out.println("emit ask peer event");
    }
    
    private JSONObject setClientMessage(int eventCode, String remoteID, String localName) throws JSONException{
    	JSONObject message = new JSONObject();
        message.put(Constant.SYSTEM_CODE, Constant.TYPE_EVENT);
        message.put(Constant.EVENT, eventCode);
        message.put(Constant.CLIENT_MESSAGE_TO, remoteID);
        message.put(Constant.SOCKETIO_NAME, localName);
        return message;
    }
    
    public boolean isConnected(){
    	return socket.connected();
    }
    
    
    /**
     * disconnect the network.
     */
    public void leaveNetwork() {
    	System.out.println(clientName + " is leaving network.");

    	if(socket != null){
    		if(socket.connected()){
        		socket.close();
    		}
//    		socket = null;
    	}
    }
    
    public void reconnectNetwork() {
    	System.out.println(clientName + " is reconnecting");
    	
    	// close the previous connection
    	if(socket != null){
    		if(socket.connected()){
        		socket.close();
    		}
    	}
    	
		socket.connect();
			

    }
    
    /**
     * inner class MessageHandler
     * 用來處理收到的訊息
     * 裡面會根據收到的訊息拆開後的不同SystemCode做不同事件處理
     */
    private class MessageHandler {

        /**
         * 獲得遠端Server配的Socket ID
         */
        private Emitter.Listener onID = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            	System.out.println("connect");
            	start();
            }
        };

        /**
         * 監聽到Connect事件的Listener
         */
        private Emitter.Listener onConnectListener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            	JSONObject data = (JSONObject) args[0];
            	
				try {
            	
            	String type;

					type = data.getString(Constant.TYPE);
				
	            	if(type.equals("connected_success")){
	//                	System.out.println("connect to Server Success");
	                	
	                	if(peerList.isEmpty())
	                		// TODO: 現在只存在在記憶體，所以程式關掉重開理論上仍然會重新要一次（因為程式開起來peerList是空的）
	                		askPeersForInit();
	                	else{
	                		// TODO: check peer in peerList isAlive or not
	                	}
	            	}
            	
				} catch (JSONException e) {
					System.out.println("Error" + e);
				}
            	mListener.onConnectionReady();
            }
        };

        /**
         * 監聽到Mqtt事件的Listener
         * (此處的Mqtt事件是指用socket傳輸的事件, 模仿mqtt的作法)
         */
        private Emitter.Listener onServerMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
//            	System.out.println("Get server Message");
            	JSONObject data = (JSONObject) args[0];
            	try{
            		int type = data.getInt(Constant.TYPE);
                	if(type == Constant.EVENT_PROVIDE_INIT_PEER){
                		JSONArray otherPeers = data.getJSONArray(Constant.PEER_LIST);
//                		System.out.println(otherPeers);
                		for(int i=0; i<otherPeers.length(); i++){
                			if(otherPeers.isNull(i)){	// workaround solution
                				continue;
                			}
                    		JSONObject peer = otherPeers.getJSONObject(i);
                    		String peerName = peer.getString(Constant.SOCKETIO_NAME);
                    		String peerID = peer.getString(Constant.TOPIC_ID);
                    		String peerPubkeyHash = peer.getString(Constant.PUBKEYHASHADDR);
                    		// if not client itself, then add to peerList.
                    		
                    		boolean oldPeerFlag = false;
                    		for(NetworkPeer p: peerList){
                    			if(p.getSocketIO_ID().equals(peerID)){
                    				oldPeerFlag = true;
                    				break;
                    			}
                    		}
                    		
                    		if( !clientName.equals( peerName ) && !oldPeerFlag ){
                    			NetworkPeer np = new NetworkPeer(peerName, peerID, peerPubkeyHash, true);
                    			peerList.add(np);

                    		}
                    	}
                		
                		System.out.print(clientName + " has peer list : ");
                		for(NetworkPeer peer : peerList){
                			System.out.print(peer.getName() + ',');
                		}
                		System.out.println();
                		
                	}else{
                		System.out.println("Receiving other event type.");
                	}
            	}catch(JSONException e){
            		e.printStackTrace();
            	}
            	
            	mListener.onInitialReady();
            	
            }
        };
        
        
        private Emitter.Listener onClientMessage = new Emitter.Listener() {
			
			@Override
			public void call(Object... args) {
//				System.out.println("Get client Message");
				
				try{
				
	            	JSONObject data = (JSONObject) args[0];
	            	String from = data.getString(Constant.CLIENT_MESSAGE_FROM);	// get remote SocketID
	            	JSONObject payload = data.getJSONObject(Constant.PAYLOAD);
	//            	System.out.println("Client data = " + data);
	            	int event = payload.getInt(Constant.EVENT);
	            	
	            	switch (event) {
	            		case Constant.EVENT_CHECK_ACTIVE:{
	            		
	            			String remoteName = payload.getString(Constant.SOCKETIO_NAME);
//	                    	System.out.println(clientName + " get Message : 'check active' from : " + remoteName);
	        				String pubkeyHashAddr = payload.getString(Constant.PUBKEYHASHADDR);
	                    	
	                    	checkNewPeer(from, remoteName, pubkeyHashAddr); // Checking new peer or not. If new, then add.
	                    	sendClientMessage( setClientMessage(Constant.EVENT_CHECK_ACTIVE_RESPONSE, from, clientName) );
	                    	
	                    	break;
	            		}
	            		case Constant.EVENT_CHECK_ACTIVE_RESPONSE:{
	            			String remoteName = payload.getString(Constant.SOCKETIO_NAME);
//	            			System.out.println(clientName + " get Message : 'response active' from : " + remoteName);
	            			
	            			break;
	            		}
	            		case Constant.EVENT_RAISE_TRANSACTION:{
//	            			System.out.println(clientName + "Receive Money");
	            			String txHash = payload.getString(Constant.TRANSACTION_HASH);
	            			int outputIndex = payload.getInt(Constant.OUTPUT_INDEX);
	            			int value = payload.getInt(Constant.VALUE);
	            			
	            			byte[] scriptPubKey = wallet.showPubKeyAddress(0).getBytes();
	            			
	            			wallet.receiveMoney(new Sha256Hash(txHash),
				            					outputIndex,
				            					value,
				            					scriptPubKey);
	            			updateBalanceOnServer();
	            		}
	            			
	            	}
            	
				}catch(JSONException ex){
					System.out.println("Error: " + ex);
				}
            	
            	
            	
            	
			}
		};
    }
    
    public void updateBalanceOnServer() throws JSONException{
    	JSONObject message = new JSONObject();
    	message.put(Constant.SYSTEM_CODE, Constant.TYPE_EVENT);
    	message.put(Constant.EVENT, Constant.EVENT_UPDATE_BALANCE_ON_SERVER);
    	String balance = wallet.getBalance() + "BTC";
    	message.put(Constant.CURRENT_BALANCE, balance);
    	sendServerMessage(message);
    	System.out.println("update balance value on server monitor");
    }
    
    private void checkNewPeer(String from, String remoteName, String pubkeyHashAddr){
    	if(peerList.size() <= MAX_PEERLIST_SIZE){
    		
    		boolean isNewPeer = true;
        	for(NetworkPeer peer: peerList){
        		
        		// peer information is newest
        		if(peer.getSocketIO_ID().equals(from)){
        			isNewPeer = false;
        			break;
        		}
        		
        		// update peer socketID
        		if(peer.getName().equals(remoteName)){
        			peer.setSocketIO_ID(from);
        			isNewPeer = false;
        			break;
        		}
        	}
        	
        	if(isNewPeer){
        		NetworkPeer newPeer = new NetworkPeer(remoteName, from, pubkeyHashAddr, true); // an online peer
        		peerList.add(newPeer);
        	}
    		
    	}
    }
	
	public ArrayList<NetworkPeer> getPeerList(){
		return peerList;
	}
	
	public String getClientName(){
		return clientName;
	}
}
