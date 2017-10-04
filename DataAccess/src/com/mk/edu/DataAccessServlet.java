package com.mk.edu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet implementation class DataAccessServlet
 */
public class DataAccessServlet extends HttpServlet {
	private final String _DatabaseName = "testdb";
	private final String _JSON_MIMEType = "application/json";
	private final String _HTML_MIMEType = "text/html";
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataAccessServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Vector<Transaction> _V = null;
				
		try {
			TransactionDAO _TransactionDAO = new TransactionDAO(null, this.getCreateTransactionsDB_Script());
			_V = _TransactionDAO.getTransactions();
		} 
		catch (SQLException ex) {
			response.setContentType(this._HTML_MIMEType);
			response.getWriter().println(this.formatError(ex));
			return;
		}
		
		response.setContentType(this._JSON_MIMEType);
		response.getWriter().println(new Gson().toJson(_V));
		
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
