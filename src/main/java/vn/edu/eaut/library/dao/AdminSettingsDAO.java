package vn.edu.eaut.library.dao;

import vn.edu.eaut.library.utils.DBConnection;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminSettingsDAO {
    public Map<String,String> findAll() {
        Map<String,String> data = new LinkedHashMap<>();
        String sql="SELECT setting_key,setting_value FROM admin_settings ORDER BY setting_key";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()){
            while(r.next()) data.put(r.getString(1), r.getString(2));
        }catch(SQLException e){
            data.put("libraryName","CNJ25 Digital Library"); data.put("libraryDescription","Thư viện số và quản lý bản quyền truy cập");
            data.put("maxUploadMb","50"); data.put("defaultAccess","PUBLIC"); data.put("permissionExpiryDays","30");
            data.put("requireApproval","true"); data.put("downloadAllowed","true"); data.put("sessionTimeout","30");
        }
        return data;
    }
    public void save(String key,String value) {
        String sql="INSERT INTO admin_settings(setting_key,setting_value,updated_at) VALUES(?,?,NOW()) " +
                   "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value),updated_at=NOW()";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,key);p.setString(2,value==null?"":value);p.executeUpdate();
        }catch(SQLException e){throw new RuntimeException("Lỗi lưu cài đặt",e);}
    }
    public void saveAll(Map<String,String> values) {
        if(values==null)return;
        for(Map.Entry<String,String> e:values.entrySet()) save(e.getKey(),e.getValue());
    }
}
