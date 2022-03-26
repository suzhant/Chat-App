package com.sushant.whatsapp.Services;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.sushant.whatsapp.R;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class MyDownloadService extends DownloadService {
    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final String DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_id";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    DownloadManager downloadManager;
    private static Cache downloadCache;
    static DatabaseProvider databaseProvider;
    static File downloadDirectory;

    public MyDownloadService() {
        super(
                FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                R.string.exo_download_notification_channel_name,
                /* channelDescriptionResourceId= */ 0);
    }

    protected MyDownloadService(int foregroundNotificationId) {
        super(foregroundNotificationId);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        // Create a data source factory.
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();

        // Create the download manager.
        if (downloadManager == null) {
            DownloadManager downloadManager = new DownloadManager(
                    this,
                    getDatabaseProvider(this),
                    getDownloadCache(this),
                    dataSourceFactory,
                    Executors.newFixedThreadPool(/* nThreads= */ 6));
            downloadManager.setMaxParallelDownloads(3);
        }
        return downloadManager;
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return Util.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads, int notMetRequirements) {
        return null;
    }

    private static synchronized Cache getDownloadCache(Context context) {
        if (downloadCache == null) {
            File downloadContentDirectory =
                    new File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(
                    downloadContentDirectory,
                    new NoOpCacheEvictor(),
                    getDatabaseProvider(context));
        }
        return downloadCache;
    }

    private static synchronized DatabaseProvider getDatabaseProvider(Context context) {
        if (databaseProvider == null) {
            databaseProvider = new StandaloneDatabaseProvider(context);
        }
        return databaseProvider;
    }

    private static synchronized File getDownloadDirectory(Context context) {
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(/* type= */ null);
            if (downloadDirectory == null) {
                downloadDirectory = context.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    public static Cache getCache() {
        return downloadCache;
    }

}