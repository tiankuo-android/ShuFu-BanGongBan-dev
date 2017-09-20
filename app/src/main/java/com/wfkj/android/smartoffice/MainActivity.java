package com.wfkj.android.smartoffice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wfkj.android.smartoffice.model.sever_model.Categorie;
import com.wfkj.android.smartoffice.model.sever_model.Device;
import com.wfkj.android.smartoffice.model.sever_model.House;
import com.wfkj.android.smartoffice.model.sever_model.OutModel_PersonalInForMation;
import com.wfkj.android.smartoffice.model.sever_model.Room;
import com.wfkj.android.smartoffice.model.weather.WeatherBean;
import com.wfkj.android.smartoffice.my_interface.HttpInterface;
import com.wfkj.android.smartoffice.ui.activity.BaseFragmentActivity;
import com.wfkj.android.smartoffice.ui.activity.DetailedControlActivity;
import com.wfkj.android.smartoffice.ui.activity.FeedBackActivity;
import com.wfkj.android.smartoffice.ui.activity.LogInActivity;
import com.wfkj.android.smartoffice.ui.activity.OfficeManagementActivity;
import com.wfkj.android.smartoffice.ui.activity.OutDoorActivity;
import com.wfkj.android.smartoffice.ui.activity.UserCenterActivity;
import com.wfkj.android.smartoffice.util.Constants;
import com.wfkj.android.smartoffice.util.EventBus_Account;
import com.wfkj.android.smartoffice.util.FileUtil;
import com.wfkj.android.smartoffice.util.GizDeviceManager;
import com.wfkj.android.smartoffice.util.HttpUtils;
import com.wfkj.android.smartoffice.util.StringHelper;
import com.wfkj.android.smartoffice.util.jzy_util.CmdCenter;
import com.wfkj.android.smartoffice.util.view_util.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends BaseFragmentActivity implements OnClickListener {

    private static final String TAG = "MainActivity";
    public static MainActivity mainActivity;

    private boolean humidity_flg = false;
    private boolean air_flg = false;
    private boolean light_flg = false;
    private boolean temperature_flg = false;
    private boolean security_flg = false;

    private final int LIGHT_BTN_STATE_AUTO = 0;
    private final int LIGHT_BTN_STATE_MENTRAL = 1;
    private final int LIGHT_BTN_STATE_MENTRALOFF = 2;
    private final int LIGHT_BTN_STATE_MENTRALON = 3;


    private ArrayList<Device> tempDevices;//温度控制的集合
    private ArrayList<Device> airDevices;//空气控制的集合
    private ArrayList<Device> lightDevices;//灯光集合
    private ArrayList<Device> humDevices;//湿度控制集合
    private ArrayList<Device> securityDevices;//办公安防控制集合
    private ArrayList<Device> electricityDevices;//电量消耗控制集合
    private ArrayList<Device> jobDevieces;//上下班集合

    private int weather_out_tag;

    private LinearLayout mian_lightnumber_layout;//灯光状态按钮
    private LinearLayout mian_secyrutynumber_layout;//办公安防状态按钮
    private LinearLayout mian_electritynumber_layout;//能耗按钮

    private TextView mian_lightnumber_txt;//灯光显示
    private TextView mian_secyrutynumber_txt;//办公安防显示
    private TextView mian_electritynumber_txt;//能耗显示


    private DrawerLayout drawer_layout;
    private ScrollView right_layout;

    private SimpleDraweeView person_head_img;//用户头像
    private TextView usermame;//用户名称
    private LinearLayout lock_house_btn;//查看房屋按钮
    private LinearLayout office_administration_btn;//办公室管理按钮
    private LinearLayout user_center_btn;//用户中心按钮
    private LinearLayout feedback_btn;//建议反馈按钮


    private LoadingDialog loadingDialog;
    private WeatherBean weatherBean;

    private ViewPager viewPager;
    private RelativeLayout vplayout;
    private LinearLayout radio_img_layout;
    private LinearLayout btn_control;//详细控制按钮
    private LinearLayout btn_outdoor;//户外详情按钮

    private ImageView main_btn_gowork;//上班按钮
    private ImageView main_btn_offwork;//下班按钮

    private ImageView main_btn_humidity;//湿度控制按钮
    private ImageView main_btn_air;//空气净化按钮
    private ImageView main_btn_light;//照明开关按钮
    private ImageView main_btn_temperature;//温度控制按钮
    private ImageView main_btn_security;//办公安防按钮

    private TextView main_txt_humidity;//显示湿度控制状态
    private TextView main_txt_air;//显示空气净化状态
    private TextView main_txt_light;//显示开关状态
    private TextView main_txt_temperature;//显示温度控制状态
    private TextView main_txt_security;//显示办公安防状态

    private ImageView head_back;
    private ImageView head_img;

    private MyPagerAdapter adapter;
    private List<View> list;
    //小点集合
    private ImageView[] dots;
    // 记录当前选中位置
    private int currentIndex;
    private Gson gson = new Gson();
    private TextView title;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    for (Device device : jobDevieces) {
                        switch (device.getSub_category()) {
                            case 70:
                                CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), device.getExtid(), true);
                                break;
                            case 71:
                                break;
                        }
                    }
//                    CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), "event_gotowork", true);
//                    CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), "event_gotowork", false);
                    break;
                case 1:
                    for (Device device : jobDevieces) {
                        switch (device.getSub_category()) {
                            case 70:
                                break;
                            case 71:
                                CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), device.getExtid(), true);
                                break;
                        }
                    }
//                    CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), "event_gooffwork", true);
//                    CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), "event_gooffwork", false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mainActivity == null) {
            mainActivity = this;
        }
        EventBus.getDefault().register(this);
        init();
    }

    private void init() {

        tempDevices = new ArrayList<>();
        airDevices = new ArrayList<>();
        lightDevices = new ArrayList<>();
        humDevices = new ArrayList<>();
        securityDevices = new ArrayList<>();
        electricityDevices = new ArrayList<>();
        jobDevieces = new ArrayList<>();

        setList();


        weather_out_tag = 1;
        loadingDialog = new LoadingDialog(MainActivity.this);


        viewPager = (ViewPager) this.findViewById(R.id.main_banner);
        vplayout = (RelativeLayout) this.findViewById(R.id.vplayout);
        radio_img_layout = (LinearLayout) this.findViewById(R.id.radio_img_layout);
        btn_control = (LinearLayout) this.findViewById(R.id.btn_control);
        main_btn_gowork = (ImageView) this.findViewById(R.id.main_btn_gowork);
        main_btn_offwork = (ImageView) this.findViewById(R.id.main_btn_offwork);

        main_btn_humidity = (ImageView) this.findViewById(R.id.main_btn_humidity);
        main_btn_air = (ImageView) this.findViewById(R.id.main_btn_air);
        main_btn_light = (ImageView) this.findViewById(R.id.main_btn_light);
        main_btn_temperature = (ImageView) this.findViewById(R.id.main_btn_temperature);
        main_btn_security = (ImageView) this.findViewById(R.id.main_btn_security);


        main_txt_humidity = (TextView) this.findViewById(R.id.main_txt_humidity);
        main_txt_air = (TextView) this.findViewById(R.id.main_txt_air);
        main_txt_light = (TextView) this.findViewById(R.id.main_txt_light);
        main_txt_temperature = (TextView) this.findViewById(R.id.main_txt_temperature);
        main_txt_security = (TextView) this.findViewById(R.id.main_txt_security);

        person_head_img = (SimpleDraweeView) this.findViewById(R.id.person_head_img);
        usermame = (TextView) this.findViewById(R.id.usermame);
        lock_house_btn = (LinearLayout) this.findViewById(R.id.lock_house_btn);
        office_administration_btn = (LinearLayout) this.findViewById(R.id.office_administration_btn);
        user_center_btn = (LinearLayout) this.findViewById(R.id.user_center_btn);
        feedback_btn = (LinearLayout) this.findViewById(R.id.feedback_btn);

        head_back = (ImageView) this.findViewById(R.id.head_back);
        head_img = (ImageView) this.findViewById(R.id.head_img);

        btn_outdoor = (LinearLayout) this.findViewById(R.id.btn_outdoor);
        head_back = (ImageView) this.findViewById(R.id.head_back);
        drawer_layout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        right_layout = (ScrollView) this.findViewById(R.id.right_layout);
        head_back.setVisibility(View.GONE);


        mian_lightnumber_layout = (LinearLayout) this.findViewById(R.id.mian_lightnumber_layout);
        mian_secyrutynumber_layout = (LinearLayout) this.findViewById(R.id.mian_secyrutynumber_layout);
        mian_electritynumber_layout = (LinearLayout) this.findViewById(R.id.mian_electritynumber_layout);

        mian_lightnumber_txt = (TextView) this.findViewById(R.id.mian_lightnumber_txt);
        mian_secyrutynumber_txt = (TextView) this.findViewById(R.id.mian_secyrutynumber_txt);
        mian_electritynumber_txt = (TextView) this.findViewById(R.id.mian_electritynumber_txt);


        title = (TextView) this.findViewById(R.id.head_title);
        title.setText("办公环境状态");

        list = new ArrayList<View>();
        LayoutInflater lf = getLayoutInflater().from(this);
        Constants.house = gson.fromJson(FileUtil.loadString(MainActivity.this, Constants.SP_HOUSE), House.class);
        if (Constants.house != null && Constants.house.getRooms() != null && Constants.house.getRooms().size() > 0) {
            for (int i = 0; i < Constants.house.getRooms().size(); i++) {
                View view = lf.inflate(R.layout.main_banner_view, null);
                list.add(view);
            }
        }
        adapter = new MyPagerAdapter(list);
        viewPager.setAdapter(adapter);

        btn_control.setOnClickListener(this);
        main_btn_gowork.setOnClickListener(this);
        main_btn_offwork.setOnClickListener(this);
        main_btn_humidity.setOnClickListener(this);
        main_btn_air.setOnClickListener(this);
        main_btn_temperature.setOnClickListener(this);
        main_btn_light.setOnClickListener(this);
        main_btn_security.setOnClickListener(this);
        btn_outdoor.setOnClickListener(this);
        lock_house_btn.setOnClickListener(this);
        office_administration_btn.setOnClickListener(this);
        user_center_btn.setOnClickListener(this);
        feedback_btn.setOnClickListener(this);
        head_back.setOnClickListener(this);
        head_img.setOnClickListener(this);
        mian_lightnumber_layout.setOnClickListener(this);
        mian_secyrutynumber_layout.setOnClickListener(this);
        mian_electritynumber_layout.setOnClickListener(this);
        person_head_img.setOnClickListener(this);

//        initDots();
//        if(Constants.house!=null){
//            getNumberUpdate();
//        }

        if (Constants.outHouse == null) {
            Constants.outHouse = gson.fromJson(FileUtil.loadString(MainActivity.this, Constants.SP_OUTHOUSE), OutModel_PersonalInForMation.class);
        }
        String imgUrl = Constants.outHouse.getResult().getProfile().getAvatarPath();
        if (imgUrl != null) {
            imgUrl = imgUrl.replace("https", "http");
            person_head_img.setImageURI(imgUrl);
        }

        String nameStr = Constants.outHouse.getResult().getProfile().getFirstName();
        if (nameStr == null || nameStr.equals("")) {
            usermame.setText("火星人");
        } else {
            usermame.setText(nameStr);
        }

        loadingDialog.show();
        getWeather("haidian");

        if (mXpgWifiDevice == null) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LogInActivity.class);
            startActivity(intent);
            loadingDialog.dismiss();
            this.finish();
        } else {
            mXpgWifiDevice.setListener(deviceListener);
            CmdCenter.getInstance(this).cGetStatus(mXpgWifiDevice);
        }


//        if (FileUtil.loadBoolean(MainActivity.this, Constants.SP_GOTOWORK, true)) {
//            workBtnUpdate(GizDeviceManager.getInstance().isGoToWork());
//        } else {
//            workBtnUpdate(false);
//        }
        workBtnUpdate(GizDeviceManager.getInstance().isGoToWork());

    }


    private void setList() {
        House h = GizDeviceManager.getInstance().getHouseByGateway(GizDeviceManager.getInstance().getCurrentGateway());
        for (Room room : h.getRooms()) {//首先循环当前房屋内所有的房间
            /*在房间内找到 所有 category 设备分类*/
            if (room.getCategories() != null) {
                for (Categorie categorie : room.getCategories()) {
                    /*找到所有 category*/
                    if (categorie.getDevices() != null) {// 确定类别
                        switch (categorie.getId()) {
                            case GizDeviceManager.SECURITY_TYPE://办公安防
                                if (categorie.getDevices() != null && categorie.getDevices().size() > 0) {
                                    securityDevices.addAll(categorie.getDevices());
                                }
                                break;
                            case GizDeviceManager.AIR_CLEANER_TYPE://空气净化
                                if (categorie.getDevices() != null && categorie.getDevices().size() > 0) {
                                    airDevices.addAll(categorie.getDevices());
                                }
                                break;
                            case GizDeviceManager.TEMHUM_TYPE://温度湿度
                                if (categorie.getDevices() != null && categorie.getDevices().size() > 0) {
                                    for (Device device : categorie.getDevices()) {
                                        if (device.getSub_category() == 33) {//湿度
                                            humDevices.add(device);
                                        } else if (device.getSub_category() == 32) {//温度
                                            tempDevices.add(device);
                                        }
                                    }
                                }
                                break;
                            case GizDeviceManager.ENERGY_TYPE://电量消耗
                                if (categorie.getDevices() != null && categorie.getDevices().size() > 0) {
                                    electricityDevices.addAll(categorie.getDevices());
                                }
                                break;
                            case GizDeviceManager.LIGHT_CURPUL_TYPE://灯光窗帘
                                if (categorie.getDevices() != null && categorie.getDevices().size() > 0) {
                                    for (Device device : categorie.getDevices()) {
                                        if (device.getSub_category() == 50 || device.getSub_category() == 51) {//白炽灯和彩灯
                                            lightDevices.add(device);
                                        }
                                        if (device.getSub_category() == 55) {
                                            Log.i("lightcmd", "setList: ");
                                            GizDeviceManager.getInstance().setEventLightOnOff(device);//保存 全开全关节点
                                        }
                                        if (device.getSub_category() == 54) {
                                            Log.i("lightcmd", "setList: ");
                                            GizDeviceManager.getInstance().setEventLightAuto(device);//保存 手动自动节点
                                        }
                                    }
                                }
                                break;
                            case GizDeviceManager.POLICY_TYPE://策略
                                if (categorie.getDevices() != null && categorie.getDevices().size() > 0) {
                                    jobDevieces.addAll(categorie.getDevices());
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    private void getUpDateDeviceList(JSONObject jsonObject) {
        //根据数据设置 开启的灯的数量
        setLightDeviceNumber(jsonObject);
        //根据数据 设置开启的净化器数量
        setAirCleanerDeviceum(jsonObject);
        //根据数值 设置 总耗电量
        setElectricityValue(jsonObject);
    }


    private void setEventState(JSONObject jsonObject) {
        for (Room room : Constants.house.getRooms()) {
            for (Categorie categorie : room.getCategories()) {
                for (Device device : categorie.getDevices()) {
                    try {
                        String value = getStringValue(jsonObject, device);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setElectricityValue(JSONObject jsonObject) {
        DecimalFormat df = new DecimalFormat("0.00");
        double electricityNumber = getElectricityNumber(jsonObject);
        mian_electritynumber_txt.setText("共消耗了" + df.format(electricityNumber) + "度电");
    }

    private void setAirCleanerDeviceum(JSONObject jsonObject) {
        int airNumber = getAirNumber(jsonObject);
        if (airNumber > 0) {
            main_btn_air.setImageResource(R.mipmap.purification_btn_on);
            main_txt_air.setText("AUTO");
            air_flg = true;
        } else {
            main_btn_air.setImageResource(R.mipmap.purification_btn_off);
            main_txt_air.setText("OFF");
            air_flg = false;
        }
    }

    private void setLightDeviceNumber(JSONObject jsonObject) {
        int lightNumber = getLightNumber(jsonObject);
        mian_lightnumber_txt.setText("共有" + lightNumber + "盏灯开启");
//        if (lightNumber > 0) {
//            light_flg = true;
//            main_btn_light.setImageResource(R.mipmap.lighting_btn_on);
//            main_txt_light.setText("AUTO");
//        } else {
//            light_flg = false;
//            main_btn_light.setImageResource(R.mipmap.lighting_btn_off);
//            main_txt_light.setText("OFF");
//        }
    }

    private int getLightButtonState(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        String temkey = null;
        String value = null;

        boolean autoState = false;//自动状态标志位
        while (keys.hasNext()) {
            temkey = keys.next();

            if (temkey.equals("modbus_auto")) {//先判断   目前是手动还是自动模式
                try {
                    value = String.valueOf(jsonObject.get(temkey));
                    Log.i("lightcmd", "getLightButtonState:  "+value);
                    autoState = (Integer.valueOf(value) != 0);
                    if (autoState) {
                        return LIGHT_BTN_STATE_AUTO;
                    }
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (autoState) {
                if (temkey.equals("modbus_all")) {//如果是手动模式  在判断 全开 全关
                    return LIGHT_BTN_STATE_MENTRALON;
                } else {
                    return LIGHT_BTN_STATE_MENTRALOFF;
                }
            }
        }
        return 0;
    }


    private double getElectricityNumber(JSONObject jsonObject) {
        double result = 0;
        for (Device device : electricityDevices) {
            try {
                String electValue = getStringValue(jsonObject, device);
                if (electValue.contains("true") || electValue.contains("false")) {

                } else {
                    double number = Double.valueOf(electValue) / 100;
                    result += number;
                }
            } catch (JSONException e) {
//                e.printStackTrace();
            }
        }
        return result;
    }

    private int getSecurityNumber(JSONObject jsonObject) {
        int resultNumber = 0;
        for (Device device : securityDevices) {
            try {
                String secvalue = getStringValue(jsonObject, device);
                if (secvalue.contains("true") || secvalue.contains("false")) {
                    if (Boolean.valueOf(secvalue)) {
                        resultNumber++;
                    }
                }
            } catch (JSONException e) {
//                e.printStackTrace();
            } catch (NullPointerException e) {

            }
        }
        return resultNumber;
    }

    private int getAirNumber(JSONObject jsonObject) {
        int result = 0;
        for (Device device : airDevices) {//根据服务器获取的 净化器设备列表 检查机智云返回数据 是否包含 如果包含 检查开启状态

//            if (device.getId() == 29) {
            try {
                String airvalue = getStringValue(jsonObject, device);
                if (airvalue.contains("true") || airvalue.contains("false")) {
                    if (Boolean.valueOf(airvalue)) {
                        result++;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            }
        }
        return result;
    }

    private int getLightNumber(JSONObject jsonObject) {
        int result = 0;
        try {
            for (Device device : lightDevices) {
                String value = getStringValue(jsonObject, device);
                if (value.contains("true") || value.contains("false")) {
                    if (Boolean.valueOf(value)) {
                        result++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getStringValue(JSONObject jsonObject, Device device) throws JSONException {
        String result = null;
        String extid;
        String key;
        Iterator<String> iterator = jsonObject.keys();
        extid = device.getExtid().trim();
        while (iterator.hasNext()) {
            key = iterator.next();
            if (extid.equals(key)) {
                Log.i(TAG, "getStringValue: get the target value " + jsonObject.get(key) + " ------  " + key);
                result = String.valueOf(jsonObject.get(key));
                break;
            }
        }
        return result;
    }


    private void setWorkEvent(JSONObject jsonObject) throws JSONException {
        String result = null;
        String extid;
        String key;
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            key = iterator.next();
            if ("event_gotowork".equals(key)) {
                result = String.valueOf(jsonObject.get(key));
                Log.i(TAG, "setWorkEvent: " + result);
                workBtnUpdate(Boolean.valueOf(result));
                GizDeviceManager.getInstance().setGoToWork(Boolean.valueOf(result));
//                FileUtil.saveBoolean(MainActivity.this,Constants.SP_GOTOWORK,Boolean.valueOf(result));
                break;
            }
        }
    }

    private void setOffEvent(JSONObject jsonObject) throws JSONException {
        String result = null;
        String extid;
        String key;
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            key = iterator.next();
            if ("event_gooffwork".equals(key)) {
                result = String.valueOf(jsonObject.get(key));
//                FileUtil.saveBoolean(MainActivity.this, Constants.SP_GOTOWORK, !Boolean.valueOf(result));
                GizDeviceManager.getInstance().setGoToWork(!Boolean.valueOf(result));
                workBtnUpdate(!Boolean.valueOf(result));
                break;
            }
        }
    }


    private void getWeather(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HttpUtils.WEATHER_URL_START)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        HttpInterface httpInterface = retrofit.create(HttpInterface.class);

        Call<JsonObject> call = httpInterface.getWeather(cityName, Constants.HeWeatherApiKey);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loadingDialog.dismiss();
                System.out.println("----weather--success----");
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
//                    JsonArray jsonArray = response.body().getAsJsonArray("HeWeather data service 3.0");
                    JsonArray jsonArray = response.body().getAsJsonArray("HeWeather5");
                    List<WeatherBean> list = gson.fromJson(jsonArray.toString(), new TypeToken<List<WeatherBean>>() {
                    }.getType());
                    weatherBean = list.get(0);
                    FileUtil.saveString(MainActivity.this, Constants.SP_WEATHER, weatherBean.toString());
                    getWeatherUpdate();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loadingDialog.dismiss();
//                System.out.println("---@--failure--@---" + t.getMessage());
            }
        });

    }


    /**
     * 更新户外天气显示
     */
    private void getWeatherUpdate() {
        for (int i = 0; i < list.size(); i++) {
            View v = list.get(i);
            TextView main_out_weather = (TextView) v.findViewById(R.id.main_out_weather);//显示户外天气
            TextView main_out_temperature = (TextView) v.findViewById(R.id.main_out_temperature);//显示户外温度
            TextView main_out_pm = (TextView) v.findViewById(R.id.main_out_pm);//显示户外pm2.5

            try {
                main_out_weather.setText(weatherBean.getNow().getCond().getTxt());
                main_out_temperature.setText(weatherBean.getNow().getTmp() + "℃");
                main_out_pm.setText(weatherBean.getAqi().getCity().getPm25());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * 根据 receive 的 alerts 字段进行设置
     */
    private void setSecurityData(JSONObject obj) {
        int securityNumber = getSecurityNumber(obj);
        mian_secyrutynumber_txt.setText("共有" + securityNumber + "个安防开启");
        if (securityNumber > 0) {
            main_btn_security.setImageResource(R.mipmap.purification_btn_on);
            main_txt_security.setText("AUTO");
            security_flg = true;
        } else {
            main_btn_security.setImageResource(R.mipmap.purification_btn_off);
            main_txt_security.setText("OFF");
            security_flg = false;
        }
    }


    /**
     * 更新数据显示
     * 这里更新的是 主要数值的显示数据  地点 pm  温湿度 等
     */
    private void setMainValue(JSONObject jsonObject) {
        House house = Constants.house;
        for (int i = 0; i < list.size(); i++) {
            Room room = house.getRooms().get(i);
            View v = list.get(i);
            TextView main_temperature_txt = (TextView) v.findViewById(R.id.main_temperature_txt);//显示温度
            TextView main_humidity_txt = (TextView) v.findViewById(R.id.main_humidity_txt);//显示湿度
            TextView main_pm_txt = (TextView) v.findViewById(R.id.main_pm_txt);//显示pm2.5
            TextView main_room_name = (TextView) v.findViewById(R.id.main_room_name);//显示房间名称
            TextView main_house_name = (TextView) v.findViewById(R.id.main_house_name);//显示房屋名称
            main_room_name.setText(StringHelper.splitName(room.getName()));
            main_house_name.setText(Constants.house.getName());
            DecimalFormat df = new DecimalFormat("0.0");
            ArrayList<Categorie> categorielist = room.getCategories();
            for (Categorie categorie : categorielist) {
                if (categorie.getId() == 30) {//温湿度
                    temhumdata(jsonObject, main_temperature_txt, main_humidity_txt, df, categorie);
                } else if (categorie.getId() == 20) {//空气
                    pmdata(jsonObject, main_pm_txt, categorie);
                }
            }
        }


    }


    private void getNumberUpdate(JSONObject jsonObject) {
        Constants.house = gson.fromJson(FileUtil.loadString(MainActivity.this, Constants.SP_HOUSE), House.class);

        for (int i = 0; i < list.size(); i++) {
            Room room = Constants.house.getRooms().get(i);
            View v = list.get(i);
            TextView main_temperature_txt = (TextView) v.findViewById(R.id.main_temperature_txt);//显示温度
            TextView main_humidity_txt = (TextView) v.findViewById(R.id.main_humidity_txt);//显示湿度
            TextView main_pm_txt = (TextView) v.findViewById(R.id.main_pm_txt);//显示pm2.5

            TextView main_room_name = (TextView) v.findViewById(R.id.main_room_name);//显示房间名称
            TextView main_house_name = (TextView) v.findViewById(R.id.main_house_name);//显示房屋名称
            main_room_name.setText(StringHelper.splitName(room.getName()));
            main_house_name.setText(Constants.house.getName());
            DecimalFormat df = new DecimalFormat("0.0");
            ArrayList<Categorie> categorielist = room.getCategories();
            for (Categorie categorie : categorielist) {
                if (categorie.getId() == 30) {//温湿度
                    temhumdata(jsonObject, main_temperature_txt, main_humidity_txt, df, categorie);
                } else if (categorie.getId() == 20) {//空气
                    pmdata(jsonObject, main_pm_txt, categorie);
                }
            }
        }
    }

    private void pmdata(JSONObject jsonObject, TextView main_pm_txt, Categorie categorie) {
        for (Device device : categorie.getDevices()) {
            if (device.getId() == 80) {//pm2.5检测
                try {
                    String value = String.valueOf(jsonObject.get(device.getExtid()));//字符串
                    if (value != null) {

                        if (value.contains("true") || value.contains("false")) {
                        } else {
                            double number = Double.valueOf(value);
                            main_pm_txt.setText(number + "");
                        }
                    }

                } catch (JSONException e) {
//                    e.printStackTrace();
                }
            }
        }
    }

    private void temhumdata(JSONObject jsonObject, TextView main_temperature_txt, TextView main_humidity_txt, DecimalFormat df, Categorie categorie) {
        for (Device device : categorie.getDevices()) {
            if (device.getId() == 22) {//湿度检测
                try {
                    Iterator<String> keyIterator = jsonObject.keys();
                    String key = null;
                    String value = null;
                    while (keyIterator.hasNext()) {
                        key = keyIterator.next();
                        if (key.equals(device.getExtid())) {
                            value = String.valueOf(jsonObject.get(key));
                            break;
                        }
                    }
                    if (value != null) {
                        if (value.contains("true") || value.contains("false")) {
                        } else {
                            double number = Double.valueOf(value) / 10;
                            main_humidity_txt.setText(df.format(number) + "");
                        }
                    }
                } catch (JSONException e) {
//                    e.printStackTrace();
                } catch (NullPointerException e) {

                }
            } else if (device.getId() == 25) {//温度检测
                try {
                    String value = String.valueOf(jsonObject.get(device.getExtid()));
                    if (value.contains("true") || value.contains("false")) {
                    } else {
                        double number = Double.valueOf(value) / 10;
                        main_temperature_txt.setText(df.format(number) + "");
                    }
                } catch (JSONException e) {
//                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 点击温度按钮
     */
    private void getTemperatureClick() {
        if (temperature_flg) {
            main_btn_temperature.setImageResource(R.mipmap.temperature_btn_off);
            main_txt_temperature.setText("OFF");
        } else {
            main_btn_temperature.setImageResource(R.mipmap.temperature_btn_on);
            main_txt_temperature.setText("AUTO");
        }
        for (Device device : tempDevices) {
            CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), device.getExtid(), !temperature_flg ? 220 : 998);
        }
        temperature_flg = !temperature_flg;
    }

    /**
     * 点击空气按钮
     */
    private void getAirClick() {
        if (air_flg) {
            main_btn_air.setImageResource(R.mipmap.purification_btn_off);
            main_txt_air.setText("OFF");
        } else {
            main_btn_air.setImageResource(R.mipmap.purification_btn_on);
            main_txt_air.setText("AUTO");
        }

        for (Device device : airDevices) {
            switch (device.getSub_category()) {
                case 25://二氧化碳设置
                    CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(), device.getExtid(), !air_flg ? 500 : 9998);
                    break;
                case 26://甲醛设置
                    CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(), device.getExtid(), !air_flg ? 3 : 9998);
                    break;
                case 24://pm25设置
                    CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(), device.getExtid(), !air_flg ? 8 : 998);
                    break;
                case 27://有害气体设置
                    CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(), device.getExtid(), !air_flg ? 20 : 9998);
                    break;
            }
        }
        air_flg = !air_flg;

    }


    int lightLevel = 0;

    /**
     * 点击灯光按钮
     * int light level
     */
    private void getLightClick() {
        if (lightLevel < 4) {
            lightLevel++;
        } else {
            lightLevel = 0;
        }
        setLightBtnState(lightLevel);

        if (GizDeviceManager.getInstance().getEventLightAuto() != null && GizDeviceManager.getInstance().getEventLightOnOff() != null) {
            switch (lightLevel) {
                case LIGHT_BTN_STATE_AUTO://自动模式开

                    Log.i("lightcmd", "getLightClick: LIGHT_BTN_STATE_AUTO " + GizDeviceManager.getInstance().getEventLightAuto().getExtid());
//                CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(),
//                        GizDeviceManager.getInstance().getEventLightAuto().getExtid(), 1);

                    break;
                case LIGHT_BTN_STATE_MENTRAL://自动模式关
                    Log.i("lightcmd", "getLightClick: LIGHT_BTN_STATE_AUTO " + GizDeviceManager.getInstance().getEventLightAuto().getExtid());
//                CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(),
//                        GizDeviceManager.getInstance().getEventLightAuto().getExtid(), 0);

                    break;
                case LIGHT_BTN_STATE_MENTRALON://自动模式关 手动全开
                    Log.i("lightcmd", "getLightClick: LIGHT_BTN_STATE_MENTRALON " + GizDeviceManager.getInstance().getEventLightAuto().getExtid());
                    Log.i("lightcmd", "getLightClick: LIGHT_BTN_STATE_MENTRALON " + GizDeviceManager.getInstance().getEventLightOnOff().getExtid());
//                CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(),
//                        GizDeviceManager.getInstance().getEventLightAuto().getExtid(), 0);
//                CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(),
//                        GizDeviceManager.getInstance().getEventLightOnOff().getExtid(), 1);
                    break;
                case LIGHT_BTN_STATE_MENTRALOFF://自动模式关 手动全关
                    Log.i("lightcmd", "getLightClick: LIGHT_BTN_STATE_MENTRALOFF " + GizDeviceManager.getInstance().getEventLightAuto().getExtid());
                    Log.i("lightcmd", "getLightClick: LIGHT_BTN_STATE_MENTRALOFF " + GizDeviceManager.getInstance().getEventLightOnOff().getExtid());
//                CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(),
//                        GizDeviceManager.getInstance().getEventLightAuto().getExtid(), 0);
//                CmdCenter.getInstance(MainActivity.this).cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(),
//                        GizDeviceManager.getInstance().getEventLightOnOff().getExtid(), 0);
                    break;
            }
        } else {
            for (Device device : lightDevices) {
                CmdCenter.getInstance(MainActivity.this).
                        cWrite(CmdCenter.getInstance(MainActivity.this).getXpgWifiDevice(),
                                device.getExtid(), !light_flg ? true : false);
            }
        }
//        AppUtils.show(MainActivity.this, " level : " + lightLevel);
    }

    /**
     * 点击湿度度按钮
     */
    private void getHumClick() {
        if (humidity_flg) {
            main_btn_humidity.setImageResource(R.mipmap.humidity_btn_off);
            main_txt_humidity.setText("OFF");

        } else {
            main_btn_humidity.setImageResource(R.mipmap.humidity_btn_on);
            main_txt_humidity.setText("AUTO");

        }
        for (Device device : humDevices) {
            CmdCenter.getInstance(getApplicationContext()).cWrite(CmdCenter.getInstance(getApplicationContext()).getXpgWifiDevice(), device.getExtid(), !humidity_flg ? 500 : 998);
        }
        humidity_flg = !humidity_flg;

    }

    /**
     * 点击安防按钮
     */
    private void getSecruityClick() {

        if (security_flg) {
            main_btn_security.setImageResource(R.mipmap.security_btn_off);
            main_txt_security.setText("OFF");
        } else {
            main_btn_security.setImageResource(R.mipmap.security_btn_on);
            main_txt_security.setText("AUTO");
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_control://详细控制
                Bundle bundle = new Bundle();
                bundle.putSerializable("ELECTRICITYDEVICES", electricityDevices);
                intent.putExtra("ELEBUNDLE", bundle);
                intent.setClass(MainActivity.this, DetailedControlActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_outdoor://户外详情
                intent.setClass(MainActivity.this, OutDoorActivity.class);
                startActivity(intent);
                break;
            case R.id.main_btn_gowork://上班
                workBtnUpdate(true);
                Message msg = new Message();
                msg.what = 0;
                mHandler.sendMessage(msg);
                break;
            case R.id.main_btn_offwork://下班
                workBtnUpdate(false);
                Message msg2 = new Message();
                msg2.what = 1;
                mHandler.sendMessage(msg2);
                break;
            case R.id.main_btn_humidity://湿度控制按钮
                getHumClick();
                break;
            case R.id.main_btn_air://空气净化按钮
                getAirClick();
                break;
            case R.id.main_btn_light://照明开关按钮
                getLightClick();
                break;
            case R.id.main_btn_temperature://温度控制按钮
                getTemperatureClick();
                break;
            case R.id.main_btn_security://办公安防范妞
                getSecruityClick();
                break;
            case R.id.lock_house_btn://查看房屋
                if (drawer_layout.isDrawerOpen(right_layout)) {
                    drawer_layout.closeDrawer(right_layout);
                }
                break;
            case R.id.office_administration_btn://办公室管理
                if (drawer_layout.isDrawerOpen(right_layout)) {
                    drawer_layout.closeDrawer(right_layout);
                } else {
                    drawer_layout.openDrawer(right_layout);
                }
                intent.setClass(MainActivity.this, OfficeManagementActivity.class);
//                startActivity(intent);
                startActivityForResult(intent, 100);
                break;
            case R.id.user_center_btn://用户中心
                if (drawer_layout.isDrawerOpen(right_layout)) {
                    drawer_layout.closeDrawer(right_layout);
                }
                intent.setClass(MainActivity.this, UserCenterActivity.class);
                startActivity(intent);
                break;
            case R.id.feedback_btn://建议反馈
                if (drawer_layout.isDrawerOpen(right_layout)) {
                    drawer_layout.closeDrawer(right_layout);
                }
                intent.setClass(MainActivity.this, FeedBackActivity.class);
                startActivity(intent);
                break;
            case R.id.head_back:
                break;
            case R.id.head_img://侧滑开关
                if (drawer_layout.isDrawerOpen(right_layout)) {
                    drawer_layout.closeDrawer(right_layout);
                } else {
                    drawer_layout.openDrawer(right_layout);
                }
                break;
            case R.id.person_head_img://点击侧边头像
                if (drawer_layout.isDrawerOpen(right_layout)) {
                    drawer_layout.closeDrawer(right_layout);
                }
                intent.setClass(MainActivity.this, UserCenterActivity.class);
                startActivity(intent);
                break;
            case R.id.mian_lightnumber_layout:
                intent.setClass(MainActivity.this, DetailedControlActivity.class);
                intent.putExtra("BANNERTAG", 3);
                Bundle bundle2 = new Bundle();
                bundle2.putSerializable("ELECTRICITYDEVICES", electricityDevices);
                intent.putExtra("ELEBUNDLE", bundle2);
                startActivity(intent);
                break;
            case R.id.mian_secyrutynumber_layout:
                intent.setClass(MainActivity.this, DetailedControlActivity.class);
                intent.putExtra("BANNERTAG", 4);
                Bundle bundle3 = new Bundle();
                bundle3.putSerializable("ELECTRICITYDEVICES", electricityDevices);
                intent.putExtra("ELEBUNDLE", bundle3);
                startActivity(intent);
                break;
            case R.id.mian_electritynumber_layout:
                intent.setClass(MainActivity.this, DetailedControlActivity.class);
                intent.putExtra("BANNERTAG", 2);
                Bundle bundle4 = new Bundle();
                bundle4.putSerializable("ELECTRICITYDEVICES", electricityDevices);
                intent.putExtra("ELEBUNDLE", bundle4);
                startActivity(intent);
                break;
        }

    }

    private void workBtnUpdate(boolean flg) {
        if (flg) {
            main_btn_gowork.setImageResource(R.mipmap.go_to_work_btn_on);
            main_btn_offwork.setImageResource(R.mipmap.after_work_off);
        } else {
            main_btn_gowork.setImageResource(R.mipmap.go_to_work_btn_off);
            main_btn_offwork.setImageResource(R.mipmap.after_work_on);
        }
    }


    public class MyPagerAdapter extends PagerAdapter {

        private List<View> list;

        public MyPagerAdapter(List<View> list) {
            this.list = list;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(list.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(list.get(position), 0);//添加页卡
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private int delayCount = 0;

    private void setLightBtnState(int state) {
        switch (state) {
            case LIGHT_BTN_STATE_AUTO:
                light_flg = true;
                main_btn_light.setImageResource(R.mipmap.lighting_btn_on);
                main_txt_light.setText("AUTO");
                break;
            case LIGHT_BTN_STATE_MENTRAL:
                light_flg = false;
                main_btn_light.setImageResource(R.mipmap.lighting_btn_on);
                main_txt_light.setText("自动关");
                break;
            case LIGHT_BTN_STATE_MENTRALON:
                light_flg = true;
                main_btn_light.setImageResource(R.mipmap.lighting_btn_off);
                main_txt_light.setText("手动开");
                break;
            case LIGHT_BTN_STATE_MENTRALOFF:
                light_flg = false;
                main_btn_light.setImageResource(R.mipmap.lighting_btn_off);
                main_txt_light.setText("手动关");
                break;
            default:
                break;
        }
    }


    @Override
    protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
        super.didReceiveData(result, device, dataMap, sn);
        if (isFinishing()) return;
//        if (delayCount == 3) {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        if (dataMap != null && !dataMap.toString().equals("{}")) {
            String str = gson.toJson(dataMap);
            try {
                if (str != null && !str.equals("")) {


                    JSONObject dataObject = new JSONObject(str).getJSONObject("data");
                    Log.i(TAG, "didReceiveData: data " + dataObject.toString());
                    FileUtil.saveString(MainActivity.this, Constants.JZYSTR, str);
                    setMainValue(dataObject);
                    getUpDateDeviceList(dataObject);
                    setWorkEvent(dataObject);
                    setOffEvent(dataObject);
                    setLightBtnState(getLightButtonState(dataObject));


                    JSONObject alertsObject = new JSONObject(str).getJSONObject("alerts");
                    setSecurityData(alertsObject);
                    if (weather_out_tag++ > 10) {
                        weather_out_tag = 1;
                        getWeather(GizDeviceManager.getInstance().
                                getHouseByGateway(GizDeviceManager.getInstance().getCurrentGateway())
                                .getCity());
                    }
                }
            } catch (JSONException e) {
//                e.printStackTrace();
            }
        }
    }


    @Override
    protected void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
        super.didDiscovered(result, deviceList);
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        if (isFinishing()) return;
        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS && (deviceList.size() > 0)) {
            //更新配置文件成功，返回设备列表
            deviceslist = deviceList;
            GizWifiDevice mDevice = deviceslist.get(0);
            if (!mDevice.isBind()) {
                GizWifiSDK.sharedInstance().bindRemoteDevice(FileUtil.loadString(MainActivity.this, Constants.SP_UID), FileUtil.loadString(MainActivity.this, Constants.SP_TOKEN),
                        mDevice.getMacAddress(), mDevice.getProductKey(), null);
            } else {
                mXpgWifiDevice = mDevice;
                mXpgWifiDevice.setListener(deviceListener);
                if (mXpgWifiDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled) {
                    GizWifiSDK.sharedInstance().updateDeviceFromServer(Constants.PRODUCT_KEY);
                } else {
                    mXpgWifiDevice.setSubscribe(true);
                }
            }

        } else {
            System.out.println("---10-----" + result.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*检查 对象回收*/
        mXpgWifiDevice = GizDeviceManager.getInstance().getCurrentDevice();
        if (mXpgWifiDevice == null) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LogInActivity.class);
            startActivity(intent);
            loadingDialog.dismiss();
            this.finish();
        } else {
//            Log.i("MainActivity", "onResume: current "+mXpgWifiDevice.getProductName());
            mXpgWifiDevice.setListener(deviceListener);
            CmdCenter.getInstance(this).cGetStatus(mXpgWifiDevice);

        }
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        init();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            mXpgWifiDevice = GizDeviceManager.getInstance().getCurrentDevice();
            mXpgWifiDevice.setListener(deviceListener);
            CmdCenter.getInstance(this).cGetStatus(mXpgWifiDevice);
            init();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mXpgWifiDevice.disconnect();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void getEventBus(EventBus_Account message) {
        int tag = message.tag;
        if (tag == Constants.EVENT_BUS_MODIFY_PERSONAL_INFORMATION) {
            if (Constants.outHouse == null) {
                Constants.outHouse = gson.fromJson(FileUtil.loadString(MainActivity.this, Constants.SP_OUTHOUSE), OutModel_PersonalInForMation.class);
            }
            String imgUrl = Constants.outHouse.getResult().getProfile().getAvatarPath();
            if (imgUrl != null) {
                imgUrl = imgUrl.replace("https", "http");
                person_head_img.setImageURI(imgUrl);
            }

            String nameStr = Constants.outHouse.getResult().getProfile().getFirstName();
            if (nameStr == null || nameStr.equals("")) {
                usermame.setText("火星人");
            } else {
                usermame.setText(nameStr);
            }
        } else if (tag == Constants.EVENT_BUS_MODIFY_PERSONAL_HOUSE) {
            if (Constants.outHouse == null) {
                Constants.outHouse = gson.fromJson(FileUtil.loadString(MainActivity.this, Constants.SP_OUTHOUSE), OutModel_PersonalInForMation.class);
            }

            Constants.house = Constants.outHouse.getResult().getHouses().get(FileUtil.loadInt(MainActivity.this, Constants.SP_HOUSE_ID, 0));
            FileUtil.saveString(MainActivity.this, Constants.SP_HOUSE, gson.toJson(Constants.house));

            list = new ArrayList<View>();
            LayoutInflater lf = getLayoutInflater().from(this);
            if (Constants.house != null && Constants.house.getRooms() != null && Constants.house.getRooms().size() > 0) {
                for (int i = 0; i < Constants.house.getRooms().size(); i++) {
                    View view = lf.inflate(R.layout.main_banner_view, null);
                    list.add(view);
                }
            }
            adapter = new MyPagerAdapter(list);
            viewPager.setAdapter(adapter);

        }
    }

}
