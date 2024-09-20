package ZeroMQTest;

import org.zeromq.ZMQ;

//模拟推拉推拉模式中的拉
public class ZeroMQPULL {
    public static void main(String[] args) {
        //创建zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建一个pull实例
        ZMQ.Socket pull = zmq.socket(ZMQ.PULL);
        //连接ip和端口
        pull.connect("tcp://localhost:5556");

        //接受数据
        while (!Thread.currentThread().isInterrupted()){
            byte[] recv = pull.recv();
            System.out.println(new String(recv));
        }
        //释放资源
        pull.close();
        zmq.close();
    }
}
