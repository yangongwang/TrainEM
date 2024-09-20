package DataCollection;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class MySqlSource extends AbstractSource implements Configurable, PollableSource {
    private SQLSourceHelper sqlSourceHelper;

    public void configure(Context context) {
        try {
            sqlSourceHelper = new SQLSourceHelper(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Status process() throws EventDeliveryException {
        List<List<Object>> result = sqlSourceHelper.executeQuery();
        ArrayList<Event> events = new ArrayList<Event>();
        if (!result.isEmpty()) {
            List<String> allRows = sqlSourceHelper.getAllRows(result);
            Event event = null;
            for (String row : allRows) {
                event = new SimpleEvent();
                event.setBody(row.getBytes());
                events.add(event);
            }
            this.getChannelProcessor().processEventBatch(events);
            sqlSourceHelper.updateOffset2DB(result.size());
        }
        try {
            Thread.sleep(sqlSourceHelper.getRunQueryDelay());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public synchronized void stop() {
        sqlSourceHelper.close();
        super.stop();
    }
}
