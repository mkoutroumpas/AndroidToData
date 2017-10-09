
package com.mk.edu.dataaccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

public class TransactionDAO extends EntityExchange {
	public TransactionDAO(String schemaName, String tableName, DataSource dataSource) {
		super(schemaName, Transaction.class.getSimpleName(), dataSource);
		this._Logger = LoggerFactory.getLogger(this.getClass());
	}
	
	/**
	 * Map ResultSet contents to Transaction entities.
	 */
	@Override
	protected ArrayList<? extends IDataEntity> mapEntities(ResultSet rs) throws SQLException {
		ArrayList<Transaction> _ret = new ArrayList<Transaction>();
		
		while (rs.next()) {
	    	Transaction _T = new Transaction();
	        _T.setID(rs.getLong(1));
	        _T.setTransactionValue(rs.getDouble(2));
	        _T.setTransactionCode(rs.getString(3));
	        _ret.add(_T);
	    }
		
		return _ret;
	}
}
