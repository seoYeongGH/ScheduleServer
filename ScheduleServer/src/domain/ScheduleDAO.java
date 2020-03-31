package domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import structure.ScheduleObject;
import structure.USession;

import static structure.Constant.ERR;
import static structure.Constant.ADD_SUCCESS;
import static structure.Constant.MOD_SUCCESS;
import static structure.Constant.DELETE_SUCCESS;
import static structure.Constant.FOR_USER;

public class ScheduleDAO {
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
	
	public ScheduleDAO() {
	}
	
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
	
	public int addSchedule(int groupNum, String date,String startTime, String endTime, String schedule) {
		int code = ADD_SUCCESS;
		
		try {
			String id;
			Integer insertNum;
			
			if(groupNum == FOR_USER) {
				id = USession.getInstance().getId();
				insertNum = null;
			}
			else {
				id = null;
				insertNum = groupNum;
			}
			
			jdbcTemplate.update("insert into scheduletable values(?,?,?,?,?,?)", id, insertNum, date, startTime, endTime, schedule);
		}catch(Exception e) {
			code = ERR;
			System.out.println("INSERT_SCH_ERR: "+e.toString());
		}
		
		return code;
	}                                                                                             
	

	public List getAllSch() {
		Connection con  = null;
		ArrayList<ScheduleObject> listObj = new ArrayList<ScheduleObject>();
		
		try {
			con = getConnection();
			String beforeDate = null;
			String currentDate = null;
			String sql = "select a.scheduledate,a.starttime,a.endtime,a.schedule from scheduletable a left outer join linktable b "
					+ "on a.groupnum = b.groupnum where b.userid=? or a.userid=? order by a.scheduledate,a.starttime";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setString(1, USession.getInstance().getId());
			pstmt.setString(2, USession.getInstance().getId());
			
			ResultSet rs = pstmt.executeQuery();
			
			ScheduleObject schObj = new ScheduleObject();
			ArrayList<String> startTimes = new ArrayList<String>();
			ArrayList<String> endTimes = new ArrayList<String>();
			ArrayList<String> schedules = new ArrayList<String>();
			
			while(rs.next()) {				
				currentDate = rs.getString("scheduledate");
				
				if(!currentDate.equals(beforeDate)) {
					if(beforeDate != null) {
						schObj.put("scheduledate",beforeDate);
						schObj.put("startTimes", startTimes);
						schObj.put("endTimes", endTimes);
						schObj.put("schedules", schedules);
						
						listObj.add(schObj);
						
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
				schObj.put("endTimes", endTimes);
				schObj.put("schedules", schedules);
			
			}
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
	
	public List getGroupSch(int groupNum) {
		Connection con  = null;
		ArrayList<ScheduleObject> listObj = new ArrayList<ScheduleObject>();
		
		try {
			con = getConnection();
			String beforeDate = null;
			String currentDate = null;
			String sql = "select scheduledate,starttime,endtime,schedule from scheduletable where groupnum=? order by scheduledate,starttime";
			
			PreparedStatement pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, groupNum);
			
			ResultSet rs = pstmt.executeQuery();
			
			ScheduleObject schObj = new ScheduleObject();
			ArrayList<String> startTimes = new ArrayList<String>();
			ArrayList<String> endTimes = new ArrayList<String>();
			ArrayList<String> schedules = new ArrayList<String>();
			
			while(rs.next()) {				
				currentDate = rs.getString("scheduledate");
				
				if(!currentDate.equals(beforeDate)) {
					if(beforeDate != null) {
						schObj.put("scheduledate",beforeDate);
						schObj.put("startTimes", startTimes);
						schObj.put("endTimes", endTimes);
						schObj.put("schedules", schedules);
						
						listObj.add(schObj);
						
						
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
				schObj.put("endTimes", endTimes);
				schObj.put("schedules", schedules);
			
			}
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
	
	public int deleteSchedule(int groupNum, String schedule, String date, String startTime, String endTime) {
		int code = DELETE_SUCCESS;
		
		try {
			if(groupNum == FOR_USER) 
				jdbcTemplate.update("delete from scheduletable where userid=? and scheduledate=? and starttime=? and endtime=? and schedule=? and rownum=1",
									USession.getInstance().getId(), date, startTime, endTime, schedule);
			else
				jdbcTemplate.update("delete from scheduletable where groupNum=? and scheduledate=? and starttime=? and endtime=? and schedule=? and rownum=1",
									groupNum, date, startTime, endTime, schedule);
		}catch(Exception e) {
			System.out.println("DELETE_SCHEDULE_EXP: "+e.getMessage());
			code = ERR;
		}
		
		return code;
	}
	
	public int modifySchedule(int groupNum, String[] datas) {
		int code = MOD_SUCCESS;
		
		try {
			if(groupNum == FOR_USER)
				jdbcTemplate.update("update scheduletable set schedule=?, starttime=?, endtime=? "
					+ "where userid=? and scheduledate=? and starttime=? and endtime=? and schedule=? and rownum=1",
					datas[0],datas[1],datas[2],datas[3],datas[4],datas[5],datas[6],datas[7]);
			
			else
				jdbcTemplate.update("update scheduletable set schedule=?, starttime=?, endtime=? "
						+ "where groupNum=? and scheduledate=? and starttime=? and endtime=? and schedule=? and rownum=1",
						datas[0],datas[1],datas[2],groupNum,datas[4],datas[5],datas[6],datas[7]);
			
		}catch(Exception e) {
			code = ERR;
			System.out.println("MODIFY_SCHEDULE_EXP: "+e.getMessage());
		}
		
		return code;
	}
}
