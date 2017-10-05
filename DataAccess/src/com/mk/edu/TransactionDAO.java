package com.mk.edu;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
	private String _SQLCreateScript;
	
	public TransactionDAO(DataSource dataSource, String sqlcreatestatement, String schemaname) throws SQLException {
		this._DataSource = dataSource;
		this._SQLCreateScript = sqlcreatestatement;
		this._SchemaName = schemaname;
		
		this._verifyTable();
	}

	private void _verifyTable() throws SQLException {
		Connection _Connection = null;

        try {
        	_Connection = this._DataSource.getConnection();
            if (!this._tableExists(_Connection)) 
                this._createTableAndFillWithData(_Connection);
        } 
        finally {
            if (_Connection != null) {
            	_Connection.close();
            	this._Logger.info("Connection to database closed");
            }
        }
	}

	public ArrayList<Transaction> getTransactions(String where) throws SQLException {
		Connection _Connection = null;
		
        try {
        	_Connection = this._DataSource.getConnection();
            PreparedStatement pstmt = _Connection.prepareStatement("SELECT * FROM " + 
            		(this._SchemaName != null && !this._SchemaName.trim().equals("") ? this._SchemaName + "." : "") + this._TableName + (where == null ? "" : " " + where));
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Transaction> list = new ArrayList<Transaction>();
            while (rs.next()) {
            	Transaction t = new Transaction();
                t.setID(rs.getLong(0));
                t.setTransactionValue(rs.getDouble(1));
                t.setTransactionCode(rs.getString(2));
                list.add(t);
            }
            
            this._Logger.info("Successfully fetched " + list.size() + " entities");
            return list;
        } 
        finally {
            if (_Connection != null) 
            	_Connection.close();
        }
	}
	
	private void _createTableAndFillWithData(Connection connection) throws SQLException {
		if (connection != null && this._SQLCreateScript != null && this._SQLCreateScript.trim() != "") {
			String[] _Statements = this._SQLCreateScript.split(";");
			for (String st : _Statements) {
				this._Logger.info("Executing statement: \"" + st + "\"");
				try {
					connection.prepareStatement(st).execute();
					this._Logger.info("Statement \"" + st + "\" executed successfully");
				}
				catch (SQLException sqex) {
					this._Logger.error("Error executing statement \"" + st + "\": " + sqex.getMessage());
				}
			}
		}
	}

	private boolean _tableExists(Connection connection) throws SQLException {
		if (connection == null) return false;
		
		DatabaseMetaData _DatabaseMetaData = connection.getMetaData();
		try {
	        ResultSet rs = _DatabaseMetaData.getTables(null, null, this._TableName, null);
	        if (rs != null) {
		        while (rs.next()) {
		            String _Name = rs.getString("TABLE_NAME");
		            if (_Name.indexOf(this._TableName) > -1) {
		            	this._Logger.info("Table " + this._TableName + " found to exist");
		                return true;
		            }
		        }
	        }
		}
		catch (SQLException sqex) {
			this._Logger.error("Error verifying existence of table: " + 
					(this._SchemaName != null && !this._SchemaName.trim().equals("") ? this._SchemaName + "." : "") + this._TableName);
			throw sqex;
		}
		
        this._Logger.info("Table " + this._TableName + " not found to exist");
        return false;
	}
}
