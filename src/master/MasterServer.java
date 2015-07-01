package master;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import migratableProcess.Configuration;
import migratableProcess.MigratableProcess;
import migratableProcess.ProcessInfo;
import migratableProcess.ProcessOperation;
import migratableProcess.ProcessStatus;
import migratableProcess.Serialization;
import slave.SlaveInfo;
import slave.SlaveMessage;

public class MasterServer {
	// count registered Slave Numbers
	private int countSlaves = 0;
	// Process ID
	private int pid = 0;
	// Record pid and ProcessInfo
	private Map<Integer, ProcessInfo> mapForProcesses = new ConcurrentHashMap<Integer, ProcessInfo>();
	// Record slaveId and SlaveInfo
	private Map<Integer, SlaveInfo> mapForSlaves = new ConcurrentHashMap<Integer, SlaveInfo>();
	// Record Slave and Process
	//private Map<SlaveInfo, ArrayList<ProcessInfo>> processCurrentStatus = new ConcurrentHashMap<SlaveInfo, ArrayList<ProcessInfo>>();

	public void MasterHandler() throws IOException {
		System.out.println("The Master's handler has been started!");
		// step1: register all the slaves
		RegisterService rs = new RegisterService();
		rs.start();
		// step2: handle all the commands
		System.out.println("Please type your commands here!");
		System.out
				.println("If you do not know the commands, please use the \"help\" command!");
		BufferedReader userInput = new BufferedReader(new InputStreamReader(
				System.in));
		String input;
		String[] arguments;

		while (true) {
			input = userInput.readLine();
			arguments = input.split(" ");

			if (arguments.length == 0) {
				continue;
			}
			// method1: Help command
			else if (arguments.length == 1 && arguments[0].equalsIgnoreCase("help")) {
				System.out.print(helpString);
			}
			// method2: This command is used to terminate all the slaves and the Main Process
			else if (arguments.length == 1 && arguments[0].equalsIgnoreCase("terminated")) {
				CloseAllSlaves();
			}
			// method3: This command is used to list all the slaves info
			else if (arguments.length == 2
					&& arguments[0].equalsIgnoreCase("ls")
					&& arguments[1].equalsIgnoreCase("slaves")) {
				ListSlaves(arguments);
			}
			// method4: This command is used to list all the status of process
			else if (arguments.length == 2
					&& arguments[0].equalsIgnoreCase("ls")
					&& arguments[1].equalsIgnoreCase("process")) {
				ListProcess(arguments);
			}
			// method5: This command is used to start specific process on
			// specific slave
			// The command always like "start ProcessId SlaveId"
			else if (arguments.length == 3 && arguments[0].equalsIgnoreCase("start")) {
				StartProcess(arguments);
			}
			// method6: This command is used to migrate the specific process
			// from one node to another node
			// The command always like
			// "migrate ProcessId SlaveIdSource SlaveIdDestination"
			else if (arguments.length == 4 && arguments[0].equalsIgnoreCase("migrate")) {
				try {
					MigrateProcess(arguments);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return;
				}
			}
			// method7: This command is used to start a new task to initialize
			// the
			// specific process
			// The command always like "task <MigratableProcess> [args...]"
			else if (arguments.length >= 2 && arguments[0].equalsIgnoreCase("task")) {
				NewTask(arguments);
			}

			//Unknown command
			else {
				System.out.print("Unknown command.");
				System.out.println(helpString);
			}
		}
	}

	public void CloseAllSlaves() {
		System.out.println("Closing all Slaves.");
		Iterator<Entry<Integer, SlaveInfo>> iter = mapForSlaves.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, SlaveInfo> entry = (Map.Entry<Integer, SlaveInfo>) iter.next();
			SlaveInfo si = entry.getValue();
			MasterMessage mps = new MasterMessage(ProcessOperation.TERMINATE, Integer.MAX_VALUE);
			try {
				si.getObjectOutputStream().writeObject(mps);
				si.getObjectOutputStream().flush();
			} catch (IOException e) {
				System.out.println("The operation cannot be sent to the" + si.getSlaveName());
				e.printStackTrace();
			}
		}
		System.out.println("Exiting System.");
		System.exit(0);
	}

	public void ListSlaves(String[] arguments) {
		Iterator<Entry<Integer, SlaveInfo>> iter = mapForSlaves.entrySet().iterator();
		System.out.println("===== Begin of slaves list =====\n");
		while (iter.hasNext()) {
			Map.Entry<Integer, SlaveInfo> entry = (Map.Entry<Integer, SlaveInfo>) iter.next();
			SlaveInfo si = entry.getValue();
			System.out.print(si.toString());
		}
		System.out.println("\n===== End of slaves list ======");
	}

	public void ListProcess(String[] arguments) {
		//Iterator<Entry<SlaveInfo, ArrayList<ProcessInfo>>> iter = processCurrentStatus.entrySet().iterator();
		Iterator<Entry<Integer, ProcessInfo>> iter = mapForProcesses.entrySet().iterator();
		System.out.println("===== Begin of process list =====\n");
		/*while (iter.hasNext()) {
			Map.Entry<SlaveInfo, ArrayList<ProcessInfo>> entry = (Map.Entry<SlaveInfo, ArrayList<ProcessInfo>>) iter.next();
			SlaveInfo si = entry.getKey();
			ArrayList<ProcessInfo> list = entry.getValue();
			StringBuilder sb = new StringBuilder();
			for (ProcessInfo processInfo : list) {
				sb.append("ProcessId : " + String.valueOf(processInfo.getPid())
						+ "              Process Status: " + processInfo.getProcessStatus()
						+ "\n");
			}
			System.out.println(si.getSlaveName() + "   contains   " + list.size() + "   Processes");
			System.out.print(sb.toString());
		}*/
		while (iter.hasNext()) {
			Map.Entry<Integer, ProcessInfo> entry = (Map.Entry<Integer, ProcessInfo>) iter.next();
			Integer processId = entry.getKey();
			ProcessInfo pi= entry.getValue();
			StringBuilder sb = new StringBuilder();
			sb.append("ProcessId : " + String.valueOf(processId)
					+ "\tProcess Name: " + pi.getProcessName() + "\tRunning Slave ID: " + pi.getSlaveId()
					+ "\tProcess Status: " + pi.getProcessStatus()
					+ "\n");
			System.out.print(sb.toString());
		}
		System.out.println("\n===== End of process list ======");
	}

	// The command always like "start ProcessId SlaveId"
	public void StartProcess(String[] arguments) {
		int processId = Integer.valueOf(arguments[1]);
		int slaveId = Integer.valueOf(arguments[2]);
		System.out.println("Process ID: #" + processId + "     Slave:" + slaveId);
		if (mapForSlaves.containsKey(slaveId)) {
			if (mapForProcesses.containsKey(processId)) {
				SlaveInfo si = mapForSlaves.get(slaveId);
				MasterMessage mps = new MasterMessage(ProcessOperation.START,
						processId);
				try {
					si.getObjectOutputStream().writeObject(mps);
					si.getObjectOutputStream().flush();
					mapForProcesses.get(processId).setProcessStatus(ProcessStatus.RUNNING);
					mapForProcesses.get(processId).setSlaveId(slaveId);
					/*ArrayList<ProcessInfo> list = null;
					  if (processCurrentStatus.containsKey(si)) {
						list = processCurrentStatus.get(si);
					} else {
						list = new ArrayList<ProcessInfo>();
					}
					ProcessInfo pi = new ProcessInfo();
					pi.setPid(processId);
					pi.setProcessStatus(ProcessStatus.RUNNING);
					list.add(pi);
					processCurrentStatus.put(si, list);*/
				} catch (IOException e) {
					System.out
							.println("The operation cannot be sent to the specific Slave");
					e.printStackTrace();
				}
			} else {
				System.out.println("The Process ID you typed is not existed!");
			}
		} else {
			System.out.println("The Slave ID you typed is not existed!");
		}
	}

	// "migrate ProcessId SlaveIdSource SlaveIdDestination"
	public void MigrateProcess(String[] arguments)
			throws ClassNotFoundException, IOException {
		int processId = Integer.valueOf(arguments[1]);
		int souceId = Integer.valueOf(arguments[2]);
		int desId = Integer.valueOf(arguments[3]);
		if (!mapForProcesses.containsKey(processId)) {
			System.out.println("The Process ID you typed is not existed!");
			return;
		}
		if (!mapForSlaves.containsKey(souceId)) {
			System.out.println("The Souce Slave ID you typed is not existed!");
			return;
		}
		if (!mapForSlaves.containsKey(desId)) {
			System.out
					.println("The Destination Slave ID you typed is not existed!");
			return;
		}
		// step 1: suspend the specific process on the Source Slave
		MasterMessage mps1 = new MasterMessage(ProcessOperation.SUSPEND,
				processId);
		SlaveInfo souceSI = mapForSlaves.get(souceId);
		SlaveInfo desSI = mapForSlaves.get(desId);
		try {
			souceSI.getObjectOutputStream().writeObject(mps1);
			souceSI.getObjectOutputStream().flush();
		} catch (IOException e) {
			System.out
					.println("The operation cannot be sent to the specific Slave when sending the suspend command");
			e.printStackTrace();
		}

		// step 2: start the process on the Destination Slave
		while (true) {
			if(mapForProcesses.get(processId).getProcessStatus().equals(ProcessStatus.SUSPENDING)) {
				break;
			}
		}

		System.out.println("Suspend the process on Souce Slave Server successfully");
		MasterMessage mps2 = new MasterMessage(ProcessOperation.START, processId);
		try {
			desSI.getObjectOutputStream().writeObject(mps2);
			desSI.getObjectOutputStream().flush();
			mapForProcesses.get(processId).setProcessStatus(ProcessStatus.RUNNING);
			mapForProcesses.get(processId).setSlaveId(desId);
			/*ArrayList<ProcessInfo> list2 = null;
			if (processCurrentStatus.containsKey(desSI)) {
				list2 = processCurrentStatus.get(desSI);
			} else {
				list2 = new ArrayList<ProcessInfo>();
			}
			ProcessInfo pi2 = new ProcessInfo();
			pi2.setPid(processId);
			pi2.setProcessStatus(ProcessStatus.RUNNING);
			list2.add(pi2);
			processCurrentStatus.put(souceSI, list2);*/
		} catch (IOException e) {
			System.out.println("The operation cannot be sent to the specific Slave when sending the start command");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void NewTask(String[] arguments) {
		MigratableProcess newProcess = null;
		Class<MigratableProcess> processClass = null;
		String processName = arguments[1];
		try {
			processClass = (Class<MigratableProcess>) (Class.forName(arguments[1]));
			Constructor<MigratableProcess> processConstructor = null;
			processConstructor = (Constructor<MigratableProcess>)(processClass.getConstructor(String[].class));
			Object[] processArgs = Arrays.copyOfRange(arguments, 2, arguments.length);
			newProcess = (MigratableProcess) processConstructor.newInstance((Object) processArgs);			
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find class " + arguments[1]);
			e.printStackTrace();
			return;
		} catch (NoSuchMethodException e) {
			System.out.println("Could not find method " + arguments[1]);
			e.printStackTrace();
			return;
		} catch (SecurityException e) {
			System.out.println("Security Exception when getting constructor!");
			e.printStackTrace();
			return;
		} catch (InstantiationException e) {
			System.out.println("Could not instantiate the object!");
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			System.out.println("Illegal Access Exception!");
			e.printStackTrace();
			return;
		} catch (IllegalArgumentException e) {
			System.out
					.println("Illegal Argument Exception, please check the arguments!");
			e.printStackTrace();
			return;
		} catch (InvocationTargetException e) {
			System.out.println("Invocation Target Exception!");
			e.printStackTrace();
			return;
		}
		newProcess.setPid(pid);
		Serialization.serialize(newProcess);
		ProcessInfo pi = new ProcessInfo();
		pi.setPid(pid);
		pi.setProcessStatus(ProcessStatus.READY);
		pi.process = newProcess;
		pi.setProcessName(processName);
		mapForProcesses.put(pid++, pi);
	}

	private String helpString = "Usage:\n"
			+ "\t1. terminated : Close all the connections among Master and slaves\n"
			+ "\t2. ls slaves : Print all the current slaves' info\n"
			+ "\t3. ls process : Print all the processes and their status\n"
			+ "\t4. start <ProcessId> <SlaveId> : Start the specific process on the specific slave\n"
			+ "\t5. migrate <ProcessId> <SlaveIdSource> <SlaveIdDestination> : Migrate the specific process from source node to des node\n"
			+ "\t6. task <MigratableProcess> [args...] : Start a new task\n";

	// This is a register service in order to register all the slave nodes
	private class RegisterService extends Thread {
		private ServerSocket serverSocket = null;

		public RegisterService() {
			try {
				serverSocket = new ServerSocket(Configuration.PORT);
				System.out.println("ServerSocket setup successfully!");
			} catch (IOException e) {
				System.err.println("ServerSocket setup error!");
				System.exit(-1);
			}
		}

		public void run() {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					System.out.println("Connection established to "
							+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
					ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					SlaveInfo slaveInfo = new SlaveInfo();
					slaveInfo.setSlaveName("Slave" + String.valueOf(countSlaves));
					slaveInfo.setIpAddress(socket.getInetAddress());
					slaveInfo.setPort(socket.getPort());
					slaveInfo.setObjectInputStream(objectInputStream);
					slaveInfo.setObjectOutputStream(objectOutputStream);
					mapForSlaves.put(countSlaves++, slaveInfo);
					System.out.println("\"" + socket.getInetAddress().getHostAddress() + ":" 
								+ socket.getPort() + "\" is registerd as \"" + slaveInfo.getSlaveName() + "\".");
					ListenerService ls = new ListenerService(slaveInfo);
					Thread newThread = new Thread(ls);
					newThread.start();
					System.out.println("The new listener has been built for " + slaveInfo.getSlaveName());
				} catch (IOException e) {
					System.err.println("The connection cannot be build for some unknown reasons!");
					e.printStackTrace();
				}
			}
		}
	}
	
	// build the listener for each slave in order to receive the "Done" Status from any slave node
	private class ListenerService implements Runnable {
		private ObjectInputStream in;
	    private SlaveInfo slaveInfo;
	    
		public ListenerService(SlaveInfo slaveInfo) {
			this.in = slaveInfo.getObjectInputStream();
			this.slaveInfo = slaveInfo;
		}

		@Override
		public void run() {
			Object slaveMsg = null;
			while(true) {
				try {
					slaveMsg = in.readObject();
					SlaveMessage slaveMessage = (SlaveMessage) slaveMsg;
					if (slaveMessage.getProcessStatus().equals(ProcessStatus.DONE)) {
						System.out.println("The #" + slaveMessage.getPid() +" process has been excuted successfully! please check the result!!");
						mapForProcesses.remove(slaveMessage.getPid());
						mapForProcesses.get(slaveMessage.getPid()).setProcessStatus(ProcessStatus.DONE);
						/*ArrayList<ProcessInfo> list = null;
						if (processCurrentStatus.containsKey(slaveInfo)) {
							list = processCurrentStatus.get(slaveInfo);
						} else {
							list = new ArrayList<ProcessInfo>();
						}
						ProcessInfo pi = new ProcessInfo();
						pi.setPid(slaveMessage.getPid());
						pi.setProcessStatus(ProcessStatus.DONE);
						list.add(pi);
						processCurrentStatus.put(slaveInfo, list);*/
						System.out.println("According to the " + slaveInfo.getSlaveName() + "    sending back message");
						System.out.println("The Process ID: #" + slaveMessage.getPid() + "     has finished!");
					} else {
						if(slaveMessage.getProcessStatus().equals(ProcessStatus.SUSPENDING)) {
							int processId = slaveMessage.getPid();
							mapForProcesses.get(processId).setProcessStatus(ProcessStatus.SUSPENDING);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return;
				} catch (EOFException e) {
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
