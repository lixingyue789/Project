package com.lixingyue.start;

import com.lixingyue.exceptions.RemoteException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
//用到的redis在桌面，状态转换机，测试：bedis-testcase
/**
 * Author:lxy1999
 * Created:2019/12/4
 */
public class Protocol {//协议[字节码解析]
    //对process进行封装
    public static Object read(InputStream is) throws IOException, RemoteException {
        return process(is);
    }
    //读lpush或者是lrange
    public static Command readCommand(InputStream is) throws Exception {
        Object o = read(is);
        ////作为Server来说，一定不会收到“+OK\r\n”,一定是*开头的
        //所有的命令都必须是List形式
        if(!(o instanceof List)){
            throw new Exception("命令必须是Array类型的");
        }
        List<Object> list = (List<Object>) o;
        if(list.size()<=1){
            throw new Exception("命令个数必须大于1");
        }
        Object o2 = list.remove(0);//必须是byte[]类型
        if(!(o2 instanceof byte[])){
            throw new Exception("错误的命令类型");
        }
        byte[] array = (byte[]) o2;
        //通过反射可以从commandName得到类名称在通过反射得到对象
        String commandName = new String(array);
        //得到类名称
        String className = String.format("com.lixingyue.commands.%sCommand",commandName.toUpperCase());
        //得到类的对象
        Class<?> cls = Class.forName(className);
        //找出类名称是否为Command接口的实现（isAssignableFrom）
        if(!Command.class.isAssignableFrom(cls)){
            throw new Exception("错误的命令");
        }
        Command command =  (Command) cls.newInstance();//返回这个类对象的实例化
        command.setArgs(list);
        return command;
    }
    private static String processSimpleString(InputStream is) throws IOException {
        return readLine(is);
    }
    private static String processError(InputStream is) throws IOException {
        return readLine(is);
    }
    private static long processInteger(InputStream is) throws IOException{
        return readInteger(is);
    }
    private static byte[] processBulkString(InputStream is) throws IOException{
        int len = (int) readInteger(is);
        if(len==-1){//$-1\r\n->null
            return  null;
        }
        byte[] r = new byte[len];
        is.read(r,0,len);
//        for(int i = 0;i<len;i++){
//            int b = is.read();
//            r[i] = (byte) b;
//        }
        //$5\r\nhello\r\n要将最后的\r\n读走
        is.read();
        is.read();
        return r;
    }
    private static List<Object> processArray(InputStream is) throws IOException{
        int len = (int)readInteger(is);
        if(len==-1){
            return null;
        }
        List<Object> list = new ArrayList<>(len);
        for(int i = 0;i<len;i++){
            try{
                list.add(process(is));
            }catch (RemoteException e){
                list.add(e);
            }
        }
        return list;
    }
    //处理协议
    private static Object process(InputStream is) throws IOException, RemoteException {
        int b = is.read();
        if(b==-1){
            throw new RuntimeException("不应该读到结尾的");
        }
        switch (b){
            case '+':
                return processSimpleString(is);
            case '-':
                throw new RemoteException(processError(is));
                //现在的异常为RuntimeException
                // 如果协议为*2\r\n-ERROR\r\n:-100\r\n
                //这样运行结果会抛出异常，我们目的是显示出异常（RemoteException类进行处理）
            case ':':
                return processInteger(is);
            case '$':
                return processBulkString(is);
            case '*':
                return processArray(is);
            default:
                    throw new RuntimeException("不识别的类型");
        }
    }
    public static String readLine(InputStream is) throws IOException {
        boolean needRead = true;
        StringBuilder sb = new StringBuilder();
        int b = -1;
        while (true){
            if(needRead==true){
                b = is.read();
                if(b==-1){
                    //throw new EOFException();
                    throw new RuntimeException("不应该读到结尾的");//读到结尾即关闭redis
                    //读到结尾的异常归类，即对方关闭连接，即退出
                }
            }else {
                needRead = true;
            }
            if(b=='\r'){
                int c = is.read();
                if(c==-1){
                    throw new RuntimeException("不应该读到结尾的");
                }
                if(c=='\n'){
                    break;
                }
                if(c=='\r'){
                    sb.append((char)b);
                    b = c;
                    needRead = false;
                }else {
                    sb.append((char)b);
                    sb.append((char)c);
                }
            }else {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }
    //读一个字节
    public static long readInteger(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean isNegative = false;
        int b = is.read();
        if (b == -1) {
            throw new RuntimeException("不应该读到结尾");
        }
        if(b=='-'){
            isNegative = true;
        }else {
            sb.append((char)b);
        }
        while (true){
            b = is.read();
            if(b==-1){
                throw new RuntimeException("不应该读到结尾");
            }
            if(b=='\r'){
                int c = is.read();
                if(c==-1){
                    throw new RuntimeException("不应该读到结尾");
                }
                if(c=='\n'){
                    break;
                }
                throw new RuntimeException("没有读到\\r\\n");
            }else {
                sb.append((char)b);
            }
        }
        long result = Long.parseLong(sb.toString());
        if(isNegative){
            result = -result;
        }
        return result;
    }

    public static void writeError(OutputStream os, String msg) throws IOException {
        //返回"-msg\r\n"
        os.write('-');
        os.write(msg.getBytes("GBK"));
        os.write("\r\n".getBytes("GBK"));
    }

    public static void writeInteger(OutputStream os, long v) throws IOException {
        //v=10 ->:10\r\n
        //v=-1 ->:-1\r\n
        os.write(':');
        os.write(String.valueOf(v).getBytes());
        os.write("\r\n".getBytes());
    }

    public static void writeArray(OutputStream os, List<?> list) throws Exception {
        os.write('*');
        os.write(String.valueOf(list.size()).getBytes());
        os.write("\r\n".getBytes());
        for(Object o :list){
            //简化版：数组中只能出现BulkString与Integer类型的
            if(o instanceof String){
                writeBulkString(os,(String)o);
            }else if(o instanceof  Integer){
                writeInteger(os,(Integer)o);
            }else if(o instanceof Long){
                writeInteger(os,(Long)o);
            }else {
                throw new Exception("错误的类型");
            }
        }
    }

    public static void writeBulkString(OutputStream os,String s) throws IOException {
        byte[] buf = s.getBytes();
        os.write('$');
        os.write(String.valueOf(buf.length).getBytes());
        os.write("\r\n".getBytes());
        os.write(buf);
        os.write("\r\n".getBytes());
    }

    public static void writeNull(OutputStream os) throws IOException {
        os.write('$');
        os.write('-');
        os.write('1');
        os.write("\r\n".getBytes());
    }
}
