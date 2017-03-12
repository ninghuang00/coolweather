package com.example.huangning.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huangning.coolweather.db.City;
import com.example.huangning.coolweather.db.County;
import com.example.huangning.coolweather.db.Province;
import com.example.huangning.coolweather.util.HttpUtil;
import com.example.huangning.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**遍历省市县的数据
 * Created by huangning on 2017/3/9.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    //主要控件
    private ProgressDialog progressDialog;
    private TextView titleText; //显示标题内容
    private Button backButton;  //后退按钮
    private ListView listView;  //省市县的数据在这里显示，会自动给每个子项之间添加分隔线，也可以用RecyclerView
    //保存数据的对象
    private ArrayAdapter<String> adapter;   //联系数据和视图控件
    private List<String> dataList = new ArrayList<>();//要显示的数据列表
    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<County> countyList;
    //选中的省份
    private Province selectedProvince;
    //选中的城市
    private City selectedCity;
    //选中的县
    private County selectedCounty;
    //当前选中级别
    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        //数组适配器，参数一：当前上下文；参数二：布局；参数三：显示在布局中的数据。
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }
                else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }
                else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();   //先查询省的数据
    }

    /**
     * 查询全国所有省，优先从数据库查询，不行再去服务器
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);    //省上面没有级别了，先隐藏后退按钮
        provinceList = DataSupport.findAll(Province.class); //参数传入泛型，从数据库查询你省的数据
        if(provinceList.size() > 0){    //如果有数据，则处理
            dataList.clear();   //清空列表？
            for(Province province : provinceList){  //将省的名字加入列表
                dataList.add(province.getProvinceName());
            }
            //Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
            adapter.notifyDataSetChanged(); //通知dataList数据发生改变
            //Sets the currently selected item. If in touch mode, the item will not be selected but it will still be positioned appropriately.
            // If the specified selection position is less than 0, then the item at position 0 will be selected.
            listView.setSelection(0);   //把第一项显示在最顶端
            currentLevel = LEVEL_PROVINCE;  //现在处于省级
        }
        else{   //数据库没有则从服务器查询
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中的省内所有市的数据，优先从数据库查询，不行再去服务器
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //将provinceid = 所选省的数据库id的列取出来
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }

    }

    /**
     * 查询选中的市的所有县的数据，优先从数据库查询，不行再去服务器
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
        else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");

        }
    }

    /**从服务器查询省市县的数据
     * @param address 网址
     * @param type 省市县类型
     */
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {    //下面实现接口对象callback
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        closeProgressDialog();      //关闭进度显示
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();  //显示提示
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {   //Call是接口，Response是一个类，response是从哪里传入的？
                String responseText = response.body().string(); //获取服务器响应的body的字段
                //判断省市县，调用省市县的handle方法处理字段，并返回结果true和false
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }
                            else if("city".equals(type)){
                                queryCities();
                            }
                            else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }



}
