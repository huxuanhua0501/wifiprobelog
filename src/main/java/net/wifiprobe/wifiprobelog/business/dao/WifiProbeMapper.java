package net.wifiprobe.wifiprobelog.business.dao;


import net.wifiprobe.wifiprobelog.business.entity.WifiLineCompanyCityBean;
import net.wifiprobe.wifiprobelog.business.entity.WifiProbeBean;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WifiProbeMapper {

    @Select("SELECT IFNULL(tls.line_name, '') AS linename, IFNULL(tbl.company_name, '') AS company, IFNULL('remark', 'remark') AS remark, IFNULL(ad.`name`, '') AS city, IFNULL(tbl.appkey, '') appkey FROM t_bus_line tbl, t_line_station tls, ad_city ad WHERE tbl.station_id = tls.id AND ad.`code` = tls.city_code AND tls.line_type = '0' AND tbl.`status` = '0'AND tbl.line_status='0' AND tbl.appkey IS NOT NULL GROUP BY tbl.appkey")
    public List<WifiLineCompanyCityBean> selGo();


    @Select("SELECT count(1) FROM wifiprobe_statistics WHERE id=#{id}")
    public int getwifiprobe(@Param("id") String id);


    @Insert("INSERT INTO wifiprobe_statistics(id,cityname,linename,repeat_iphonemac,iphonemac,bus_repeat_iphonemac,bus_iphonemac,period_time,createtime)VALUES(#{bean.id},#{bean.cityname},#{bean.linename},#{bean.repeat_iphonemac},#{bean.iphonemac},#{bean.bus_repeat_iphonemac},#{bean.bus_iphonemac},#{bean.period_time},now())")
    public void insertwifiprobeStatistics(@Param("bean") WifiProbeBean bean);

    @Update("update wifiprobe_statistics SET repeat_iphonemac=repeat_iphonemac+#{bean.repeat_iphonemac},iphonemac=iphonemac+#{bean.iphonemac},bus_repeat_iphonemac=bus_repeat_iphonemac+#{bean.bus_repeat_iphonemac},bus_iphonemac=bus_iphonemac+#{bean.bus_iphonemac},`update`=now() WHERE id=#{bean.id}")
    public void updatewifiprobeStatistics(@Param("bean") WifiProbeBean bean);



//    @Insert("INSERT INTO wifiprobe_statistics(id,cityname,linename,bus_repeat_iphonemac,bus_iphonemac,period_time,createtime)VALUES(#{bean.id},#{bean.cityname},#{bean.linename},#{bean.bus_repeat_iphonemac},#{bean.bus_iphonemac},#{bean.period_time},now())")
//    public void insertBuswifiprobeStatistics(@Param("bean") WifiProbeBean bean);
//
//    @Update("update wifiprobe_statistics SET bus_repeat_iphonemac=bus_repeat_iphonemac+#{bean.bus_repeat_iphonemac},bus_iphonemac=bus_iphonemac+#{bean.bus_iphonemac},bus_repeat_iphonemac=bus_repeat_iphonemac+#{bean.bus_iphonemac},`update`=now()")
//    public void updateBuswifiprobeStatistics(@Param("bean") WifiProbeBean bean);



}
