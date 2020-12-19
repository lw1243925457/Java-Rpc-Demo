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

package com.rpc.core.demo.proxy;

import com.rpc.core.demo.annotation.ProviderService;
import com.rpc.core.demo.api.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author lw1243925457
 */
@Slf4j
public class ProviderServiceManagement {

    /**
     * group -> version -> service class
     */
    private static Map<String, Map<String, Map<String, Object>>> proxyMap = new HashMap<>();

    public static void init(String packageName) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        System.out.println("\n-------- Loader Rpc Provider class start ----------------------\n");
        Class[] classes = getClasses(packageName);
        for (Class c: classes) {
            ProviderService annotation = (ProviderService) c.getAnnotation(ProviderService.class);
            if (annotation == null) {
                continue;
            }
            String group = annotation.group();
            String version = annotation.version();
            Map<String, Map<String, Object>> groupMap = proxyMap.getOrDefault(group, new HashMap<>(16));
            Map<String, Object> versionMap = groupMap.getOrDefault(version, new HashMap<>(16));

            versionMap.put(annotation.service(), c.newInstance());
            groupMap.put(version, versionMap);
            proxyMap.put(group, groupMap);
            log.info("load provider class: " + annotation.service() + ":" + group + ":" + version + " :: " + c.getName());
        }
        System.out.println("\n-------- Loader Rpc Provider class end ----------------------\n");
    }

    public static Object getProviderService(RpcRequest request) {
        String group = "default";
        String version= "default";
        String className = request.getServiceClass();
        if (request.getGroup() != null) {
            group = request.getGroup();
        }
        if (request.getVersion() != null) {
            version = request.getVersion();
        }
        return proxyMap.get(group).get(version).get(className);
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException exception
     * @throws IOException exception
     */
    private static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[0]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
