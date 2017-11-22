/*
 * Copyright (C) 2017 Haoge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lzh.framework.updatepluginlib;

import android.text.TextUtils;

import org.lzh.framework.updatepluginlib.business.DefaultDownloadWorker;
import org.lzh.framework.updatepluginlib.business.DefaultUpdateWorker;
import org.lzh.framework.updatepluginlib.business.DownloadWorker;
import org.lzh.framework.updatepluginlib.business.UpdateWorker;
import org.lzh.framework.updatepluginlib.callback.DownloadCallback;
import org.lzh.framework.updatepluginlib.callback.LogCallback;
import org.lzh.framework.updatepluginlib.callback.CheckCallback;
import org.lzh.framework.updatepluginlib.creator.DownloadCreator;
import org.lzh.framework.updatepluginlib.creator.UpdateCreator;
import org.lzh.framework.updatepluginlib.creator.FileCreator;
import org.lzh.framework.updatepluginlib.creator.DefaultFileChecker;
import org.lzh.framework.updatepluginlib.creator.DefaultFileCreator;
import org.lzh.framework.updatepluginlib.creator.DefaultNeedDownloadCreator;
import org.lzh.framework.updatepluginlib.creator.DefaultNeedInstallCreator;
import org.lzh.framework.updatepluginlib.creator.DefaultNeedUpdateCreator;
import org.lzh.framework.updatepluginlib.creator.FileChecker;
import org.lzh.framework.updatepluginlib.creator.InstallCreator;
import org.lzh.framework.updatepluginlib.model.CheckEntity;
import org.lzh.framework.updatepluginlib.model.DefaultChecker;
import org.lzh.framework.updatepluginlib.model.UpdateChecker;
import org.lzh.framework.updatepluginlib.model.UpdateParser;
import org.lzh.framework.updatepluginlib.strategy.DefaultInstallStrategy;
import org.lzh.framework.updatepluginlib.strategy.ForcedUpdateStrategy;
import org.lzh.framework.updatepluginlib.strategy.InstallStrategy;
import org.lzh.framework.updatepluginlib.strategy.UpdateStrategy;
import org.lzh.framework.updatepluginlib.strategy.WifiFirstStrategy;

/**
 * 此类用于提供一些默认使用的更新配置。在进行更新任务时，当{@link UpdateBuilder} 中未设置对应的配置时。
 * 将从此配置类中读取默认的配置进行使用
 *
 * @author haoge
 */
@SuppressWarnings("WeakerAccess")
public class UpdateConfig {

    private UpdateWorker checkWorker;
    private DownloadWorker downloadWorker;
    private CheckCallback checkCallback;
    private DownloadCallback downloadCallback;
    private CheckEntity entity;
    private UpdateStrategy strategy;
    private UpdateCreator updateCreator;
    private InstallCreator installCreator;
    private DownloadCreator downloadCreator;
    private UpdateParser updateParser;
    private FileCreator fileCreator;
    private UpdateChecker updateChecker;
    private FileChecker fileChecker;
    private InstallStrategy installStrategy;

    private static UpdateConfig DEFAULT;

    /**
     * 获取一个全局默认的更新配置。正常情况下，使用的即是此默认的更新配置。
     *
     * <p>当使用{@link UpdateBuilder#check()}建立新的更新任务时，则将使用此默认的更新配置
     * @return 默认的更新配置。
     */
    public static UpdateConfig getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new UpdateConfig();
        }
        return DEFAULT;
    }

    /**
     * 创建一个新的更新配置提供使用，当有多个需求需要用到更新逻辑时，可使用此方法针对不同的更新逻辑创建不同的更新配置使用：
     * 如插件化远程插件的下载。
     *
     * <p>要使用此方式创建出来的更新配置。需要使用{@link UpdateBuilder#create(UpdateConfig)}进行更新任务创建。
     *
     * @return 新的更新配置。
     */
    public static UpdateConfig create() {
        return new UpdateConfig();
    }

    /**
     * 配置更新api。此方法设置的api是对于只有url数据。请求方式为GET请求时所使用的。
     *
     * <p>请注意：此配置方法与{@link #setCheckEntity(CheckEntity)}互斥。
     *
     * @param url 用于进行检查更新的url地址
     * @return itself
     * @see #setCheckEntity(CheckEntity)
     */
    public UpdateConfig setUrl(String url) {
        this.entity = new CheckEntity().setUrl(url);
        return this;
    }

    /**
     * 配置更新api。此方法是用于针对复杂api的需求进行配置的。本身提供url,method,params。对于其他需要的数据。
     * 可通过继承此{@link CheckEntity}实体类，加入更多数据。并通过{@link #setCheckWorker(UpdateWorker)}配置对应
     * 的网络任务进行匹配兼容
     * @param entity 更新api数据实体类
     * @return itself
     */
    public UpdateConfig setCheckEntity(CheckEntity entity) {
        this.entity = entity;
        return this;
    }

    /**
     * 配置更新数据检查器。默认使用{@link DefaultChecker}
     *
     * @param checker 更新检查器。
     * @return itself
     * @see UpdateChecker
     */
    public UpdateConfig setUpdateChecker(UpdateChecker checker) {
        this.updateChecker = checker;
        return this;
    }

    /**
     * 配置更新文件检查器，默认使用{@link DefaultFileChecker}
     * @param checker 文件检查器
     * @return itself
     * @see FileChecker
     */
    public UpdateConfig setFileChecker(FileChecker checker) {
        this.fileChecker = checker;
        return this;
    }

    /**
     * 配置更新api的访问网络任务，默认使用{@link DefaultUpdateWorker}
     * @param checkWorker 更新api访问网络任务。
     * @return itself
     * @see UpdateWorker
     */
    public UpdateConfig setCheckWorker(UpdateWorker checkWorker) {
        this.checkWorker = checkWorker;
        return this;
    }

    /**
     * 配置apk下载网络任务，默认使用{@link DefaultDownloadWorker}
     * @param downloadWorker 下载网络任务
     * @return itself
     * @see DownloadWorker
     */
    public UpdateConfig setDownloadWorker(DownloadWorker downloadWorker) {
        this.downloadWorker = downloadWorker;
        return this;
    }

    /**
     * 配置下载回调监听。 默认使用{@link LogCallback}
     * @param downloadCallback 下载回调监听
     * @return itself
     * @see DownloadCallback
     */
    public UpdateConfig setDownloadCallback(DownloadCallback downloadCallback) {
        this.downloadCallback = downloadCallback;
        return this;
    }

    /**
     * 配置更新检查回调监听，默认使用{@link LogCallback}
     *
     * @param checkCallback 更新检查回调器
     * @return itself
     * @see CheckCallback
     */
    public UpdateConfig setCheckCallback(CheckCallback checkCallback) {
        this.checkCallback = checkCallback;
        return this;
    }

    /**
     * 配置更新数据解析器。
     * @param updateParser 解析器
     * @return itself
     * @see UpdateParser
     */
    public UpdateConfig setUpdateParser(UpdateParser updateParser) {
        this.updateParser = updateParser;
        return this;
    }

    /**
     * 配置apk下载缓存文件创建器 默认参考{@link DefaultFileCreator}
     * @param fileCreator 文件创建器
     * @return itself
     * @see FileCreator
     */
    public UpdateConfig setFileCreator(FileCreator fileCreator) {
        this.fileCreator = fileCreator;
        return this;
    }

    /**
     * 配置下载进度通知创建器 默认参考{@link DefaultNeedDownloadCreator}
     * @param downloadCreator 下载进度通知创建器
     * @return itself
     * @see DownloadCreator
     */
    public UpdateConfig setDownloadCreator(DownloadCreator downloadCreator) {
        this.downloadCreator = downloadCreator;
        return this;
    }

    /**
     * 配置启动安装任务前通知创建器 默认参考{@link DefaultNeedInstallCreator}
     *
     * @param installCreator 下载完成后。启动安装前的通知创建器
     * @return itself
     * @see InstallCreator
     */
    public UpdateConfig setInstallCreator(InstallCreator installCreator) {
        this.installCreator = installCreator;
        return this;
    }

    /**
     * 配置检查到有更新时的通知创建器 默认参考{@link DefaultNeedUpdateCreator}
     * @param updateCreator 通知创建器
     * @return itself
     * @see UpdateCreator
     */
    public UpdateConfig setUpdateCreator(UpdateCreator updateCreator) {
        this.updateCreator = updateCreator;
        return this;
    }

    /**
     * 配置更新策略，默认使用{@link WifiFirstStrategy}, 当更新数据为要求强制更新时。则强制使用{@link ForcedUpdateStrategy}
     * @param strategy 更新策略
     * @return itself
     * @see UpdateStrategy
     * @see WifiFirstStrategy
     * @see ForcedUpdateStrategy
     */
    public UpdateConfig setUpdateStrategy(UpdateStrategy strategy) {
        this.strategy = strategy;
        return this;
    }


    /**
     * 配置安装策略 默认参考 {@link DefaultInstallStrategy}
     *
     * @param installStrategy 安装策略
     * @return itself
     * @see InstallStrategy
     */
    public UpdateConfig setInstallStrategy(InstallStrategy installStrategy) {
        this.installStrategy = installStrategy;
        return this;
    }

    // ===== getter methods =======

    public UpdateStrategy getUpdateStrategy() {
        if (strategy == null) {
            strategy = new WifiFirstStrategy();
        }
        return strategy;
    }

    public CheckEntity getCheckEntity () {
        if (this.entity == null || TextUtils.isEmpty(this.entity.getUrl())) {
            throw new IllegalArgumentException("Do not set setUrl in CheckEntity");
        }
        return this.entity;
    }

    public UpdateCreator getUpdateCreator() {
        if (updateCreator == null) {
            updateCreator = new DefaultNeedUpdateCreator();
        }
        return updateCreator;
    }

    public InstallCreator getInstallCreator() {
        if (installCreator == null) {
            installCreator = new DefaultNeedInstallCreator();
        }
        return installCreator;
    }

    public UpdateChecker getUpdateChecker() {
        if (updateChecker == null) {
            updateChecker = new DefaultChecker();
        }
        return updateChecker;
    }

    public FileChecker getFileChecker() {
        if (fileChecker == null) {
            fileChecker = new DefaultFileChecker();
        }
        return fileChecker;
    }

    public DownloadCreator getDownloadCreator() {
        if (downloadCreator == null) {
            downloadCreator = new DefaultNeedDownloadCreator();
        }
        return downloadCreator;
    }

    public UpdateParser getUpdateParser() {
        if (updateParser == null) {
            throw new IllegalStateException("update parser is null");
        }
        return updateParser;
    }

    public UpdateWorker getCheckWorker() {
        if (checkWorker == null) {
            checkWorker = new DefaultUpdateWorker();
        }
        return checkWorker;
    }

    public DownloadWorker getDownloadWorker() {
        if (downloadWorker == null) {
            downloadWorker = new DefaultDownloadWorker();
        }
        return downloadWorker;
    }

    public FileCreator getFileCreator() {
        if (fileCreator == null) {
            fileCreator = new DefaultFileCreator();
        }
        return fileCreator;
    }

    public InstallStrategy getInstallStrategy() {
        if (installStrategy == null) {
            installStrategy = new DefaultInstallStrategy();
        }
        return installStrategy;
    }

    public CheckCallback getCheckCallback() {
        if (checkCallback == null) {
            checkCallback = LogCallback.get();
        }
        return checkCallback;
    }

    public DownloadCallback getDownloadCallback() {
        if (downloadCallback == null) {
            downloadCallback = LogCallback.get();
        }
        return downloadCallback;
    }
}

