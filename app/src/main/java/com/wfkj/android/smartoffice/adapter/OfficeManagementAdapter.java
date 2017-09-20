package com.wfkj.android.smartoffice.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wfkj.android.smartoffice.R;
import com.wfkj.android.smartoffice.model.sever_model.House;
import com.wfkj.android.smartoffice.my_interface.ManagementInterface;
import com.wfkj.android.smartoffice.util.Constants;
import com.wfkj.android.smartoffice.util.view_util.CustomListView;

import java.util.List;

/**
 * Created by wangdongyang on 17/2/7.
 */
public class OfficeManagementAdapter extends BaseAdapter {


    private Context context;
    private List<House> houses;
    private ManagementInterface managementInterface;
    private RoomsAdapter adapter;

    public OfficeManagementAdapter(Context context, List<House> houses, ManagementInterface managementInterface) {
        this.context = context;
        this.houses = houses;
        this.managementInterface = managementInterface;
    }

    @Override
    public int getCount() {
        return houses.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_office_management, null);

            holder.tag_txt_1 = (TextView) convertView.findViewById(R.id.tag_txt_1);
            holder.tag_txt_2 = (TextView) convertView.findViewById(R.id.tag_txt_2);
            holder.house_name_txt = (TextView) convertView.findViewById(R.id.house_name_txt);
            holder.modify_img = (ImageView) convertView.findViewById(R.id.modify_img);
            holder.address_txt = (TextView) convertView.findViewById(R.id.address_txt);
            holder.rooms_listview = (CustomListView) convertView.findViewById(R.id.rooms_listview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final House house = houses.get(position);

        holder.house_name_txt.setText(house.getName());
        holder.address_txt.setText(house.getAddress());

        /*如果数据*/
        if (house.getGateways().get(0).getExtid().equals(Constants.house.getGateways().get(0).getExtid())) {
            holder.tag_txt_1.setText("当前");
            holder.tag_txt_2.setText("显示");
            holder.tag_txt_1.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.tag_txt_2.setTextColor(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.tag_txt_1.setText("设为");
            holder.tag_txt_2.setText("当前");
            holder.tag_txt_1.setTextColor(Color.BLACK);
            holder.tag_txt_2.setTextColor(Color.BLACK);
        }


        holder.tag_txt_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    /*设置 显示数据模型*/
                if (listener != null) {
                    listener.houseSwitched(house.getGateways().get(0).getExtid());
                }
            }
        });
        holder.tag_txt_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    /*设置 显示数据模型*/
                if (listener != null) {
                    listener.houseSwitched(house.getGateways().get(0).getExtid());
                }
            }
        });

        holder.modify_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managementInterface.getClick(1, house.getId() + "", "");
            }
        });

        adapter = new RoomsAdapter(context, house.getRooms(), new ManagementInterface() {
            @Override
            public void getClick(int tag, String id, String name) {
                managementInterface.getClick(tag, id, name);
            }
        });
        holder.rooms_listview.setAdapter(adapter);

        return convertView;
    }

    class ViewHolder {
        TextView tag_txt_1;
        TextView tag_txt_2;
        TextView house_name_txt;
        ImageView modify_img;
        TextView address_txt;
        CustomListView rooms_listview;
    }

    public void setListener(HouseSwitchClickListener listener) {
        this.listener = listener;
    }

    private HouseSwitchClickListener listener;

    public interface HouseSwitchClickListener {
        void houseSwitched(String productKey);
    }

}
