package beans

case class Logs(
                 //300T|3001003|C2048|哈|上|FALSE|D001035|20150109102323|11||12||||12|1|9001|当前ATP系统处于故|1201|1505|无源|1|9001|上|B1505|北京南站|道岔|1345|S1345|北京南站|1505|1|9001|116.46|39.92|电源|1345|S1345|北京南站|1505|1|9001|116.46|39.92|D001035||||TRUE|1|{ef348a64-06eb-e14f-8b4f-d08dc07d2105}|15||雪|90
                 //300T|3001003|C2049|沈|昌|FALSE|D001035|20150109102324|12||13||||13|1|9002|车载主机|1202|1505|无源|1|9002|昌|B1505|北京南站|道岔|1345|S1345|北京南站|1505|1|9002|116.46|39.92|电源|1345|S1345|北京南站|1505|1|9002|116.46|39.92|D001035||||TRUE|1|{ef348a64-06eb-e14f-8b4f-d08dc07d2106}|15||雨|90
                 val MPacketHead_ATPType: String,
                 val MPacketHead_TrainID: String,
                 val MPacketHead_TrainNum: String,
                 val MPacketHead_AttachRWBureau: String,
                 val MPacketHead_ViaRWBureau: String,
                 val MPacketHead_CrossDayTrainNum: String,
                 val MPacketHead_DriverID: String,
                 val MATPBaseInfo_DataTime: String,
                 val MATPBaseInfo_Speed: String,
                 val MATPBaseInfo_Level: String,
                 val MATPBaseInfo_Mileage: String,
                 val MATPBaseInfo_Braking: String,
                 val MATPBaseInfo_EmergentBrakSpd: String,
                 val MATPBaseInfo_CommonBrakSpd: String,
                 val MATPBaseInfo_RunDistance: String,
                 val MATPBaseInfo_Direction: String,
                 val MATPBaseInfo_LineID: String,
                 val MATPBaseInfo_AtpError: String,
                 val MBalisePocket_BaliseID: String,
                 val MBalisePocket_BaliseMile: String,
                 val MBalisePocket_BaliseType: String,
                 val MBalisePocket_Direction: String,
                 val MBalisePocket_LineID: String,
                 val MBalisePocket_AttachRWBureau: String,
                 val MBalisePocket_BaliseNum: String,
                 val MBalisePocket_Station: String,
                 val MBalisePocket_BaliseError: String,
                 val Signal_SignalID: String,
                 val Signal_SignalName: String,
                 val Signal_Station: String,
                 val Signal_SignalMile: String,
                 val Signal_Direction: String,
                 val Signal_LineID: String,
                 val Signal_Longitude: String,
                 val Signal_Latitude: String,
                 val Signal_SignalError: String,
                 val RunNextSignal_SignalID: String,
                 val RunNextSignal_SignalName: String,
                 val RunNextSignal_Station: String,
                 val RunNextSignal_SignalMile: String,
                 val RunNextSignal_Direction: String,
                 val RunNextSignal_LineID: String,
                 val RunNextSignal_Longitude: String,
                 val RunNextSignal_Latitude: String,
                 val DriverInfo_DriverID: String,
                 val DriverInfo_DriverName: String,
                 val DriverInfo_DriverPhone: String,
                 val DriverInfo_DriverOption: String,
                 val DriverInfo_Validit: String,
                 val RunDirection: String,
                 val UUID: String,
                 val Temperature: String,
                 val Road: String,
                 val Weather: String,
                 val Humidity: String
               )
