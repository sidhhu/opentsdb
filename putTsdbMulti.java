import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class putTsdbMulti implements Runnable {
	Random r = new Random();
	//private final long countUntil;
	int tag1_v,tag2_v,tag3_v,tag4_v, metrics;
	public static AtomicLong count = new AtomicLong();
	Socket echoSocket = null;
	PrintWriter out = null;
	String host = "ymshbase-01.ops.bf1.yahoo.com";
	int port;
	putTsdbMulti(int metrics, int tag1_v,int tag2_v,int tag3_v,int tag4_v,int port) {
		this.metrics = metrics;
		this.tag1_v = tag1_v;
		this.tag2_v = tag2_v;
		this.tag3_v = tag3_v;
		this.tag4_v = tag4_v;
		this.port = port;

	}

	@Override
	public void run() {
		long time1 = (System.currentTimeMillis()/1000L);
		long st = (System.currentTimeMillis()/1000L);
		long ts = st - 43200;
		long t1;
		int iteration = 0;
		for(long t=time1;t<time1+(3*60*60);t+=60){
			iteration = iteration + 1;
			t1 = System.currentTimeMillis();
			for(int i=(metrics-1)*500;i<metrics*500;i++){ //Number of metrics
				for(int tag1=tag1_v;tag1<=tag1_v+10;tag1++){ // tag1 values
					for(int tag2=tag2_v;tag2<=tag2_v+10;tag2++){ // tag2 values
						for(int tag3=tag3_v;tag3<=tag3_v+10;tag3++){ // tag3 values
							for(int tag4=tag4_v;tag4<=tag4_v+10;tag4++){
								try {
									if (echoSocket == null) {  // Only open the connection if necessary.
										echoSocket = new Socket(host, port);
										System.out.println("*************New connection created************");
										out = new PrintWriter(echoSocket.getOutputStream(), true);
									}
									out.println("put sample.metric"+i+" "+ts+" "+r.nextInt(50)+" tag1="+tag1+" tag2="+tag2+" tag3="+tag3+" tag4="+tag4);

									out.flush();
																		//System.out.println(echoSocket.getOutputStream());


								} catch (UnknownHostException e) {
									System.err.println("Don't know about host: " + host);
									return;
								} catch (IOException e) {
									System.err.println("Couldn't get I/O for the connection to: " + host);
									out.close();  // Only close the connection on errors.
									try {
										echoSocket.close();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									out = null;
									echoSocket = null;
								}
								//System.out.println("put sample.metric"+i+" "+ts+" "+r.nextInt(50)+" tag1="+tag1+" tag2="+tag2+" tag3="+tag3+" tag4="+tag4);
								putTsdbMulti.count.incrementAndGet();
							}
						}
					}
				}
			}
			ts = ts+60;
			java.util.Date logicalTime=new java.util.Date((long)ts*1000);

			        System.out.println("***** 10M datapoints written in "+ (System.currentTimeMillis()-t1) +"ms logicalTime="+logicalTime);
		}

		//System.out.println("countUntil=" +countUntil);
        //System.out.println("***** 10000000 datapoints written in "+ (System.currentTimeMillis()-t1) +"ms  **********");

	}
	public static long getCount() {
		return count.get();
	}
	public static void main(String[] args) throws InterruptedException {
		List<Thread> threads = new ArrayList<Thread>();
		
		Runnable task;
	
		for (int i = 1; i <= Integer.parseInt(args[0]); i++) {
			
			task = new putTsdbMulti(i,1001 + (i*10),2001 + (i*10),3001 + (i*10),4001 + (i*10),9999);
		
			Thread worker = new Thread(task);

			worker.setName(String.valueOf(i));
			worker.start();

			System.out.println("thread started:" +worker.getName());
			//threads.add(worker);
		}
		 
	} 
}
