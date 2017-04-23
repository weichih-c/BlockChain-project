package network;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Node implements Runnable{
	private SocketIOClient mClient = null;
	
	public Node(String name){
		this.mClient = new SocketIOClient(name);	// will create the initial link when initialize object
	}

	@Override
	public void run() {
		System.out.println("run the runnable");
		Timer timer = new Timer();
		Random rand = new Random();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		while(true){
//			System.out.println("Start loop");
			
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					if(mClient.isConnected())
						mClient.leaveNetwork();
					else
						mClient.reconnectNetwork();
				}
				
			}, 1000 * ( rand.nextInt(10) + 1 ));	// random delayTime in 1000-10000 millisecond
			
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
