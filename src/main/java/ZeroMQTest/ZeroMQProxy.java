package ZeroMQTest;

import org.zeromq.ZMQ;

//模拟推拉模式中的代理
public class ZeroMQProxy {
    public static void main(String[] args) {
        //创建zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建一个pull的实例
        ZMQ.Socket pull = zmq.socket(ZMQ.PULL);
        //连接ip和端口
        pull.connect("tcp://localhost:5555");

        //创建一个push的实例
        ZMQ.Socket push = zmq.socket(ZMQ.PUSH);
        //绑定ip和端口
        push.bind("tcp://localhost:5556");

        //调用代理方法
        ZMQ.proxy(pull,push,null);

        //关闭资源
        push.close();
        pull.close();
        zmq.close();
    }
}
