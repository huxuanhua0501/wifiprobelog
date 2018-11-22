package net.wifiprobe.wifiprobelog.business.util;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import sun.security.provider.MD5;

import java.io.File;
import java.io.IOException;

/**
 * 打zip包
 */
public class ZipCompressorByAnt extends MD5Util {
    private File zipFile;

    /**
     * 压缩文件构造函数
     * 最终压缩生成的压缩文件：目录+压缩文件名.zip
     */
    public ZipCompressorByAnt(String finalFile) {
        zipFile = new File(finalFile);
    }

    /**
     * 执行压缩操作
     * @param srcPathName 需要被压缩的文件/文件夹
     */
    public boolean compressExe(String srcPathName) {
        File srcdir = new File(srcPathName);
        try {
        if (!srcdir.exists()){
            throw new RuntimeException(srcPathName + "不存在！");
        }
        Project prj = new Project();
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(zipFile);
        FileSet fileSet = new FileSet();
        fileSet.setProject(prj);
        fileSet.setDir(srcdir);
        //fileSet.setIncludes("**/*.java"); //包括哪些文件或文件夹 eg:zip.setIncludes("*.java");
        //fileSet.setExcludes(...); //排除哪些文件或文件夹
        zip.addFileset(fileSet);
        zip.execute();
        }catch (Exception e){

        }finally {
        }
        return true;
    }
    public static void main(String[] args) throws IOException {
        ZipCompressorByAnt zca = new ZipCompressorByAnt("E:\\2017-09-08\\18.tar.gz");
//        zca.compress("E:\\test");
        zca.compressExe("E:\\2017-09-08\\18");
//        System.err.println(getFileMD5String(new File("D:\\wifi\\97newlog\\2017-09-07\\2017-09-06.tar.gz")));
    }
}
