package example;

import core.Block;
import core.DBConnector;
import network.Node;
import network.NodeCharactor;

public class test {

	public static void main(String[] args) {
		Block genesisBlock = new Block().createGenesisBlock();
		DBConnector dbConnector = new DBConnector();
		dbConnector.saveBlock(genesisBlock);
		
		String[] nameList ={"Jeff", "Amy", "Edward", "Neo", "Pan", "Jessie", "Krystal", "Joy", "Melanie", "Terry", "Irene", "Jake"};
//		String[] nameList = {"Chorong", "Bomi", "Eunji", "Naeun", "Namjoo", "HaYoung", "Joy", "Yeri", "Seulgi", "Irene", "Wendy"};
		
		new Thread(new Node(nameList[0], NodeCharactor.TYPE_MINER)).start();
		
		for(int i=1; i<nameList.length; i++){
			new Thread(new Node(nameList[i], NodeCharactor.TYPE_CLIENT)).start();;
		}
		
//		Node chorong = new Node(nameList[0], NodeCharactor.TYPE_MINER);
//		new Thread(chorong).start();
//
//		Node bomi = new Node(nameList[1], NodeCharactor.TYPE_CLIENT);
//		new Thread(bomi).start();
//		
//		Node eunji = new Node(nameList[2], NodeCharactor.TYPE_CLIENT);
//		new Thread(eunji).start();
		
	}

}
