package com.schedule.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.schedule.web.Constant.DUP_ID;
import static com.schedule.web.Constant.SUCCESS;
import static com.schedule.web.Constant.ERR_LOG_ID;
import static com.schedule.web.Constant.ERR_LOG_PW;

import domain.UserDAO;


/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/user.do")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO dao;
	
    public UserServlet() {}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session= request.getSession();
		
		dao = new UserDAO();
		 PrintWriter out = response.getWriter();
			
			String doing = request.getParameter("doing");
			System.out.println(doing);
			if("chkId".equals(doing)) {
				if(dao.chkIdDup(request.getParameter("id")))
					out.print(DUP_ID);
				else
					out.print(SUCCESS);
			}
			else if("join".equals(doing)) {
				Integer code = dao.insertUser(request.getParameter("id"), request.getParameter("password"),request.getParameter("name"),request.getParameter("email"));
				out.print(code);
			}
			else if("login".equals(doing)) {
				String id = request.getParameter("id");
				
				if(!dao.chkIdDup(id)) {
					out.print(ERR_LOG_ID);
				}
				else {
					Integer code = dao.chkPw(id, request.getParameter("password")); 
					out.print(code);
				}
			}
			
	}

}
