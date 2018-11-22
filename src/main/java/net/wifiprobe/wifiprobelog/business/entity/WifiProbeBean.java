package net.wifiprobe.wifiprobelog.business.entity;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WifiProbeBean {
    private String id;
    private String cityname;
    private String linename;
    private int repeat_iphonemac;
    private int iphonemac;
    private int bus_repeat_iphonemac;
    private int bus_iphonemac;
    private String period_time;
    private String createtime;
    private String update;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }

    public String getLinename() {
        return linename;
    }

    public void setLinename(String linename) {
        this.linename = linename;
    }

    public int getRepeat_iphonemac() {
        return repeat_iphonemac;
    }

    public void setRepeat_iphonemac(int repeat_iphonemac) {
        this.repeat_iphonemac = repeat_iphonemac;
    }

    public int getIphonemac() {
        return iphonemac;
    }

    public void setIphonemac(int iphonemac) {
        this.iphonemac = iphonemac;
    }

    public int getBus_repeat_iphonemac() {
        return bus_repeat_iphonemac;
    }

    public void setBus_repeat_iphonemac(int bus_repeat_iphonemac) {
        this.bus_repeat_iphonemac = bus_repeat_iphonemac;
    }

    public int getBus_iphonemac() {
        return bus_iphonemac;
    }

    public void setBus_iphonemac(int bus_iphonemac) {
        this.bus_iphonemac = bus_iphonemac;
    }

    public String getPeriod_time() {
        return period_time;
    }

    public void setPeriod_time(String period_time) {
        this.period_time = period_time;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }
}
