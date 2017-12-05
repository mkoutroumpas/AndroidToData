package com.mk.edu.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EntityExchange implements IEntityExchange {
	protected final DataSource _DataSource;
	
	protected Logger mLogger = LoggerFactory.getLogger(this.getClass());
	protected String mSchemaName = "";
	protected String mTableName = "";
	
	public EntityExchange(String schemaName, String tableName, DataSource dataSource) {
		this.mSchemaName = schemaName;
		this.mTableName = tableName;
		this.mDataSource = dataSource;
	}
	
	/**
	 * Simply fetch Entities rows from the configured table by executing a single SELECT statement.
	 * @param where A valid WHERE SQL statement.
	 * @return List of Entities implementing IDataEntity.
	 * @throws Exception
	 */
	@Override
	public ArrayList<? extends IDataEntity> getEntities(String where) throws Exception {
		Connection _Connection = null;
		ArrayList<? extends IDataEntity> _ret = null;
		
		try {
			_Connection = this._DataSource.getConnection();
			PreparedStatement pstmt = _Connection.prepareStatement("SELECT * FROM " + 
				(this._SchemaName != null && !this._SchemaName.trim().equals("") ? this._SchemaName + "." : "") + this._TableName + (where == null ? "" : " " + where));
			ResultSet rs = pstmt.executeQuery();
			_ret = this.mapEntities(rs);

			this.mLogger.info("Successfully fetched " + _ret.size() + " entities");
		} 
		catch (Exception ex) { 
			this.mLogger.error(ex.getMessage());
			throw ex;
		}
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
        
        	return _ret;
	}
	
	/**
	 * Map ResultSet contents to Entities list (force implementation on subclasses).
	 * @param rs The ResultSet to draw the Entities from
	 * @return The list of Entities
	 */
	protected abstract ArrayList<? extends IDataEntity> mapEntities(ResultSet rs) throws SQLException;

}
