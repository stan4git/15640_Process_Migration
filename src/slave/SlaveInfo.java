package slave;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

public class SlaveInfo {
	private String slaveName;
	private InetAddress ipAddress;
	private int port;
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	
	public String toString() {
		return 	"slaveName: " + slaveName +
				"\tipAddress: " + ipAddress.getHostAddress() +
				"\tport: " + port + "\n";
	}	
	
	public String getSlaveName() {
		return slaveName;
	}
	
	public void setSlaveName(String slaveName) {
		this.slaveName = slaveName;
	}
	
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public ObjectInputStream getObjectInputStream() {
		return objectInputStream;
	}
	
	public void setObjectInputStream(ObjectInputStream objectInputStream) {
		this.objectInputStream = objectInputStream;
	}
	
	public ObjectOutputStream getObjectOutputStream() {
		return objectOutputStream;
	}
	
	public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
		this.objectOutputStream = objectOutputStream;
	}
}
