package structure;

import org.json.JSONObject;

import com.google.gson.annotations.SerializedName;

public class InviteObject extends JSONObject {
    @SerializedName("groupNum")
    Integer groupNum;

    @SerializedName("groupName")
    String groupName;

    @SerializedName("managerName")
    String managerName;

    public InviteObject(){}

    public int getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(int groupNum) {
        this.groupNum = groupNum;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerId(String managerName) {
        this.managerName = managerName;
    }
}

