Lab2-Convert-Wordcount-To-Urlcount
==================================

This lab comprised of the following objectives :
*   Getting used to the Google Cloud environment
*   Provisioning Dataproc clusters
*   Running a already coded mapreduce program (Wordcount) & analyzing its output
*   Developing a new program (similar to Wordcount) in this case, counting the urls

Environment Setup
-----------------

I setup Dataproc cluster in Google Cloud environment with the following specs :
*    1 Master Node : 1 CPU, 3.5 GB RAM
*    2 Worker Nodes : 1 CPU, 3.5 GB RAM

Then, I created a git repository, & connected it to my local using ssh authentication. The same public key I added to Google Metadata, to be able to access git in compute instances. This also involved copying the private key to the compute instances to setup that git repository there.

Running Provided Program
------------------------
SSHing on the master node of the Dataproc cluster & getting all files via git, I built the java runtime jar with the command
<code>make WordCount1.jar</code>
and ran the jar with
<code>hadoop jar WordCount1.jar WordCount1 /user/kach8488/input /user/kach8488/output</code>
Also, running the python code with 
<code>make stream</code>
gives the same output, but takes more time.

Developing UrlMapper & UrlReducer
---------------------------------

I created new files, UrlMapper.py & UrlReducer.py copying contents from Mapper.py & Reducer.py respectively. The only code change was in the UrlMapper, where for each of the word, I check whether it matches the regex 
>   href=\"(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\), ]|(?:%[0-9a-fA-F][0-9a-fA-F]))+\"

When a word would match the regex, I would send that word to stdout for the reducer to aggregate further. Also, I updated the Makefile with a new build parameter (urlstream), to run the hadoop streaming job with the new files. 

Output
------

The output after running the hadoop-streaming job contained 1196 unique URLs. The directory 'stream-output-url' contains the output files.