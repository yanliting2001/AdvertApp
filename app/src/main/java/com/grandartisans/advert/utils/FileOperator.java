package com.grandartisans.advert.utils;
import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/** 
 *  
 * @ClassName:  FileOperator    
 * @Description:  文件操作类，删除文件或文件目录 
 * @author: SAU_LC66 
 * @date:   2014-09-25 19:01   
 */ 
public class FileOperator {
    private static final String SEPARATOR = File.separator;//路径分隔符
	/** 
     * 复制文件目录 
     * @param srcDir 要复制的源目录 eg:/mnt/sdcard/DB 
     * @param destDir 复制到的目标目录 eg:/mnt/sdcard/db/ 
     * @return  
     */  
    public static boolean copyDir(String srcDir, String destDir){  
        File sourceDir = new File(srcDir);  
        //判断文件目录是否存在  
        if(!sourceDir.exists()){  
            return false;  
        }  
        //判断是否是目录  
        if (sourceDir.isDirectory()) {  
            File[] fileList = sourceDir.listFiles();  
            File targetDir = new File(destDir);  
            //创建目标目录  
            if(!targetDir.exists()){  
                targetDir.mkdirs();  
            }  
            //遍历要复制该目录下的全部文件  
            for(int i= 0;i<fileList.length;i++){  
                if(fileList[i].isDirectory()){//如果如果是子目录进行递归  
                    copyDir(fileList[i].getPath()+ "/",   
                            destDir + fileList[i].getName() + "/");  
                }else{//如果是文件则进行文件拷贝  
                    copyFile(fileList[i].getPath(), destDir +fileList[i].getName());  
                }  
            }  
            return true;  
        }else {  
            copyFileToDir(srcDir,destDir);  
            return true;  
        }  
    }  
      
      
    /** 
     * 复制文件（非目录） 
     * @param srcFile 要复制的源文件   
     * @param destFile 复制到的目标文件  
     * @return 
     */  
    private static boolean copyFile(String srcFile, String destFile){  
        try{  
            InputStream streamFrom = new FileInputStream(srcFile);  
            OutputStream streamTo = new FileOutputStream(destFile);  
            byte buffer[]=new byte[1024];  
            int len;  
            while ((len= streamFrom.read(buffer)) > 0){  
                streamTo.write(buffer, 0, len);  
            }  
            streamFrom.close();  
            streamTo.close();  
            return true;  
        } catch(Exception ex){  
            return false;  
        }  
    }  
      
      
    /** 
     * 把文件拷贝到某一目录下 
     * @param srcFile 
     * @param destDir 
     * @return 
     */  
    public static boolean copyFileToDir(String srcFile, String destDir){  
        File fileDir = new File(destDir);  
        if (!fileDir.exists()) {  
            fileDir.mkdir();  
        }  
        String destFile = destDir +"/" + new File(srcFile).getName();  
        try{  
            InputStream streamFrom = new FileInputStream(srcFile);  
            OutputStream streamTo = new FileOutputStream(destFile);  
            byte buffer[]=new byte[1024];  
            int len;  
            while ((len= streamFrom.read(buffer)) > 0){  
                streamTo.write(buffer, 0, len);  
            }  
            streamFrom.close();  
            streamTo.close();  
            return true;  
        } catch(Exception ex){  
            return false;  
        }  
    }  
  
      
    /** 
     * 移动文件目录到某一路径下 
     * @param srcFile 
     * @param destDir 
     * @return 
     */  
    public static boolean moveFileToDir(String srcFile, String destDir) {  
        //复制后删除原目录  
        if (copyDir(srcFile, destDir)) {  
            deleteFile(new File(srcFile));  
            return true;  
        }  
        return false;  
    } 
    
    public static boolean moveFile(String srcFile,String destFile) {
    	if(copyFile(srcFile, destFile)) {
    		deleteFile(new File(srcFile));  
            return true;
    	}
    	return false;
    }
  
    /** 
     * 删除文件（包括目录） 
     * @param delFile 
     */  
    public static void deleteFile(File delFile) {  
        //如果是目录递归删除  
      if (delFile.isDirectory()) {  
       File[] files = delFile.listFiles();  
       for (File file : files) {  
         deleteFile(file);  
       }  
      }else{  
          delFile.delete();  
      }  
      //如果不执行下面这句，目录下所有文件都删除了，但是还剩下子目录空文件夹  
      delFile.delete();  
    }

    /**
     *
     * @描述:读取文件并将结果转为String格式
     * @方法名: convertStreamToString
     * @param path
     * @return
     * @throws UnsupportedEncodingException
     * @返回类型 StringBuilder
     * @创建人 gao
     * @创建时间 2014年6月23日下午12:24:20
     * @修改人 gao
     * @修改时间 2014年6月23日下午12:24:20
     * @修改备注
     * @since
     * @throws
     */
    public static StringBuilder convertStreamToString(String path) throws UnsupportedEncodingException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        BufferedInputStream in = new BufferedInputStream(inputStream);
        in.mark(4);
        byte[] first3bytes = new byte[3];
        try {
            in.read(first3bytes);
            in.reset();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(in, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String line = null;
        StringBuilder sb = new StringBuilder();

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    public static boolean fileIsExists(String path){
        try{
            File f=new File(path);
            Log.i("USB","fileIsExists path = " + path + "isexists = " + f.exists());
            if(!f.exists()){
                return false;
            }

        }catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    /**
     * 字符串转换成为16进制(无需Unicode编码)
     * @param str
     * @return
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }
    /**
     * 16进制直接转换成为字符串(无需Unicode解码)
     * @param hexStr
     * @return
     */
    public static String hexStr2Str(String hexStr) {

        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    public static void saveStringToFile(String fileName, String value) {
        File file = new File(fileName);
        FileOutputStream fos = null;
        FileDescriptor fd = null;
        try {
            fos = new FileOutputStream(file, false);
            fd = fos.getFD();
            fos.write(value.getBytes());
            fos.flush();
            fd.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
    /**
     * 复制res/raw中的文件到指定目录
     * @param context 上下文
     * @param assetName 资源名
     * @param fileName 文件名
     * @param storagePath 目标文件夹的路径
     */
    public static void copyFileFromAsserts(Context context, String assetName, String fileName, String storagePath){
        InputStream inputStream= null;
        try {
            inputStream = context.getAssets().open(assetName);
            File file = new File(storagePath);
            if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
                file.mkdirs();
            }
            readInputStream(storagePath + SEPARATOR + fileName, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
