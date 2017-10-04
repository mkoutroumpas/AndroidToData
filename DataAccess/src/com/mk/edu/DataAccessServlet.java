package com.mk.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.Gson;

/**
 * Servlet implementation class DataAccessServlet
 */
public class DataAccessServlet extends HttpServlet {
	private final String _JSON_MIMEType = "application/json";
	
	private String _DatabaseName = "";
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
		this._DatabaseName = this.getInitParameter("DatabaseName");
		
		try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/" + this._DatabaseName);
            this._TransactionDAO = new TransactionDAO(ds, this.getCreateTransactionsDB_Script());
		} 
		catch (IOException ioex) {
			throw new ServletException(ioex);
        } 
		catch (SQLException e) {
            throw new ServletException(e);
        } 
		catch (NamingException e) {
            throw new ServletException(e);
        }
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ArrayList<Transaction> _V = null;
				
		try {
			_V = _TransactionDAO.getTransactions(null);
		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		
		if (_V != null && _V.size() > 0) {
			response.setContentType(this._JSON_MIMEType);
			response.getWriter().println(new Gson().toJson(_V));
			return;
		}
		
		response.getWriter().println(this.getClass().getSimpleName() + ": " + "No data");
	}
	
	private String getCreateTransactionsDB_Script() throws IOException {
		InputStream _Create_TransactionsDB = this.getServletContext().getResourceAsStream("/documents/_Create_TransactionsDB.sql");
		BufferedReader _Reader = new BufferedReader(new InputStreamReader(_Create_TransactionsDB));
        StringBuilder _SB = new StringBuilder();
        String _Line;
        while ((_Line = _Reader.readLine()) != null) 
        	_SB.append(_Line);
        _Reader.close();
        
        return _SB.toString();
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

}
