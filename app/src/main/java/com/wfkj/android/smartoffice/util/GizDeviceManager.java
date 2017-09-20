package com.wfkj.android.smartoffice.util;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.wfkj.android.smartoffice.model.sever_model.Device;
import com.wfkj.android.smartoffice.model.sever_model.House;

import java.util.ArrayList;

/**
 * Created by zhuxiaolong on 2017/8/8.
 */

public class GizDeviceManager {


    public static final int AIR_CLEANER_TYPE = 20;
    public static final int SECURITY_TYPE = 60;
    public static final int TEMHUM_TYPE = 30;
    public static final int ENERGY_TYPE = 40;
    public static final int LIGHT_CURPUL_TYPE = 50;
    public static final int POLICY_TYPE = 70;

    private GizWifiDevice currentDevice;//当前 网关下绑定的所有设备   一般只有一台树莓派设备 包含各个数据节点
    private ArrayList<House> userHouses;//公司服务器返回的 用户房屋列表 里面有各种设备 分类  以及各个房屋的表示 gateway
    private String currentGateway;//当前 网关标识
    private String gizUID;
    private String gizToken;


    private Device eventLightAuto;//自动手动模式虚拟节点
    private Device eventLightOnOff;// 全开全关虚拟节点

    private boolean goToWork = true;//默认为 上班策略

    public Device getEventLightAuto() {
        return eventLightAuto;
    }

    public void setEventLightAuto(Device eventLightAuto) {
        this.eventLightAuto = eventLightAuto;
    }

    public Device getEventLightOnOff() {
        return eventLightOnOff;
    }

    public void setEventLightOnOff(Device eventLightOnOff) {
        this.eventLightOnOff = eventLightOnOff;
    }

    public boolean isGoToWork() {
        return goToWork;
    }

    public void setGoToWork(boolean goToWork) {
        this.goToWork = goToWork;
    }

    private static GizDeviceManager instance;

    public static GizDeviceManager getInstance() {
        if (instance == null) {
            instance = new GizDeviceManager();
        }
        return instance;
    }

    public House getHouseByGateway(String gateway) {
        House result = null;
        if (userHouses == null) {
            return null;
        }

        for (House house : userHouses) {
            if (house.getGateways().get(0).getExtid().equals(gateway)) {
                result = house;
                break;
            }
        }
        if (result == null) {
            if (userHouses != null) {
                result = userHouses.get(0);
            }
        }
        return result;
    }


    public String getGizUID() {
        return gizUID;
    }

    public void setGizUID(String gizUID) {
        this.gizUID = gizUID;
    }

    public String getGizToken() {
        return gizToken;
    }

    public void setGizToken(String gizToken) {
        this.gizToken = gizToken;
    }

    public GizWifiDevice getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(GizWifiDevice currentDevice) {
        this.currentDevice = currentDevice;
    }

    public ArrayList<House> getUserHouses() {
        return userHouses;
    }

    public void setUserHouses(ArrayList<House> userHouses) {
        this.userHouses = userHouses;
    }

    public String getCurrentGateway() {
        return currentGateway;
    }

    public void setCurrentGateway(String currentGateway) {
        this.currentGateway = currentGateway;
    }
}
