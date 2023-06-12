package cn.edu.whut.androidwebsocketclient.entity;

import static cn.edu.whut.androidwebsocketclient.constants.MESSAGE_KEY.*;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * @author : GuMorming
 * @Project : AndroidMonitor
 * @Package : cn.edu.whut.androidmonitor.entity
 * @createTime : 2023/6/12 13:35
 * @Email : gumorming@163.com
 * @Description :
 */

public class MonitorMessage {
    String command;
    String poolName;
    String username;
    String data;

    public MonitorMessage(String json) throws JSONException {
        if (json != null && json.length() > 0) {
            JSONObject jsonObject = new JSONObject(json);
            this.poolName = jsonObject.optString(KEY_POOL_NAME);
            this.command = jsonObject.optString(KEY_COMMAND);
            this.data = jsonObject.optString(KEY_DATA);
            this.username = jsonObject.optString(KEY_USERNAME);
        }
    }
    
    public JSONObject toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_DATA, this.data);
            jsonObject.put(KEY_USERNAME, this.username);
            jsonObject.put(KEY_COMMAND, this.command);
            jsonObject.put(KEY_POOL_NAME, this.poolName);
            return jsonObject;
        } catch (JSONException e) {
            return null;
        }
        
    }
    
    public String getPoolName() {
        return poolName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getCommand() {
        return command;
    }
    
    public String getData() {
        return data;
    }
}