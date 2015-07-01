package transactionIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/*
 * This stream will be stored together with the process information
 * 
 */
public class TransactionalFileInputStream extends InputStream implements Serializable {
	private static final long serialVersionUID = -936312110005156454L;
	private transient RandomAccessFile file;
	private Long cursorPosition;
	private String filePath;
	
	public TransactionalFileInputStream(String filePath, Long cursorPosition) {
		this.filePath = filePath;
		this.cursorPosition = cursorPosition;
	}
	
	public void open() throws IOException {
		this.file = new RandomAccessFile(this.filePath, "r");
		file.seek(this.cursorPosition);
	}
	
	@Override
	public int read() throws IOException {
		byte data = -1;
		open();
		data = file.readByte();
		close();
		if (data != -1) {
			this.cursorPosition++;
		}
		return data;
	}

	public String readLine() throws IOException {
		open();
		String ret = file.readLine();
		this.cursorPosition = file.getFilePointer();
		close();
		return ret;
	}
	
	public void close() throws IOException {
		file.close();
	}
	
	public boolean hasNext() throws IOException {
		return file.length() >= cursorPosition;
	}
	
	public Long getCursorPosition() {
		return cursorPosition;
	}
	
	public Long getFileSize() throws IOException {
		return this.file.length();
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setCursorPosition(Long cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
