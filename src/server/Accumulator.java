package server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;

import api.Task;

public class Accumulator {

	private static Map<Integer, Queue<Task>> putQueue = new LinkedHashMap<>();
	
	private static Map<Integer, Queue<Task>> getQueue = new LinkedHashMap<>();	
	
	public static void addToPutQueue(Task task){
		
	}
	
	public static void addToGetQueue(Task task){
		
	}
	
	class Scheduler extends Thread{
		
		public void run(){
			while(true){
				// Check to see which bucket's task queue can be removed and executed.
				
			}
		}
	}
	
}
