package net.wifiprobe.wifiprobelog.business.service;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

public interface IWifiProbeService {
    /**
     * 查询线路信息,将数据存放进内存中
     */
    public void basedDataStorage();

    public void basedCCTVDataStorage();

    /**
     * 将24个文件打包,删除源文件
     */
    public void compressedPackageProcesseData() throws Exception;

    /**
     * 打包源文件,删除源文件
     */
    public void compressedPackageOriginalData();

    /**
     * 友盟需要的格式,暂时无用
     *
     * @param response
     * @throws IOException
     */
    public void theirAllies(HttpServletResponse response) throws IOException;

    /**
     * 处理数据,将数据处理成一个百度需要的文件格式
     *
     * @throws IOException
     */
    public void processingLog1();


    /**
     * 统计手机mac
     * 格式:城市+线路+mac+mac(非重复)+busmac+busmac(非重复)+时间段+创建时间
     */
    public void wifiprobeStatistics();

    /**
     * 定时消费macqueue队列中的数据,进入reids中
     */
    public void consumptionMacQueue();

}
