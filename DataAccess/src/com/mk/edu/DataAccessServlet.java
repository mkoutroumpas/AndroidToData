package com.mk.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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
	private DataSource _DataSource = null;
	private TransactionDAO _TransactionDAO;
	
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
		
		this._Logger.info("Database to connect to: " + this._DatabaseName);
		
		this._TestCRUD_Init();
		
		/*try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + this._DatabaseName);
            if (ds != null)
            	this._Logger.info("Database lookup successful");
            this._TransactionDAO = new TransactionDAO(ds, this.getCreateTransactionsDB_Script_Hardcoded(this._SchemaName), this._SchemaName);
            if (this._TransactionDAO != null)
            	this._Logger.info("Datasource initialization successful");
		} 
		catch (IOException ioex) {
			this._Logger.error(ioex.getMessage());
			throw new ServletException(ioex);
        } 
		catch (SQLException e) {
			this._Logger.error(e.getMessage());
            throw new ServletException(e);
        } 
		catch (NamingException e) {
			this._Logger.error(e.getMessage());
            throw new ServletException(e);
        }*/
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this._TestCRUD(response);
		
		/*ArrayList<Transaction> _V = null;
				
		try {
			_V = _TransactionDAO.getTransactions(null);
		}
		catch (SQLException e) {
			this._Logger.error(e.getMessage());
			throw new ServletException(e);
		}
		
		if (_V != null && _V.size() > 0) {
			response.setContentType(this._JSON_MIMEType);
			response.getWriter().println(new Gson().toJson(_V));
			return;
		}
		
		this._Logger.info("No entities fetched");
		response.getWriter().println("No entities fetched");*/
	}
	
	private String getCreateTransactionsDB_Script() throws IOException {
		String _ScriptFile = "_Create_TransactionsDB.sql";
		InputStream _Create_TransactionsDB = this.getServletContext().getResourceAsStream(_ScriptFile);
		
		//Thread.currentThread().getContextClassLoader().getResourceAsStream(name)
		
		this._Logger.info("this.getServletContext() is null: " + (this.getServletContext() == null));
		this._Logger.info("_Create_TransactionsDB is null: " + (_Create_TransactionsDB == null));
		
		if (_Create_TransactionsDB != null) {
			this._Logger.info("Successfully loaded file " + _ScriptFile);
			
			Scanner _Scanner = new Scanner(_Create_TransactionsDB).useDelimiter("\\A");
			String _ret = _Scanner.hasNext() ? _Scanner.next() : "";
	        _Scanner.close();
	        
	        this._Logger.info("Successfully read contents of file " + _ScriptFile);
	        return _ret;
		}
		
		this._Logger.error("Error loading file " + _ScriptFile);
		return null;
	}
	
	private String getCreateTransactionsDB_Script_Hardcoded(String schemaname) throws IOException {
		if (schemaname == null) schemaname = "";
		return "DROP TABLE " + schemaname + ".Transaction;" +
			"CREATE COLUMN TABLE " + schemaname + ".Transaction (" +
				"ID INTEGER," +
				"TransactionValue DECIMAL," +
				"TransactionCode VARCHAR(255)," +
				"PRIMARY KEY (ID));" +
				" " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (1, 0.45, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (2, 23.3342, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (3, 1.23, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (4, 0, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (5, 15.01, SYSUUID); " + 
				"INSERT INTO " + schemaname + ".Transaction VALUES (6, 7, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (7, 2.223, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (8, 83.234, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (9, 9.0023, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (10, 1.001, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (11, 1.004, SYSUUID); " + 
				"INSERT INTO " + schemaname + ".Transaction VALUES (12, 0, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (13, 0, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (14, 63.03, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (15, 14.933, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (16, 0.3409, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (17, -23.34, SYSUUID);" +  
				"INSERT INTO " + schemaname + ".Transaction VALUES (18, 0, SYSUUID);  " +
				"INSERT INTO " + schemaname + ".Transaction VALUES (19, -4.009, SYSUUID);" +
				"INSERT INTO " + schemaname + ".Transaction VALUES (20, 1.0231, SYSUUID);";
	}
	
	/**
	 * See: https://www.avajava.com/tutorials/lessons/how-do-i-display-a-stack-trace-on-a-web-page.html
	 * @param t
	 * @return
	 */
	public String formatError(Throwable t) { 
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString().replace(System.getProperty("line.separator"), "<br/>\n");
	}

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
	
}
