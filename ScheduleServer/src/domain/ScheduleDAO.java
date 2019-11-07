package domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import structure.ScheduleObject;
import structure.USession;

import static structure.Constant.SUCCESS;
import static structure.Constant.ERR;

public class ScheduleDAO {
	static {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			System.out.println("Connect Driver");
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private Connection getConnection() {
		DataSource ds = null;
		Connection con = null;
		
		try {
			Context ctx = new InitialContext();
			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/Oracle");
			con = ds.getConnection();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return con;
	}
	
	private void closeConnection(Connection con) {
		if(con != null) {
			try {
				con.close();
			}catch(Exception e) {}
		}
		
		return;
	}
	
	public int addSchedule(String date,String startTime, String endTime, String schedule) {
		Connection con = null;
		int code = SUCCESS;
		
		try {
			con = getConnection();
			
			String sql = "insert into scheduletable values(?,?,?,?,?,?)";
			PreparedStatement pstmt = con.prepareStatement(sql);
			
			pstmt.setString(1, USession.getInstance().getId());
			pstmt.setNull(2, Types.INTEGER);
			pstmt.setString(3, date);
			pstmt.setString(4, startTime);
			pstmt.setString(5, endTime);
			pstmt.setString(6,schedule);
			pstmt.executeQuery();
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("INSERT_SCH_ERR: "+e.toString());
		}finally {
			closeConnection(con);
		}
		
		return code;
	}                                                                                             
	
	public List getAllSch() {
		Connection con  = null;
		//JSONArray listObj = new JSONArray();
		ArrayList<ScheduleObject> listObj = new ArrayList<ScheduleObject>();
		
		try {
			con = getConnection();
			String beforeDate = null;
			String currentDate = null;
			String sql = "select scheduledate,starttime,endtime,schedule from scheduletable where userid=? order by scheduledate,starttime";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			
			ScheduleObject schObj = new ScheduleObject();
			//JSONObject schObj = new JSONObject();
			ArrayList<String> startTimes = new ArrayList<String>();
			ArrayList<String> endTimes = new ArrayList<String>();
			ArrayList<String> schedules = new ArrayList<String>();
			
			while(rs.next()) {				
				currentDate = rs.getString("scheduledate");
				
				if(!currentDate.equals(beforeDate)) {
					if(beforeDate != null) {
						schObj.put("scheduledate",beforeDate);
						schObj.put("startTimes", startTimes);
						schObj.put("endtimes", endTimes);
						schObj.put("schedules", schedules);
						
						/*
						schObj.setDate(beforeDate);
						schObj.setStartTime(startTimes);
						schObj.setEndTime(endTimes);
						schObj.setSchedule(schedules);
						*/
						listObj.add(schObj);
						
						
						//listObj.put(schObj);
						
						startTimes.clear();
						endTimes.clear();
						schedules.clear();
					
						schObj = new ScheduleObject();
						
					}
						beforeDate = currentDate;
				}
				
				startTimes.add(rs.getString("starttime"));
				endTimes.add(rs.getString("endtime"));
				schedules.add(rs.getString("schedule"));
				}
			
			if(beforeDate.equals(currentDate)) {
				schObj.put("scheduledate",currentDate);
				schObj.put("startTimes", startTimes);
				schObj.put("endtimes", endTimes);
				schObj.put("schedules", schedules);
				
			}

			//jsonArr.put(schObj);
			listObj.add(schObj);
			
			startTimes.clear();
			endTimes.clear();
			schedules.clear();
		
		}catch(Exception e) {
			System.out.println("GET_ALL_DATA_EXP: "+e.toString());
		}finally {
			closeConnection(con);
		}

		return listObj;
	}
}
