package ZeroMQTest;

import org.zeromq.ZMQ;

//模拟发布订阅模式中的发布
public class ZeroMQPUB {
    public static void main(String[] args) throws InterruptedException {
        //创建一个zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建发布端实例
        ZMQ.Socket pub = zmq.socket(ZMQ.PUB);
        //绑定ip和端口
        pub.bind("tcp://localhost:5555");
        int i = 0;
        //发布数据
        while (true){
            Thread.sleep(1000);
            String data = "hello";
            pub.send(data+i);
            i ++;
        }

    }
}
