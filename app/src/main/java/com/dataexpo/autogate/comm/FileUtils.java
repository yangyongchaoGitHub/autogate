package com.dataexpo.autogate.comm;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * description :文件工具类
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    /**
     * 读取txt文件的内容
     *
     * @param filePath 想要读取的文件对象
     * @return 返回文件内容
     */
    public static String txt2String(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                result.append(System.lineSeparator() + s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    //使用RandomAccessFile进行写文件
    public static boolean writeByteFile(byte[] bytes, String filePath) {
        try {
            Log.i(TAG, "writeByteFile 1 " + (new Date()).getTime());// 50ms
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            Log.i(TAG, "writeByteFile 2 " + (new Date()).getTime());
            raf.write(bytes);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //File file = new File(filePath);
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        boolean flag = false;
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = new FileOutputStream(file);
//            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
//            bos.write(bytes);
//            //fileOutputStream.write(bytes);
//            bos.flush();
//            bos.close();
//            fileOutputStream.close();
//            flag = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return flag;
        return true;
    }

    /**
     * 写入TXT文件
     */
    public static boolean writeTxtFile(String content, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Log.i("FileUtils ", "!file.exists");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //
            // return false;
        }

        boolean flag = false;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes("utf-8"));
            fileOutputStream.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Checks if is sd card available.检查SD卡是否可用
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Gets the SD root file.获取SD卡根目录
     */
    public static File getSDRootFile() {
        if (isSdCardAvailable()) {
            return Environment.getExternalStorageDirectory();
        } else {
            return null;
        }
    }

    public static int getImgCount() {
        File file = getRegistedDirectory();
        if (file != null && file.exists()) {
            File[] files = file.listFiles();
            if (files == null) {
                return 0;
            }

            return files.length;
        }
        return 0;
    }

    public static void clearImg() {
        File file = getRegistedDirectory();

        if (file != null) {
            deleteFiles(file);
        }
    }

    public static void deleteFiles(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFiles(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        path.delete();
    }

    /**
     *
     * @param code 图片名称
     * @return 返回用户图片的uri
     */
    public static String getUserPic(String code) {
        try {
            String res = "";
            if (code.endsWith(".jpg")) {
                res = getRegistedDirectory() + "/" + code;
            } else {
                res = getRegistedDirectory() + "/" + code + ".jpg";
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取服务器人像数据文件的目录信息
     */
    public static File getRegistedDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "UserImage-autogate");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取背景图
     */
    public static File getBgDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "autogate-bg");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取批量导入压缩图片文件的目录信息
     */
    public static File getBatchImportDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "dataexpo");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取批量导入压缩图片文件的解压文件夹目录信息
     */
    public static File getBatchImportDatas() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "dataexpo/ag-import");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    public static String getFacePicPath(String name) {
        return getFaceRecordDirectory() + "/" + name + ".jpg";
    }

    public static File getFaceRecordDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "autogate-facerecord");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 获取导入图片成功的目录信息
     */
    public static File getBatchImportSuccessDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "Success-Import-autogate");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 判断文件是否存在
     */
    public static File isFileExist(String fileDirectory, String fileName) {
        File file = new File(fileDirectory + "/" + fileName);
        try {
            if (!file.exists()) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return file;
    }

    /**
     * 判断文件是否存在
     */
    public static File isFileExist(String filePath) {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return file;
    }

    /**
     * 删除文件
     */
    public static void deleteFile(String filePath) {
        try {
            // 找到文件所在的路径并删除该文件
            File file = new File(filePath);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 获取不带扩展名的文件名
     * */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 保存图片
     */
    public static boolean saveBitmap(File file, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        boolean result = false;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            // 判断目录是否存在
            File newfile = new File(newPath);
            File newFileDir = new File(newfile.getPath().replace(newfile.getName(), ""));
            if (!newFileDir.exists()) {
                newFileDir.mkdirs();
            }
            if (oldfile.exists()) { // 文件存在时
                inStream = new FileInputStream(oldPath); // 读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static byte[] base64ToBytes(String base) {
        // like /9j/4A is image
        byte[] decodes = null;
        if (base != null) {
            try {
                decodes = Base64.decode(base.getBytes(), Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return decodes;
    }


    public static String base64ToFile(String base) {
        if (base == null) {
            Log.i("FileUtils ", "b2f is null");
            return null;
        }


        String decodeWord = null;
        byte[] decodes;
        try {
             decodes = Base64.decode(base.getBytes(), Base64.NO_WRAP);
             String log = "";
             for (int i = 0; i < 10; i++) {
                 log += decodes[i] + " ";
             }

             Log.i(TAG, "----- " + log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodeWord;
    }

    public static String toBase64(File file) {
        long size = file.length();
        //byte[] imageByte = new byte[100];
        byte[] imageByte = new byte[(int) size];
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.read(imageByte);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.i(TAG, "head: " + imageByte[0] + " " + imageByte[1] + " " + imageByte[2] + " " + imageByte[3]);

        return Base64.encodeToString(imageByte, Base64.NO_WRAP);
    }

    public static String toBase64(String src) {
        Log.i(TAG, "toBase64 : " + src.getBytes()[0] + " " + src.getBytes()[1]);

        return Base64.encodeToString(src.getBytes(), Base64.NO_WRAP);
    }

    public static boolean unZipFolder(String zipFileString, String outPathString) {
        boolean flag = false;

        try {
            Charset gbk = Charset.forName("GBK");
            ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString), gbk);
            String szName = "";

            while(true) {
                ZipEntry zipEntry;
                while((zipEntry = inZip.getNextEntry()) != null) {
                    szName = zipEntry.getName();
                    File file;
                    if (zipEntry.isDirectory()) {
                        szName = szName.substring(0, szName.length() - 1);
                        file = new File(outPathString + File.separator + szName);
                        file.mkdirs();
                    } else {
                        Log.i(TAG, outPathString + File.separator + szName);
                        file = new File(outPathString + File.separator + szName);
                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                        }

                        FileOutputStream out = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];

                        int len;
                        while((len = inZip.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                            out.flush();
                        }

                        out.close();
                    }
                }

                inZip.close();
                flag = true;
                break;
            }
        } catch (Exception e) {
            Log.e(TAG, "e = " + e.getMessage());
            e.printStackTrace();
        }

        return flag;
    }
}
