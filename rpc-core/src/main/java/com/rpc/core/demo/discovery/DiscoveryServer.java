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

package com.rpc.core.demo.discovery;

import com.google.common.base.Joiner;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lw1243925457
 */
public class DiscoveryServer extends ZookeeperClient {

    List<ServiceDiscovery<String>> discoveryList = new ArrayList<>();

    public DiscoveryServer() {
    }

    public void registerService(String service, String group, String version, int port, String tags) throws Exception {
        ServiceInstance<String> instance = ServiceInstance.<String>builder()
                .name(Joiner.on(":").join(service, group, version))
                .port(port)
                .address(InetAddress.getLocalHost().getHostAddress())
                .payload(tags)
                .build();

        ServiceDiscovery<String> discovery = ServiceDiscoveryBuilder.builder(String.class)
                .client(client)
                .basePath(REGISTER_ROOT_PATH)
                .thisInstance(instance)
                .build();
        discovery.start();

        discoveryList.add(discovery);
    }

    public void close() throws IOException {
        for (ServiceDiscovery<String> discovery: discoveryList) {
            discovery.close();
        }
        client.close();
    }
}
