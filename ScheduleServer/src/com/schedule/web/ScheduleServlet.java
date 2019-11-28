package com.schedule.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;

import domain.ScheduleDAO;
import structure.ScheduleObject;
import structure.USession;

import static structure.Constant.FOR_USER;
import static structure.Constant.FLAG_ADD;
import static structure.Constant.FLAG_ADD_GROUP;
import static structure.Constant.FLAG_MODIFY;
@WebServlet("/sch.do")
public class ScheduleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
     
    public ScheduleServlet() { }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		
		ScheduleDAO dao = new ScheduleDAO();
		PrintWriter out = response.getWriter();
		
		String doing = request.getParameter("doing");
		
		if(FLAG_ADD.equals(doing)) {
			int groupNum = Integer.parseInt(request.getParameter("groupNum"));
			int code = dao.addSchedule(groupNum,request.getParameter("date"),request.getParameter("startTime"),request.getParameter("endTime"),request.getParameter("schedule"));
			
			out.print(code);
		}
		else if(FLAG_MODIFY.equals(doing)) {
			String[] datas = new String[8];

			String[] strTimes = request.getParameter("time").split("~");
			
			datas[0] = request.getParameter("aftSchedule");
			datas[1] = request.getParameter("aftStartTime");
			datas[2] = request.getParameter("aftEndTime");
			datas[3] = USession.getInstance().getId();
			datas[4] = request.getParameter("date");
			datas[5] = strTimes[0];
			datas[6] = strTimes[1];
			datas[7] = request.getParameter("schedule");
			
			int code = dao.modifySchedule(datas);
			out.print(code);
		}
		else if("initSchedule".equals(doing)) {
			List<ScheduleObject> listObj = dao.getAllSch();
			out.print(listObj);
		}
		else if("deleteSchedule".equals(doing)) {
			String[] strTimes = request.getParameter("time").split("~");
			int code = dao.deleteSchedule(request.getParameter("schedule"), request.getParameter("scheduleDate"),strTimes[0],strTimes[1]);
			out.print(code);
		}
		else if("getGpSchedule".equals(doing)) {
			int groupNum = Integer.parseInt(request.getParameter("groupNum"));
			out.print(dao.getGroupSch(groupNum));
		}
	}

}
