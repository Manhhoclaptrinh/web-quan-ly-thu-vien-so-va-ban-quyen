package vn.edu.eaut.library.model;
import java.time.LocalDateTime;
public class VideoReview {
    private int reviewId,userId,videoId,rating;
    private String username,userFullName,comment;
    private LocalDateTime createdAt,updatedAt;
    public int getReviewId(){return reviewId;} public void setReviewId(int v){reviewId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public int getVideoId(){return videoId;} public void setVideoId(int v){videoId=v;}
    public int getRating(){return rating;} public void setRating(int v){rating=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getUserFullName(){return userFullName;} public void setUserFullName(String v){userFullName=v;}
    public String getComment(){return comment;} public void setComment(String v){comment=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}