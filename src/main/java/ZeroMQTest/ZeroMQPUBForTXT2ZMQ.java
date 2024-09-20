package ZeroMQTest;

import com.google.protobuf.ByteString;
import com.google.protobuf.UInt32Value;
import dms.MATPOuterClass;
import dms.MTransfCtrlOuterClass;
import org.zeromq.ZMQ;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


//模拟发布订阅模式中的发布
public class ZeroMQPUBForTXT2ZMQ {

    public static void main(String[] args) throws InterruptedException {
        //创建一个zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建发布端实例
        ZMQ.Socket pub = zmq.socket(ZMQ.PUB);
        //绑定ip和端口
        pub.bind("tcp://localhost:5555");
        int i = 0;
        //发布数据
        BufferedReader reader;
        String dataPath = "/opt/data/TrainData_min.txt";

        try {
            //创建mTransfCtrl对象
            MTransfCtrlOuterClass.MTransfCtrl.Builder mTransfCtrl = MTransfCtrlOuterClass.MTransfCtrl.newBuilder();
            //创建Matp对象
            MATPOuterClass.MATP.Builder matp = null;
            MATPOuterClass.MPacketHead.Builder packetHead = null;
            MATPOuterClass.MATPBaseInfo.Builder atpBaseInfo = null;
            MATPOuterClass.MBalisePocket.Builder balisePocket = null;
            MATPOuterClass.MSignal.Builder signal = null;
            MATPOuterClass.MSignal.Builder runNextSignal = null;
            MATPOuterClass.MDriverInfo.Builder driverInfo = null;




            reader = new BufferedReader(new FileReader(dataPath));
            String line = reader.readLine();
            String[] fields = null;
            while (line != null) {
                fields = line.split("\\|", -1);
                if (fields.length < 55)
                    continue;
                matp = MATPOuterClass.MATP.newBuilder();

                // 设置头数据包内容
                packetHead = MATPOuterClass.MPacketHead.newBuilder();
                if (!fields[0].isEmpty())
                    packetHead.setATPType(fields[0]);
                if (!fields[1].isEmpty())
                    packetHead.setTrainID(Integer.parseInt(fields[1]));
                if (!fields[2].isEmpty())
                    packetHead.setTrainNum(ByteString.copyFromUtf8(fields[2]));
                if (!fields[3].isEmpty())
                    packetHead.setAttachRWBureau(fields[3]);
                if (!fields[4].isEmpty())
                    packetHead.setViaRWBureau(fields[4]);
                if (!fields[5].isEmpty())
                    packetHead.setCrossDayTrainNum(Boolean.parseBoolean(fields[5]));
                if (!fields[6].isEmpty())
                    packetHead.setDriverID(ByteString.copyFromUtf8(fields[6]));
                matp.setPacketHead(packetHead);

                // 设置 MATPBaseInfo 数据
                atpBaseInfo = MATPOuterClass.MATPBaseInfo.newBuilder();
                if (!fields[7].isEmpty())
                    atpBaseInfo.setDataTime(fields[7]);
                if (!fields[8].isEmpty())
                    atpBaseInfo.setSpeed(Integer.parseInt(fields[8]));
                if (!fields[9].isEmpty())
                    atpBaseInfo.setLevel(Integer.parseInt(fields[9]));
                if (!fields[10].isEmpty())
                    atpBaseInfo.setMileage(Integer.parseInt(fields[10]));
                if (!fields[11].isEmpty())
                    atpBaseInfo.setBraking(Integer.parseInt(fields[11]));
                if (!fields[12].isEmpty())
                    atpBaseInfo.setEmergentBrakSpd(Integer.parseInt(fields[12]));
                if (!fields[13].isEmpty())
                    atpBaseInfo.setCommonBrakSpd(Integer.parseInt(fields[13]));
                if (!fields[14].isEmpty())
                    atpBaseInfo.setRunDistance(Integer.parseInt(fields[14]));
                if (!fields[15].isEmpty())
                    atpBaseInfo.setDirection(Integer.parseInt(fields[15]));
                if (!fields[16].isEmpty())
                    atpBaseInfo.setLineID(Integer.parseInt(fields[16]));
                if (!fields[17].isEmpty())
                    atpBaseInfo.setATPError(ByteString.copyFromUtf8(fields[17]));
                matp.setATPBaseInfo(atpBaseInfo);

                // 设置 MBalisePocket 数据
                balisePocket = MATPOuterClass.MBalisePocket.newBuilder();
                if (!fields[18].isEmpty())
                    balisePocket.setBaliseID(Integer.parseInt(fields[18]));
                if (!fields[19].isEmpty())
                    balisePocket.setBaliseMile(Integer.parseInt(fields[19]));
                if (!fields[20].isEmpty())
                    balisePocket.setBaliseType(fields[20]);
                if (!fields[21].isEmpty())
                    balisePocket.setDirection(Integer.parseInt(fields[21]));
                if (!fields[22].isEmpty())
                    balisePocket.setLineID(Integer.parseInt(fields[22]));
                if (!fields[23].isEmpty())
                    balisePocket.setAttachRWBureau(fields[23]);
                if (!fields[24].isEmpty())
                    balisePocket.setBaliseNum(ByteString.copyFromUtf8(fields[24]));
                if (!fields[25].isEmpty())
                    balisePocket.setStation(ByteString.copyFromUtf8(fields[25]));
                if (!fields[26].isEmpty())
                    balisePocket.setBaliseError(ByteString.copyFromUtf8(fields[26]));
                matp.setBalisePocket(balisePocket);

                // 设置 Signal 数据
                signal = MATPOuterClass.MSignal.newBuilder();
                if (!fields[27].isEmpty())
                    signal.setSignalID(Integer.parseInt(fields[27]));
                if (!fields[28].isEmpty())
                    signal.setSignalName(ByteString.copyFromUtf8(fields[28]));
                if (!fields[29].isEmpty())
                    signal.setStation(ByteString.copyFromUtf8(fields[29]));
                if (!fields[30].isEmpty())
                    signal.setSignalMile(Integer.parseInt(fields[30]));
                if (!fields[31].isEmpty())
                    signal.setDirection(Integer.parseInt(fields[31]));
                if (!fields[32].isEmpty())
                    signal.setLineID(Integer.parseInt(fields[32]));
                if (!fields[33].isEmpty())
                    signal.setLongitude(Float.parseFloat(fields[33]));
                if (!fields[34].isEmpty())
                    signal.setLatitude(Float.parseFloat(fields[34]));
                if (!fields[35].isEmpty())
                    signal.setSignalError(ByteString.copyFromUtf8(fields[35]));
                matp.setSignal(signal);

                // 设置 RunNextSignal 数据
                runNextSignal = MATPOuterClass.MSignal.newBuilder();
                if (!fields[36].isEmpty())
                    runNextSignal.setSignalID(Integer.parseInt(fields[36]));
                if (!fields[37].isEmpty())
                    runNextSignal.setSignalName(ByteString.copyFromUtf8(fields[37]));
                if (!fields[38].isEmpty())
                    runNextSignal.setStation(ByteString.copyFromUtf8(fields[38]));
                if (!fields[39].isEmpty())
                    runNextSignal.setSignalMile(Integer.parseInt(fields[39]));
                if (!fields[40].isEmpty())
                    runNextSignal.setDirection(Integer.parseInt(fields[40]));
                if (!fields[41].isEmpty())
                    runNextSignal.setLineID(Integer.parseInt(fields[41]));
                if (!fields[42].isEmpty())
                    runNextSignal.setLongitude(Float.parseFloat(fields[42]));
                if (!fields[43].isEmpty())
                    runNextSignal.setLatitude(Float.parseFloat(fields[43]));
                matp.setRunNextSignal(runNextSignal);

                // 设置 DriverInfo 数据
                driverInfo = MATPOuterClass.MDriverInfo.newBuilder();
                if (!fields[44].isEmpty())
                    driverInfo.setDriverID(ByteString.copyFromUtf8(fields[44]));
                if (!fields[45].isEmpty())
                    driverInfo.setDriverName(ByteString.copyFromUtf8(fields[45]));
                if (!fields[46].isEmpty())
                    driverInfo.setDriverPhone(ByteString.copyFromUtf8(fields[46]));
                if (!fields[47].isEmpty())
                    driverInfo.setDriverOption(ByteString.copyFromUtf8(fields[47]));
                if (!fields[48].isEmpty())
                    driverInfo.setValidit(ByteString.copyFromUtf8(fields[48]));
                matp.setDriverInfo(driverInfo);

                // 设置 RunDirection
                if (!fields[49].isEmpty())
                    matp.setRunDirection(Integer.parseInt(fields[49]));

                // 设置 UUID
                if (!fields[50].isEmpty())
                    matp.setUUID(ByteString.copyFromUtf8(fields[50]));

                // 获取环境信息
                if (!fields[51].isEmpty())
                    matp.setTemperature(Integer.parseInt(fields[51]));
                if (!fields[52].isEmpty())
                    matp.setRoad(ByteString.copyFromUtf8(fields[52]));

                if (!fields[53].isEmpty())
                    matp.setWeather(ByteString.copyFromUtf8(fields[53]));
                if (!fields[54].isEmpty())
                    matp.setHumidity(Integer.parseInt(fields[54]));

                mTransfCtrl.setData(ByteString.copyFrom(matp.build().toByteArray()));
                mTransfCtrl.setMsgtype(1);
                pub.send(mTransfCtrl.build().toByteArray());

                // read next line
                Thread.sleep(50);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
