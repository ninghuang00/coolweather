package com.example.huangning.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by huangning on 2017/3/6.
 * 用于存放县的数据信息
 */

public class County extends DataSupport {

    private int id;
    private  String countyName;
    private String weatherId;//记录县所对应的天气id
    private int cityId;//记录当前县所属市的id

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getCountyName(){
        return countyName;
    }

    public void setCountyName(String countyName){
        this.countyName = countyName;
    }

    public String getWeatherId(){
        return weatherId;
    }

    public void setWeatherId(String weatherId){
        this.weatherId = weatherId;
    }

    public int getCityId(){
        return cityId;
    }

    public void setCityId(int cityId){
        this.cityId = cityId;
    }
}
