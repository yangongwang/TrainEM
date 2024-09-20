package DataCollection;

import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;

public class MySourceForFile extends AbstractSource implements Configurable, PollableSource {

    //定义成员属性
    private String filepath;
    private String offsetpath;
    private String charset;
    private Long interval;


    @Override
    public Status process() throws EventDeliveryException {
        return null;
    }

    @Override
    public long getBackOffSleepIncrement() {
        return 0;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        return 0;
    }

    @Override
    public void configure(Context context) {
        //加载原始日志文件
        filepath = context.getString("filepath");
        //加载偏移量文件
        offsetpath = context.getString("offsetpath");
        //记载编码格式
        charset = context.getString("charset", "UTF-8");
        //加载睡眠时间
        interval = context.getLong("interval", 5000L);
    }
}
