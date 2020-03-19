package com.realmax.cameras.bean;

/**
 * @ProjectName: Cars
 * @Package: com.realmax.cars.bean
 * @ClassName: CameraBean
 * @CreateDate: 2020/3/18 16:37
 */
public class CameraBean {
    private String name;
    private int id;
    private int cameraId;

    public CameraBean() {
    }

    public CameraBean(String name, int id, int cameraId) {
        this.name = name;
        this.id = id;
        this.cameraId = cameraId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    @Override
    public String toString() {
        return "CameraBean{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", cameraId=" + cameraId +
                '}';
    }
}
