package com.lixingyue.exceptions;

/**
 * Author:lxy1999
 * Created:2019/12/5
 */
//特殊异常处理，即就是在数组中有异常，将异常显示但不抛出
public class RemoteException extends Exception {
    public RemoteException() {
    }

    public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteException(Throwable cause) {
        super(cause);
    }

    public RemoteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
