package com.nativedevps.ftp.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.nativedevps.ftp.model.FtpFileModel;

import java.io.InputStream;

public class FTPModelLoader implements ModelLoader<FtpFileModel, InputStream> {

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull FtpFileModel ftpFileModel, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(ftpFileModel), new FTPDataFetcher(ftpFileModel));
    }

    @Override
    public boolean handles(@NonNull FtpFileModel credentialModelFtpFileModelPair) {
        return true;
    }
}
