package com.mk.edu.dataaccess;

import java.util.ArrayList;

public interface ITransactionDAO {
	public ArrayList<ITransaction> getTransactions(String where) throws Exception;
}
