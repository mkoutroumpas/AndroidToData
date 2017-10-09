package com.mk.edu.dataaccess;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Adapter implements IAdapter {
	/***
	 * Get a proper DataSource depending on the JDBC scheme and database name.
	 */
	@Override
	public DataSource getDataSource(InitialContext initialContext, String scheme, String databaseName) throws NamingException {
		if (initialContext != null && scheme != null && !scheme.trim().equals("")
				&& databaseName != null && !databaseName.trim().equals(""))
			return (DataSource) initialContext.lookup(scheme + databaseName);
		return null;
	}

}
