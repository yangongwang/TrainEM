package DataCollection;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LogETLInterceptor implements Interceptor {
    public void initialize() {

    }

    public Event intercept(Event event) {
        byte[] body = event.getBody();
        String log = new String(body, Charset.forName("utf-8"));
        if (log.contains("header")) {
            return event;
        }
        return null;
    }

    public List<Event> intercept(List<Event> list) {
        ArrayList<Event> events = new ArrayList<Event>();
        for (Event event : list) {
            if (event != null){
                events.add(event);
            }
        }
        return events;
    }

    public void close() {

    }
    public static class Builder implements Interceptor.Builder{
        @Override
        public Interceptor build() {
            return new LogETLInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}
