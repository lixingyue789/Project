package com.lixingyue.commands;

import com.lixingyue.start.Command;
import com.lixingyue.start.Database;
import com.lixingyue.start.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Author:lxy1999
 * Created:2019/12/7
 */
public class HSETCommand implements Command {
    private List<Object> args;
    @Override
    public void setArgs(List<Object> args) {
        this.args = args;
    }

    @Override
    public void run(OutputStream os) throws IOException {
        String key = new String((byte[]) args.get(0));
        String field = new String((byte[]) args.get(1));
        String value = new String((byte[]) args.get(2));
        Map<String,String> hash = Database.getHashes(key);
        boolean isUpdate = hash.containsKey(field);
        hash.put(field,value);
        if(isUpdate){
            Protocol.writeInteger(os,0);
        }else {
            Protocol.writeInteger(os,1);
        }
    }
}
