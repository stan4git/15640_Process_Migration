package master;

import java.io.Serializable;
import migratableProcess.ProcessOperation;

public class MasterMessage implements Serializable{

	private static final long serialVersionUID = 4354861370207662562L;
	
	private ProcessOperation operation;// including start, suspend, terminate
	private int pid; //Process ID
	
	public MasterMessage(ProcessOperation operation, int pid) {
		this.operation = operation;
		this.pid = pid;
	}
	

    public ProcessOperation getOperation() {
		return operation;
	}

	public int getPid() {
        return pid;
    }

}
