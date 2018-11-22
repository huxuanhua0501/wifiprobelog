package net.wifiprobe.wifiprobelog.business.util;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * 打tar.gz包
 */
public class GZIPbyAnt {

//    public static void main(String[] args) {
//        tarGz(String path);
//    }

    /**
     * 测试压缩归档tar.gz文件
     */
    public  static   boolean tarGz(String path) {
        File tarFile = tar(path);//生成的tar文件
        File gzFile = new File(tarFile + ".gz");//将要生成的压缩文件

        GZIPOutputStream out = null;

        InputStream in = null;

        boolean boo = false;//是否成功

        try {

            in = new FileInputStream(tarFile);

            out = new GZIPOutputStream(new FileOutputStream(gzFile), 1024 * 2);

            byte b[] = new byte[1024 * 2];

            int length = 0;

            while ((length = in.read(b)) != -1) {

                out.write(b, 0, length);
            }

            boo= true;
        } catch (Exception ex) {

            throw new RuntimeException("压缩归档文件失败", ex);
        } finally {

            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException ex) {
                throw new RuntimeException("关闭流出现异常", ex);
            } finally {

                if (!boo) {//清理操作

                    tarFile.delete();

                    if (gzFile.exists())
                        gzFile.delete();

                }

            }

        }
        return  boo;
    }

    /**
     * 测试归档tar文件
     */
    public static File tar(String path) {

        File srcFile = new File(path);//要归档的文件对象

        File targetTarFile = new File(path+".tar");//归档后的文件名

        TarOutputStream out = null;

        boolean boo = false;//是否压缩成功

        try {
            out = new TarOutputStream(new BufferedOutputStream(new FileOutputStream(targetTarFile)));

            tar(srcFile, out, "", true);

            boo = true;

            //归档成功

            return targetTarFile;

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {

            try {
                if (out != null)
                    out.close();
            } catch (IOException ex) {
                throw new RuntimeException("关闭Tar输出流出现异常", ex);
            } finally {
                //清理操作
                if (!boo && targetTarFile.exists())//归档不成功,
                    targetTarFile.delete();

            }

        }
    }

    /**
     * 归档tar文件
     *
     * @param file 归档的文件对象
     * @param out  输出tar流
     * @param dir  相对父目录名称
     * @param boo  是否把空目录归档进去
     */
    private static void tar(File file, TarOutputStream out, String dir, boolean boo) throws IOException {

        if (file.isDirectory()) {//是目录

            File[] listFile = file.listFiles();//得出目录下所有的文件对象

            if (listFile.length == 0 && boo) {//空目录归档

                out.putNextEntry(new TarEntry(dir + file.getName() + "/"));//将实体放入输出Tar流中

                System.out.println("归档." + dir + file.getName() + "/");

                return;
            } else {

                for (File cfile : listFile) {

                    tar(cfile, out, dir + file.getName() + "/", boo);//递归归档
                }
            }

        } else if (file.isFile()) {//是文件

            System.out.println("归档." + dir + file.getName() + "/");

            byte[] bt = new byte[2048 * 2];

            TarEntry ze = new TarEntry(dir + file.getName());//构建tar实体
            //设置压缩前的文件大小
            ze.setSize(file.length());

            //ze.setName(file.getName());//设置实体名称.使用默认名称

            out.putNextEntry(ze);////将实体放入输出Tar流中

            FileInputStream fis = null;

            try {

                fis = new FileInputStream(file);

                int i = 0;

                while ((i = fis.read(bt)) != -1) {//循环读出并写入输出Tar流中

                    out.write(bt, 0, i);
                }

            } catch (IOException ex) {
                throw new IOException("写入归档文件出现异常", ex);
            } finally {

                try {
                    if (fis != null)
                        fis.close();//关闭输入流
                    out.closeEntry();
                } catch (IOException ex) {

                    throw new IOException("关闭输入流出现异常");
                }

            }
        }

    }
}
