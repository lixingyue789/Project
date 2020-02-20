package com.lixingyue.commands;

import com.lixingyue.start.Command;
import com.lixingyue.start.Database;
import com.lixingyue.start.Protocol;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Author:lxy1999
 * Created:2019/12/7
 */
public class LPUSHCommand implements Command {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LPUSHCommand.class);
    private List<Object> args;
    @Override
    public void setArgs(List<Object> args) {
        this.args = args;
    }

    @Override
    public void run(OutputStream os) throws IOException {
        if(args.size()!=2){
            Protocol.writeError(os,"命令至少需要两个参数");
            return;
        }
        String key = new String((byte[]) args.get(0));
        String value = new String((byte[]) args.get(1));
        logger.debug("运行的是lpush命令：{} {}",key,value);
        //这种方式不是一个很好的线程同步的方式
        List<String> list = Database.getList(key);
        list.add(0,value);//头插
        logger.debug("插入后数据共有 {} 个",list.size());
        Protocol.writeInteger(os,list.size());//将其序列化，即就是编码转为二进制
    }
}
