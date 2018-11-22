package net.wifiprobe.wifiprobelog.business.entity;

public class WifiLineCompanyCityBean {
    private String linename;
    private String company;
    private String remark;
    private String city;
    private String appkey;

    public String getLinename() {
        return linename;
    }

    public void setLinename(String linename) {
        this.linename = linename;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    @Override
    public String toString() {
        return "WifiLineCompanyCityBean{" +
                "linename='" + linename + '\'' +
                ", company='" + company + '\'' +
                ", remark='" + remark + '\'' +
                ", city='" + city + '\'' +
                ", appkey='" + appkey + '\'' +
                '}';
    }
}
