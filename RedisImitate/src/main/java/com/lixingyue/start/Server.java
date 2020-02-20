package com.lixingyue.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
//Logger对象可以通过getLogger工厂方法之一的调用获得。
// 这些将创建一个新的Logger或返回一个合适的现有Logger。
// 重要的是要注意，如果没有保存对Logger的强烈引用，
// 那么getLogger工厂方法之一返回的记录器可能随时被垃圾回收。
//记录消息将被转发到已注册的Handler对象，该对象可以将消息转发到各种目的地，
// 包括控制台，文件，操作系统日志.
/**
 * Author:lxy1999
 * Created:2019/12/6
 */
public class Server {
    /**
     * 其中//是自己备注
     * ////老师备注
     */
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    public void run(int port) throws IOException {
        //监听端口（////建立一个Listen在port上的TCP监听Socket）
        try(ServerSocket serverSocket = new ServerSocket(port)){
            //对应TCP中：绑定了socket，并且开始listen port端口
            //通过netstate可以查看网上链接，可以看是否监听到它（Linux）
            while (true){
                //accept一条协议
                ////通过accept方法调用，返回一个代表建立好的连接的TCP Socket
                try (Socket socket = serverSocket.accept()){
                    //建立连接：相当于server监听端口，建立连接过程中发起三次握手，三次握手完成后，连接成功
                    //socket代表背后建立的三次握手的链接（网络）
                    logger.info("{} 已连接",socket.getInetAddress().getHostName());
                    //拿到输入输出流
                    ////代表连接中的输入输出流
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();
                    Command command = null;//将协议解析得到命令
                    while (true){
                        try {
                            command = Protocol.readCommand(is);
                            command.run(os);//写入
                        } catch (Exception e) {
                            e.printStackTrace();
                            Protocol.writeError(os,"不识别的命令");
                        }
                    }
                }
            }//while循环代表我的服务器是长期监听这个端口的，只要有用户过来连接，我就会处理它，
            //处理成功后，在处理用户的下一条连接
        }
    }
    public static void main(String[] args) throws IOException {
//        logger.error("我是{}","啦啦啦");
//        logger.warn();
//        logger.info();
//        logger.debug();
        new Server().run(6379);
    }
}
