package net.wifiprobe.wifiprobelog.business.cctvdao;


import net.wifiprobe.wifiprobelog.business.entity.CCTVLineBean;
import net.wifiprobe.wifiprobelog.business.entity.WifiLineCompanyCityBean;
import net.wifiprobe.wifiprobelog.business.entity.WifiProbeBean;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CCTVWifiProbeMapper {

    @Select("SELECT t.bus_no, t.app_key, b.`name` AS linename, a.`name` AS cityname, bc.`name` AS buscompany FROM `terminal` t, bus_line b, area a, bus_company bc WHERE b.id = t.line_id AND b.area_id = a.id AND b.company_id = bc.id AND t.app_key IS NOT NULL")
    public List<CCTVLineBean> selCCTV();




}
