package com.mk.edu.dataaccess;

import java.util.ArrayList;

/**
 * Common interface for Entity exchange DAOs.
 */
public interface IEntityExchange {
	public ArrayList<? extends IDataEntity> getEntities(String where) throws Exception;
}
