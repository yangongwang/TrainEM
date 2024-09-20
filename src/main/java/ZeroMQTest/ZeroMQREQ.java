package ZeroMQTest;

import org.zeromq.ZMQ;

//模拟问答模式中的问
public class ZeroMQREQ {
    public static void main(String[] args) {
        //创建zeromq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建客户端实例
        ZMQ.Socket req = zmq.socket(ZMQ.REQ);
        //连接ip和端口
        req.connect("tcp://localhost:5555");
        //先发送后接收
        while (!Thread.currentThread().isInterrupted()){
            //先发送
            String data = "0701";
            System.out.println(data);
            req.send(data);
            //接收数据
            byte[] recv = req.recv();
            System.out.println(new String(recv));
        }
        //关闭资源
        req.close();
        zmq.close();
    }
}
