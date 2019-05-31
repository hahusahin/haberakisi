package com.haberinadresi.androidapp.utilities;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

// new since Glide v4
@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    /*
    // TELEFONDAKİ KAYITLI RESİMLER 100 MB'I GEÇMESİN DİYE EKLENEBİLİR
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        int diskCacheSizeBytes = 1024 * 1024 * 100; // 100 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }
    */
}
