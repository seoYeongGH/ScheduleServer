package structure;

import java.time.LocalDate;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.annotations.SerializedName;

public class ScheduleObject extends JSONObject{
	@SerializedName("scheduledate")
	String scheduledate;
	
	@SerializedName("startTimes")
    ArrayList<String> startTimes;
	
	@SerializedName("endTimes")
    ArrayList<String> endTimes;
	
	@SerializedName("schedules")
    ArrayList<String> schedules;

    public ScheduleObject(){}

    public String getDate() {
        return scheduledate;
    }

    public  ArrayList<String> getStartTime() {
        return startTimes;
    }

    public  ArrayList<String> getEndTime() {
        return endTimes;
    }

    public  ArrayList<String> getSchedule() {
        return schedules;
    }

    public void setDate(String date) {
        this.scheduledate = date;
    }

    public void setStartTime( ArrayList<String> startTime) {
        this.startTimes = startTime;
    }

    public void setEndTime( ArrayList<String> endTime) {
        this.endTimes = endTime;
    }

    public void setSchedule( ArrayList<String> schedule) {
        this.schedules = schedule;
    }

}
