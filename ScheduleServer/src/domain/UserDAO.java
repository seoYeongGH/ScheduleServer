package domain;

import static structure.Constant.DUP_ID;
import static structure.Constant.DUP_USER;
import static structure.Constant.ERR;
import static structure.Constant.ERR_LOG_PW;
import static structure.Constant.NO_DATA;
import static structure.Constant.SUCCESS;
import static structure.Constant.EXIST_INVITE;
import static structure.Constant.NO_EXIST_INVITE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.json.JSONException;

import com.schedule.mail.MailSender;

import structure.FriendObject;
import structure.GroupObject;
import structure.InviteObject;
import structure.USession;

public class UserDAO {
	static {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("Connect");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private Connection getConnection() {
		DataSource ds = null;
		Connection con = null;

		try {
			Context ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/Oracle");
			con = ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return con;
	}

	private void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
			}
		}

		return;
	}

	public boolean chkIdDup(String id) {
		Connection con = null;
		boolean flag = false;

		try {
			con = getConnection();
			String sql = "select friend from usertable where id=?";

			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next())
				flag = true;

			else
				flag = false;
		} catch (Exception e) {
			System.out.println("CHK_ID_EXP: " + e.toString());
		} finally {
			closeConnection(con);
		}

		return flag;
	}
	
	public int chkPw(String id, String pw) {
		Connection con = null;
		int code = ERR;
		
		try {
			con = getConnection();
			System.out.println("CHK ID: "+id);
			System.out.println("CHK PW: "+pw);
			
			String sql = "select password from usertable where id=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				if(pw.equals(rs.getString("password")))
					code = SUCCESS;
				else
					code = ERR_LOG_PW;
			}
			else {
				code = ERR;
			}
		}catch(Exception e) {
			System.out.println("PW_CHK_ERR: "+e.toString());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	public int insertUser(String id, String pw, String name, String email) {
		Connection con = null;
		int flag = SUCCESS;

		try {
			con = getConnection();

			if (chkUserDup(id, name, email))
				return DUP_USER;

			if (chkIdDup(id))
				return DUP_ID;

			String sql = "insert into usertable values(?,?,?,?,?)";

			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			pstmt.setString(3, name);
			pstmt.setString(4, email);
			pstmt.setInt(5, 0);
			pstmt.executeQuery();


		} catch (Exception e) {
			System.out.println("INSERT_USER_EXP: " + e.getMessage());
			flag = ERR;
		} finally {
			closeConnection(con);
		}

		return flag;
	}

	private boolean chkUserDup(String id, String name, String email) {
		Connection con = null;
		boolean flag = false;

		try {
			con = getConnection();

			String sql = "select friend from usertable where email=? and name=?";

			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, email);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next())
				flag = true;
			else
				flag = false;

		} catch (Exception e) {
			System.out.println("CHK_USER_DATA_EXP: " + e.getMessage());
		} finally {
			closeConnection(con);
		}

		return flag;
	}
	
	public int findId(String name, String email) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "select id from usertable where name=? and email=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, email);
			
			ResultSet rs = pstmt.executeQuery();
			String id;
			
			if(rs.next()) {
				MailSender sender = MailSender.getInstance();
				id = rs.getString("id");
				
				sender.sendMail(email, "ScheduleApp�뿉�꽌 id瑜� �븣�젮�뱶由쎈땲�떎." ,name+"�떂�쓽 scheduleApp id�뒗 ["+id+"]�엯�땲�떎.");
			}
			else {
				code = NO_DATA;
			}
		}catch(Exception e) {
			code = ERR;
			System.out.println("FIND_ID_ERR: "+e.toString());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public int findPw(String name, String email, String id) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "select password from usertable where name=? and email=? and id=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, email);
			pstmt.setString(3, id);
			
			ResultSet rs = pstmt.executeQuery();
			String pw;
			
			if(rs.next()) {
				MailSender sender = MailSender.getInstance();
				pw = rs.getString("password");
				
				sender.sendMail(email, "SchedleApp�뿉�꽌 鍮꾨�踰덊샇瑜� �븣�젮�뱶由쎈땲�떎.", name+"�떂("+id+"�쓽 鍮꾨�踰덊샇�뒗 "+"["+pw+"] �엯�땲�떎.");
			}
			else {
				code = NO_DATA;
			}
		}catch(Exception e) {
			code = ERR;
			System.out.println("FIND_PW_ERR: "+e.toString());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public HashMap getInfo(String id) {
		Connection con = null;
		HashMap<String,String> hashMap = new HashMap<String,String>();
		
		try {
			con = getConnection();
			
			String sql = "select name,email from usertable where id=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				hashMap.put("name",rs.getString("name"));
				hashMap.put("email", rs.getString("email"));
			}

			sql = "select count(*) from invitetable where userid=?";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			rs = pstmt.executeQuery();
			if(rs.next()) {
				if(rs.getInt(1)>0)
					hashMap.put("invite","exist");
				else
					hashMap.put("invite", "noExist");
			}
			
		}catch(Exception e) {
			System.out.println("GET_INFO_ERR: "+e.toString());
		}finally {
			closeConnection(con);
		}
		
		return hashMap;
	}
	
	public Integer changePw(String id, String pw) {
		Connection con = null;
		Integer code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "update usertable set password=? where id=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, pw);
			pstmt.setString(2, id);
			
			pstmt.executeQuery();
		}catch(Exception e) {
			System.out.println("CHANGE_PW_ERR: "+e.toString());
			code = ERR;
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public int chkFrdExist(String frdName, String frdId) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "select friend from usertable where id=? and name=?";

			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, frdId);
			pstmt.setString(2, frdName);
			
			ResultSet rs = pstmt.executeQuery();
			if(!rs.next()) {
				code = NO_DATA;
			}
		}catch(SQLException e) {
			code = ERR;
			System.out.println("CHK_FRD_EXIST_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		return code;
	}
	
	public ArrayList<FriendObject> getFriends() {
		Connection con = null;
		ArrayList<FriendObject> list = new ArrayList<FriendObject>();
		
		try {
			con = getConnection();
			
			String sql = "select friendid, friendname from friendtable where userid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				FriendObject obj = new FriendObject();
				
				obj.put("name",rs.getString("friendname"));
				obj.put("id", rs.getString("friendid"));
				list.add(obj);
			}
		}catch(Exception e) {
			System.out.println("GET_FRIEND_EXP: "+e.getMessage());
			e.printStackTrace();
		}finally {
			closeConnection(con);
		}
		return list;
	}
	
	public HashMap getGroups(){
		Connection con = null;
		HashMap<String,ArrayList<GroupObject>> groups = new HashMap();
		
		try {
			con = getConnection();
			
			String sql = "select b.groupnum,b.groupname,b.managerid "
					+ "from grouptable a join groupproto b on a.groupnum = b.groupnum where memberid=?"
					+ "order by b.groupname";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ArrayList<GroupObject> isManager = new ArrayList<GroupObject>();
			ArrayList<GroupObject> notManager = new ArrayList<GroupObject>();
			String id = USession.getInstance().getId();
			
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				GroupObject obj = new GroupObject();
				obj.put("groupNum",rs.getInt("groupnum"));
				obj.put("groupName",rs.getString("groupname"));
				
				if(id.equals(rs.getString("managerid")))
					isManager.add(obj);
				else
					notManager.add(obj);
			}
			
			groups.put("isManager",isManager);
			groups.put("notManager",notManager);
		}catch(Exception e) {
			System.out.println("GET_GROUP_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return groups;
	}
	
	public int addFriend(String name, String id) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "insert into friendtable values(?,?,?)";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			pstmt.setString(2, id);
			pstmt.setString(3, name);
			
			pstmt.executeQuery();
		}catch(SQLException e) {
			code = ERR;
			System.out.println("ADD_FRIEND_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public int deleteFriend(String name, String id) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "delete from friendtable where userid=? and friendid=? and friendname=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1,USession.getInstance().getId());
			pstmt.setString(2, id);
			pstmt.setString(3, name);
			
			pstmt.executeQuery();
		}catch(SQLException e) {
			code = ERR;
			System.out.println("DELETE_FRIEND_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public int deleteGroup(int groupNum) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "delete from grouptable where groupnum=? and memberid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			pstmt.setString(2, USession.getInstance().getId());
			
			pstmt.execute();
			
			sql = "select count(*) from grouptable where groupnum=?";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				if(rs.getInt(1)<=0) {
					sql = "delete from groupproto where groupnum=?";
					
					pstmt = con.prepareStatement(sql);
					pstmt.setInt(1, groupNum);
					pstmt.execute();
					
					sql = "update groupproto set groupnum=(groupnum-1) where groupnum>?";
					
					pstmt = con.prepareStatement(sql);
					pstmt.setInt(1, groupNum);
					pstmt.execute();
				}
			}
		}catch(SQLException e) {
			code = ERR;
			System.out.println("DEL_GROUP_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public int addMember(int groupNum) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "insert into grouptable values(?,?)";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			pstmt.setString(2, USession.getInstance().getId());
			pstmt.execute();
		}catch(SQLException e) {
			code = ERR;
			System.out.println("ADD_MEMBER_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public int sendInvite(int groupNum,String[] friends) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "insert into invitetable values(?,?)";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
		
			int size = friends.length;
			for(int i=0; i<size; i++) {
				pstmt.setString(2,friends[i]);
				pstmt.execute();
			}
		}catch(SQLException e) {
			code = ERR;
			System.out.println("SEND_INVITE_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	public int createGroup(String name, String[] friends) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "select count(*) from groupproto";
			int groupNum = -1;

			PreparedStatement pstmt = con.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next())
				groupNum = rs.getInt(1)+1;
			else {
				code = ERR;
			}
			
			String id = USession.getInstance().getId();
			sql = "insert into groupproto values(?,?,?)";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			pstmt.setString(2,name);
			pstmt.setString(3, id);
			pstmt.executeQuery();
			
			code = addMember(groupNum);
			
			if(code == SUCCESS) 
				code = sendInvite(groupNum,friends);
			
		}catch(SQLException e) {
			code = ERR;
			System.out.println("CREATE_GROUP_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}

	
	public ArrayList<InviteObject> getInvites(){
		Connection con = null;
		ArrayList<InviteObject> invites = new ArrayList<InviteObject>();
		
		try {
			con = getConnection();
			
			String sql = "select b.groupnum,b.groupname,b.managerid "
					+ "from invitetable a join groupproto b on a.groupnum = b.groupnum where userid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				InviteObject obj = new InviteObject();
				
				obj.put("groupNum",rs.getInt("groupnum"));
				obj.put("groupName",rs.getString("groupname"));
				obj.put("managerId",rs.getString("managerid"));
				invites.add(obj);
			}
		}catch(Exception e) {
			System.out.println("GET_INVITE_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return invites;
	}
	
	public int deleteInvite(int groupNum) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "delete from invitetable where groupnum=? and userid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			pstmt.setString(2, USession.getInstance().getId());
			pstmt.execute();
		}catch(SQLException e) {
			code = ERR;
			System.out.println("DEL_INVITE_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public ArrayList<FriendObject> getMembers(int groupNum){
		Connection con = null;
		ArrayList<FriendObject> friends = new ArrayList<FriendObject>();
		
		try {
			con = getConnection();
			
			String sql = "select b.name, b.id from grouptable a join usertable b "
					+ "on a.memberid = b.id where groupnum=? order by b.name";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				FriendObject obj = new FriendObject();
				obj.put("name", rs.getString("name"));
				obj.put("id", rs.getString("id"));
				
				friends.add(obj);
			}
			
		}catch(Exception e) {
			System.out.println("GET_NAMES_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return friends;
	}
	
	public int withdrawMember(int groupNum, String[] ids) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "delete from grouptable where groupnum=? and memberid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			
			int idSize = ids.length;
			for(int i=0; i<idSize; i++) {
				pstmt.setString(2, ids[i]);
				pstmt.execute();
			}
		}catch(SQLException e) {
			code = ERR;
			System.out.println("WITHDRAW_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public ArrayList<Integer> getGroupNums(){
		Connection con = null;
		ArrayList<Integer> groupNums = new ArrayList<Integer>();
		
		try {
			con = getConnection();
			
			String sql = "select groupnum from linktable where userid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				groupNums.add(rs.getInt(1));
			}
		}catch(SQLException e) {
			System.out.println("GET_GPNUM_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return groupNums;
		
}
	public int connectGroup(int groupNum) {
		Connection con  = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "insert into linktable values(?,?)";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1,groupNum);
			pstmt.setString(2,USession.getInstance().getId());
			pstmt.execute();
		}catch(SQLException e) {
			code = ERR;
			System.out.println("CONNECT_GROUP_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
	
	public int disConnectGroup(int groupNum) {
		Connection con  = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "delete from linktable where groupNum=? and userid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1,groupNum);
			pstmt.setString(2,USession.getInstance().getId());
			pstmt.execute();
		}catch(SQLException e) {
			code = ERR;
			System.out.println("DISCON_GROUP_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}
}
