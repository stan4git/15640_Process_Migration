package migratableProcess;


import slave.SlaveServer;
import master.MasterServer;

public class ProcessManager {
	
	public static void main(String[] args) {
		/* start master server */
		if (args.length == 0) {
			MasterServer ms = new MasterServer();
			try {
				ms.MasterHandler();
			} catch (Exception e) {
				System.err.println("Master Hanlder Started failure!");
			}
			System.out.println("Master Serivce Ended");
			//System.exit(0);
		}
		
		/* start slave server */
		else if (args.length == 2 && args[0].equals("-s")) {
			SlaveServer ss = new SlaveServer();
			try {
				ss.startService(args[1]);
			} catch (Exception e) {
				System.err.println("Slave Hanlder Started failure!");
			}
			System.out.println("Slave Serivce Ended");
		}
		else {
			System.out.println("Usage: java ProcessManager [-s <master's ip>]");
		}
	}
}
