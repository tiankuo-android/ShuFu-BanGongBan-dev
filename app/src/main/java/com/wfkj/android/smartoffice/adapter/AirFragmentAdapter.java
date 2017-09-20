package com.wfkj.android.smartoffice.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wfkj.android.smartoffice.R;
import com.wfkj.android.smartoffice.model.sever_model.Categorie;
import com.wfkj.android.smartoffice.model.sever_model.Device;
import com.wfkj.android.smartoffice.model.sever_model.Room;
import com.wfkj.android.smartoffice.my_interface.AirFragmentAdapterUpDate;
import com.wfkj.android.smartoffice.ui.activity.DetailedControlActivity;
import com.wfkj.android.smartoffice.util.GizDeviceManager;
import com.wfkj.android.smartoffice.util.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wangdongyang on 16/11/21.
 */
public class AirFragmentAdapter extends BaseAdapter {
    private final String TAG = "AirFragment";

    private Context context;
    private List<com.wfkj.android.smartoffice.model.sever_model.Room> rooms;
    private AirFragmentAdapterUpDate airFragmentAdapterUpDate;
    private JSONObject jsonObject;

    public AirFragmentAdapter(Context context, JSONObject jsonObject, List<com.wfkj.android.smartoffice.model.sever_model.Room> rooms, AirFragmentAdapterUpDate airFragmentAdapterUpDate) {
        this.context = context;
        this.rooms = rooms;
        this.airFragmentAdapterUpDate = airFragmentAdapterUpDate;
        this.jsonObject = jsonObject;
    }

    /**
     * 将 json 中的数据填充到 list 中
     */
    public void upDate(List<com.wfkj.android.smartoffice.model.sever_model.Room> rooms, JSONObject jsonObject) {
        this.rooms = rooms;
        this.jsonObject = jsonObject;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_air, null);
            viewHolder.air_room_name = (TextView) convertView.findViewById(R.id.air_room_name);
            viewHolder.air_room_pm = (TextView) convertView.findViewById(R.id.air_room_pm);
            viewHolder.air_room_btn1 = (TextView) convertView.findViewById(R.id.air_room_btn1);
            viewHolder.air_room_btn2 = (TextView) convertView.findViewById(R.id.air_room_btn2);
            viewHolder.air_room_btn3 = (TextView) convertView.findViewById(R.id.air_room_btn3);
            viewHolder.air_room_btn_off = (TextView) convertView.findViewById(R.id.air_room_btn_off);
            viewHolder.air_room_co2 = (TextView) convertView.findViewById(R.id.air_room_co2);
            viewHolder.air_room_voc = (TextView) convertView.findViewById(R.id.air_room_voc);
            viewHolder.air_room_jq = (TextView) convertView.findViewById(R.id.air_room_jq);
            viewHolder.air_room_pressure = (TextView) convertView.findViewById(R.id.air_room_pressure);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Room room = rooms.get(position);
        String pm = "0";
        viewHolder.air_room_name.setText(StringHelper.splitName(room.getName()));
        ArrayList<Categorie> categories = rooms.get(position).getCategories();
        Categorie categorieAir = getCategorie(categories);
        ArrayList<Device> devices = null;
        if (categorieAir != null) {
            devices = categorieAir.getDevices();
            viewHolder.air_room_btn1.setEnabled(true);
            viewHolder.air_room_btn2.setEnabled(true);
            viewHolder.air_room_btn3.setEnabled(true);
            viewHolder.air_room_btn_off.setEnabled(true);
        } else {
            System.out.println("------null-------");
            viewHolder.air_room_btn1.setEnabled(false);
            viewHolder.air_room_btn2.setEnabled(false);
            viewHolder.air_room_btn3.setEnabled(false);
            viewHolder.air_room_btn_off.setEnabled(false);
        }

        DecimalFormat df = new DecimalFormat("0.00");

        int pmValue = -1;
        int vocValue = -1;
        int hchoValue = -1;
        int carValue = -1;

        if (devices != null && devices.size() > 0) {
            for (Device device : devices) {
                if (device.getId() == 78) {//二氧化碳检测
                    try {
                        int number = (Integer) (jsonObject.get(device.getExtid()));
                        viewHolder.air_room_co2.setText(number + "");
                        carValue = number;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (device.getId() == 79) {//甲醛检测
                    try {
                        double number = (double) (Integer) (jsonObject.get(device.getExtid())) / 100;
                        viewHolder.air_room_jq.setText(df.format(number) + "");
                        hchoValue = (int) number;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (device.getId() == 80) {//pm25检测
                    try {
                        int number = (Integer) (jsonObject.get(device.getExtid()));
                        viewHolder.air_room_pm.setText(number + "");
                        pmValue = number;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (device.getId() == 81) {//voc检测
                    try {
                        double number = (double) (Integer) (jsonObject.get(device.getExtid())) / 100;
                        viewHolder.air_room_voc.setText(df.format(number) + "");
                        vocValue = (int) number;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            int gear = AirEventClass.checkGear(getEventPm(jsonObject), getEventVoc(jsonObject), getEventHcho(jsonObject), getEventCar(jsonObject));
            getBtnClick(gear, viewHolder.air_room_btn1, viewHolder.air_room_btn2, viewHolder.air_room_btn3, viewHolder.air_room_btn_off);
            Log.i(TAG, "getView: " + gear);
        } else {

        }

        viewHolder.air_room_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBtnClick(3, viewHolder.air_room_btn1, viewHolder.air_room_btn2, viewHolder.air_room_btn3, viewHolder.air_room_btn_off);
                update_fragment(position, 3);
                DetailedControlActivity.air_set_numbers.set(position, "3");
            }
        });
        viewHolder.air_room_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBtnClick(2, viewHolder.air_room_btn1, viewHolder.air_room_btn2, viewHolder.air_room_btn3, viewHolder.air_room_btn_off);
                update_fragment(position, 2);
                DetailedControlActivity.air_set_numbers.set(position, "2");
            }
        });

        viewHolder.air_room_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBtnClick(1, viewHolder.air_room_btn1, viewHolder.air_room_btn2, viewHolder.air_room_btn3, viewHolder.air_room_btn_off);
                update_fragment(position, 1);
                DetailedControlActivity.air_set_numbers.set(position, "1");
            }
        });
        viewHolder.air_room_btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getBtnClick(0, viewHolder.air_room_btn1, viewHolder.air_room_btn2, viewHolder.air_room_btn3, viewHolder.air_room_btn_off);
                update_fragment(position, 0);
                DetailedControlActivity.air_set_numbers.set(position, "0");

            }
        });


        return convertView;
    }

    private int getEventPm(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        String key = null;
        String value = null;
        while (keys.hasNext()) {
            key = keys.next();
            if (key.equals("event_air_sensor_pm25")) {
                try {
                    value = String.valueOf(jsonObject.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "getEventPm: " + value);
                return Integer.valueOf(value);
            }
        }
        return -1;
    }

    private int getEventVoc(JSONObject jsonObject) {

        Iterator<String> keys = jsonObject.keys();
        String key = null;
        String value = null;
        while (keys.hasNext()) {
            key = keys.next();
            if (key.equals("event_air_sensor_voc")) {
                try {
                    value = String.valueOf(jsonObject.get(key));
                    Log.i(TAG, "getEventVoc: " + value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return Integer.valueOf(value);
            }
        }
        return -1;
    }

    private int getEventHcho(JSONObject jsonObject) {

        Iterator<String> keys = jsonObject.keys();
        String key = null;
        String value = null;
        while (keys.hasNext()) {
            key = keys.next();
            if (key.equals("event_air_sensor_HCHO")) {
                try {
                    value = String.valueOf(jsonObject.get(key));
                    Log.i(TAG, "getEventHcho: " + value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return Integer.valueOf(value);
            }
        }
        return -1;
    }

    private int getEventCar(JSONObject jsonObject) {

        Iterator<String> keys = jsonObject.keys();
        String key = null;
        String value = null;
        while (keys.hasNext()) {
            key = keys.next();
            if (key.equals("event_air_sensor_co2")) {
                try {
                    value = String.valueOf(jsonObject.get(key));
                    Log.i(TAG, "getEventCar: " + value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return Integer.valueOf(value);
            }
        }
        return -1;
    }

    private Categorie getCategorie(ArrayList<Categorie> categories) {
        for (Categorie categorie : categories) {
            if (categorie.getId() == GizDeviceManager.AIR_CLEANER_TYPE) {//获取房间内  净化器设备分类
                return categorie;
            }
        }
        return null;
    }

    private void getBtnClick(int tag, TextView air_room_btn1, TextView air_room_btn2, TextView air_room_btn3, TextView air_room_btn_off) {
        switch (tag) {
            case -1:
                air_room_btn1.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn2.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn3.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn_off.setBackgroundResource(R.drawable.radio_text_grap_shape);

                air_room_btn1.setTextColor(Color.parseColor("#585858"));
                air_room_btn2.setTextColor(Color.parseColor("#585858"));
                air_room_btn3.setTextColor(Color.parseColor("#585858"));
                air_room_btn_off.setTextColor(Color.parseColor("#585858"));
                break;
            case 0:
                air_room_btn1.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn2.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn3.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn_off.setBackgroundResource(R.drawable.radio_text_red_shape);


                air_room_btn1.setTextColor(Color.parseColor("#585858"));
                air_room_btn2.setTextColor(Color.parseColor("#585858"));
                air_room_btn3.setTextColor(Color.parseColor("#585858"));
                air_room_btn_off.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 1:
                air_room_btn1.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn2.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn3.setBackgroundResource(R.drawable.radio_text_blue_shape);
                air_room_btn_off.setBackgroundResource(R.drawable.radio_text_grap_shape);


                air_room_btn1.setTextColor(Color.parseColor("#585858"));
                air_room_btn2.setTextColor(Color.parseColor("#585858"));
                air_room_btn3.setTextColor(Color.parseColor("#ffffff"));
                air_room_btn_off.setTextColor(Color.parseColor("#585858"));
                break;
            case 2:

                air_room_btn1.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn2.setBackgroundResource(R.drawable.radio_text_blue_shape);
                air_room_btn3.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn_off.setBackgroundResource(R.drawable.radio_text_grap_shape);

                air_room_btn1.setTextColor(Color.parseColor("#585858"));
                air_room_btn2.setTextColor(Color.parseColor("#ffffff"));
                air_room_btn3.setTextColor(Color.parseColor("#585858"));
                air_room_btn_off.setTextColor(Color.parseColor("#585858"));
                break;
            case 3:
                air_room_btn1.setBackgroundResource(R.drawable.radio_text_blue_shape);
                air_room_btn2.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn3.setBackgroundResource(R.drawable.radio_text_grap_shape);
                air_room_btn_off.setBackgroundResource(R.drawable.radio_text_grap_shape);

                air_room_btn1.setTextColor(Color.parseColor("#ffffff"));
                air_room_btn2.setTextColor(Color.parseColor("#585858"));
                air_room_btn3.setTextColor(Color.parseColor("#585858"));
                air_room_btn_off.setTextColor(Color.parseColor("#585858"));
                break;
        }
    }

    private void update_fragment(int position, int tag) {
        airFragmentAdapterUpDate.upDate(position, tag);
    }

    public static class ViewHolder {
        TextView air_room_name;//房间名称
        TextView air_room_pm;//当前pm2.5
        TextView air_room_btn1;//极优按钮
        TextView air_room_btn2;//优按钮
        TextView air_room_btn3;//良按钮
        TextView air_room_btn_off;//关闭按钮
        TextView air_room_co2;//显示co2
        TextView air_room_voc;//显示voc
        TextView air_room_jq;//显示甲醛
        TextView air_room_pressure;//显示压差
    }

///***极优***/
//                [parameter setObject:@"8" forKey:pm25SetKey];
//                [parameter setObject:@"20" forKey:vocSetKey];
//                [parameter setObject:@"3" forKey:hchoSetKey];
//                [parameter setObject:@"500" forKey:co2SetKey];
//优
//    [parameter setObject:@"30" forKey:pm25SetKey];
//                [parameter setObject:@"30" forKey:vocSetKey];
//                [parameter setObject:@"5" forKey:hchoSetKey];
//                [parameter setObject:@"700" forKey:co2SetKey];
//良
//    [parameter setObject:@"50" forKey:pm25SetKey];
//                [parameter setObject:@"50" forKey:vocSetKey];
//                [parameter setObject:@"8" forKey:hchoSetKey];
//                [parameter setObject:@"1000" forKey:co2SetKey];
//
//
//    /***关***/
//                [parameter setObject:@"999" forKey:pm25SetKey];
//                [parameter setObject:@"9999" forKey:vocSetKey];
//                [parameter setObject:@"999" forKey:hchoSetKey];
//                [parameter setObject:@"999" forKey:co2SetKey];
//

    public static class AirEventClass {

        public static int checkGear(int pm, int voc, int hcho, int car) {
            if (pm < 0 || voc < 0 || hcho < 0 || car < 0) {
                return 0;
            } else if (pm == 8 && voc == 20 && hcho == 3 && car == 500) {
                return 3;
            } else if (pm == 30 && voc == 30 && hcho == 5 && car == 700) {
                return 2;
            } else if (pm == 50 && voc == 50 && hcho == 8 && car == 1000) {
                return 1;
            } else if (pm == 999 && voc == 9999 && hcho == 999 && car == 999) {
                return 0;
            } else {
                return 0;
            }
        }

        public static int get0hcho() {
            return 3;
        }

        public static int get1hcho() {
            return 5;
        }

        public static int get2hcho() {
            return 8;
        }

        public static int getOffhcho() {
            return 999;
        }

        public static int get0car() {
            return 500;
        }

        public static int get1car() {
            return 700;
        }

        public static int get2car() {
            return 1000;
        }

        public static int getOffcar() {
            return 999;
        }

        public static int get0Pm() {
            return 8;
        }

        public static int get1pm() {
            return 30;
        }

        public static int get2pm() {
            return 50;
        }

        public static int getOffpm() {
            return 999;
        }

        public static int get0voc() {
            return 20;
        }

        public static int get1voc() {
            return 30;
        }

        public static int get2voc() {
            return 50;
        }

        public static int getOffvoc() {
            return 9999;
        }
    }

}
