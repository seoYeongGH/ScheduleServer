package com.schedule.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

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
		HttpSession session= request.getSession(true);
		
		dao = new UserDAO();
		 PrintWriter out = response.getWriter();
			
			String doing = request.getParameter("doing");
			System.out.println("Doing: "+doing);
			
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
					if(code==SUCCESS) {
						//session.setAttribute("id", id);
						USession.getInstance().setId(id);
					}
					System.out.println("Login ID: "+USession.getInstance().getId());
					out.print(code);
				}
			}
			else if("findId".equals(doing)) {
				int code = dao.findId(request.getParameter("name"), request.getParameter("email"));
				out.print(code);
			}
			else if("findPw".equals(doing)) {
				int code = dao.findPw(request.getParameter("name"), request.getParameter("email"),request.getParameter("id"));
				out.print(code);

			}
			else if("getInfo".equals(doing)) {
				HashMap<String,String> hashInfo = dao.getInfo(request.getParameter("id"));
				System.out.println("Info: "+ hashInfo.get("name"));
				out.print(hashInfo);
			}
			else if("changePw".equals(doing)) {
				Integer code = dao.chkPw(USession.getInstance().getId(), request.getParameter("oldPw"));
				if(code == SUCCESS) {
					code = dao.changePw(USession.getInstance().getId(), request.getParameter("newPw"));
				}
				
				out.print(code);
			}
			else if("withdraw".equals(doing)) {
				Integer code = dao.chkPw(USession.getInstance().getId(), request.getParameter("password"));
				if(code == SUCCESS) {
					USession.getInstance().setId(null);
					USession.getInstance().setIsLogin(false);
				}
				out.print(code);
			}
			
			
	}

}
