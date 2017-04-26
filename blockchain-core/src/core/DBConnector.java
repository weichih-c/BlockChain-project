package core;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import javax.sql.rowset.serial.SerialBlob;

public class DBConnector {
	private Connection conn;
	private Statement st;
	private PreparedStatement pst;
	private ResultSet resultSet;
	private final String MySQL_URL = "jdbc:mysql://140.112.107.199:3306/blockchain";
	private final String dbUser = "root";
	private final String dbPwd = "1234qwer";
	
//	public static void main(String[] args){
//		DBConnector dbc = new DBConnector();
//	}
	
	public DBConnector(){
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection( MySQL_URL, dbUser, dbPwd); 
//			st = conn.createStatement(); 
//			resultSet = st.executeQuery("select * from transactions"); 
//			while(resultSet.next()) 
//			{
//				System.out.println(resultSet.getInt("id")+"\t\t"+ 
//						resultSet.getString("Hash")); 
//			} 
			
		}catch(Exception ex){
			System.out.println("Error: " + ex);
		}
		
	}
	
	
	public void saveTransaction(Transaction transaction){
		String insertSQL = "INSERT INTO transactions (Hash, Data) VALUES (?, ?);";
		try {
			pst = conn.prepareStatement(insertSQL);
			
			pst.setString(1, transaction.getTx_hash().toString()); // set 'Hash'
			
			Blob txBlob = new SerialBlob( Transaction.serializeTransaction(transaction) );
			pst.setBlob(2, txBlob); // set 'Data'
			
			pst.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("Error1: " + e.getMessage());
		}
		finally{
			try {
				pst.close();
			} catch (SQLException e) {
				System.out.println("Error2: " + e.getMessage());
			}
		}
	}
	
	public void saveBlock(Block block){
		String insertSQL = "INSERT INTO blocks (Time, Nonce, PrevBlockHash, MerkleRoot, BlockHash) VALUES (?,?,?,?,?)";
		try {
			pst = conn.prepareStatement(insertSQL);
			String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(block.getTime()));
			pst.setString(1, time); // set 'Time'
			
			pst.setInt(2, block.getNonce()); // set 'Nonce'
			
			pst.setString(3, block.getPrevBlockHash().toString()); // set 'PrevBlockHash'
			
			pst.setString(4, block.getMerkleRoot().toString()); // set 'MerkleRoot'
			
			pst.setString(5, block.getBlockHeaderHash()); // set 'BlockHash'
			
			pst.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("Error1: " + e.getMessage());
		}
		finally{
			try {
				pst.close();
			} catch (SQLException e) {
				System.out.println("Error2: " + e.getMessage());
			}
		}
	}
}
