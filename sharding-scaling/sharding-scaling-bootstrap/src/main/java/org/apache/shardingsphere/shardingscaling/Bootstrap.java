/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingscaling;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.shardingscaling.web.HttpServerInitializer;
import org.apache.shardingsphere.shardingscaling.utils.RuntimeUtil;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import java.io.File;
import java.io.IOException;

/**
 * Bootstrap of ShardingScaling.
 */
@Slf4j
public final class Bootstrap {
    
    private static final String DEFAULT_CONFIG_PATH = "/conf/";
    
    private static final String DEFAULT_CONFIG_FILE_NAME = "server.yaml";
    
    /**
     * Main entry.
     *
     * @param args running args
     */
    @SneakyThrows
    public static void main(final String[] args) {
        log.info("Init server config");
        initServerConfig();
        log.info("ShardingScaling Startup");
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());
            int port = ScalingContext.getInstance().getServerConfiguration().getPort();
            Channel channel = bootstrap.bind(port).sync().channel();
            log.info("ShardingScaling is server on http://127.0.0.1:" + port + '/');
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    private static void initServerConfig() throws IOException {
        File yamlFile = new File(RuntimeUtil.getResourcePath(DEFAULT_CONFIG_PATH + DEFAULT_CONFIG_FILE_NAME));
        ServerConfiguration serverConfiguration = YamlEngine.unmarshal(yamlFile, ServerConfiguration.class);
        Preconditions.checkNotNull(serverConfiguration, "Server configuration file `%s` is invalid.", yamlFile.getName());
        ScalingContext.getInstance().init(serverConfiguration);
    }
}
