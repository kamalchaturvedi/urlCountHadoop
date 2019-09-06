import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.MalformedURLException;

public class UrlCount {

  public static class LinkMapper 
        extends Mapper<Object, Text, Text, IntWritable>{
    private Pattern htmltag = Pattern.compile("<a\\b[^>]*href=\"[^>]*>(.*?)</a>");
    private Pattern link = Pattern.compile("href=\"[^>]*\">");
    private Text url = new Text();
    private final static IntWritable one = new IntWritable(1);
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
        StringTokenizer itr = new StringTokenizer(value.toString());
        while (itr.hasMoreTokens()) {
            try {
                Matcher tagmatch = htmltag.matcher(itr.nextToken());
                while (tagmatch.find()) {
                    Matcher matcher = link.matcher(tagmatch.group());
                    matcher.find();
                    String link = matcher.group().replaceFirst("href=\"", "")
                            .replaceFirst("\">", "")
                            .replaceFirst("\"[\\s]?target=\"[a-zA-Z_0-9]*", "");
                    System.out.println(link);                    
                    if (valid(link)) {
                        url.set(link);
                        context.write(url, one);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean valid(String s) {
        if (s.matches("javascript:.*|mailto:.*")) {
            return false;
        }
        return true;
    }
  }

  public static class UrlReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "url count");
    job.setJarByClass(UrlCount.class);
    job.setMapperClass(LinkMapper.class);
    job.setCombinerClass(UrlReducer.class);
    job.setReducerClass(UrlReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
