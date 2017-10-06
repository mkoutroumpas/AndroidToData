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

/**
 * Servlet implementation class DataAccessServlet
 */
public class DataAccessServlet extends HttpServlet {
	private final String _JSON_MIMEType = "application/json";
	private final Logger _Logger = LoggerFactory.getLogger(this.getClass());
	
	private String _DatabaseName = "";
	private String _SchemaName = "";
	private String _DatabaseScriptFileName = "";
	private DataSource _DataSource = null;
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataAccessServlet() { 
        super();
    }

	@Override
	public void init() throws ServletException {
		this._SchemaName = this.getInitParameter("SchemaName");
		this._DatabaseName = this.getInitParameter("DatabaseName");
		this._DatabaseScriptFileName = this.getInitParameter("DatabaseScriptFileName");
		
		this._Logger.info("Database to connect to: " + this._DatabaseName);
		this._Logger.info("Selected Database schema: " + this._SchemaName);
		this._Logger.info("Database script file: " + this._DatabaseScriptFileName);
		
		String script = null;
		try {
			script = this.loadDatabaseScript(this._DatabaseScriptFileName);
			this._Logger.info("Successfully loaded script file: " + this._DatabaseScriptFileName);
		} catch (Exception e) { this._Logger.error(e.getMessage()); }
		
		ArrayList<String> statements = null;
		try {
			if (script != null && !script.trim().equals("")) {
				statements = this.getScriptStatements(script, ";");
				this._Logger.info("Successfully loaded script file statements.");
			}
			else
				this._Logger.info("Script file statements not loaded.");
		} catch (Exception e) { this._Logger.error(e.getMessage()); }
		
		if (statements != null && statements.size() > 0) {
			this.executeStatements(statements);
			this._Logger.info(statements.size() + " statements executed successfully.");
		}
		else
			this._Logger.info("No SQL statements found to execute.");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.showTransactions(response);
	}

	/***
	 * Display all Transactions entered.
	 * @param response HttpServletResponse object to write to.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void showTransactions(HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Transaction> _V = null;
		
		try {
			_V = new TransactionDAO(this._DataSource).getTransactions(null);
		}
		catch (Exception e) {
			this._Logger.error(e.getMessage());
			throw new ServletException(e);
		}
		
		if (_V != null && _V.size() > 0) {
			response.setContentType(this._JSON_MIMEType);
			response.getWriter().println(new Gson().toJson(_V));
			return;
		}
		
		this._Logger.info("No entities fetched");
		response.getWriter().println("No entities fetched");
	}
	
	/**
	 * Parse script file to individual SQL script statements.
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
	 * Load SQL script file
	 * @param scriptfilename Name of script file
	 * @return Script file contents
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private String loadDatabaseScript(String scriptfilename) throws IOException {
		if (scriptfilename != null && !scriptfilename.trim().equals("")) {
			InputStream _CreateDBStream = this.getServletContext().getResourceAsStream(scriptfilename);
			
			if (_CreateDBStream != null) {
				this._Logger.info("Successfully loaded file " + scriptfilename);
				
				Scanner _Scanner = new Scanner(_CreateDBStream).useDelimiter("\\A");
				String _ret = _Scanner.hasNext() ? _Scanner.next() : "";
		        _Scanner.close();
		        
		        this._Logger.info("Successfully read contents of file " + scriptfilename);
		        return _ret;
			}
		}
		this._Logger.error("Error loading file " + scriptfilename);
		return null;
	}
	
	/**
	 * Execute a bunch of SQL statements 
	 * @param statements List of SQL statements to execute
	 * @throws ServletException
	 */
	private void executeStatements(ArrayList<String> statements) throws ServletException {
		Connection connection = null;
		
		if (statements != null && statements.size() > 0) {
			try {
				InitialContext ctx = new InitialContext();
				this._DataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/" + this._DatabaseName);
				connection = this._DataSource.getConnection();
				
				for (String st : statements) {
					try {
						connection.prepareStatement(st).execute();
						this._Logger.info("Successfully executed statement: " + st);
					} catch (Exception e) { this._Logger.error("Error executing statement: " + st + ". Error description: " + e.getMessage()); }
				}
			}
			catch (Exception e) {
				this._Logger.error(e.getMessage());
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
	}
	
	/**
	 * Test method
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private void _TestCRUD(HttpServletResponse response) throws ServletException, IOException {
		ArrayList<Transaction> list = new ArrayList<Transaction>();
		Connection connection = null;
		try {
			connection = this._DataSource.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Transaction");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	Transaction t = new Transaction();
                t.setID(rs.getLong(1));
                t.setTransactionValue(rs.getDouble(2));
                t.setTransactionCode(rs.getString(3));
                list.add(t);
            }
            
            this._Logger.info("Successfully fetched " + list.size() + " entities");
        } 
		catch (Exception ex) {
			this._Logger.error(ex.getMessage());
			throw new ServletException(ex);
		}
        finally {
            if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					this._Logger.error(e.getMessage());
					throw new ServletException(e);
				}
            }
        }
		
		if (list.size() > 0) {
			response.setContentType(this._JSON_MIMEType);
			response.getWriter().println(new Gson().toJson(list));
			return;
		}
		
		this._Logger.info("No entities fetched");
		response.getWriter().println("No entities fetched");
	}
	
	/**
	 * Test method
	 * @throws ServletException
	 */
	@SuppressWarnings("unused")
	private void _TestCRUD_Init() throws ServletException {
		Connection connection = null;
		try {
			InitialContext ctx = new InitialContext();
			this._DataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/" + this._DatabaseName);
			connection = this._DataSource.getConnection();
			try {
				connection.prepareStatement("DROP TABLE Transaction").execute();
				this._Logger.info("Table Transaction deleted successfully");
			} catch (Exception e) { this._Logger.error("Error deleting Transaction table: " + e.getMessage()); }
			try {
				connection.prepareStatement("CREATE COLUMN TABLE Transaction " +
					"(ID INTEGER, " +
					"TransactionValue DECIMAL, " +
					"TransactionCode VARCHAR(255), " +
					"PRIMARY KEY (ID))").execute();
				this._Logger.info("Table Transaction created successfully");
			} catch (Exception e) { this._Logger.error("Error creating Transaction table: " + e.getMessage()); }
			try {
				connection.prepareStatement("INSERT INTO Transaction VALUES (1, 0.45, SYSUUID)").execute();
				this._Logger.info("Row inserted successfully into table Transaction");
			} catch (Exception e) { this._Logger.error("Error inserting row in Transaction table: " + e.getMessage()); }
			
		}
		catch (Exception e) {
			this._Logger.error(e.getMessage());
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
	 * Test method
	 */
	@SuppressWarnings("unused")
	private void _TestFileAccess_Init() {
		try {
			String _S = this.loadDatabaseScript(this._DatabaseScriptFileName);
			this._Logger.info("Successfully read contents: " + _S);
		} 
		catch (IOException e) {
			this._Logger.info("Error reading contents: " + e.getMessage());
		}
	}
	
}
