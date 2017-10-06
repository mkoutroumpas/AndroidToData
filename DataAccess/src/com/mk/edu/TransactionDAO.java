package com.mk.edu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionDAO {
	private final Logger _Logger = LoggerFactory.getLogger(this.getClass());
	
	private final DataSource _DataSource;
	private final String _TableName = Transaction.class.getSimpleName();
	
	private String _SchemaName = "";
	
	public TransactionDAO(DataSource dataSource, String schemaname) throws SQLException {
		this._DataSource = dataSource;
		this._SchemaName = schemaname;
	}
	
	public TransactionDAO(DataSource dataSource) throws SQLException {
		this(dataSource, "");
	}
	
	/***
	 * Fetch Transaction rows from this table.
	 * @param where A valid WHERE SQL statement.
	 * @return List of Transaction entities.
	 * @throws Exception
	 */
	public ArrayList<Transaction> getTransactions(String where) throws Exception {
		Connection _Connection = null;
		ArrayList<Transaction> list = null;
		
        try {
        	_Connection = this._DataSource.getConnection();
            PreparedStatement pstmt = _Connection.prepareStatement("SELECT * FROM " + 
            		(this._SchemaName != null && !this._SchemaName.trim().equals("") ? this._SchemaName + "." : "") + this._TableName + (where == null ? "" : " " + where));
            ResultSet rs = pstmt.executeQuery();
            list = new ArrayList<Transaction>();
            while (rs.next()) {
            	Transaction t = new Transaction();
                t.setID(rs.getLong(1));
                t.setTransactionValue(rs.getDouble(2));
                t.setTransactionCode(rs.getString(3));
                list.add(t);
            }
            
            this._Logger.info("Successfully fetched " + list.size() + " entities");
        } 
        catch (Exception ex) { this._Logger.error(ex.getMessage()); }
        finally {
            if (_Connection != null) {
				try {
					_Connection.close();
				} 
				catch (SQLException e) {
					e.printStackTrace();
					throw e;
				}
            }
        }
        
        return list;
	}
}
