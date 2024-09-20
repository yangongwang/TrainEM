package ZeroMQTest;

import org.zeromq.ZMQ;

//模拟问答模式中的答
public class ZeroMQREP {
    public static void main(String[] args) throws InterruptedException {
        //创建zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建服务端实例
        ZMQ.Socket rep = zmq.socket(ZMQ.REP);
        //绑定ip和端口
        rep.bind("tcp://localhost:5555");
        int i = 0;
        //先接收数据在回答
        while (true){
            //接收数据
            byte[] recv = rep.recv();
            //睡眠一会
            Thread.sleep(500);
            String data = "hello world";
            //回答数据
            rep.send(data+i);
            i++;
        }
    }
}
