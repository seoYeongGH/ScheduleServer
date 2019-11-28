package com.schedule.web;

import static structure.Constant.DUP_ID;
import static structure.Constant.ERR_LOG_ID;
import static structure.Constant.ERR_LOG_PW;
import static structure.Constant.SUCCESS;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import domain.UserDAO;
import structure.FriendObject;
import structure.InviteObject;
import structure.USession;


/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/user.do")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO dao;
	
    public UserServlet() {
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session= request.getSession(true);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		dao = new UserDAO();
		 PrintWriter out = response.getWriter();
			
			String doing = request.getParameter("doing");
			
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
			else if("findFriend".equals(doing)) {
				int code = dao.chkFrdExist(request.getParameter("name"), request.getParameter("id"));
				out.print(code);
			}
			else if("getFriends".equals(doing)) {
				out.print(dao.getFriends());
			}
			else if("getGroups".equals(doing)) {
				out.print(dao.getGroups());
			}
			else if("addFriend".equals(doing)) {
				out.print(dao.addFriend(request.getParameter("name"),request.getParameter("id")));
			}
			else if("deleteFriend".equals(doing)) {
				out.print(dao.deleteFriend(request.getParameter("name"), request.getParameter("id")));
			}
			else if("deleteGroup".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				out.print(dao.deleteGroup(groupNum));
			}
			else if("createGroup".equals(doing)) {
				String ids = request.getParameter("ids");
				ids = ids.substring(1,ids.length()-1);
				
				String[] friendIds = ids.split("\\)\\(");
				
				out.print(dao.createGroup(request.getParameter("name"), friendIds));
			}
			else if("getInvites".equals(doing)) {
				out.print(dao.getInvites());
			}
			else if("acceptInvite".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				int code = dao.addMember(groupNum);
				
				if(code == SUCCESS)
					out.print(dao.deleteInvite(groupNum));
			}
			else if("denyInvite".equals(doing)) {
				out.print(dao.deleteInvite(Integer.parseInt(request.getParameter("groupNum"))));
			}
			else if("getMembers".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				out.print(dao.getMembers(groupNum));
			}
			else if("sendInvite".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				String ids = request.getParameter("ids");
				ids = ids.substring(1,ids.length()-1);
				
				String[] friendIds = ids.split("\\)\\(");
				
				out.print(dao.sendInvite(groupNum, friendIds));
			}
			else if("withdrawMember".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				String ids = request.getParameter("ids");
				ids = ids.substring(1,ids.length()-1);
				
				String[] friendIds = ids.split("\\)\\(");
				out.print(dao.withdrawMember(groupNum, friendIds));
				
			}
			else if("getGroupNums".equals(doing)) {
				out.print(dao.getGroupNums());
			}
			else if("connectGroup".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				out.print(dao.connectGroup(groupNum));
			}
			else if("disconnectGroup".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				out.print(dao.disConnectGroup(groupNum));
			}
			
	}

}
