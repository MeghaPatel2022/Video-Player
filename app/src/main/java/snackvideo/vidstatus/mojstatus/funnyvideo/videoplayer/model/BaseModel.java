package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model;

import java.io.Serializable;
import java.util.ArrayList;

public class BaseModel implements Serializable {

    public ArrayList<String> pathlist;
    public long totalSize = 0;
    public int type;
    boolean isDirectory = false;
    private String id;
    private String P_id;
    private String did;
    private String name, folderName;
    private String path;
    private long size;
    private String bucketId;
    private String bucketName, bucketPath;
    private String date;

    public BaseModel(String bucketId, String bucketName, String bucketPath, String name) {
        this.bucketId = bucketId;
        this.bucketName = bucketName;
        this.bucketPath = bucketPath;
        this.name = name;
    }

    public BaseModel() {
    }

    public String getP_id() {
        return P_id;
    }

    public void setP_id(String p_id) {
        P_id = p_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<String> getPathlist() {
        return pathlist;
    }

    public void setPathlist(ArrayList<String> pathlist) {
        this.pathlist = pathlist;
    }

    public String getBucketPath() {
        return bucketPath;
    }

    public void setBucketPath(String bucketPath) {
        this.bucketPath = bucketPath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
