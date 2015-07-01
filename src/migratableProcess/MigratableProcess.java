package migratableProcess;

import java.io.Serializable;

public abstract class MigratableProcess implements Runnable, Serializable {

	private static final long serialVersionUID = -1737903831354763210L;
	protected int pid;
	protected boolean isFinished;
	protected boolean isSuspended;
	
    public MigratableProcess(String[] args) {
    }

	@Override
    public abstract void run();

    public abstract void suspend();

    public abstract String toString();

    
    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public boolean isSuspended() {
		return isSuspended;
	}

	public void setSuspended(boolean isSuspended) {
		this.isSuspended = isSuspended;
	}
}
