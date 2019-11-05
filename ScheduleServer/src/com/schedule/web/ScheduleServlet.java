package com.schedule.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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

import static structure.Constant.FOR_USER;

/**
 * Servlet implementation class ScheduleServlet
 */
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
		ScheduleDAO dao = new ScheduleDAO();
		PrintWriter out = response.getWriter();
		
		String doing = request.getParameter("doing");
		
		if("addSchedule".equals(doing)) {
			Integer code = dao.addSchedule(request.getParameter("date"),request.getParameter("startTime"),request.getParameter("endTime"),request.getParameter("schedule"));
			
			//out.print(code);
		}
		else if("initSchedule".equals(doing)) {
			JSONArray jsonArr = dao.getAllSch();
			out.print(jsonArr);
		}
	}

}
