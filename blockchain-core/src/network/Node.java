package network;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import core.Constant;
import network.SocketIOClient.ConnectionListener;

public class Node implements Runnable, ConnectionListener{
	private SocketIOClient mClient = null;
	private ArrayList<NetworkPeer> peerList;
	
	public Node(String name){
//		System.out.println("constructor");
		this.mClient = new SocketIOClient(Node.this, name);	// will create the initial link when initialize object
	}

	@Override
	public void run() {
//		System.out.println("run the runnable");
		Timer timer = new Timer();
		Random rand = new Random();
//		try {
			
//			wait(2000);
//			Thread.sleep(2000);
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
		this.peerList = mClient.getPeerList();
		

		
//		while(true){
//			
//			timer.schedule(new TimerTask(){
//
//				@Override
//				public void run() {
//					if(mClient.isConnected())
//						mClient.leaveNetwork();
//					else
//						mClient.reconnectNetwork();
//				}
//				
//			}, 1000 * ( rand.nextInt(10) + 1 ));	// random delayTime in 1000-10000 millisecond
//			
//			
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		
		
	}
	
	public void connectOthers() throws JSONException{
		System.out.println(peerList.size());
		if(peerList.size() > 0){
			NetworkPeer peer = peerList.get(0);
			JSONObject payload = new JSONObject();
			payload.put(Constant.CLIENT_MESSAGE_TO, peer.getSocketIO_ID());
			payload.put(Constant.EVENT, Constant.EVENT_CHECK_ACTIVE);
			payload.put(Constant.SOCKETIO_NAME, mClient.getClientName());	// tell remote who am I
			mClient.sendClientMessage(payload);
		}
	}

	@Override
	public void onConnectionReady() {
		
	}

	@Override
	public void onInitialReady() {
		System.out.println("Node onInitialReady");
		
		try {
			connectOthers();
		} catch (JSONException e) {
			System.out.println("Error: " + e);
		}
	}

}
