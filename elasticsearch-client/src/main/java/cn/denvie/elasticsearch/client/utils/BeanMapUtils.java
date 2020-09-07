/*
 * Copyright © 2020-2020 尛飛俠（Denvie） All rights reserved.
 */

package cn.denvie.elasticsearch.client.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean和Map互相转换工具类。
 *
 * @author denvie
 * @since 2020/8/4
 */
public class BeanMapUtils {
    /**
     * 将Bean转换为Map。
     *
     * @param bean Bean实例
     * @param <T>  Bean的类型
     * @return Map<String, Object>
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        if (bean == null) {
            return Maps.newHashMap();
        }
        BeanMap beanMap = new BeanMap(bean);
        HashMap<String, Object> properties = new HashMap<>();
        beanMap.forEach((key, value) -> {
            String stringKey = String.valueOf(key);
            if (!"null".equalsIgnoreCase(stringKey) && !"class".equalsIgnoreCase(stringKey)) {
                properties.put(stringKey, value);
            }
        });
        return properties;
    }

    /**
     * 将Map装换为Bean对象。
     *
     * @param properties 属性Map
     * @param clazz      Bean实例
     * @param <T>        Bean的类型
     * @return Bean
     */
    public static <T> T mapToBean(Map<String, Object> properties, Class<T> clazz) {
        if (properties == null || clazz == null) {
            return null;
        }
        T instance = null;
        try {
            instance = clazz.newInstance();
            BeanUtils.populate(instance, properties);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * 将List<T>转换为List<Map<String, Object>>。
     *
     * @param objList Bean实例列表。
     * @param <T>     Bean的类型
     * @return List<Map < String, Object>>
     */
    public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList) {
        List<Map<String, Object>> list = Lists.newArrayList();
        if (objList != null && objList.size() > 0) {
            for (int i = 0, size = objList.size(); i < size; i++) {
                list.add(beanToMap(objList.get(i)));
            }
        }
        return list;
    }

    /**
     * 将List<Map<String,Object>>转换为List<T>。
     *
     * @param maps  Map列表
     * @param clazz Bean的Clazz对象
     * @param <T>   Bean的类型
     * @return Bean实例列表
     */
    public static <T> List<T> mapsToObjects(List<Map<String, Object>> maps, Class<T> clazz) {
        List<T> list = Lists.newArrayList();
        if (maps != null && maps.size() > 0) {
            Map<String, Object> map;
            for (int i = 0, size = maps.size(); i < size; i++) {
                map = maps.get(i);
                list.add(mapToBean(map, clazz));
            }
        }
        return list;
    }
}
