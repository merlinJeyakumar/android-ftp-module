package com.nativedevps.ftp.glide;

import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.nativedevps.ftp.model.FtpUrlModel;
import com.nativedevps.ftp.model.FtpFileModel;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;


public class FTPDataFetcher implements DataFetcher<InputStream> {
    private final FtpUrlModel model;
    private final FtpFileModel ftpFileModel;
    private InputStream stream;
    FTPClient ftpClient;

    FTPDataFetcher(FtpFileModel ftpFileModel) {
        this.model = ftpFileModel.getCredentialModel();
        this.ftpFileModel = ftpFileModel;
        ftpClient = new FTPClient();
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        ftpClient.setConnectTimeout(15 * 1000); // 15s
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
        try {
            int reply; // 服务器响应值
            if (model.getPort() == null) {
                ftpClient.connect(model.getAddress());
            } else {
                ftpClient.connect(model.getAddress(), Integer.parseInt(model.getPort()));
            }
            // 获取响应值
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                // 断开连接
                ftpClient.disconnect();
                throw new Exception("Can't connect to " + model.getAddress()
                        + ":" + model.getPort()
                        + ". The server response is: " + ftpClient.getReplyString());
            }

            ftpClient.login(model.getUserName(), model.getPassword());
            // 获取响应值
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                // 断开连接
                ftpClient.disconnect();
                throw new Exception("Error while performing login on " + model.getAddress()
                        + ":" + model.getPort()
                        + " with username: " + model.getUserName()
                        + ". Check your credentials and try again.");

            } else {
                // 获取登录信息
                FTPClientConfig config = new FTPClientConfig(ftpClient.getSystemType().split(" ")[0]);
                config.setServerLanguageCode("zh");
                ftpClient.configure(config);
                // 使用被动模式设为默认
                ftpClient.enterLocalPassiveMode();
                // 二进制文件支持
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            }
            // 先判断服务器文件是否存在
            FTPFile[] files = ftpClient.listFiles(ftpFileModel.getFtpAddress());
            if (files.length == 0 || !files[0].isFile()) {
                Log.d("", "文件不存在");
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            stream = ftpClient.retrieveFileStream(ftpFileModel.getFtpAddress());
            callback.onDataReady(stream);

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (Exception exc) {
                }
            }
            ftpClient = null;
        }
    }

    @Override
    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
//    @Override
//    public String getId() {
//        return model.ftpPath;
//    }

    @Override
    public void cancel() {
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

}
