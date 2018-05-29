import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnitMultiplication {

    public static class TransitionMapper extends Mapper<Object, Text, Text, Text> {

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //input format: fromPage\t toPage1,toPage2,toPage3
            //target: build transition matrix unit -> fromPage\t toPage=probability

            String line = value.toString().trim();
            String[] to_from = line.split("\t");


            //this link does not point to any other websites: dead end
            if(to_from.length == 1)
            	return;

            String from = to_from[0];

            //1\t2,9,8,27
            String[] tos = to_from[1].split(",");
            for(String to : tos)
            	context.write(new Text(from), new Text(to + "=" + (double)1/tos.length));
        }
    }

    public static class PRMapper extends Mapper<Object, Text, Text, Text> {

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //input format: Page\t PageRank
            //target: write to reducer
            //read pr0.txt
            //outputKey = id
            //outputValue = pr0
            //1\t1

            String[] id_pr = value.toString().trim().split("\t");
            context.write(new Text(id_pr[0]), new Text(id_pr[1]));

        }
    }

    public static class MultiplicationReducer extends Reducer<Text, Text, Text, Text> {


        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            //input key = fromPage value=<toPage=probability..., pageRank>
            //target: get the unit multiplication

            //key: fromId = 1
            //value = <2=1/3, 8=1/3, 27=1/3, 1>
            //iterate list -> pr0*prob -> outputValue(subPr)
            //outputKey = toId
            double pr = 0;
            List<String> cells = new ArrayList<>();
            for(Text line : values){
            	String value = line.toString();
            	if(value.contains("="))
            		cells.add(value);
            	else
            		pr = Double.valueOf(value);
            }

            for(String cell : cells){
            	String to = cell.split("=")[0];
            	String relation = String.valueOf(Double.valueOf(cell.split("=")[1]) * pr);
            	context.write(new Text(to), new Text(relation));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);
        job.setJarByClass(UnitMultiplication.class);

        //chain two mapper classes

        job.setReducerClass(MultiplicationReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

		//set the input of TransitionMapper and PRMapper
        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, TransitionMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, PRMapper.class);

        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.waitForCompletion(true);
    }

}
