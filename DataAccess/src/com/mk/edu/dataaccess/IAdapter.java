package com.mk.edu.dataaccess;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/***
 * Common interface for data Adapters
 */
public interface IAdapter {
	public DataSource getDataSource(InitialContext initialContext, String scheme, String databaseName) throws NamingException; 
}
