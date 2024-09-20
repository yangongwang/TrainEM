package ZeroMQTest;

import org.zeromq.ZMQ;

//模拟推拉模式中的推
public class ZeroMQPUSH {
    public static void main(String[] args) throws InterruptedException {
        //创建zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建push实例
        ZMQ.Socket push = zmq.socket(ZMQ.PUSH);
        //绑定ip和端口
        push.bind("tcp://localhost:5555");

        int i = 0;
        while (true){
            push.send("data"+i);
            i ++;
            Thread.sleep(1000);
        }
    }
}
