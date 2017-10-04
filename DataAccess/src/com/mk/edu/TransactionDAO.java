package com.mk.edu;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

public class TransactionDAO {
	private final DataSource _DataSource;
	private final String _TableName = Transaction.class.getSimpleName();
	private final String _SQLCreateStatement;
	
	public TransactionDAO(DataSource dataSource, String sqlcreatestatement) throws SQLException {
		this._DataSource = dataSource;
		this._SQLCreateStatement = sqlcreatestatement;
		this._verifyTable();
	}

	private void _verifyTable() throws SQLException {
		Connection _Connection = null;

        try {
        	_Connection = this._DataSource.getConnection();
            if (!this._tableExists(_Connection)) 
                this._createTable(_Connection);
        } 
        finally {
            if (_Connection != null)
            	_Connection.close();
        }
	}

	public ArrayList<Transaction> getTransactions(String where) throws SQLException {
		Connection _Connection = this._DataSource.getConnection();
        try {
            PreparedStatement pstmt = _Connection.prepareStatement("SELECT * FROM " + this._TableName + (where == null ? "" : " " + where));
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Transaction> list = new ArrayList<Transaction>();
            while (rs.next()) {
            	Transaction t = new Transaction();
                t.setID(rs.getLong(0));
                t.setTransactionValue(rs.getDouble(1));
                t.setTransactionCode(rs.getString(2));
                list.add(t);
            }
            return list;
        } 
        finally {
            if (_Connection != null) 
            	_Connection.close();
        }
	}
	
	private void _createTable(Connection connection) throws SQLException {
		if (connection != null && this._SQLCreateStatement != null) 
			connection.prepareStatement(this._SQLCreateStatement).executeUpdate();
	}

	private boolean _tableExists(Connection connection) throws SQLException {
		if (connection == null) return false;
		DatabaseMetaData _DatabaseMetaData = connection.getMetaData();
        ResultSet rs = _DatabaseMetaData.getTables(null, null, this._TableName, null);
        while (rs.next()) {
            String _Name = rs.getString("TABLE_NAME");
            if (_Name.equals(this._TableName)) 
                return true;
        }
        
        return false;
	}
}