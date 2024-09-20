package DataCollection;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import dms.MATPOuterClass;
import dms.MTransfCtrlOuterClass;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.zeromq.ZMQ;

import java.util.Properties;

/**
 * 主数据来源：
 * 从zeromq接收到数据，利用protobuf解析，解析之后的数据推动到kafka
 */
public class log2Kafka {
    public static void main(String[] args) throws Exception {
        //创建一个kafka生产者的配置文件
        Properties kafkaProps = new Properties();
        //增加kafka的ip和port
        kafkaProps.put("bootstrap.servers", "slave1:9092, slave2:9092,slave3:9092");
        //指定key和value的序列化
        // kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("key.serializer", StringSerializer.class.getName());
        kafkaProps.put("value.serializer", StringSerializer.class.getName());
        //创建一个kafka的生产者实例
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(kafkaProps);
        //从zmq接收数据发送给kafka
        //创建一个zmq实例
        ZMQ.Context zmq = ZMQ.context(1);
        //创建订阅端
        ZMQ.Socket sub = zmq.socket(ZMQ.SUB);
        //连接ip和端口
        sub.connect("tcp://localhost:5555");
        //subscribe
        sub.subscribe("".getBytes());
        //创建mTransfCtrl对象
        MTransfCtrlOuterClass.MTransfCtrl mTransfCtrl = null;
        //创建Matp对象
        MATPOuterClass.MATP matp = null;
        //接收数据
        while (true) {
            //订阅数据
            byte[] recv = sub.recv();
            //字节数组（数据+protobuf序列化）
            mTransfCtrl = MTransfCtrlOuterClass.MTransfCtrl.parseFrom(recv);
            //判断Msgtype是否存在并且是否为1
            //获取data，判断data是否存在并且data是否为null
            if (mTransfCtrl.hasMsgtype() && mTransfCtrl.getMsgtype() == 1 && mTransfCtrl.hasData() && null != mTransfCtrl.getData()) {
                //创建一个容器存放数据
                StringBuffer line = new StringBuffer();
                ByteString data = mTransfCtrl.getData();
                //反序列化
                matp = MATPOuterClass.MATP.parseFrom(data);
                //获取头数据包内容
                //定义字段
                String MPacketHead_atpType = "";
                String MPacketHead_trainID = "";
                String MPacketHead_trainNum = "";
                String MPacketHead_attachRWBureau = "";
                String MPacketHead_viaRWBureau = "";
                String MPacketHead_crossDayTrainNum = "";
                String MPacketHead_driverID = "";
                if (matp.hasPacketHead() && null != matp.getPacketHead()) {
                    MATPOuterClass.MPacketHead packetHead = matp.getPacketHead();
                    //判断ATPType是否存在并且不为null
                    if (packetHead.hasATPType()) {
                        MPacketHead_atpType = String.valueOf(packetHead.getATPType());
                    }
                    //判断TrainID是否存在并且不为null
                    if (packetHead.hasTrainID()) {
                        MPacketHead_trainID = String.valueOf(packetHead.getTrainID());
                    }
                    //判断TrainNum是否存在并且不为null
                    if (packetHead.hasTrainNum()) {
                        MPacketHead_trainNum = packetHead.getTrainNum().toStringUtf8();
                    }
                    //判断AttachRWBureau是否存在并且不为null
                    if (packetHead.hasAttachRWBureau()) {
                        MPacketHead_attachRWBureau = String.valueOf(packetHead.getAttachRWBureau());
                    }
                    //判断ViaRWBureau是否存在并且不为null
                    if (packetHead.hasViaRWBureau()) {
                        MPacketHead_viaRWBureau = String.valueOf(packetHead.getViaRWBureau());
                    }
                    //判断CrossDayTrainNum是否存在并且不为null
                    if (packetHead.hasCrossDayTrainNum()) {
                        MPacketHead_crossDayTrainNum = String.valueOf(packetHead.getCrossDayTrainNum());
                    }
                    //判断DriverID是否存在并且不为null
                    if (packetHead.hasDriverID()) {
                        MPacketHead_driverID = packetHead.getDriverID().toStringUtf8();
                    }
                }
                //获取MATPBaseInfo 数据
                String MATPBaseInfo_DataTime = "";
                String MATPBaseInfo_Speed = "";
                String MATPBaseInfo_Level = "";
                String MATPBaseInfo_Mileage = "";
                String MATPBaseInfo_Braking = "";
                String MATPBaseInfo_EmergentBrakSpd = "";
                String MATPBaseInfo_CommonBrakSpd = "";
                String MATPBaseInfo_RunDistance = "";
                String MATPBaseInfo_Direction = "";
                String MATPBaseInfo_LineID = "";
                String MATPBaseInfo_AtpError = "";
                if (matp.hasATPBaseInfo() && null != matp.getATPBaseInfo()) {
                    MATPOuterClass.MATPBaseInfo atpBaseInfo = matp.getATPBaseInfo();
                    //判断DataTime是否存在并且不为null
                    if (atpBaseInfo.hasDataTime()) {
                        MATPBaseInfo_DataTime = String.valueOf(atpBaseInfo.getDataTime());
                    }
                    //判断Speed是否存在并且不为null
                    if (atpBaseInfo.hasSpeed()) {
                        MATPBaseInfo_Speed = String.valueOf(atpBaseInfo.getSpeed());
                    }
                    //判断Level否存在并且不为null
                    if (atpBaseInfo.hasLevel()) {
                        MATPBaseInfo_Level = String.valueOf(atpBaseInfo.getLevel());
                    }
                    //判断Mileage是否存在并且不为null
                    if (atpBaseInfo.hasMileage()) {
                        MATPBaseInfo_Mileage = String.valueOf(atpBaseInfo.getMileage());
                    }
                    //判断Braking是否存在并且不为null
                    if (atpBaseInfo.hasBraking()) {
                        MATPBaseInfo_Braking = String.valueOf(atpBaseInfo.getBraking());
                    }
                    //判断EmergentBrakSpd是否存在并且不为null
                    if (atpBaseInfo.hasEmergentBrakSpd()) {
                        MATPBaseInfo_EmergentBrakSpd = String.valueOf(atpBaseInfo.getEmergentBrakSpd());
                    }
                    //判断CommonBrakSpd是否存在并且不为null
                    if (atpBaseInfo.hasCommonBrakSpd()) {
                        MATPBaseInfo_CommonBrakSpd = String.valueOf(atpBaseInfo.getCommonBrakSpd());
                    }
                    //判断RunDistance是否存在并且不为null
                    if (atpBaseInfo.hasRunDistance()) {
                        MATPBaseInfo_RunDistance = String.valueOf(atpBaseInfo.getRunDistance());
                    }
                    //判断Direction是否存在并且不为null
                    if (atpBaseInfo.hasDirection()) {
                        MATPBaseInfo_Direction = String.valueOf(atpBaseInfo.getDirection());
                    }
                    //判断LineID是否存在并且不为null
                    if (atpBaseInfo.hasLineID()) {
                        MATPBaseInfo_LineID = String.valueOf(atpBaseInfo.getLineID());
                    }
                    //判断AtpError是否存在并且不为null
                    if (atpBaseInfo.hasATPError()) {
                        MATPBaseInfo_AtpError = atpBaseInfo.getATPError().toStringUtf8();
                    }
                }

                //获取MBalisePocket 数据
                String MBalisePocket_BaliseID = "";
                String MBalisePocket_BaliseMile = "";
                String MBalisePocket_BaliseType = "";
                String MBalisePocket_Direction = "";
                String MBalisePocket_LineID = "";
                String MBalisePocket_AttachRWBureau = "";
                String MBalisePocket_BaliseNum = "";
                String MBalisePocket_Station = "";
                String MBalisePocket_BaliseError = "";
                if (matp.hasBalisePocket() && null != matp.getBalisePocket()) {
                    MATPOuterClass.MBalisePocket balisePocket = matp.getBalisePocket();
                    //判断BaliseID是否存在并且不为null
                    if (balisePocket.hasBaliseID()) {
                        MBalisePocket_BaliseID = String.valueOf(balisePocket.getBaliseID());
                    }
                    //判断BaliseMile是否存在并且不为null
                    if (balisePocket.hasBaliseMile()) {
                        MBalisePocket_BaliseMile = String.valueOf(balisePocket.getBaliseMile());
                    }
                    //判断BaliseType否存在并且不为null
                    if (balisePocket.hasBaliseType()) {
                        MBalisePocket_BaliseType = String.valueOf(balisePocket.getBaliseType());
                    }
                    //判断Direction是否存在并且不为null
                    if (balisePocket.hasDirection()) {
                        MBalisePocket_Direction = String.valueOf(balisePocket.getDirection());
                    }
                    //判断LineID是否存在并且不为null
                    if (balisePocket.hasLineID()) {
                        MBalisePocket_LineID = String.valueOf(balisePocket.getLineID());
                    }
                    //判断AttachRWBureau是否存在并且不为null
                    if (balisePocket.hasAttachRWBureau()) {
                        MBalisePocket_AttachRWBureau = String.valueOf(balisePocket.getAttachRWBureau());
                    }
                    //判断BaliseNum是否存在并且不为null
                    if (balisePocket.hasBaliseNum()) {
                        MBalisePocket_BaliseNum = balisePocket.getBaliseNum().toStringUtf8();
                    }
                    //判断Station是否存在并且不为null
                    if (balisePocket.hasStation()) {
                        MBalisePocket_Station = balisePocket.getStation().toStringUtf8();
                    }
                    //判断BaliseError是否存在并且不为null
                    if (balisePocket.hasBaliseError()) {
                        MBalisePocket_BaliseError = balisePocket.getBaliseError().toStringUtf8();
                    }
                }
                //获取Signal 数据
                String Signal_SignalID = "";
                String Signal_SignalName = "";
                String Signal_Station = "";
                String Signal_SignalMile = "";
                String Signal_Direction = "";
                String Signal_LineID = "";
                String Signal_Longitude = "";
                String Signal_Latitude = "";
                String Signal_SignalError = "";
                if (matp.hasSignal() && null != matp.getSignal()) {
                    MATPOuterClass.MSignal signal = matp.getSignal();
                    //判断SignalID是否存在并且不为null
                    if (signal.hasSignalID()) {
                        Signal_SignalID = String.valueOf(signal.getSignalID());
                    }
                    //判断SignalName是否存在并且不为null
                    if (signal.hasSignalName()) {
                        Signal_SignalName = signal.getSignalName().toStringUtf8();
                    }
                    //判断Station否存在并且不为null
                    if (signal.hasStation()) {
                        Signal_Station = signal.getStation().toStringUtf8();
                    }
                    //判断ignalMile是否存在并且不为null
                    if (signal.hasSignalMile()) {
                        Signal_SignalMile = String.valueOf(signal.getSignalMile());
                    }
                    //判断Direction否存在并且不为null
                    if (signal.hasDirection()) {
                        Signal_Direction = String.valueOf(signal.getDirection());
                    }
                    //判断LineID是否存在并且不为null
                    if (signal.hasLineID()) {
                        Signal_LineID = String.valueOf(signal.getLineID());
                    }
                    //判断Longitude是否存在并且不为null
                    if (signal.hasLongitude()) {
                        Signal_Longitude = String.valueOf(signal.getLongitude());
                    }
                    //判断Latitude是否存在并且不为null
                    if (signal.hasLatitude()) {
                        Signal_Latitude = String.valueOf(signal.getLatitude());
                    }
                    //判断SignalError是否存在并且不为null
                    if (signal.hasSignalError()) {
                        Signal_SignalError = signal.getSignalError().toStringUtf8();
                    }
                }

                //获取RunNextSignal 数据
                String RunNextSignal_SignalID = "";
                String RunNextSignal_SignalName = "";
                String RunNextSignal_Station = "";
                String RunNextSignal_SignalMile = "";
                String RunNextSignal_Direction = "";
                String RunNextSignal_LineID = "";
                String RunNextSignal_Longitude = "";
                String RunNextSignal_Latitude = "";
                if (matp.hasRunNextSignal() && null != matp.getRunNextSignal()) {
                    MATPOuterClass.MSignal signal = matp.getRunNextSignal();
                    //判断SignalID是否存在并且不为null
                    if (signal.hasSignalID()) {
                        RunNextSignal_SignalID = String.valueOf(signal.getSignalID());
                    }
                    //判断SignalName是否存在并且不为null
                    if (signal.hasSignalName()) {
                        RunNextSignal_SignalName = signal.getSignalName().toStringUtf8();
                    }
                    //判断Station否存在并且不为null
                    if (signal.hasStation()) {
                        RunNextSignal_Station = signal.getStation().toStringUtf8();
                    }
                    //判断ignalMile是否存在并且不为null
                    if (signal.hasSignalMile()) {
                        RunNextSignal_SignalMile = String.valueOf(signal.getSignalMile());
                    }
                    //判断Direction否存在并且不为null
                    if (signal.hasDirection()) {
                        RunNextSignal_Direction = String.valueOf(signal.getDirection());
                    }
                    //判断LineID是否存在并且不为null
                    if (signal.hasLineID()) {
                        RunNextSignal_LineID = String.valueOf(signal.getLineID());
                    }
                    //判断Longitude是否存在并且不为null
                    if (signal.hasLongitude()) {
                        RunNextSignal_Longitude = String.valueOf(signal.getLongitude());
                    }
                    //判断Latitude是否存在并且不为null
                    if (signal.hasLatitude()) {
                        RunNextSignal_Latitude = String.valueOf(signal.getLatitude());
                    }
                }

                //获取DriverInfo 数据
                String DriverInfo_DriverID = "";
                String DriverInfo_DriverName = "";
                String DriverInfo_DriverPhone = "";
                String DriverInfo_DriverOption = "";
                String DriverInfo_Validit = "";
                if (matp.hasDriverInfo() && null != matp.getDriverInfo()) {
                    MATPOuterClass.MDriverInfo driverInfo = matp.getDriverInfo();
                    //判断DriverID是否存在并且不为null
                    if (driverInfo.hasDriverID()) {
                        DriverInfo_DriverID = driverInfo.getDriverID().toStringUtf8();
                    }
                    //判断DriverName是否存在并且不为null
                    if (driverInfo.hasDriverName()) {
                        DriverInfo_DriverName = driverInfo.getDriverName().toStringUtf8();
                    }
                    //判断DriverPhone否存在并且不为null
                    if (driverInfo.hasDriverPhone()) {
                        DriverInfo_DriverPhone = driverInfo.getDriverPhone().toStringUtf8();
                    }
                    //判断DriverOption是否存在并且不为null
                    if (driverInfo.hasDriverOption()) {
                        DriverInfo_DriverOption = driverInfo.getDriverOption().toStringUtf8();
                    }
                    //判断Validit否存在并且不为null
                    if (driverInfo.hasValidit()) {
                        DriverInfo_Validit = driverInfo.getValidit().toStringUtf8();
                    }
                }

                //获取RunDirection
                String RunDirection = "";
                if (matp.hasRunDirection()) {
                    RunDirection = String.valueOf(matp.getRunDirection());
                }

                //获取UUID
                String UUID = "";
                if (matp.hasUUID() && null != matp.getUUID()) {
                    UUID = matp.getUUID().toStringUtf8();
                }

                //获取环境信息
                String Temperature = "";
                String Road = "";
                if (matp.hasTemperature()) {
                    Temperature = String.valueOf(matp.getTemperature());
                }
                if (matp.hasRoad()) {
                    Road = matp.getRoad().toStringUtf8();
                }
                // String wea = MakeWeather.getWea(String.valueOf(RunNextSignal_Longitude), String.valueOf(RunNextSignal_Latitude));
                String wea = matp.getWeather().toStringUtf8();

                //拼接字符串
                line.append(MPacketHead_atpType).append("|")
                        .append(MPacketHead_trainID).append("|")
                        .append(MPacketHead_trainNum).append("|")
                        .append(MPacketHead_attachRWBureau).append("|")
                        .append(MPacketHead_viaRWBureau).append("|")
                        .append(MPacketHead_crossDayTrainNum).append("|")
                        .append(MPacketHead_driverID).append("|")
                        .append(MATPBaseInfo_DataTime).append("|")
                        .append(MATPBaseInfo_Speed).append("|")
                        .append(MATPBaseInfo_Level).append("|")
                        .append(MATPBaseInfo_Mileage).append("|")
                        .append(MATPBaseInfo_Braking).append("|")
                        .append(MATPBaseInfo_EmergentBrakSpd).append("|")
                        .append(MATPBaseInfo_CommonBrakSpd).append("|")
                        .append(MATPBaseInfo_RunDistance).append("|")
                        .append(MATPBaseInfo_Direction).append("|")
                        .append(MATPBaseInfo_LineID).append("|")
                        .append(MATPBaseInfo_AtpError).append("|")
                        .append(MBalisePocket_BaliseID).append("|")
                        .append(MBalisePocket_BaliseMile).append("|")
                        .append(MBalisePocket_BaliseType).append("|")
                        .append(MBalisePocket_Direction).append("|")
                        .append(MBalisePocket_LineID).append("|")
                        .append(MBalisePocket_AttachRWBureau).append("|")
                        .append(MBalisePocket_BaliseNum).append("|")
                        .append(MBalisePocket_Station).append("|")
                        .append(MBalisePocket_BaliseError).append("|")
                        .append(Signal_SignalID).append("|")
                        .append(Signal_SignalName).append("|")
                        .append(Signal_Station).append("|")
                        .append(Signal_SignalMile).append("|")
                        .append(Signal_Direction).append("|")
                        .append(Signal_LineID).append("|")
                        .append(Signal_Longitude).append("|")
                        .append(Signal_Latitude).append("|")
                        .append(Signal_SignalError).append("|")
                        .append(RunNextSignal_SignalID).append("|")
                        .append(RunNextSignal_SignalName).append("|")
                        .append(RunNextSignal_Station).append("|")
                        .append(RunNextSignal_SignalMile).append("|")
                        .append(RunNextSignal_Direction).append("|")
                        .append(RunNextSignal_LineID).append("|")
                        .append(RunNextSignal_Longitude).append("|")
                        .append(RunNextSignal_Latitude).append("|")
                        .append(DriverInfo_DriverID).append("|")
                        .append(DriverInfo_DriverName).append("|")
                        .append(DriverInfo_DriverPhone).append("|")
                        .append(DriverInfo_DriverOption).append("|")
                        .append(DriverInfo_Validit).append("|")
                        .append(RunDirection).append("|")
                        .append(UUID).append("|")
                        .append(Temperature).append("|")
                        .append(Road).append("|")
                        .append(wea).append("|")
                        .append(matp.getHumidity());

                //将数据发送到指定kafkad额主题
                producer.send(new ProducerRecord<String, String>("t_traindata", line.toString()));
            }
        }
    }
}
