package network;

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

import core.Constant;;



public class SocketIOClient {
	private Socket socket;
	private String clientName;
	private ArrayList<NetworkPeer> peerList;
	
	
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
    
    public SocketIOClient(String clientName){
    	this.clientName = clientName;
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
        System.out.println("Message content in payload = " + message);
        socket.emit(Constant.SERVER_MESSAGE, message);
    }
    
    /**
     * 當第一次執行時會發送連線訊息, 向socketIO server註冊
     */
    private void start(){
        try {
            JSONObject message = new JSONObject();
            message.put(Constant.SOCKETIO_NAME, clientName);
            message.put(Constant.SYSTEM_CODE, Constant.TYPE_CONNECTION);
            sendServerMessage(message);
            System.out.println("emit connected");
            
        } catch (JSONException e) {
            System.out.println( e.getMessage());
        }
    }
    
    private void askPeersForInit(){
    	JSONObject message = new JSONObject();
    	message.put(Constant.SYSTEM_CODE, Constant.TYPE_EVENT);
    	message.put(Constant.EVENT, Constant.EVENT_ASK_PEER);
    	sendServerMessage(message);
    	System.out.println("emit ask peer event");
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
            	
            	String type = data.getString(Constant.TYPE);
            	if(type.equals("connected_success")){
                	System.out.println("connect to Server Success");
                	
                	if(peerList.isEmpty())
                		// TODO: 現在只存在在記憶體，所以程式關掉重開理論上仍然會重新要一次（因為程式開起來peerList是空的）
                		askPeersForInit();
                	else{
                		// TODO: check peer in peerList isAlive or not
                	}
            	}
            	
            }
        };

        /**
         * 監聽到Mqtt事件的Listener
         * (此處的Mqtt事件是指用socket傳輸的事件, 模仿mqtt的作法)
         */
        private Emitter.Listener onServerMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            	System.out.println("Get server Message");
            	JSONObject data = (JSONObject) args[0];
            	try{
            		int type = data.getInt(Constant.TYPE);
                	if(type == Constant.EVENT_PROVIDE_INIT_PEER){
                		JSONArray otherPeers = data.getJSONArray(Constant.PEER_LIST);
                		for(int i=0; i<otherPeers.length(); i++){
                			if(otherPeers.isNull(i)){	// workaround solution
                				continue;
                			}
                    		JSONObject peer = otherPeers.getJSONObject(i);
                    		String peerName = peer.getString(Constant.SOCKETIO_NAME);
                    		String peerID = peer.getString(Constant.TOPIC_ID);
                    		// if not client itself, then add to peerList.
                    		if( !clientName.equals( peerName ) ){
                    			NetworkPeer np = new NetworkPeer(peerName, peerID, null, true);
                    			peerList.add(np);

                    		}
                    	}
                		
                		System.out.println(clientName + " has peer list : ");
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
            	
            	
            }
        };
    }
	
	
}
