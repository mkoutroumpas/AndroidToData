package com.mk.edu;

import java.io.IOException;
import java.util.UUID;
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
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataAccessServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Vector<Transaction> _V = new Vector<Transaction>();
		
		for (int _i = 0; _i < 200; _i++) {
			Transaction _T = new Transaction();
			_T.setID(100000000 + _i);
			_T.setTransactionCode(UUID.randomUUID().toString());
			_V.add(_T);
		}
		
		response.setContentType("application/json");
		response.getWriter().println(new Gson().toJson(_V));
		
	}

}
