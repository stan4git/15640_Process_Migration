package slave;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import master.MasterMessage;
import migratableProcess.Configuration;
import migratableProcess.MigratableProcess;
import migratableProcess.ProcessStatus;
import migratableProcess.Serialization;

public class SlaveServer {
	public void startService(String masterIP) {
		ListenerThread listener = new ListenerThread(masterIP);
		listener.run();
	}

	private static class ListenerThread implements Runnable {
		Socket serviceSocket;
		ObjectOutputStream msgOutStream;
		ObjectInputStream msgInStream;
		String masterIP;
		ConcurrentHashMap<Integer, MigratableProcess> processMap;
		public ListenerThread(String masterIP) {
			this.masterIP = masterIP;
			this.processMap = new ConcurrentHashMap<Integer, MigratableProcess>();
		}

		@Override
		public void run() {
			while (true) {
				try {
					serviceSocket = new Socket(masterIP, Configuration.PORT);
					System.out.println("Connected to master.");

					msgOutStream = new ObjectOutputStream(serviceSocket.getOutputStream());
					System.out.println("Output stream has been setup.");

					msgInStream = new ObjectInputStream(serviceSocket.getInputStream());
					System.out.println("Input stream has been setup.");
					
					break;
				} catch (IOException e) {
					System.out.println("Cannot connect to master...");
				} 
			}
			
			scanThread st = new scanThread();
			Thread tt = new Thread(st);
			tt.start();
			System.out.print("System is running...");
				
			while (true) { // Waiting for master's command
				MasterMessage input = null;
				try {
						input = (MasterMessage) msgInStream.readObject();
						if (input != null) {
							MigratableProcess migratableProcess;
							switch (input.getOperation()) {
							case START:
								System.out.println("Starting process " + input.getPid());
								migratableProcess = Serialization.deserialize(input.getPid());
								processMap.put(input.getPid(), migratableProcess);
								System.out.println("Process " + input.getPid() + " is running.");
								Thread thread = new Thread(migratableProcess);
								thread.start();
								break;

							case SUSPEND:
								System.out.println("Suspending process " + input.getPid() + "...");
								processMap.get(input.getPid()).suspend();
								/*try {
									System.out.println("Suspending...");
									Thread.sleep(5 * 1000);
								} catch (InterruptedException e1) {
								}*/
								System.out.println("Process " + input.getPid() + " has been suspended.");
								Serialization.serialize(processMap.get(input.getPid()));
								processMap.remove(input.getPid());
								SlaveMessage sm = new SlaveMessage(ProcessStatus.SUSPENDING, input.getPid());
								msgOutStream.writeObject(sm);
								msgOutStream.flush();
								System.out.println("Message sent...");
								break;

							case TERMINATE:
								for (Entry<Integer, MigratableProcess> e : processMap.entrySet()) {
									e.getValue().suspend();
									System.out.println("Process " + e.getKey() + " has been suspended.");
								}
								serviceSocket.close();
								System.out.println("Socket connection closed.");
								System.out.println("Terminating.");
								System.exit(0);

							default:
								System.out.println("Unknown command.");
								break;
							}
						}
				} catch (IOException e) {
				} catch (ClassNotFoundException e) {
				}
			}
		}
		
		private class scanThread implements Runnable {
			@Override
			public void run() {
				while (true) {
					Iterator<Entry<Integer, MigratableProcess>> iter = (Iterator<Entry<Integer, MigratableProcess>>) processMap.entrySet().iterator();
					SlaveMessage doneMsg = new SlaveMessage(ProcessStatus.DONE, Integer.MIN_VALUE);
					while (iter.hasNext()) {
						Entry<Integer, MigratableProcess> process = iter.next();
						if (process.getValue().isFinished()) {
							doneMsg.setPid(process.getKey());
							try {
								msgOutStream.writeObject(doneMsg);
								msgOutStream.flush();
							} catch (IOException e) {
								e.printStackTrace();
								System.out.println("Connection error.");
							}
							processMap.remove(process.getKey());
							System.out.println("Process " + process.getKey() + " is finished. Informing master...");
						}
					}
				}
			}
		}
	}
}	
