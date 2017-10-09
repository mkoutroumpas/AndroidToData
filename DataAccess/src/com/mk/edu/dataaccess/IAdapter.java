package com.mk.edu.dataaccess;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public interface IAdapter {
	public DataSource getDataSource(InitialContext initialContext, String scheme, String databaseName) throws NamingException; 
}
