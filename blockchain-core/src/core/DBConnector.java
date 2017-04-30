package core;

import java.sql.*;
import java.text.ParseException;
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
//			System.out.println("time before save = " + time);
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
	
	public byte[] getTransactionsForVerify(int selectSize){
		String querySQL = "select * from Transactions where Verified=0 and isChoose=0;";
		byte[] tx={};
		try {
			st = conn.createStatement(); 
			resultSet = st.executeQuery(querySQL);
			
			
			if(resultSet.next()) 
			{
//				System.out.println(resultSet.getInt("id")+"\t\t"+ 
//						resultSet.getString("Hash")); 
				
				setTransactionChosen(resultSet.getInt("id"));
				Blob tmp = resultSet.getBlob(3);
				int blobLength = (int)tmp.length();
//				System.out.println("Len = " + blobLength);
				tx = tmp.getBytes(1, blobLength);
				tmp.free();
			} 
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			try {
				st.close();
				return tx;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
		
	}
	
	public byte[] getTransaction(String txHash) {
		String querySQL = "select * from Transactions where Hash='" + txHash +"';";
		byte[] tx = {};
		try {
			st = conn.createStatement(); 
			resultSet = st.executeQuery(querySQL);
			
			
			if(resultSet.next()) 
			{
//				System.out.println(resultSet.getInt("id")+"\t\t"+ 
//						resultSet.getString("Hash")); 
				
				Blob tmp = resultSet.getBlob(3);
				int blobLength = (int)tmp.length();
				tx = tmp.getBytes(1, blobLength);
				tmp.free();
			} 
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		finally{
			try {
				st.close();
				return tx;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private void setTransactionChosen(int id){
		String updateSQL = "update Transactions set isChoose = ? where id = ?";
		
		try {
			pst = conn.prepareStatement(updateSQL);
			pst.setInt   (1, 1);
			pst.setInt(2, id);

			pst.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
		finally{
			try {
				pst.close();
			} catch (SQLException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
	
	public void setTransactionVerified(String txHash, int passOrNot){
		String updateSQL = "update Transactions set Verified = ?, verifyPass = ? where Hash = ?";
		
		try {
			pst = conn.prepareStatement(updateSQL);
			pst.setInt(1, 1);
			pst.setInt(2, passOrNot);
			pst.setString(3, txHash);

			pst.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
		finally{
			try {
				pst.close();
			} catch (SQLException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
	
	public Block getLastBlock(){
		String querySQL = "select * from blocks;";
		Block b = new Block();
		try {
			st = conn.createStatement(); 
			resultSet = st.executeQuery(querySQL);
			
			if(resultSet.last()){
//				System.out.println(resultSet.getInt("id")+"\t"+
//									resultSet.getString("BlockHash"));

				b.setVersion(1);
				b.setMerkleRoot(new Sha256Hash(resultSet.getString("MerkleRoot")));
				b.setDifficultyTarget(486604799L);
				b.setNonce(resultSet.getInt("Nonce"));
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				java.util.Date time;
				try {
					time = formatter.parse(resultSet.getString("Time"));
//					System.out.println("Time after get = " + time);
					b.setTime(time.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}

				b.setPrevBlockHash(new Sha256Hash(resultSet.getString("PrevBlockHash")));
				b.setBlockHash(new Sha256Hash(resultSet.getString("BlockHash")));
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		finally{
			try {
				st.close();
				return b;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public boolean checkTransactionVerified(String txHash){
		String querySQL = "select * from Transactions where Hash='" + txHash +"';";
		int isVerified = 0;
		try {
			st = conn.createStatement(); 
			resultSet = st.executeQuery(querySQL);
			
			
			if(resultSet.next()) 
			{
//				System.out.println(resultSet.getInt("id")+"\t\t"+ 
//						resultSet.getString("Hash")); 
				
				isVerified = resultSet.getInt("Verified");
				
			} 
			
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		finally{
			try {
				st.close();
				
				return (isVerified==0) ? false : true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
}
