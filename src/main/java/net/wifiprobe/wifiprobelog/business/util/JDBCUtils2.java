package net.wifiprobe.wifiprobelog.business.util;//package net.wifiprobe.wifiprobelog.business.util;
//
//import ch.qos.logback.core.db.dialect.DBUtil;
//import com.alibaba.druid.pool.DruidDataSourceFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.annotation.PropertySources;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import javax.sql.DataSource;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Properties;
//
///**
// * Created by win7 on 2017/5/16.
// */
//@Component
//@PropertySource({"classpath:/application.properties"})//鎵弿
//public class JDBCUtils2 {
//    @Autowired
//    Environment env;
//    private static DataSource dataSource = null;
//    protected Connection con = null;
//    protected PreparedStatement pre = null;
//    protected ResultSet res = null;
//    //澹版槑绾跨▼鍏变韩鍙橀噺
//    public static ThreadLocal<Connection> container = new ThreadLocal<Connection>();
//    //閰嶇疆璇存槑锛屽弬鑰冨畼鏂圭綉鍧?
//    //http://blog.163.com/hongwei_benbear/blog/static/1183952912013518405588/
///*	    static{
//	        dataSource.setUrl("jdbc:mysql://192.168.108.145:3306/cctv_dev?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true");
//	        dataSource.setUsername("xuanhua.hu");//鐢ㄦ埛鍚?
//	        dataSource.setPassword("1234qwer");//瀵嗙爜
//	        dataSource.setInitialSize(2);
//	        dataSource.setMaxActive(20);
//	        dataSource.setMinIdle(0);
//	        dataSource.setMaxWait(60000);
//	        dataSource.setValidationQuery("SELECT 1");
//	        dataSource.setTestOnBorrow(false);
//	        dataSource.setTestWhileIdle(true);
//	        dataSource.setPoolPreparedStatements(false);
//	    }*/
//
//    @PostConstruct
//    public void init(){
//        try{
//            Properties properties = new Properties();
//            properties.setProperty("url", env.getProperty("url_p"));
//            properties.setProperty("username", env.getProperty("mysqlusername_p"));
//            properties.setProperty("password", env.getProperty("password_p"));
//            properties.setProperty("initialSize", env.getProperty("initialSize_p"));
//            properties.setProperty("maxActive", env.getProperty("maxActive_p"));
//            properties.setProperty("minIdle", env.getProperty("minIdle_p"));
//            properties.setProperty("maxWait", env.getProperty("maxWait_p"));
//            properties.setProperty("removeAbandoned", env.getProperty("removeAbandoned_p"));
//            properties.setProperty("removeAbandonedTimeout", env.getProperty("removeAbandonedTimeout_p"));
//            properties.setProperty("timeBetweenEvictionRunsMillis", env.getProperty("timeBetweenEvictionRunsMillis_p"));
//            properties.setProperty("minEvictableIdleTimeMillis", env.getProperty("minEvictableIdleTimeMillis_p"));
//            properties.setProperty("validationQuery", env.getProperty("validationQuery_p"));
//            properties.setProperty("testWhileIdle", env.getProperty("testWhileIdle_p"));
//            properties.setProperty("testOnBorrow", env.getProperty("testOnBorrow_p"));
//            properties.setProperty("testOnReturn", env.getProperty("testOnReturn_p"));
//            dataSource = DruidDataSourceFactory.createDataSource(properties);
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }
//    }
//
//    /**
//     * 鑾峰彇鏁版嵁杩炴帴
//     * @return
//     */
//    public static Connection getConnection(){
//        Connection conn =null;
//        try{
//            conn = dataSource.getConnection();
//            System.out.println(Thread.currentThread().getName()+"杩炴帴宸茬粡寮?鍚?......");
//            container.set(conn);
//        }catch(Exception e){
//            System.out.println("杩炴帴鑾峰彇澶辫触");
//            e.printStackTrace();
//        }
//        return conn;
//    }
//    /***鑾峰彇褰撳墠绾跨▼涓婄殑杩炴帴寮?鍚簨鍔?*/
//    public static void startTransaction(){
//        Connection conn=container.get();//棣栧厛鑾峰彇褰撳墠绾跨▼鐨勮繛鎺?
//        if(conn==null){//濡傛灉杩炴帴涓虹┖
//            conn=getConnection();//浠庤繛鎺ユ睜涓幏鍙栬繛鎺?
//            container.set(conn);//灏嗘杩炴帴鏀惧湪褰撳墠绾跨▼涓?
//            System.out.println(Thread.currentThread().getName()+"绌鸿繛鎺ヤ粠dataSource鑾峰彇杩炴帴");
//        }else{
//            System.out.println(Thread.currentThread().getName()+"浠庣紦瀛樹腑鑾峰彇杩炴帴");
//        }
//        try{
//            conn.setAutoCommit(false);//寮?鍚簨鍔?
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//    //鎻愪氦浜嬪姟
//    public static void commit(){
//        try{
//            Connection conn=container.get();//浠庡綋鍓嶇嚎绋嬩笂鑾峰彇杩炴帴if(conn!=null){//濡傛灉杩炴帴涓虹┖锛屽垯涓嶅仛澶勭悊
//            if(null!=conn){
//                conn.commit();//鎻愪氦浜嬪姟
//                System.out.println(Thread.currentThread().getName()+"浜嬪姟宸茬粡鎻愪氦......");
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//
//
//    /***鍥炴粴浜嬪姟*/
//    public static void rollback(){
//        try{
//            Connection conn=container.get();//妫?鏌ュ綋鍓嶇嚎绋嬫槸鍚﹀瓨鍦ㄨ繛鎺?
//            if(conn!=null){
//                conn.rollback();//鍥炴粴浜嬪姟
//                container.remove();//濡傛灉鍥炴粴浜嗭紝灏辩Щ闄よ繖涓繛鎺?
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//    /***鍏抽棴杩炴帴*/
//    public static void closeconnection(){
//        try{
//            Connection conn=container.get();
//            if(conn!=null){
//                conn.close();
//                System.out.println(Thread.currentThread().getName()+"杩炴帴鍏抽棴");
//            }
//        }catch(SQLException e){
//            throw new RuntimeException(e.getMessage(),e);
//        }finally{
//            try {
//                container.remove();//浠庡綋鍓嶇嚎绋嬬Щ闄よ繛鎺ュ垏璁?
//            } catch (Exception e2) {
//                e2.printStackTrace();
//            }
//        }
//    }
//    //绠?鍗曚娇鐢ㄦ柟寮?
//    public static void main(String[] args) throws SQLException {
//        //select鏌ヨ
//	        /*Connection conn = JDBCUtils.getConnection();
//	        PreparedStatement ps = conn.prepareStatement("SELECT 1");
//	        ResultSet rs = ps.executeQuery();
//	        JDBCUtils.close();*/
//
//        //update,insert,delete鎿嶄綔
//        Connection conn2 = JDBCUtils.getConnection();
//        //寮?鍚簨鍔?1
//        JDBCUtils.startTransaction();
//        System.out.println("鎵ц浜嬪姟鎿嶄綔111111111111111....");
//        JDBCUtils.commit();
//        //寮?鍚簨鍔?2
//        JDBCUtils.startTransaction();
//        System.out.println("鎵ц浜嬪姟鎿嶄綔222222222222....");
//        JDBCUtils.commit();
//        JDBCUtils.closeconnection();
//        for (int i = 0; i < 2; i++) {
//            new Thread(new Runnable() {
//
//                public void run() {
//                    Connection conn2 = JDBCUtils.getConnection();
//                    for (int i = 0; i < 2; i++) {
//                        JDBCUtils.startTransaction();
//                        System.out.println(conn2);
//                        System.out.println(Thread.currentThread().getName()+"鎵ц浜嬪姟鎿嶄綔銆傘?傘?傘?傘?傘?傘?傘?傘?傘?傘?傘?傘??");
//                        JDBCUtils.commit();
//                    }
//                    JDBCUtils.closeconnection();
//                }
//            }).start();
//        }
//
//    }
//}