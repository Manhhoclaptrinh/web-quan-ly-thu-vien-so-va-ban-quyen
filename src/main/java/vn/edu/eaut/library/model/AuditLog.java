package vn.edu.eaut.library.model;
import java.time.LocalDateTime;
public class AuditLog {
    private int auditId,userId;
    private Integer targetId;
    private String username,actionType,targetType,description,ipAddress;
    private LocalDateTime createdAt;
    public int getAuditId(){return auditId;} public void setAuditId(int v){auditId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public Integer getTargetId(){return targetId;} public void setTargetId(Integer v){targetId=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getActionType(){return actionType;} public void setActionType(String v){actionType=v;}
    public String getTargetType(){return targetType;} public void setTargetType(String v){targetType=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getIpAddress(){return ipAddress;} public void setIpAddress(String v){ipAddress=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
