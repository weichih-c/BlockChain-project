package network;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;



public class SocketIOClient {
	
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
    
    public SocketIOClient(){
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
			
			Socket socket = IO.socket("https://127.0.0.1:3001", opts);

			socket.on("id", m.onID);
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
            }
        };

        /**
         * 監聽到Connect事件的Listener
         */
        private Emitter.Listener onConnectListener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            	System.out.println("connect");
            }
        };

        /**
         * 監聽到Mqtt事件的Listener
         * (此處的Mqtt事件是指用socket傳輸的事件, 模仿mqtt的作法)
         */
        private Emitter.Listener onMqttMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                
            }
        };
    }
	
	
}
