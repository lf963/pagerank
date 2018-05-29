# PageRank

Utilize MapReduce on Hadoop to implement page rank
Use adjacency matrix to construct relationship between websites
Utilize convergence of page rank to rank all websites

## Data Source
https://www.limfinity.com/ir/

## How to Run
Run hadoop
```
hdfs dfs -rm -r /transition 
#remove /transition directory in HDFS
#if /transition doesn't exist, just ignore this command

hdfs dfs -mkdir /transition
hdfs dfs -put transitionsmall.txt /transition
#At first, we try small data set, if it works, we try larger data set(transition.txt)

hdfs dfs -rm -r /output*
hdfs dfs -rm -r /pagerank*
hdfs dfs -mkdir /pagerank0
hdfs dfs -put prsmall.txt /pagerank0
#At first, we try small data set, if it works, we try larger data set(pr.txt)

hadoop com.sun.tools.javac.Main *.java #compile
jar cf pr.jar *.class #put all class into jar
hadoop jar pr.jar Driver /transition /pagerank /output 1 #run
//args0: dir of transition.txt
//args1: dir of PageRank.txt
//args2: dir of unitMultiplication result
//args3: times of convergence（make sure the code run successfully when args3=1, then test
args3=40）
```

## Result
The output of MapReduce will be in /pagerankN where N=args3.
We can use ```hdfs dfs -cat /pagerankN/*``` to check the output.

We can use ```hdfs dfs -get /pagerankN/part-r-00000 prN.txt``` to download out output from HDFS to our local computer.

We can use ```Helper.java``` to convert our output to CSV file.

In ```Helper.java```, modify the following three lines:
```
BufferedReader pr = new BufferedReader(new FileReader("path_to_the_final_prN.txt"));
BufferedReader transition = new BufferedReader(new FileReader("path_to_transition.txt"));
FileWriter fileWriter = new FileWriter("path_to_where_you_want_to_save_result.cvs");
```
and then run ```Helper.java```