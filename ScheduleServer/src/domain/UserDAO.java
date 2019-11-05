package domain;

import static structure.Constant.DUP_ID;
import static structure.Constant.DUP_USER;
import static structure.Constant.ERR;
import static structure.Constant.ERR_LOG_PW;
import static structure.Constant.NO_DATA;
import static structure.Constant.SUCCESS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.schedule.mail.MailSender;


public class UserDAO {
	static {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("�뱶�씪�씠踰� 濡쒕뱶 �꽦怨�");
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
			else {
				hashMap.put("err","null");
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
}
