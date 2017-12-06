package com.mk.edu;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mk.edu.dataaccess.Adapter;
import com.mk.edu.dataaccess.Transaction;
import com.mk.edu.dataaccess.TransactionDAO;

/**
 * Access databases (including SAP HANA MDC) through JDBC and return the results.
 */
public class DataAccessServlet extends HttpServlet {
	private final String mJSONMimeType = "application/json";
	private final Logger mLogger = LoggerFactory.getLogger(this.getClass());
	
	private String mDatabaseName = "";
	private String mSchemaName = "";
	private String mDataAccessScheme = "";
	private String mDatabaseScriptFileName = "";
	private DataSource mDataSource = null;
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataAccessServlet() { 
        super();
    }

    /***
     * Initialize the Servlet and prepare it to access the configured DataSource
     */
	@Override
	public void init() throws ServletException {
		this.mSchemaName = this.getInitParameter("SchemaName");
		this.mDatabaseName = this.getInitParameter("DatabaseName");
		this.mDataAccessScheme = this.getInitParameter("DataAccessScheme");
		this.mDatabaseScriptFileName = this.getInitParameter("DatabaseScriptFileName");
		
		this.mLogger.info("Database to connect to: " + this.mDatabaseName);
		this.mLogger.info("Selected Database schema: " + this.mSchemaName);
		this.mLogger.info("Database script file: " + this.mDatabaseScriptFileName);
		
		String script = null;
		try {
			script = this.loadDatabaseScript(this.mDatabaseScriptFileName);
			this.mLogger.info("Successfully loaded script file: " + this.mDatabaseScriptFileName);
		} catch (Exception e) { 
			this.mLogger.error(e.getMessage()); 
		}
		
		ArrayList<String> statements = null;
		try {
			if (script != null && !script.trim().equals("")) {
				statements = this.getScriptStatements(script, ";");
			}
			else
				this.mLogger.info("Script file statements not loaded.");
		} 
		catch (Exception e) { 
			this.mLogger.error(e.getMessage()); 
		}
		
		if (statements != null && statements.size() > 0) {
			this.executeStatements(statements);
			this.mLogger.info(statements.size() + " statements executed successfully.");
		}
		else
			this.mLogger.info("No SQL statements found to execute.");
	}

	/**
	 * TODO: expand it to accept filtering and paging parameters
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.writeTransactions(response);
	}

	/***
	 * Write all Transactions to Response with the configured MIME type.
	 * @param response HttpServletResponse object to write to.
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void writeTransactions(HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Transaction> _Transactions = null;
		
		try {
			_Transactions = (ArrayList<Transaction>) new TransactionDAO("", "Transaction", this.mDataSource).getEntities(null);
		}
		catch (Exception e) {
			this.mLogger.error(e.getMessage());
			throw new ServletException(e);
		}
		
		if (_Transactions != null && _Transactions.size() > 0) {
			response.setContentType(this.mJSONMimeType);
			response.getWriter().println(new Gson().toJson(_Transactions));
			return;
		}
		
		this.mLogger.info("No Transactions fetched");
		response.getWriter().println("No Transactions fetched");
	}
	
	/**
	 * Simple parser of script file to individual SQL script statements.
	 * @param scriptcontent Whole content of script file
	 * @param delimiter Statement delimiter
	 * @return List of SQL statements
	 */
	private ArrayList<String> getScriptStatements(String scriptcontent, String delimiter) {
		if (delimiter == null || delimiter.trim().equals("")) 
			delimiter = ";";
		
		ArrayList<String> _ret = new ArrayList<String>();
		if (scriptcontent != null && !scriptcontent.trim().equals("")) {
			for (String sa : scriptcontent.split(delimiter)) 
				if (!sa.trim().equals(""))
					_ret.add(sa);
		}
		
		return _ret;
	}
	
	/**
	 * Load SQL script file bundled in this JAR
	 * @param scriptfilename Name of script file
	 * @return Script file contents
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private String loadDatabaseScript(String scriptfilename) throws IOException {
		if (scriptfilename != null && !scriptfilename.trim().equals("")) {
			InputStream _CreateDBStream = this.getServletContext().getResourceAsStream(scriptfilename);
			
			if (_CreateDBStream != null) {
				Scanner _Scanner = new Scanner(_CreateDBStream).useDelimiter("\\A");
				String _ret = _Scanner.hasNext() ? _Scanner.next() : "";
				_Scanner.close();
				return _ret;
			}
		}
		this.mLogger.error("Error loading file " + scriptfilename);
		return null;
	}
	
	/**
	 * Execute a series of SQL statements 
	 * @param statements List of SQL statements to execute
	 * @throws ServletException
	 */
	private void executeStatements(ArrayList<String> statements) throws ServletException {
		Connection connection = null;
		
		if (statements != null && statements.size() > 0) {
			try {
				InitialContext ctx = new InitialContext();
				this.mDataSource = new Adapter().getDataSource(ctx, this.mDataAccessScheme, this.mDatabaseName);
				connection = this.mDataSource.getConnection();
				
				for (String st : statements) {
					try {
						connection.prepareStatement(st).execute();
					} catch (Exception e) { 
						this.mLogger.error("Error executing statement: " + st + ". Error description: " + e.getMessage()); 
					}
				}
			}
			catch (Exception e) {
				this.mLogger.error(e.getMessage());
				throw new ServletException(e);
			}
			finally {
				if (connection != null){
					try {
						connection.close();
					} 
					catch (SQLException sqex) { 
						throw new ServletException(sqex); 
					}
				}
			}
		}
	}
	
	/**
	 * Test method: standard CRUD operations primarily with SAP HANA MDC
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	@Test
	private void _TestCRUD(HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Transaction> list = new ArrayList<Transaction>();
		Connection connection = null;
		try {
			connection = this.mDataSource.getConnection();
		    	PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Transaction");
		    	ResultSet rs = pstmt.executeQuery();
		    	while (rs.next()) {
				Transaction t = new Transaction();
				t.setID(rs.getLong(1));
				t.setTransactionValue(rs.getDouble(2));
				t.setTransactionCode(rs.getString(3));
				list.add(t);
		    	}
        	} 
		catch (Exception ex) {
			this.mLogger.error(ex.getMessage());
			throw new ServletException(ex);
		}
		finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					this.mLogger.error(e.getMessage());
					throw new ServletException(e);
				}
			}
		}

		if (list.size() > 0) {
			response.setContentType(this.mJSONMimeType);
			response.getWriter().println(new Gson().toJson(list));
			return;
		}

		this.mLogger.info("No entities fetched");
		response.getWriter().println("No entities fetched");
	}
	
	/**
	 * Test method: standard CRUD operations primarily with SAP HANA MDC (initialization)
	 * @throws ServletException
	 */
	@SuppressWarnings("unused")
	@Test
	private void _TestCRUD_Init() throws ServletException {
		Connection connection = null;
		try {
			InitialContext ctx = new InitialContext();
			this.mDataSource = new Adapter().getDataSource(ctx, this.mDataAccessScheme, this.mDatabaseName);
			connection = this.mDataSource.getConnection();
			try {
				connection.prepareStatement("DROP TABLE Transaction").execute();
			} 
			catch (Exception e) { 
				this.mLogger.error("Error deleting Transaction table: " + e.getMessage()); 
			}
			try {
				connection.prepareStatement("CREATE COLUMN TABLE Transaction " +
					"(ID INTEGER, " +
					"TransactionValue DECIMAL, " +
					"TransactionCode VARCHAR(255), " +
					"PRIMARY KEY (ID))").execute();
			} catch (Exception e) { 
				this.mLogger.error("Error creating Transaction table: " + e.getMessage()); 
			}
			try {
				connection.prepareStatement("INSERT INTO Transaction VALUES (1, 0.45, SYSUUID)").execute();
			} catch (Exception e) { 
				this.mLogger.error("Error inserting row in Transaction table: " + e.getMessage()); 
			}
			
		}
		catch (Exception e) {
			this.mLogger.error(e.getMessage());
            		throw new ServletException(e);
        	}
		finally {
			if (connection != null){
				try {
					connection.close();
				} 
				catch (SQLException sqex) { throw new ServletException(sqex); }
			}
		}
	}
	
	/**
	 * Test method: loading resources from this JAR
	 */
	@SuppressWarnings("unused")
	@Test
	private void _TestFileAccess_Init() {
		try {
			String _S = this.loadDatabaseScript(this.mDatabaseScriptFileName);
		} 
		catch (IOException e) {
			this.mLogger.info("Error reading contents: " + e.getMessage());
		}
	}
}
