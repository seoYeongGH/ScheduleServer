package domain;

import static structure.Constant.DUP_ID;
import static structure.Constant.DUP_USER;
import static structure.Constant.ERR;
import static structure.Constant.ERR_LOG_PW;
import static structure.Constant.NO_DATA;
import static structure.Constant.SUCCESS;
import static structure.Constant.ERR_AUTO_LOG_IN;
import static structure.Constant.AUTO_LOG_SUCCESS;
import static structure.Constant.LOG_IN_SUCCESS;


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
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.schedule.message.FirebaseMsgSender;
import com.schedule.message.MailSender;

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
	
	DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	
	public UserDAO() {}
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSource = dataSource;
	}
	
	public Connection getConnection() {
		Connection con = null;

		try {
			con = dataSource.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return con;
	}

	public void closeConnection(Connection con) {
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
			String sql = "select id from usertable where id=?";

			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next())
				flag = true;

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
			System.out.println(jdbcTemplate==null);
			
			String sql = "select password from usertable where id=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				if(BCrypt.checkpw(pw,rs.getString("password")))
					code = SUCCESS;
				else {
					code = ERR_LOG_PW;
				}
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
		int flag = SUCCESS;

		try {
			if (chkUserDup(id, name, email))
				return DUP_USER;

			if (chkIdDup(id))
				return DUP_ID;

			jdbcTemplate.update("insert into usertable values(?,?,?,?)", id, pw, name, email);
		} catch (Exception e) {
			flag = ERR;
			System.out.println("INSERT_USER_EXP: " + e.getMessage());
		}

		return flag;
	}

	private boolean chkUserDup(String id, String name, String email) {
		Connection con = null;
		boolean flag = false;

		try {
			con = getConnection();

			String sql = "select id from usertable where email=? and name=?";

			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, email);
			pstmt.setString(2, name);

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
			
			if(rs.next()) {
				String id = rs.getString("id");
				String pw = "";
				for(int i=0; i<6; i++)
					pw += (int)(Math.random()*10);
				
				code = changePw(id, BCrypt.hashpw(pw, BCrypt.gensalt(12)));
				
				MailSender sender = MailSender.getInstance();
				sender.sendMail(email, name+"님의 회원정보\n"
						+ "아이디: "+id+"\n"
						+ "임시 비밀번호: "+pw+"\n"
						+"SCHappy에서 로그인 후 비밀번호를 꼭 변경해주세요!");
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
	
	public String getEmail() {
		Connection con = null;
		String email = null;
		
		try {
			con = getConnection();
			
			String sql = "select email from usertable where id=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
				email = rs.getString("email");
			
			
		}catch(Exception e) {
			System.out.println("GET_INFO_ERR: "+e.toString());
		}finally {
			closeConnection(con);
		}
		
		return email;
	}
	
	public boolean getInviteExist() {
		Connection con = null;
		boolean isInviteExist = false;
		
		try {
			con = getConnection();
			
			String sql = "select count(*) from invitetable where userid=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				if(rs.getInt(1)>0)
					isInviteExist = true;
			}
		}catch(SQLException e) {
			System.out.println("GET_INVITE_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return isInviteExist;
	}
	
	public Integer changePw(String id, String pw) {
		Integer code = SUCCESS;
		
		try {
			jdbcTemplate.update("update usertable set password=? where id=?",pw,id);
		
		}catch(Exception e) {
			code = ERR;
			System.out.println("CHANGE_PW_ERR: "+e.toString());
		}
		
		return code;
	}
	
	public int changeEmail(String email) {
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("update usertable set email=? where id=?",email,USession.getInstance().getId());
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("CHANGE_EMAIL_EXP: "+e.toString());
		}
		
		return code;
	}
	
	public int withdraw() {
		int code = SUCCESS;
		
		try {			
			jdbcTemplate.update("delete from usertable where id=?",USession.getInstance().getId());
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("WITHDRAW_EXP: "+e.getMessage());
		}
		
		return code;
	}
	
	public int chkFrdExist(String frdName, String frdId) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "select id from usertable where id=? and name=?";

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
	
	public List<FriendObject> getFriends() {
		return jdbcTemplate.query("select friendid, friendname from friendtable where userid=? order by friendname", 
									new Object[] {USession.getInstance().getId()},
									
									new RowMapper<FriendObject>() {
										public FriendObject mapRow(ResultSet rs, int rowNum) throws SQLException{
											FriendObject friend = new FriendObject();
											
											try {
												friend.put("name",rs.getString("friendname"));
												friend.put("id", rs.getString("friendid"));
											}catch(JSONException e) {
												System.out.println("GET_FRIEND_EXP: "+e.getMessage());
											}
											return friend;
										}
		});
	}
	
	public HashMap getGroups(){
		Connection con = null;
		HashMap<String,ArrayList<GroupObject>> groups = new HashMap();
		
		try {
			/*con = getConnection();
			
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
			*/
			String id = USession.getInstance().getId();
			ArrayList<GroupObject> isManager = new ArrayList<GroupObject>();
			ArrayList<GroupObject> notManager = new ArrayList<GroupObject>();
			
			jdbcTemplate.query("select b.groupnum,b.groupname,b.managerid from grouptable a join groupproto b on a.groupnum = b.groupnum "
								+ "where memberid=? order by b.groupname",
								new Object[] {id},
															 
									new RowMapper<GroupObject>() {
										public GroupObject mapRow(ResultSet rs, int rowNum) throws SQLException{
											GroupObject group = new GroupObject();
																	
											try {
													group.put("groupNum",rs.getInt("groupnum"));
													group.put("groupName",rs.getString("groupname"));
												}catch(JSONException e) {
													System.out.println("GET_GROUP_EXP: "+e.getMessage());
												}
																	
											if(id.equals(rs.getString("managerid")))
												isManager.add(group);
											else
												notManager.add(group);
											
											return group;
										}
			});
			
			
			groups.put("isManager",isManager);
			groups.put("notManager",notManager);
		}catch(Exception e) {
			System.out.println("GET_GROUP_EXP: "+e.getMessage());
		}finally {
			//closeConnection(con);
		}
		
		return groups;
	}
	
	public int addFriend(String name, String id) {
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("insert into friendtable values(?,?,?)",USession.getInstance().getId(),id,name);
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("ADD_FRIEND_EXP: "+e.getMessage());
		}
		
		return code;
	}
	
	public int deleteFriend(String name, String id) {
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("delete from friendtable where userid=? and friendid=? and friendname=?",USession.getInstance().getId(),id,name);
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("DELETE_FRIEND_EXP: "+e.getMessage());
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
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("insert into grouptable values(?,?)",groupNum,USession.getInstance().getId());
		
		}catch(Exception e) {
			code = ERR;
			System.out.println("ADD_MEMBER_EXP: "+e.getMessage());
		}
		
		return code;
	}
	
	public int sendInvite(int groupNum,String[] friends) {
		Connection con = null;
		
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
			
			FirebaseMsgSender msgSender = new FirebaseMsgSender();
			ArrayList<String> tokens = new ArrayList<>();
			
			sql = "select usercode from codetable where id=?";
			pstmt = con.prepareStatement(sql);
			ResultSet rs;
			
			for(String friend : friends){
				pstmt.setString(1, friend);
				rs = pstmt.executeQuery();
				
				while(rs.next())
					tokens.add(rs.getString(1));
			}
			
			msgSender.sendGroupInvite(tokens);
		}catch(SQLException e) {
			groupNum = ERR;
			System.out.println("SEND_INVITE_EXP: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return groupNum;
	}
	public int createGroup(String name) {
		int groupNum = ERR;
		
		try {
			groupNum = jdbcTemplate.queryForInt("select max(groupnum) from groupproto")+1;
			jdbcTemplate.update("insert into groupproto values(?,?,?)", groupNum, name, USession.getInstance().getId());
			
			addMember(groupNum);
		}catch(Exception e) {
			groupNum = ERR;
			System.out.println("CREATE_GROUP_EXP: "+e.getMessage());
		}
		
		return groupNum;
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
			
			PreparedStatement innerPstmt;
			ResultSet innerRs;
			
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				InviteObject obj = new InviteObject();
				
				obj.put("groupNum",rs.getInt("groupnum"));
				obj.put("groupName",rs.getString("groupname"));
				
				sql = "select name from usertable where id=?";
				
				innerPstmt = con.prepareStatement(sql);
				innerPstmt.setString(1, rs.getString("managerid"));
				
				innerRs = innerPstmt.executeQuery();
				if(innerRs.next())
					obj.put("managerName",innerRs.getString("name"));
				
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
		int code = SUCCESS;
		
		try {			
			jdbcTemplate.update("delete from invitetable where groupnum=? and userid=?", groupNum, USession.getInstance().getId());
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("DEL_INVITE_EXP: "+e.getMessage());
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
		int code = SUCCESS;
		
		try {
			int idSize = ids.length;
			for(int i=0; i<idSize; i++)
				jdbcTemplate.update("delete from grouptable where groupnum=? and memberid=?", groupNum, ids[i]);
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("WITHDRAW_EXP: "+e.getMessage());
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
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("insert into linktable values(?,?)", groupNum, USession.getInstance().getId());
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("CONNECT_GROUP_EXP: "+e.getMessage());
		}
		
		return code;
	}
	
	public int disConnectGroup(int groupNum) {
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("delete from linktable where groupNum=? and userid=?", groupNum, USession.getInstance().getId());
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("DISCON_GROUP_EXP: "+e.getMessage());
		}
		
		return code;
	}
	
	public String getName() {
		Connection con = null;
		String name = null;
		
		try {
			con = getConnection();
			
			String sql = "select name from usertable where id=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next())
				name = rs.getString(1);
			
		}catch(SQLException e) {
			System.out.println("GET_NAME_EXCEPTION: "+e.getMessage());
		}finally {
			closeConnection(con);
		}
		
		return name;
	}
	
	public HashMap<String, Object> autoLogin(String userCode) {
		Connection con = null;
		
		HashMap<String, Object> hashMap = new HashMap<>();
		int code = AUTO_LOG_SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "select id from codetable where usercode=?";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, userCode);
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()) {
				USession.getInstance().setId(rs.getString(1));
				hashMap.put("id", rs.getString(1));
			}
			else {
				code = ERR_AUTO_LOG_IN;
			}
		}catch(SQLException e) {
			code = ERR_AUTO_LOG_IN;
			System.out.println("AUTO_LOGIN_ERR"+e.getMessage());
		}finally {
			closeConnection(con);
			hashMap.put("code", code);
		}
		
		return hashMap;
	}
	
	public int updateCode(String oldCode, String newCode) {
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("update codetable set usercode=? where usercode=?", newCode, oldCode);
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("UPDATE_CODE_ERR: "+e.getMessage());
		}
		
		return code;
	}
	
	public int addCode(String userCode) {
		int code = LOG_IN_SUCCESS;
		
		try {
			jdbcTemplate.update("insert into codetable values(?,?)", userCode, USession.getInstance().getId());
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("ADD_CODE_ERR: "+e.getMessage());
		}
		
		return code;
	}
	
	public int logout(String userCode) {
		int code = SUCCESS;
		
		try {
			jdbcTemplate.update("delete from codetable where usercode=?", userCode);
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("LOG_OUT_ERR: "+e.getMessage());
		}
		
		return code;
	}
	
}
