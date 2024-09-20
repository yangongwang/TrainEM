package DataCollection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * 自定义source，用来保证不重复消费
 */

//要继承抽象的AbstractSource类，然后要实现 Configurable, EventDrivenSource接口
public class MySource extends AbstractSource implements EventDrivenSource, Configurable {

    /*监听的文件*/
    private String filePath;
    /*记录读取偏移量的文件*/
    private String posiFile;
    /*若读取文件暂无内容，则等待数秒*/
    private Long interval;
    /*读写文件的字符集*/
    private String charset;

    /*读取文件内容的线程*/
    private FileRunner fileRunner;
    /*线程池*/
    private ExecutorService executor;

    private static final Logger logger = LoggerFactory.getLogger(MySource.class);


    @Override
    public void configure(Context context) {
        filePath = context.getString("filepath");
        posiFile = context.getString("offsetpath");
        interval = context.getLong("interval", 2000L);
        charset = context.getString("charset", "UTF-8");
    }

    @Override
    public synchronized void start() {
        //启动一个线程，用于监听对应的日志文件
        //创建一个线程池
        executor = Executors.newSingleThreadExecutor();
        //用channelProcessor发送数据给channel
        ChannelProcessor channelProcessor = super.getChannelProcessor();
        fileRunner = new FileRunner(filePath, posiFile, interval, charset, channelProcessor);
        executor.submit(fileRunner);
        super.start();
    }

    @Override
    public synchronized void stop() {
        fileRunner.setFlag(Boolean.FALSE);
        while (!executor.isTerminated()) {
            logger.debug("waiting for exec executor service to stop");
            try {
                executor.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.debug("Interrupted while waiting for executor service to stop,Just exiting.");
                Thread.currentThread().interrupt();
            }
        }
        super.stop();
    }


    public static class FileRunner implements Runnable {
        private Long interval;

        private String charset;

        private Long offset = 0L;

        private File pFile;

        private RandomAccessFile raf;

        private ChannelProcessor channelProcessor;

        private Boolean flag = Boolean.TRUE;

        public void setFlag(Boolean flag) {
            this.flag = flag;
        }

        public FileRunner(String filePath, String posiFile, Long interval, String charset, ChannelProcessor channelProcessor) {
            this.interval = interval;
            this.charset = charset;
            this.channelProcessor = channelProcessor;

            //1、判断是否有偏移量文件，有则读取偏移量,没有则创建
            pFile = new File(posiFile);
            if (!pFile.exists()) {
                try {
                    pFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("create position file error!", e);
                }
            }
            //2、判断偏移量中的文件内容是否大于0
            try {
                String offsetStr = FileUtils.readFileToString(pFile, this.charset);
//          3、如果偏移量文件中有记录，则将内容转换为Long
                if (StringUtils.isNotBlank(offsetStr)) {
                    offset = Long.parseLong(offsetStr);
                }
//           4、如果有偏移量，则直接跳到文件的偏移量位置
                raf = new RandomAccessFile(filePath, "r");
//              跳到指定的位置
                raf.seek(offset);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("read position file error!", e);
            }
        }

        @Override
        public void run() {
            //监听文件
            while (flag) {
//            读取文件中的内容
                String line = null;
                try {
                    line = raf.readLine();
                    if (StringUtils.isNotBlank(line)) {
//                      把数据打包成Event,发送到Channel
                        line = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                        Event event = EventBuilder.withBody(line.getBytes());
                        channelProcessor.processEvent(event);
                        //更新偏移量文件，把偏移量写入文件
                        offset = raf.getFilePointer();
                        FileUtils.writeStringToFile(pFile, offset.toString());
                    } else {
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            logger.error("thread sleep error", e);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

