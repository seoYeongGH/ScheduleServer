package com.schedule.web;

import static structure.Constant.DUP_ID;
import static structure.Constant.ERR_LOG_ID;
import static structure.Constant.ERR_LOG_PW;
import static structure.Constant.SUCCESS;
import static structure.Constant.ERR;
import static structure.Constant.LOG_IN_SUCCESS;
import static structure.Constant.AUTO_LOG_SUCCESS;

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

import org.mindrot.jbcrypt.BCrypt;

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
		dao = new UserDAO();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session= request.getSession(true);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		

		PrintWriter out = response.getWriter();
		String doing = request.getParameter("doing");
			
		if("chkId".equals(doing)) {
			System.out.println("START_CHKID");
			if(dao.chkIdDup(request.getParameter("id")))
				out.print(DUP_ID);
			else
				out.print(SUCCESS);
		}
		else if("getInviteExist".equals(doing)) {
			out.print(dao.getInviteExist());
		}
		else if("getGroups".equals(doing)) {
			out.print(dao.getGroups());
		}
		else if("deleteGroup".equals(doing)) {
			int groupNum = Integer.parseInt(request.getParameter("groupNum"));
			out.print(dao.deleteGroup(groupNum));
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
		else if("createGroup".equals(doing)) {
			int groupNum = dao.createGroup(request.getParameter("name"));
			String ids = request.getParameter("ids");
			
			if(ids.length()!=0) {
				String[] friendIds = ids.split(",");
				
				groupNum = dao.sendInvite(groupNum, friendIds);
			}
			
			out.println(groupNum);
		}
		else if("getLinkGroups".equals(doing)) {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session= request.getSession(true);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		 PrintWriter out = response.getWriter();
			String doing = request.getParameter("doing");
			
			if("join".equals(doing)) {
				Integer code = dao.insertUser(request.getParameter("id"), BCrypt.hashpw(request.getParameter("password"), BCrypt.gensalt(12)), request.getParameter("name"),request.getParameter("email"));
				out.print(code);
			}
			else if("login".equals(doing)) {
				String id = request.getParameter("id");
				HashMap<String, Object> hashMap = new HashMap<>();
				
				if(!dao.chkIdDup(id)) {
					out.print(ERR_LOG_ID);
				}
				else {
					Integer code = dao.chkPw(id, request.getParameter("password")); 
					if(code==SUCCESS) {
						USession.getInstance().setId(id);
						code = LOG_IN_SUCCESS;
						
						hashMap.put("userCode", dao.getUserCode());
					}
					hashMap.put("code", code);
					
					out.print(hashMap);
				}
			}
			else if("findInfo".equals(doing)) {
				int code = dao.findId(request.getParameter("name"), request.getParameter("email"));
				out.print(code);
			}
			else if("getInfo".equals(doing)) {
				HashMap<String,String> hashInfo = dao.getInfo();
				out.print(hashInfo);
			}
			else if("chkPw".equals(doing)) {
				out.print(dao.chkPw(USession.getInstance().getId(), request.getParameter("password")));
			}
			else if("changePw".equals(doing)) {
				out.print(dao.changePw(USession.getInstance().getId(), BCrypt.hashpw(request.getParameter("newPw"), BCrypt.gensalt(12))));
			}
			else if("changeEmail".equals(doing)) {
				out.print(dao.changeEmail(request.getParameter("email")));
			}
			else if("withdraw".equals(doing)) {
				int code = dao.withdraw();
				
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
			else if("addFriend".equals(doing)) {
				out.print(dao.addFriend(request.getParameter("name"),request.getParameter("id")));
			}
			else if("deleteFriend".equals(doing)) {
				out.print(dao.deleteFriend(request.getParameter("name"), request.getParameter("id")));
			}
			else if("getMembers".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				out.print(dao.getMembers(groupNum));
			}
			else if("sendInvite".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				String ids = request.getParameter("ids");
				
				String[] friendIds = ids.split(",");
				
				out.print(dao.sendInvite(groupNum, friendIds));
			}
			else if("withdrawMember".equals(doing)) {
				int groupNum = Integer.parseInt(request.getParameter("groupNum"));
				String ids = request.getParameter("ids");
				ids = ids.substring(1,ids.length()-1);
				
				String[] 
						friendIds = ids.split("\\)\\(");
				out.print(dao.withdrawMember(groupNum, friendIds));
				
			}
			else if("getName".contentEquals(doing)) {
				out.print(dao.getName());
			}
			
			else if("autoLogin".equals(doing)) {
				System.out.println("user: "+request.getParameter("userCode"));
				out.print(dao.autoLogin(Integer.parseInt(request.getParameter("userCode"))));
			}
			
			else if("getLoginInfo".equals(doing)) {
				HashMap<String, Object> hashMap = new HashMap<>();
				hashMap.put("name", dao.getName());
				hashMap.put("userCode", dao.getUserCode());
				System.out.println(dao.getUserCode());
				out.print(hashMap);
			}
			
	}
	
	

}
