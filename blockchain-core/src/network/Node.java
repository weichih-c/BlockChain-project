package network;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import core.Constant;
import core.DBConnector;
import core.Miner;
import core.Transaction;
import core.Wallet;
import network.SocketIOClient.ConnectionListener;

public class Node implements Runnable, ConnectionListener{
	private String name;
	private SocketIOClient mClient = null;
	private ArrayList<NetworkPeer> peerList = new ArrayList<>();
	private Miner miner;
	private Wallet wallet;
	private int spendValue = 500000000;
	private DBConnector dbConnector = new DBConnector();
	private String nodeType;
	
	public Node(String name, String charactor){
		this.name = name;
//		System.out.println("constructor");
		this.wallet = new Wallet(name);
		this.mClient = new SocketIOClient(Node.this, name, wallet);	// will create the initial link when initialize object

		
		if(charactor.equals(NodeCharactor.TYPE_MINER)){
			this.miner = new Miner();
			this.spendValue = 2000000000;
			this.nodeType = NodeCharactor.TYPE_MINER;
		}
		else{
			this.nodeType = NodeCharactor.TYPE_CLIENT;
		}


	}

	@Override
	public void run() {

		Timer timer = new Timer();
		Timer timer2 = new Timer();
		
		if(nodeType.equals(NodeCharactor.TYPE_MINER)){
			new Thread(new Runnable(){
	
				@Override
				public void run() {
					System.out.println(name + " run miner loop");

					timer.schedule(new TimerTask(){

						@Override
						public void run() {
//							System.out.println(name + "-action1");
							miner.mineBlock(wallet);
							try {
								mClient.updateBalanceOnServer();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						
					}, 0, 100);
				}
				
			}).start();;
		}
		new Thread(new Runnable(){

			@Override
			public void run() {
				System.out.println(name + " run client loop");

				Random rand = new Random();
				timer2.scheduleAtFixedRate(new TimerTask(){

					@Override
					public void run() {
//						System.out.println(name + "-action2");
						int peerSize = peerList.size();
						if(peerSize > 0){
							NetworkPeer peer = peerList.get(rand.nextInt(peerSize));
							boolean canSpend = makeTransaction(peer);
							if(!canSpend){
								try {
									System.out.println(name + " Lack of money, waiting for donation.");
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
					
				}, 3000, 5000);

			}
			
		}).start();
		
	}
	
	private double getWalletBalance(){
		return wallet.getBalance();
	}
	
	public boolean makeTransaction(NetworkPeer receiverPeer){
		if(getWalletBalance() > 20){
			System.out.println(name + " create a transaction to " + receiverPeer.getName());
			Transaction tx = wallet.createGeneralTransaction(spendValue, wallet, receiverPeer.getPubKeyHashAddress());
			dbConnector.saveTransaction(tx);
			if(tx.isAnyChange()){
				wallet.receiveMoney(tx);
			}
			JSONObject payload = new JSONObject();
			try {
				payload.put(Constant.CLIENT_MESSAGE_TO, receiverPeer.getSocketIO_ID());
				payload.put(Constant.EVENT, Constant.EVENT_RAISE_TRANSACTION);
				payload.put(Constant.TRANSACTION_HASH, tx.getTx_hash());
				payload.put(Constant.OUTPUT_INDEX, 0);	//remote peer utxo default index 0
				payload.put(Constant.VALUE, spendValue);
				mClient.sendClientMessage(payload);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			wallet.clearWalletSpentTXO(); // clear spent txo
			
			try {
				mClient.updateBalanceOnServer();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			System.out.println(name + " current balance = " + getWalletBalance() + "BTC");
			return true;
			
		}else{
			return false;
		}
	}
	
	public void connectOthers() throws JSONException{
//		System.out.println(peerList.size());
		if(peerList.size() > 0){
			for(NetworkPeer peer : peerList){
				JSONObject payload = new JSONObject();
				payload.put(Constant.CLIENT_MESSAGE_TO, peer.getSocketIO_ID());
				payload.put(Constant.EVENT, Constant.EVENT_CHECK_ACTIVE);
				payload.put(Constant.SOCKETIO_NAME, mClient.getClientName());	// tell remote who am I
				payload.put(Constant.PUBKEYHASHADDR, wallet.showPubKeyAddress(0));
				mClient.sendClientMessage(payload);
			}
		}
	}

	@Override
	public void onConnectionReady() {
		
	}

	@Override
	public void onInitialReady() {
		System.out.println("Node onInitialReady");

		this.peerList = mClient.getPeerList();
		
		try {
			
			connectOthers();
			
		} catch (JSONException e) {
			System.out.println("Error: " + e);
		}
	}

}
