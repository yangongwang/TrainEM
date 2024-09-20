package ZeroMQTest;

import org.zeromq.ZMQ;

//模拟发布订阅模式中的订阅
public class ZeroMQSUB {
    public static void main(String[] args) {
        //创建zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建订阅端实例
        ZMQ.Socket sub = zmq.socket(ZMQ.SUB);
        //连接ip和端口
        sub.connect("tcp://localhost:5555");
        //subscibe
        sub.subscribe("".getBytes());
        //接收数据
        while (!Thread.currentThread().isInterrupted()){
            byte[] recv = sub.recv();
            System.out.println(new String(recv));
        }
        //释放资源
        sub.close();
        zmq.close();
    }
}
