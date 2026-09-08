package vn.edu.eaut.library.model;
import java.time.LocalDateTime;
public class Favorite {
    private int favoriteId, userId, documentId, videoId;
    private String itemType; // DOCUMENT, VIDEO
    private String documentTitle, author;
    private LocalDateTime createdAt;
    public int getFavoriteId(){return favoriteId;} public void setFavoriteId(int v){favoriteId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public int getDocumentId(){return documentId;} public void setDocumentId(int v){documentId=v;}
    public int getVideoId(){return videoId;} public void setVideoId(int v){videoId=v;}
    public String getItemType(){return itemType;} public void setItemType(String v){itemType=v;}
    public String getDocumentTitle(){return documentTitle;} public void setDocumentTitle(String v){documentTitle=v;}
    public String getAuthor(){return author;} public void setAuthor(String v){author=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public boolean isVideo(){return "VIDEO".equalsIgnoreCase(itemType);}
}