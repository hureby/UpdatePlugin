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

import org.lzh.framework.updatepluginlib.business.DefaultDownloadWorker;
import org.lzh.framework.updatepluginlib.business.DefaultUpdateWorker;
import org.lzh.framework.updatepluginlib.business.DownloadWorker;
import org.lzh.framework.updatepluginlib.business.UpdateWorker;
import org.lzh.framework.updatepluginlib.callback.CheckCallback;
import org.lzh.framework.updatepluginlib.callback.DownloadCallback;
import org.lzh.framework.updatepluginlib.callback.LogCallback;
import org.lzh.framework.updatepluginlib.creator.DefaultFileChecker;
import org.lzh.framework.updatepluginlib.creator.DefaultFileCreator;
import org.lzh.framework.updatepluginlib.creator.DefaultNeedDownloadCreator;
import org.lzh.framework.updatepluginlib.creator.DefaultNeedInstallCreator;
import org.lzh.framework.updatepluginlib.creator.DefaultNeedUpdateCreator;
import org.lzh.framework.updatepluginlib.creator.DownloadCreator;
import org.lzh.framework.updatepluginlib.creator.FileChecker;
import org.lzh.framework.updatepluginlib.creator.FileCreator;
import org.lzh.framework.updatepluginlib.creator.InstallCreator;
import org.lzh.framework.updatepluginlib.creator.UpdateCreator;
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
 * 此类用于建立真正的更新任务。每个更新任务对应于一个{@link UpdateBuilder}实例。
 *
 * <p>创建更新任务有两种方式：<br>
 *      1. 通过{@link #create()}进行创建，代表将使用默认提供的全局更新配置。此默认更新配置通过{@link UpdateConfig#getDefault()}进行获取。<br>
 *      2. 通过{@link #create(UpdateConfig)}指定使用某个特殊的更新配置。<br>
 *
 * <p>此Builder中的所有配置项，均在{@link UpdateConfig}中有对应的相同方法名的配置函数。此两者的关系为：
 * 在更新流程中，当Builder中未设置对应的配置，将会使用在{@link UpdateConfig}更新配置中所提供的默认配置进行使用
 *
 * <p>正常启动：调用{@link #check()}进行启动。
 *
 * @author haoge
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class UpdateBuilder {

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
    private UpdateConfig config;
    
    private UpdateBuilder(UpdateConfig config) {
        this.config = config;
    }

    /**
     * 使用默认全局配置进行更新任务创建，默认全局配置可通过{@link UpdateConfig#getDefault()}进行获取
     * @return Builder
     */
    public static UpdateBuilder create() {
        return create(UpdateConfig.getDefault());
    }

    /**
     * 指定该更新任务所使用的更新配置。可通过{@link UpdateConfig#create()}进行新的更新配置创建。
     * @param config 指定使用的更新配置
     * @return Builder
     */
    public static UpdateBuilder create(UpdateConfig config) {
        return new UpdateBuilder(config);
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
    public UpdateBuilder setUrl(String url) {
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
    public UpdateBuilder setCheckEntity(CheckEntity entity) {
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
    public UpdateBuilder setUpdateChecker(UpdateChecker checker) {
        this.updateChecker = checker;
        return this;
    }

    /**
     * 配置更新文件检查器，默认使用{@link DefaultFileChecker}
     * @param checker 文件检查器
     * @return itself
     * @see FileChecker
     */
    public UpdateBuilder setFileChecker(FileChecker checker) {
        this.fileChecker = checker;
        return this;
    }

    /**
     * 配置更新api的访问网络任务，默认使用{@link DefaultUpdateWorker}
     * @param checkWorker 更新api访问网络任务。
     * @return itself
     * @see UpdateWorker
     */
    public UpdateBuilder setCheckWorker(UpdateWorker checkWorker) {
        this.checkWorker = checkWorker;
        return this;
    }

    /**
     * 配置apk下载网络任务，默认使用{@link DefaultDownloadWorker}
     * @param downloadWorker 下载网络任务
     * @return itself
     * @see DownloadWorker
     */
    public UpdateBuilder setDownloadWorker(DownloadWorker downloadWorker) {
        this.downloadWorker = downloadWorker;
        return this;
    }

    /**
     * 配置下载回调监听。 默认使用{@link LogCallback}
     * @param downloadCallback 下载回调监听
     * @return itself
     * @see DownloadCallback
     */
    public UpdateBuilder setDownloadCallback(DownloadCallback downloadCallback) {
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
    public UpdateBuilder setCheckCallback(CheckCallback checkCallback) {
        this.checkCallback = checkCallback;
        return this;
    }

    /**
     * 配置更新数据解析器。
     * @param updateParser 解析器
     * @return itself
     * @see UpdateParser
     */
    public UpdateBuilder setUpdateParser(UpdateParser updateParser) {
        this.updateParser = updateParser;
        return this;
    }

    /**
     * 配置apk下载缓存文件创建器 默认参考{@link DefaultFileCreator}
     * @param fileCreator 文件创建器
     * @return itself
     * @see FileCreator
     */
    public UpdateBuilder setFileCreator(FileCreator fileCreator) {
        this.fileCreator = fileCreator;
        return this;
    }

    /**
     * 配置下载进度通知创建器 默认参考{@link DefaultNeedDownloadCreator}
     * @param downloadCreator 下载进度通知创建器
     * @return itself
     * @see DownloadCreator
     */
    public UpdateBuilder setDownloadCreator(DownloadCreator downloadCreator) {
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
    public UpdateBuilder setInstallCreator(InstallCreator installCreator) {
        this.installCreator = installCreator;
        return this;
    }

    /**
     * 配置检查到有更新时的通知创建器 默认参考{@link DefaultNeedUpdateCreator}
     * @param updateCreator 通知创建器
     * @return itself
     * @see UpdateCreator
     */
    public UpdateBuilder setUpdateCreator(UpdateCreator updateCreator) {
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
    public UpdateBuilder setUpdateStrategy(UpdateStrategy strategy) {
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
    public UpdateBuilder setInstallStrategy(InstallStrategy installStrategy) {
        this.installStrategy = installStrategy;
        return this;
    }

    // ===== getter methods =======

    public UpdateStrategy getUpdateStrategy() {
        if (strategy == null) {
            strategy = config.getUpdateStrategy();
        }
        return strategy;
    }

    public CheckEntity getCheckEntity () {
        if (this.entity == null) {
            this.entity = config.getCheckEntity();
        }
        return entity;
    }

    public UpdateCreator getUpdateCreator() {
        if (updateCreator == null) {
            updateCreator = config.getUpdateCreator();
        }
        return updateCreator;
    }

    public InstallCreator getInstallCreator() {
        if (installCreator == null) {
            installCreator = config.getInstallCreator();
        }
        return installCreator;
    }

    public UpdateChecker getUpdateChecker() {
        if (updateChecker == null) {
            updateChecker = config.getUpdateChecker();
        }
        return updateChecker;
    }

    public FileChecker getFileChecker() {
        if (fileChecker == null) {
            fileChecker = config.getFileChecker();
        }
        return fileChecker;
    }

    public DownloadCreator getDownloadCreator() {
        if (downloadCreator == null) {
            downloadCreator = config.getDownloadCreator();
        }
        return downloadCreator;
    }

    public UpdateParser getUpdateParser() {
        if (updateParser == null) {
            updateParser = config.getUpdateParser();
        }
        return updateParser;
    }

    public UpdateWorker getCheckWorker() {
        if (checkWorker == null) {
            checkWorker = config.getCheckWorker();
        }
        return checkWorker;
    }

    public DownloadWorker getDownloadWorker() {
        if (downloadWorker == null) {
            downloadWorker = config.getDownloadWorker();
        }
        return downloadWorker;
    }

    public FileCreator getFileCreator() {
        if (fileCreator == null) {
            fileCreator = config.getFileCreator();
        }
        return fileCreator;
    }

    public InstallStrategy getInstallStrategy() {
        if (installStrategy == null) {
            installStrategy = config.getInstallStrategy();
        }
        return installStrategy;
    }

    public CheckCallback getCheckCallback() {
        if (checkCallback == null) {
            checkCallback = config.getCheckCallback();
        }
        return checkCallback;
    }

    public DownloadCallback getDownloadCallback() {
        if (downloadCallback == null) {
            downloadCallback = config.getDownloadCallback();
        }
        return downloadCallback;
    }

    /**
     * 启动更新任务。可在任意线程进行启动。
     */
    public void check() {
        Updater.getInstance().checkUpdate(this);
    }

}
