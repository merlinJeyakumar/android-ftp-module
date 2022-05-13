package com.nativedevps.ftp.glide;

import android.content.Context;
import android.util.Pair;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.nativedevps.ftp.model.CredentialModel;
import com.nativedevps.ftp.model.FtpFileModel;

import java.io.InputStream;

public class FTPModelLoaderFactory implements ModelLoaderFactory<FtpFileModel, InputStream> {
    public FTPModelLoaderFactory(Context context) {
    }

    @Override
    public ModelLoader<FtpFileModel, InputStream> build(MultiModelLoaderFactory multiFactory) {
        return new FTPModelLoader();
    }

    @Override
    public void teardown() {
        // Do nothing.
    }
}