package com.lixingyue.start;

import java.util.*;

/**
 * Author:lxy1999
 * Created:2019/12/6
 */
//Protocol里都是静态方法，静态属性的创建与回收不稳定，静态属性随着类的消亡而消亡，一旦涉及有属性的地方，就不放在类中
//对象的创建回收都是GC管理
//静态属性放在类中它的生命周期很难管理，所有用一个对象去管理它
public class Database {
//redis数据类型
    // string 类型
    //private static Map<String, String> strings;
    // hash 类型
    private static Map<String, Map<String, String>> hashes = new HashMap<>();
    // list 类型
    private static Map<String, List<String>> lists = new HashMap<>();
    // set 类型
    //private static Map<String, Set<String>> sets;
    // zset 类型
    //private static Map<String, LinkedHashSet<String>> zsets;
    //单例模式：只能有一个对象，所有的数据必须在一个对象中

    private Database(){
        //strings = new HashMap<>();
        hashes = new HashMap<>();
        lists = new HashMap<>();
        //sets = new HashMap<>();
        //zsets = new HashMap<>();
    }
    //饿汉式
//    private static com.lixingyue.start.Database instance = new com.lixingyue.start.Database();
//    public static com.lixingyue.start.Database getInstance(){
//        return instance;
//    }


    public static List<String> getList(String key){
        //如果key存在返回对应的list，如果list不存在，则调用computeIfAbsent方法，重新创建一个list
//        List<String> list = lists.computeIfAbsent(key,k->{
//            return new ArrayList<>();
//        });
        List<String> list = lists.get(key);
        if(list==null){
            list = new ArrayList<>();
            lists.put(key,list);
        }
        return list;
    }

    public static Map<String,String> getHashes(String key) {
        Map<String,String> hash = hashes.get(key);
        if(hash==null){
            hash = new HashMap<>();
            hashes.put(key,hash);
        }
        return hash;
    }
}
