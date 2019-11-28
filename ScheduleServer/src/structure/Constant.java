package structure;

public class Constant {
	public static final int SUCCESS = 200;
	public static final int ERR = -1;
	
	//join
    public static final int DUP_ID = 2;
    public static final int DUP_USER = 3;
    
    //login
    public static final int ERR_LOG_ID = 1;
    public static final int ERR_LOG_PW = 2;
    
    //find
    public static final int NO_DATA = 2;
    
    //scheduleServlet
    public static final String FLAG_ADD = "addSchedule";
    public static final String FLAG_MODIFY = "modifySchedule";
    public static final String FLAG_ADD_GROUP = "addGroupSch";

    //scheduleDao
    public static final int FOR_USER = 0;
    public static final int FOR_GROUP = 1;
    
    //ScheduleModify
    public static final int ADD_SUCCESS = 0;
    public static final int MOD_SUCCESS = 1;
    public static final int DELETE_SUCCESS = 2;
    
    //social
    public static final int EXIST_INVITE = 0;
    public static final int NO_EXIST_INVITE = 1;
}
 