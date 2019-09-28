package domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.schedule.mail.MailSender;

import static com.schedule.web.Constant.DUP_USER;
import static com.schedule.web.Constant.DUP_ID;
import static com.schedule.web.Constant.ERR;
import static com.schedule.web.Constant.SUCCESS;
import static com.schedule.web.Constant.ERR_LOG_PW;
import static com.schedule.web.Constant.NO_DATA;


public class UserDAO {
	static {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("드라이버 로드 성공");
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
			String sql = "select code from usertable where id=?";

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
			System.out.println("LOGIN_PW_CHK_ERR: "+e.toString());
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

			String sql = "select max(code) from usertable";
			PreparedStatement pstmt = con.prepareStatement(sql);

			int code;

			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				code = rs.getInt(1) + 1;
			} else {
				return ERR;
			}

			sql = "insert into usertable values(?,?,?,?,?,?)";

			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, code);
			pstmt.setString(2, id);
			pstmt.setString(3, pw);
			pstmt.setString(4, name);
			pstmt.setString(5, email);
			pstmt.setInt(6, 0);
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

			String sql = "select code from usertable where email=? and name=?";

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
				
				sender.sendMail(email, "ScheduleApp에서 id를 알려드립니다." ,name+"님의 scheduleApp id는 ["+id+"]입니다.");
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
				
				sender.sendMail(email, "SchedleApp에서 비밀번호를 알려드립니다.", name+"님("+id+"의 비밀번호는 "+"["+pw+"] 입니다.");
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
}
