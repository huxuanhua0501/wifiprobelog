package net.wifiprobe.wifiprobelog.business.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import net.wifiprobe.wifiprobelog.business.cctvdao.CCTVWifiProbeMapper;
import net.wifiprobe.wifiprobelog.business.dao.WifiProbeMapper;
import net.wifiprobe.wifiprobelog.business.entity.CCTVLineBean;
import net.wifiprobe.wifiprobelog.business.entity.WifiLineCompanyCityBean;
import net.wifiprobe.wifiprobelog.business.entity.WifiProbeBean;
import net.wifiprobe.wifiprobelog.business.service.IWifiProbeService;
import net.wifiprobe.wifiprobelog.business.util.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
//@Scope("prototype")
public class WifiProbeService implements IWifiProbeService {
    Logger logger = LoggerFactory.getLogger(WifiProbeService.class);
    @Autowired
    Environment env;
    @Autowired
    WifiProbeBean bean;

    @Autowired
    private CCTVWifiProbeMapper cctvWifiProbeMapper;
    @Autowired
    private WifiProbeMapper wifiProbeMapper;
    private static Map<String, Object> basedDataStorageMap = new ConcurrentHashMap<>();
//    private static Map<String, Object> basedCCTVDataStorageMap = new ConcurrentHashMap<String, Object>();
    //    private static Queue<String> macQueue = new ConcurrentLinkedDeque<>();
    final static String SEPARATOR = "\t";//分隔符


    /**
     * 查询线路信息,将数据存放进内存中
     */
    @Override
    public void basedDataStorage() {
        List<WifiLineCompanyCityBean> basedDataStorageList = wifiProbeMapper.selGo();
        if (basedDataStorageList.size() > 0) {
            for (WifiLineCompanyCityBean bean : basedDataStorageList) {
                basedDataStorageMap.put(bean.getAppkey(), bean);
            }
        }
    }

    /**
     * 查询CCTV
     */
    @Override
    public void basedCCTVDataStorage() {
//        List<CCTVLineBean> basedDataStorageList = cctvWifiProbeMapper.selCCTV();
//        if (basedDataStorageList.size() > 0) {
//            for (CCTVLineBean bean : basedDataStorageList) {
//                basedCCTVDataStorageMap.put(bean.getApp_key(), bean);
//            }
//        }
    }

    /**
     * 存放文件地址
     */
    private File getStoreFileAdress() {
        String newpath = env.getProperty("wifiprobenew.path");
        // System.err.println(path);
        File storefile = null;
        if (hour().equals("23")) {//临界点时间凌晨一点
            storefile = new File(newpath + File.separator + yesterday());
        } else {//正常点的时间
            storefile = new File(newpath + File.separator + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }

        if (!storefile.exists()) {
            storefile.mkdirs();
        }
        return storefile;
    }


    /**
     * 获取时间差
     *
     * @param firsttime
     * @param secondtime
     * @return
     * @throws ParseException
     */
    private long accessTime(String firsttime, String secondtime) throws ParseException {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date d1 = df.parse(secondtime);
        Date d2 = df.parse(firsttime);
        long diff = d1.getTime() - d2.getTime();
        long minutes = diff / (1000 * 60);
        return minutes;

    }

    /**
     * 转化成北京时间
     *
     * @param time
     * @return
     */
    private String intoBeijingTime(String time) throws ParseException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss-yyyy-MM-dd");
        cal.setTime(sf.parse(time));//开始时间
        cal.add(Calendar.HOUR, +8);//格林时间转换成北京时间
        String bjtime = sf.format(cal.getTime());
        return bjtime;
    }

    /**
     * 读取每个文本的数据
     *
     * @return
     */
    private Map<String, List<Map<String, Object>>> readFile(BufferedReader bufr) throws IOException {
//        Map<String,  List<Map<String,Object>>storemap = new HashMap<>();
        Map<String, List<Map<String, Object>>> storemap = null;
        storemap = new HashMap<>();
        JSONObject json = null;
        String line = null;
        while ((line = bufr.readLine()) != null) {
            Map<String, String> map = new HashMap<>();
            if (line.contains("wifiInfo") && line.contains("Mac")) {
                String strs = null;
                try {
                    json = JSONObject.parseObject(line);
                    strs = json.get("wifiInfo").toString();
                } catch (Exception e) {
                    line = line.substring(0, line.lastIndexOf("},")) + "}]}";
                    try {
                        json = JSONObject.parseObject(line);
                    } catch (Exception e1) {
                    }
                    strs = json.get("wifiInfo").toString();
                }
                List<HashMap> storelist = JSON.parseArray(strs, HashMap.class);
                List<Map<String, Object>> list = null;
                for (int i = 0; i < storelist.size(); i++) {
                    if (!storelist.get(i).get("Mac").toString().contains("**") && storelist.get(i).get("DType").toString().equals("0")) {//符合存储的数据

                        String mac = storelist.get(i).get("Mac").toString();
                        if (storemap.containsKey(mac) && !mac.contains("daa1-19")) {//存储相同mac的数据
                            storemap.get(mac).add(storelist.get(i));
//                               macQueue.add(mac);//存储采集到的mac
                        } else if (!mac.contains("daa1-19")) {
                            list = new ArrayList<>();
                            list.add(storelist.get(i));
                            storemap.put(mac, list);
//                              macQueue.add(mac);//存储采集到的mac
                        }
                    } else if (storelist.get(i).get("Mac").toString().contains("**")) {
                        return null;
                    }
                }
            }
        }
        return storemap;
    }


    /**
     * 合并相同mac的list,统一放进要写入文本的list中
     *
     * @param storelist
     * @param bean
     * @param writtenfilelist
     * @throws ParseException
     */
    private void mergeData(List<HashMap> storelist, WifiLineCompanyCityBean bean, List<String> writtenfilelist) throws ParseException {
        String linedata = null;
        String firsttime = null;
        String secondtime = null;
        if (storelist.size() > 1) {
            Map<String, Object> map = storelist.get(0);
            for (int i = 1; i < storelist.size(); i++) {
                firsttime = map.get("Logintime").toString();//获取已有的时间
                secondtime = storelist.get(i).get("Logintime").toString();//获取遍历的时间
                String startime = intoBeijingTime(firsttime);
                String entime = intoBeijingTime(secondtime);
                long minutes = accessTime(startime, entime);
                if (minutes <= 30) {
                    if (i < storelist.size() - 1) {
                        continue;
                    } else {
                        secondtime = storelist.get(i).get("Logintime").toString();//获取遍历的时间
                        entime = intoBeijingTime(secondtime);
                        linedata = bean.getLinename() + SEPARATOR + bean.getCompany().replaceAll(" ", "") + SEPARATOR + bean.getRemark() + SEPARATOR + bean.getCity() + SEPARATOR + storelist.get(0).get("Mac") + SEPARATOR + startime + SEPARATOR + entime + SEPARATOR + storelist.get(0).get("DManu") + SEPARATOR + storelist.get(0).get("DModelName") + SEPARATOR + storelist.get(0).get("DModelNum") + SEPARATOR + storelist.get(0).get("DeviceName");
                        writtenfilelist.add(linedata);
                    }
                } else if (i == 1) {
                    secondtime = storelist.get(i).get("Logintime").toString();//获取遍历的时间
                    entime = intoBeijingTime(secondtime);
                    linedata = bean.getLinename() + SEPARATOR + bean.getCompany().replaceAll(" ", "") + SEPARATOR + bean.getRemark() + SEPARATOR + bean.getCity() + SEPARATOR + storelist.get(0).get("Mac") + SEPARATOR + startime + SEPARATOR + entime + SEPARATOR + storelist.get(0).get("DManu") + SEPARATOR + storelist.get(0).get("DModelName") + SEPARATOR + storelist.get(0).get("DModelNum") + SEPARATOR + storelist.get(0).get("DeviceName");
                    writtenfilelist.add(linedata);
                    map = storelist.get(i);
                } else if (i < storelist.size() - 1) {
                    secondtime = storelist.get(i - 1).get("Logintime").toString();//获取遍历的时间
                    entime = intoBeijingTime(secondtime);
                    linedata = bean.getLinename() + SEPARATOR + bean.getCompany().replaceAll(" ", "") + SEPARATOR + bean.getRemark() + SEPARATOR + bean.getCity() + SEPARATOR + storelist.get(0).get("Mac") + SEPARATOR + startime + SEPARATOR + entime + SEPARATOR + storelist.get(0).get("DManu") + SEPARATOR + storelist.get(0).get("DModelName") + SEPARATOR + storelist.get(0).get("DModelNum") + SEPARATOR + storelist.get(0).get("DeviceName");
                    writtenfilelist.add(linedata);
                    map = storelist.get(i);
                } else {
                    secondtime = storelist.get(i).get("Logintime").toString();//获取遍历的时间
                    entime = intoBeijingTime(secondtime);
                    linedata = bean.getLinename() + SEPARATOR + bean.getCompany().replaceAll(" ", "") + SEPARATOR + bean.getRemark() + SEPARATOR + bean.getCity() + SEPARATOR + storelist.get(0).get("Mac") + SEPARATOR + startime + SEPARATOR + entime + SEPARATOR + storelist.get(0).get("DManu") + SEPARATOR + storelist.get(0).get("DModelName") + SEPARATOR + storelist.get(0).get("DModelNum") + SEPARATOR + storelist.get(0).get("DeviceName");
                    writtenfilelist.add(linedata);
                }
            }
        } else {
            firsttime = storelist.get(0).get("Logintime").toString();//获取已有的时间
            String startime = intoBeijingTime(firsttime);
            linedata = bean.getLinename() + SEPARATOR + bean.getCompany().replaceAll(" ", "") + SEPARATOR + bean.getRemark() + SEPARATOR + bean.getCity() + SEPARATOR + storelist.get(0).get("Mac") + SEPARATOR + startime + SEPARATOR + startime + SEPARATOR + storelist.get(0).get("DManu") + SEPARATOR + storelist.get(0).get("DModelName") + SEPARATOR + storelist.get(0).get("DModelNum") + SEPARATOR + storelist.get(0).get("DeviceName");
            writtenfilelist.add(linedata);

        }
    }

    /**
     * 整理要写入文本的数据
     *
     * @param file
     * @param storemap
     * @return
     */
    private List<String> IntegrationData(File file, Map<String, List<Map<String, Object>>> storemap) throws ParseException {
        WifiLineCompanyCityBean bean = (WifiLineCompanyCityBean) basedDataStorageMap.get(file.getName().substring(0, file.getName().indexOf("_WiFiProbe")));
        String linedata = null;
        List<String> writtenfilelist = new ArrayList<String>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : storemap.entrySet()) {
            List<Map<String, Object>> list = entry.getValue();
            List<HashMap> storelist = JSON.parseArray(JSON.toJSONString(list), HashMap.class);
            mergeData(storelist, bean, writtenfilelist);//合并相同mac
        }

        return writtenfilelist;
    }

    /**
     * 写文件
     *
     * @param startingPosition
     * @param endPosition
     * @param sourcefiles      final Set<CCTVLineBean>  allWithoutAsterisk = new HashSet<CCTVLineBean>();//全部不带星号的车辆
     *                         final Set<CCTVLineBean>  allMatchOnAppkey = new HashSet<CCTVLineBean>();//全部匹配上appkey的车辆
     *                         final Set<CCTVLineBean> notAllNatchingVehiclesAppkey = new HashSet<CCTVLineBean>();//全部匹配不上车辆的appkey
     *                         final Set<CCTVLineBean> allTheBusesWithAsterisk=new HashSet<CCTVLineBean>();//全部带星号的车辆
     *                         final Set<CCTVLineBean> everyDayWithoutAsteriskVehicles = new HashSet<CCTVLineBean>();//每天不带星号的车辆
     */
    private void writeFile(int startingPosition, int endPosition, File[] sourcefiles, File writefile,
                           Map<String, Object> basedDataStorageMap) {
//            , Set<CCTVLineBean> allWithoutAsterisk,
//                           Set<CCTVLineBean> allMatchOnAppkey, Set<String> notAllNatchingVehiclesAppkey,
//                           Set<CCTVLineBean> allTheBusesWithAsterisk, Set<CCTVLineBean> everyDayWithoutAsteriskVehicles) {
        for (int i = startingPosition; i < endPosition; i++) {
            File sourceFile = sourcefiles[i];
//            File writefile = null;
            FileReader fr = null;
            BufferedReader bufr = null;
            BufferedWriter bufw = null;
//            writefile = new File(getStoreFileAdress() + File.separator + "WifiProbe" + dateAndHour() + ".log");
            try {
                fr = new FileReader(sourceFile);
                bufr = new BufferedReader(fr);
                bufw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writefile, true))); //不覆盖原有数据处理
                WifiLineCompanyCityBean bean = (WifiLineCompanyCityBean) basedDataStorageMap.get(sourceFile.getName().substring(0, sourceFile.getName().indexOf("_WiFiProbe")));//匹配文件是那辆车
//                CCTVLineBean cctvLineBean = (CCTVLineBean) basedCCTVDataStorageMap.get(sourceFile.getName().substring(0, sourceFile.getName().indexOf("_WiFiProbe")));//匹配文件是那辆车
                if (!PubMethod.isEmpty(bean)) {//匹配到车辆
                    Map<String, List<Map<String, Object>>> storemap = readFile(bufr);//读取文件
//                    allMatchOnAppkey.add(cctvLineBean);
                    if (storemap != null && storemap.size() > 0) {
//                        allWithoutAsterisk.add(cctvLineBean);
//                        everyDayWithoutAsteriskVehicles.add(cctvLineBean);
                        List<String> IntegrationDataList = IntegrationData(sourceFile, storemap);//整合数据
                        for (String str : IntegrationDataList) {//写文本
                            bufw.write(str);
                            bufw.newLine();
                            bufw.flush();
                        }
                    }
//                    else {
//                        allTheBusesWithAsterisk.add(cctvLineBean);
//                    }
                }
//                else {//匹配不上的车辆
//                    notAllNatchingVehiclesAppkey.add(sourceFile.getName().substring(0, sourceFile.getName().indexOf("_WiFiProbe")));
//                }
            } catch (Exception e) {//异常情况下关闭流
                try {
                    bufr.close();
                    bufw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } finally {//关闭流
                try {
                    bufr.close();
                    bufw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    /**
     * 处理数据
     *
     * @throws IOException
     */

    public void processingLog1() {
        final File[] sourcefiles = getSourceFileAdress().listFiles();//获取源文件
//        final File[] sourcefiles = new File("E:\\2017-10-11\\08").listFiles();//获取源文件
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        final File writefile = new File(getStoreFileAdress() + File.separator + "WifiProbe" + dateAndHour() + ".log");
        int threadsum = Runtime.getRuntime().availableProcessors() * 2;
//        final Set<CCTVLineBean> allWithoutAsterisk =Collections.synchronizedSet(new HashSet<CCTVLineBean>());//全部不带星号的车辆
//        final Set<CCTVLineBean> allMatchOnAppkey = Collections.synchronizedSet(new HashSet<CCTVLineBean>());//全部匹配上appkey的车辆
//        final Set<String> notAllNatchingVehiclesAppkey =Collections.synchronizedSet( new HashSet<String>());//全部匹配不上车辆的appkey
//        final Set<CCTVLineBean> allTheBusesWithAsterisk = Collections.synchronizedSet(new HashSet<CCTVLineBean>());//全部带星号的车辆
//        final Set<CCTVLineBean> everyDayWithoutAsteriskVehicles = new HashSet<CCTVLineBean>();//每天不带星号的车辆
        for (int i = 0; i < threadsum; i++) {//分页创建线程跑文件
            final int startingPosition = i * sourcefiles.length / threadsum;
            final int endPosition = (i + 1) * sourcefiles.length / threadsum;
            cachedThreadPool.execute(new Runnable() {
                public void run() {
//                    System.err.println(startingPosition+"==="+endPosition);
                    writeFile(startingPosition, endPosition, sourcefiles, writefile, basedDataStorageMap);
//                            , allWithoutAsterisk, allMatchOnAppkey, notAllNatchingVehiclesAppkey, allTheBusesWithAsterisk, everyDayWithoutAsteriskVehicles);
                }
            });
        }
        cachedThreadPool.shutdown();
        //判断是否所有的线程已经运行完
        while (!cachedThreadPool.isTerminated()) {
        }
        System.out.println("开始扔车辆mac时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//        ShardedJedis shardedJedis =new  ShardedJedisUtils().getShardedJedis();
//        try {
//            ShardedJedisPipeline jedisPipeline = shardedJedis.pipelined();
//            Collection<Jedis> collection = shardedJedis.getAllShards();
//            Iterator<Jedis> jedis = collection.iterator();
//            while (jedis.hasNext()) {
//                jedis.next().select(5);
//            }
//        if (allWithoutAsterisk != null && allWithoutAsterisk.size() > 0) {
//            for (CCTVLineBean cctvLineBean : allWithoutAsterisk) {
//                jedisPipeline.sadd("全部不带星号的车辆", JSON.toJSONString(cctvLineBean));//用于查看有效的车辆数,于本业务无关.
////                stringRedisTemplate.opsForSet().add("全部不带星号的车辆", JSON.toJSONString(cctvLineBean));//用于查看有效的车辆数,于本业务无关.
//            }
//        }
//        if (allMatchOnAppkey != null && allMatchOnAppkey.size() > 0) {
//            for (CCTVLineBean cctvLineBean : allMatchOnAppkey) {
//                jedisPipeline.sadd("全部匹配上appkey的车辆", JSON.toJSONString(cctvLineBean));//全部匹配上的车辆
//            }
//        }
//        if (notAllNatchingVehiclesAppkey != null && notAllNatchingVehiclesAppkey.size() > 0) {
//            for (String appkey : notAllNatchingVehiclesAppkey) {
//                jedisPipeline.sadd("全部匹配不上车辆的appkey", appkey);//全部匹配不上车辆的appkey
//            }
//        }
//        if (allTheBusesWithAsterisk != null && allTheBusesWithAsterisk.size() > 0) {
//            for (CCTVLineBean cctvLineBean : allTheBusesWithAsterisk) {
//                jedisPipeline.sadd("全部带星号的车辆", JSON.toJSONString(cctvLineBean));//用于查看有效的车辆数,于本业务无关
////                stringRedisTemplate.opsForSet().add("全部带星号的车辆", JSON.toJSONString(cctvLineBean));//用于查看有效的车辆数,于本业务无关
//            }
//        }
//        if (everyDayWithoutAsteriskVehicles != null && everyDayWithoutAsteriskVehicles.size() > 0) {
//            for (CCTVLineBean cctvLineBean : everyDayWithoutAsteriskVehicles) {
//                jedisPipeline.sadd("每天不带星号的车辆" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), JSON.toJSONString(cctvLineBean));//用于查看有效的车辆数,于本业务无关
////                stringRedisTemplate.opsForSet().add("每天不带星号的车辆" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), JSON.toJSONString(cctvLineBean));//用于查看有效的车辆数,于本业务无关                }
//            }
//
//        }
//            jedisPipeline.sync();
//    } catch (Exception e) {
//        shardedJedis.close();
//    } finally {
//        shardedJedis.close();
//    }
        System.out.println("结束扔车辆mac时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

    }

    /**
     * 将24个文件打包,删除源文件
     */
    @Override
    public void compressedPackageProcesseData() throws Exception {
        BufferedWriter bufw = null;
        String newpath = env.getProperty("wifiprobenew.path");
//        try {

        ZipCompressorByAnt zca = new ZipCompressorByAnt(newpath + File.separator + yesterday() + ".tar.gz");//打包生成压缩包名称
        boolean isok = zca.compressExe(newpath + File.separator + yesterday());//要打包的文件
        if (true) {
            final File writefile = new File(newpath + File.separator + yesterday() + ".done");
            bufw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writefile, true))); //不覆盖原有数据处理
            String md5sum = zca.getFileMD5String(new File(newpath + File.separator + yesterday() + ".tar.gz"));
            bufw.write(md5sum);
            bufw.flush();
            delProcesseData();//删除没有打包的数据
            System.err.println("结束");

        }
//        } catch (IOException e) {
//            bufw.close();
//        } finally {
//            bufw.close();
//        }


    }



    /**
     * 源文件地址
     */
    private File getSourceFileAdress() {
        String oldpath = env.getProperty("wifiprobeold.path");
        File sourcefile = null;
        File file5 = null;
        if (hour().equals("23")) {//临界点时间凌晨一点
            sourcefile = new File(oldpath + File.separator + yesterday() + File.separator + hour());
        } else {//正常点的时间
            sourcefile = new File(oldpath + File.separator + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + File.separator + hour());
        }
        if (!sourcefile.exists()) {
            sourcefile.mkdirs();
        }
        return sourcefile;
    }

    /**
     * 统计手机mac
     * 格式:城市+线路+mac+mac(非重复)+busmac+busmac(非重复)+时间段+创建时间
     */
    @Override
    public void wifiprobeStatistics() {
        Map<String, List<String>> baiduwifimap = new HashMap<>();//存放给百度的数据量
        Map<String, List<String>> buswifimap = new HashMap<>();//存放巴士自己用的数据
        List<WifiProbeBean> wifiProbeBeans = new ArrayList<>();
        File file = getwifiFile();//读取文件
        try {
            readwifimacfile(file, baiduwifimap, buswifimap);//处理文件
            getWifiProbeBean(baiduwifimap, buswifimap, wifiProbeBeans);//组装数据
            insertWifiDb(wifiProbeBeans);//插入的mac相关数据
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("统计mac结束");
    }

    /**
     * 定时消费macqueue队列中的数据,进入reids中
     */
    @Override
    public void consumptionMacQueue() {
//        System.out.println("开始时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//        ShardedJedis shardedJedis =new  ShardedJedisUtils().getShardedJedis();
//        try {
//            ShardedJedisPipeline jedisPipeline = shardedJedis.pipelined();
//            Collection<Jedis> collection = shardedJedis.getAllShards();
//            Iterator<Jedis> jedis = collection.iterator();
//            while (jedis.hasNext()) {
//                jedis.next().select(6);
//            }
//            if (!macQueue.isEmpty()) {
//                int size = macQueue.size();
//                for (int i = 0; i <size; i++) {
//                    jedisPipeline.sadd("iphoneMac", macQueue.poll());
//                }
//                jedisPipeline.sync();
//            }
//            System.out.println("结束时间:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//        } catch (Exception e) {
//            shardedJedis.close();
//        } finally {
//            shardedJedis.close();
//        }


    }

    private void getWifiProbeBean(Map<String, List<String>> baiduwifimap, Map<String, List<String>> buswifimap, List<WifiProbeBean> wifiProbeBeans) {
        String id = null;
        String key = null;
        List<String> baidulistvalue = new ArrayList<>();
        List<String> buslistvalue = new ArrayList<>();
        Set<String> baiduset = new HashSet<String>();
        Set<String> busset = new HashSet<String>();
        String[] keys = null;
        try {
            for (Map.Entry<String, List<String>> entry : baiduwifimap.entrySet()) {
                key = entry.getKey();
                baidulistvalue = entry.getValue();
                keys = key.split(SEPARATOR);
                id = Bit32(key);
                buslistvalue = buswifimap.get(key);
                bean = new WifiProbeBean();
                bean.setId(id);
                bean.setCityname(keys[0]);
                bean.setLinename(keys[1]);
                bean.setPeriod_time(keys[2]);
                bean.setRepeat_iphonemac(baidulistvalue.size());
                baiduset = new HashSet<>();
                baiduset.addAll(baidulistvalue);
                bean.setIphonemac(baiduset.size());

                if (buslistvalue != null && buslistvalue.size() > 0) {
                    bean.setBus_repeat_iphonemac(buslistvalue.size());
                    busset = new HashSet<>();
                    busset.addAll(buslistvalue);
                    bean.setBus_iphonemac(busset.size());
                }
                wifiProbeBeans.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 插入的数据
     *
     * @param listbean
     */
    private void insertWifiDb(List<WifiProbeBean> listbean) {
        try {
            for (int i = 0; i < listbean.size(); i++) {//将数据写入文档
                int isinsertorupdate = wifiProbeMapper.getwifiprobe(listbean.get(i).getId());//是否存在
                if (isinsertorupdate > 0) {
                    wifiProbeMapper.updatewifiprobeStatistics(listbean.get(i));//更新
                } else {
                    wifiProbeMapper.insertwifiprobeStatistics(listbean.get(i));//插入
                }

            }
        } catch (Exception e) {

        }
    }

    /**
     * @param SourceString
     * @return
     * @throws Exception
     */

    private String Bit32(String SourceString) throws Exception {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(SourceString.getBytes());
        byte messageDigest[] = digest.digest();
        return toHexString(messageDigest);
    }

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 统计手机mac\\\读取源文件
     */
    private File getwifiFile() {
        String newpath = env.getProperty("wifiprobenew.path");
        File sourcefile = null;
        File file5 = null;
        if (hour().equals("23")) {//临界点时间凌晨一点
            sourcefile = new File(newpath + File.separator + yesterday() + File.separator + "WifiProbe" + yesterday() + " " + hour() + ".log");
        } else {//正常点的时间
            sourcefile = new File(newpath + File.separator + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + File.separator + "WifiProbe" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " " + hour() + ".log");
        }
        return sourcefile;
    }

    private void readwifimacfile(File file, Map<String, List<String>> baiduwifimap, Map<String, List<String>> buswifimap) {
        FileInputStream inputStream = null;
        try {
            Scanner sc = null;
            inputStream = new FileInputStream(file);

            sc = new Scanner(inputStream, "UTF-8");
            String line = null;
            String[] macs = null;
            String key = null;
            List<String> baidumaclist = null;
            List<String> busmaclist = null;
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                macs = line.split(SEPARATOR);
                key = macs[3] + SEPARATOR + macs[0] + SEPARATOR + updateFormat(macs[5]);
                if (baiduwifimap.containsKey(key)) {
                    baiduwifimap.get(key).add(macs[4]);
                } else {
                    baidumaclist = new ArrayList<String>();
                    baidumaclist.add(macs[4]);
                    baiduwifimap.put(key, baidumaclist);
                }
                if (!macs[5].equals(macs[6])) {
                    if (buswifimap.containsKey(key)) {
                        buswifimap.get(key).add(macs[4]);
                    } else {
                        busmaclist = new ArrayList<String>();
                        busmaclist.add(macs[4]);
                        buswifimap.put(key, busmaclist);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            try {
                inputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String updateFormat(String time) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss-yyyy-MM-dd");
        try {
            cal.setTime(sf.parse(time));//开始时间
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat retrunsf = new SimpleDateFormat("yyyy-MM-dd HH");
        String datetime = retrunsf.format(cal.getTime());
        return datetime;
    }

    /**
     * 打包源文件,删除源文件
     */
    @Override
    public void compressedPackageOriginalData() {
        String oldpath = env.getProperty("wifiprobeold.path");
        String address = oldpath + File.separator + yesterday();
        GZIPbyAnt gzip = new GZIPbyAnt();
        boolean bol = gzip.tarGz(address);
        System.out.println(bol);
        File delFiletar = new File(address + ".tar");
        File delFiles = new File(address);
        boolean sucess = delFile(delFiles);
        sucess = delFile(delFiletar);
        if (sucess) {
            System.err.println("删除成功");
        } else System.err.println("删除失败");


    }

    /**
     * 删除24个打包完的文件
     */
    private void delProcesseData() {
        String newpath = env.getProperty("wifiprobenew.path");
        String address = newpath + File.separator + yesterday();
        System.err.println(address);
        File delFiles = new File(address);
        boolean sucess = delFile(delFiles);
        if (sucess) {
            System.err.println("删除成功");
        } else System.err.println("删除失败");


    }

    /**
     * 递归删除文件
     *
     * @param delFiles
     * @return
     */
    private boolean delFile(File delFiles) {
        if (delFiles.isDirectory()) {
            String[] children = delFiles.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = delFile(new File(delFiles, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return delFiles.delete();//删除空目录
    }


    private String yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        return yesterday;
    }

    private String dateAndHour() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd HH").format(cal.getTime());
        return yesterday;
    }

    private String hour() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        String hour = new SimpleDateFormat("HH").format(cal.getTime());
        return hour;
    }


    /**
     * 友盟的excel导出
     */
    public void theirAllies(HttpServletResponse response) throws IOException {
//        File[] files = file2.listFiles();
        File[] sourceFiles = new File("E:\\wif探针重要\\724\\cc\\data").listFiles();
        List<List<Object>> poiList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (File sourceFile : sourceFiles) {//遍历接收回来的原始文件
            WifiLineCompanyCityBean bean = (WifiLineCompanyCityBean) basedDataStorageMap.get(sourceFile.getName().substring(0, sourceFile.getName().indexOf("_WiFiProbe")));
            if (!PubMethod.isEmpty(bean)) {
                FileReader fr = null;
                BufferedReader bufr = null;
                fr = new FileReader(sourceFile);
                bufr = new BufferedReader(fr);
                String line = null;
                JSONObject json = null;
                while ((line = bufr.readLine()) != null) {
                    if (line.contains("wifiInfo") && line.contains("Mac")) {
                        String jsonstrs = null;
                        try {
                            json = JSONObject.parseObject(line);
                            jsonstrs = json.get("wifiInfo").toString();
                        } catch (Exception e) {
                            line = line.substring(0, line.lastIndexOf("},")) + "}]}";
                            try {
                                json = JSONObject.parseObject(line);
                            } catch (Exception e1) {
                                System.err.println(sourceFile.getName());
                            }
                        }
                        jsonstrs = json.get("wifiInfo").toString();
                        List<HashMap> listMac = JSON.parseArray(jsonstrs, HashMap.class);
                        String gps = json.get("GPS").toString();
                        JSONObject obj = JSON.parseObject(gps);

                        for (int i = 0; i < listMac.size(); i++) {
                            if (listMac.get(i).get("DType").equals("0") && !listMac.get(i).get("Mac").toString().contains("**")) {
                                if (!map.containsKey(listMac.get(i).get("Mac"))) {
                                    map.put(listMac.get(i).get("Mac").toString(), null);
                                    List<Object> lineList = new ArrayList<Object>();
                                    List<Object> gpsList = new ArrayList<Object>();
                                    gpsList.add("998测试线路v2.4");
                                    gpsList.add(bean.getCompany());
                                    gpsList.add(bean.getCity());
                                    gpsList.add(obj.get("lon") + "," + obj.get("lat"));
                                    poiList.add(gpsList);
                                    lineList.add(listMac.get(i).get("Logintime"));
                                    lineList.add(listMac.get(i).get("Mac"));
                                    lineList.add(listMac.get(i).get("RSSI"));
                                    lineList.add(listMac.get(i).get("DType"));
                                    lineList.add(listMac.get(i).get("DManu"));
                                    lineList.add(listMac.get(i).get("DModelName"));
                                    lineList.add(listMac.get(i).get("DModelNum"));
                                    lineList.add(listMac.get(i).get("DeviceName"));
                                    poiList.add(lineList);
                                }
                            }
                        }

                    }

                }
            }
        }
        exportutil2(poiList, response, "theirAllies");
    }


    /**
     * 大数据量用(百万级绝对够用)
     *
     * @param list
     * @param response
     * @param name
     * @throws IOException
     */

    private void exportutil2(List<List<Object>> list, HttpServletResponse response, String name) {
        SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
        Sheet sh = wb.createSheet();
        //String[] cellTitle={"1a","2w","3e","4r","5t","6t","7y","8u"};
        int rowNumber = 0;
        Row row = null;
        Cell cell = null;
        OutputStream out = null;
        try {
            out = response.getOutputStream();
//            for(int i = 0 ;i<cellTitle.length;i++){
//                cell = row.createCell(i);
//                String address = cellTitle[i];
//                cell.setCellValue(address);
//            }
            for (int rownum = 0; rownum < list.size(); rownum++) {
                row = sh.createRow(rowNumber++);
                List<Object> listresult = list.get(rownum);
                for (int cellnum = 0; cellnum < listresult.size(); cellnum++) {
                    cell = row.createCell(cellnum);
                    String address = (String) listresult.get(cellnum);
                    cell.setCellValue(address);
                }
            }


            // FileOutputStream out = new FileOutputStream("sxssf.xlsx");
            String fileName = name + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");// 定义输出类型
            wb.write(out);
            //  out.close();
            // wb.dispose();
        } catch (IOException e) {
            try {
                out.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            wb.dispose();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            wb.dispose();
        }
    }

    public static void main(String[] args) throws ParseException {
//        for (int a = 10; a >= 0; a--) {
//            System.err.println(a);
//        }
//        System.err.println(hour());
//        System.err.println(yesterday());
//        System.err.println(dateAndHour());
//        System.err.println(new SimpleDateFormat("HH").format(new Date()));
//        new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//        String str = "wwww:L";
//        System.err.println(str.replaceAll(":", ""));
//        String str = "{\"Mac\":\"62db-16**-89be\",\"Logintime\":\"12:05:12-2017-08-23\",\"RSSI\":\"-93\",\"DType\":\"0\",\"DManu\":\"NA\",\"DModelName\":\"NA\",\"DModelNum\":\"NA\",\"DeviceName\":\"NA\"}, {\"Mac\":\"dcfe-18**-dbcd\",\"Logintime\n";
//        System.err.println(str.substring(0, str.lastIndexOf("},")) + "}]}");

//        Map<String, List<String>> storemap = new HashMap<String, List<String>>();
//        List<String> storelist = new ArrayList<String>();
//        storelist.add("小妹");
//        storemap.put("go", storelist);
//        if (storemap.containsKey("go")) {
//            storemap.get("go").add("大妹");
//        }
//        for (Map.Entry<String, List<String>> entry : storemap.entrySet()) {//将数据写入文档
//            System.err.println(entry.getValue());
//        }

//        String str = "M419路,西部二分公司,remark,深圳,a086-c608-6cd9,16:24:50-2017-09-04,16:24:50-2017-09-04,NA,NA,NA,NA";
//        System.err.println(str.replaceAll("", " "));
//        final int k = 5007;
//        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
//
//        for (int i = 0; i < 10; i++) {
//            final  int a = (i) * k / 10;
//            final  int b = (i + 1) * k / 10;
////            System.err.println("a=" + a);
////            System.err.println("b=" + b);
//            cachedThreadPool.execute(new Runnable() {
//                public void run() {
//                    System.err.println("a=" + a);
//                    System.err.println("b=" + b);
//                }
//            });
//        }
//        cachedThreadPool.shutdown();
//        System.err.println(Runtime.getRuntime().availableProcessors());
//        List<Integer> list = new ArrayList<>();
//        list.add(1);
//        list.add(1);
//        list.add(1);
//        list.add(1);
//        System.err.println(list.size());
//        Set<Integer> set = new HashSet<>();
//        set.addAll(list);
//        System.err.println(set.size());

//        for (int i = 0; i < 5; i++) {
//            if (i == 2) {
//                continue;
//            }
//            System.err.println(i);
//        }
//
//
        Queue<String> queue = new ConcurrentLinkedDeque<>();
        queue.add("xxx");
        for (int i = 0; i < queue.size(); i++) {
            System.out.println(queue.poll());
        }
        for (int i = 0; i < queue.size(); i++) {
            System.out.println(queue.poll());
        }

    }
}
