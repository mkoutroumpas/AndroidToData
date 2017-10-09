package com.mk.edu;

import java.util.ArrayList;

public interface ITransactionDAO {
	public ArrayList<ITransaction> getTransactions(String where) throws Exception;
}
