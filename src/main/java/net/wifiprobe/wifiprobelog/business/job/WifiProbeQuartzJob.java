package net.wifiprobe.wifiprobelog.business.job;

import net.wifiprobe.wifiprobelog.business.service.IWifiProbeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling
public class WifiProbeQuartzJob {
    Logger logger = LoggerFactory.getLogger(WifiProbeQuartzJob.class);
    @Autowired
    private IWifiProbeService wifiProbeService;

    @Scheduled(cron = "${ProcessingLog}") // 每小时执行一次,存储基础数据,将探针数据处理完,生成一小时的文件
    public void ProcessingLog() throws Exception {
        System.out.println("执行调度任务：" + new Date());
        wifiProbeService.basedCCTVDataStorage();
        wifiProbeService.basedDataStorage();
        wifiProbeService.processingLog1();
    }

    @Scheduled(cron = "${compressedPackageProcesseData}") // 打包程序,将处理完的数据每天凌晨打包,删除处理完的源文件
    public void compressedPackageProcesseData() throws Exception {
        System.out.println("执行调度任务2：" + new Date());
        wifiProbeService.compressedPackageProcesseData();
    }

    @Scheduled(cron = "${compressedPackageOriginalData}") // 打包原始数据,删除没有打包的数据
    public void compressedPackageOriginalData() throws Exception {
        System.out.println("执行调度任务2：" + new Date());
        wifiProbeService.compressedPackageOriginalData();
    }



    /**
     * 统计手机mac
     * 格式:城市+线路+mac+mac(非重复)+busmac+busmac(非重复)+时间段+创建时间
     */
    @Scheduled(cron = "${wifiprobeStatistics}")
    public void wifiprobeStatistics() {
        wifiProbeService.wifiprobeStatistics();
    }

    /**
     * 定时消费macqueue队列中的数据,进入reids中
     */
  //  @Scheduled(cron = "${consumptionMacQueue}")
    public void consumptionMacQueue() {
        wifiProbeService.consumptionMacQueue();
    }
}
