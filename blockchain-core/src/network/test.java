package network;

public class test {

	public static void main(String[] args) {
//		String[] nameList = {"Joy", "Yeri"};
		String[] nameList = {"Terry", "Jake", "Halt", "Amy", "Rosa", "Boyle", "Gina", "Joy", "Yeri", "Seulgi", "Irene", "Wendy"};
		
		for(int i=0; i<nameList.length; i++){
			new Thread(new Node(nameList[i])).start();;
		}
		
		// TODO: create connection to other peers
		
		// TODO: exchange the peerList of each other
	}

}
