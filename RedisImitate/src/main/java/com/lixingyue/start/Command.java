package com.lixingyue.start;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Author:lxy1999
 * Created:2019/12/5
 */
public interface Command {
    void setArgs(List<Object> args);

    void run(OutputStream os) throws IOException;
}
