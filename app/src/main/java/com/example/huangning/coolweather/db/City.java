package com.example.huangning.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by huangning on 2017/3/6.
 * 用于保存城市的数据信息
 */

public class City extends DataSupport {

    private int id;
    private  String cityName;
    private  int cityCode;//城市代号
    private  int provinceId;//记录当前城市所属省的id值

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getCityName(){
        return cityName;
    }

    public void setCityName(String cityName){
        this.cityName = cityName;
    }

    public int getCityCode(){
        return cityCode;
    }

    public void setCityCode(int cityCode){
        this.cityCode = cityCode;
    }

    public int getProvinceId(){
        return provinceId;
    }

    public void setProvinceId(int provinceId){
        this.provinceId = provinceId;
    }
}
