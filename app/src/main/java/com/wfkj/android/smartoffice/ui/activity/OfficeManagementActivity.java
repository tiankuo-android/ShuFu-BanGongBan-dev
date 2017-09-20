package com.wfkj.android.smartoffice.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.google.gson.Gson;
import com.wfkj.android.smartoffice.R;
import com.wfkj.android.smartoffice.adapter.OfficeManagementAdapter;
import com.wfkj.android.smartoffice.model.sever_model.House;
import com.wfkj.android.smartoffice.model.sever_model.OutModel_PersonalInForMation;
import com.wfkj.android.smartoffice.my_interface.ManagementInterface;
import com.wfkj.android.smartoffice.util.Constants;
import com.wfkj.android.smartoffice.util.FileUtil;
import com.wfkj.android.smartoffice.util.GizDeviceManager;
import com.wfkj.android.smartoffice.util.jzy_util.CmdCenter;
import com.wfkj.android.smartoffice.util.view_util.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdongyang on 17/2/7.
 */
public class OfficeManagementActivity extends Activity implements OnClickListener {

    private final String TAG = "OfficeManagement";
    private Gson gson = new Gson();

    private ImageView head_back;
    private TextView head_title;
    private ImageView head_img;

    private LoadingDialog loadingDialog;

    private ListView office_management_listview;
    private OfficeManagementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_management);
        init();
        loadingDialog = new LoadingDialog(OfficeManagementActivity.this);
    }

    private void init() {
        head_back = (ImageView) this.findViewById(R.id.head_back);
        head_title = (TextView) this.findViewById(R.id.head_title);
        head_img = (ImageView) this.findViewById(R.id.head_img);
        office_management_listview = (ListView) this.findViewById(R.id.office_management_listview);

        head_back.setOnClickListener(this);
        head_title.setText("房屋管理");
        head_img.setVisibility(View.GONE);

        adapter = new OfficeManagementAdapter(OfficeManagementActivity.this, Constants.outHouse.getResult().getHouses(), new ManagementInterface() {
            @Override
            public void getClick(int tag, String id, String name) {
                Intent intent = new Intent();
                intent.setClass(OfficeManagementActivity.this, ModifyActivity.class);
                intent.putExtra("TAG", tag);
                intent.putExtra("ID", id);
                intent.putExtra("NAME", name);
                startActivityForResult(intent, 100);
            }
        });
        office_management_listview.setAdapter(adapter);
        adapter.setListener(new OfficeManagementAdapter.HouseSwitchClickListener() {
            @Override
            public void houseSwitched(final String productKey) {
                /*判断是否是当前正在显示的机器*/
                if (productKey.equals(GizDeviceManager.getInstance().getCurrentGateway())) {
                    return;
                } else {
                    loadingDialog.show();
                    /*切换 目标productkey 下绑定的设备*/
                    GizWifiSDK.sharedInstance().setListener(new GizWifiSDKListener() {
                        @Override
                        public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
                            super.didDiscovered(result, deviceList);
                            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS && deviceList.size() > 0) {//获取 设备列表成功
                                Log.i(TAG, "didDiscovered: success");
                                /* 取消 当前设备 的数据订阅*/
                                final GizWifiDevice device = GizDeviceManager.getInstance().getCurrentDevice();
                                if (device.isSubscribed()) {
                                    device.setSubscribe(device.getProductKey(), false);
                                }
                                GizDeviceManager.getInstance().setCurrentDevice(deviceList.get(0));//保存当前设备
                                GizDeviceManager.getInstance().setCurrentGateway(productKey);//保存当前 key
                                GizDeviceManager.getInstance().getCurrentDevice().setSubscribe(productKey, true);//对获取的设备订阅消息
                                /*以前方法   更换这个 house 对象  保证界面的显示切换*/
                                for (House house : Constants.outHouse.getResult().getHouses()) {
                                    if (house.getGateways().get(0).getExtid().equals(productKey)) {
                                        Log.i(TAG, "didDiscovered: "+house.getName());
                                        Constants.house = house;
                                        CmdCenter.getInstance(OfficeManagementActivity.this).setXpgWifiDevice(GizDeviceManager.getInstance().getCurrentDevice());
                                        FileUtil.saveString(OfficeManagementActivity.this, Constants.SP_HOUSE, gson.toJson(house));
                                    }
                                }
                                /*关闭界面*/
                                loadingDialog.dismiss();
                                OfficeManagementActivity.this.finish();
                            } else {//如果从机智云获取设备失败了  保持原来的订阅状态
                                Log.i(TAG, "didDiscovered: failure");
                            }
                        }
                    });
                    /*执行获取绑定设备 在上面的回调里处理*/
                    List<String> keys = new ArrayList<String>(1);
                    keys.add(productKey);
                    GizWifiSDK.sharedInstance().getBoundDevices(GizDeviceManager.getInstance().getGizUID(),
                            GizDeviceManager.getInstance().getGizToken(), keys);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.head_back:
                this.finish();
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 111:
            case 112:
                if (Constants.outHouse == null) {
                    Constants.outHouse = gson.fromJson(FileUtil.loadString(OfficeManagementActivity.this, Constants.SP_OUTHOUSE), OutModel_PersonalInForMation.class);
                }
                adapter.notifyDataSetChanged();
                break;
        }
    }
}
