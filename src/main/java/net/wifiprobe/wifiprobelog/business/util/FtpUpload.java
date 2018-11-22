package net.wifiprobe.wifiprobelog.business.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;

public class FtpUpload {
    private FTPClient ftp;
    /**
     *
     * @param path 上传到ftp服务器哪个路径下
     * @param addr 地址
     * @param port 端口号
     * @param username 用户名
     * @param password 密码
     * @return
     * @throws Exception
     */
    public boolean connect(String path, String addr, int port, String username, String password) throws Exception {
        boolean result = false;
        ftp = new FTPClient();
        int reply;
        ftp.connect(addr,port);
        ftp.login(username,password);
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            return result;
        }
        ftp.changeWorkingDirectory(path);
        result = true;
        return result;
    }
    /**
     *
     * @param file 上传的文件或文件夹
     * @throws Exception
     */
    public void upload(File file) throws Exception{
        if(file.isDirectory()){
            ftp.enterLocalPassiveMode();
            ftp.makeDirectory(file.getName());
            ftp.changeWorkingDirectory(file.getName());
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                File file1 = new File(file.getPath()+"\\"+files[i] );
                if(file1.isDirectory()){
                    upload(file1);
                    ftp.changeToParentDirectory();
                }else{
                    File file2 = new File(file.getPath()+"\\"+files[i]);
                    FileInputStream input = new FileInputStream(file2);
                    ftp.enterLocalPassiveMode();
                    System.out.println(ftp.storeFile(file2.getName(), input));
                    input.close();
                }
            }
        }else{
            File file2 = new File(file.getPath());
            FileInputStream input = new FileInputStream(file2);
            ftp.enterLocalPassiveMode();
            System.out.println(ftp.storeFile(file2.getName(), input));
            input.close();
        }
    }
    public static void main(String[] args) {
        FtpUpload t = new FtpUpload();
        try {
            t.connect("", "117.78.41.232", 21, "neibu", "*4vUngN$");
            File file = new File("E:\\ZenTaoPMS.8.2.5.zip");
            t.upload(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
