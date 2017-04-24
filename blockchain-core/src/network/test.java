package network;

public class test {

	public static void main(String[] args) {
//		String[] nameList = {"Chorong", "Bomi"};
		String[] nameList = {"Chorong", "Bomi", "Eunji", "Naeun", "Namjoo", "HaYoung", "Joy", "Yeri", "Seulgi", "Irene", "Wendy"};
		
//		for(int i=0; i<nameList.length; i++){
//			new Thread(new Node(nameList[i])).start();;
//		}
//		
		Node chorong = new Node(nameList[0]);
		new Thread(chorong).start();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Node bomi = new Node(nameList[1]);
		new Thread(bomi).start();
		
		
		// TODO: create connection to other peers
		
		// TODO: exchange the peerList of each other
	}

}
