package transactionIO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable {
	private static final long serialVersionUID = -6592437451721610258L;
	private transient RandomAccessFile file;
	private Long cursorPosition;
	private String filePath;

	public TransactionalFileOutputStream(String filePath, Long cursorPosition) throws IOException {
		this.filePath = filePath;
		this.cursorPosition = cursorPosition;
	}

	public void open() throws IOException {
		File fileObj = new File(filePath);
		if (!fileObj.exists()) {
			fileObj.createNewFile();
		}
		file = new RandomAccessFile(fileObj, "rw");
		file.seek(this.cursorPosition);
	}
	
	@Override
	public void write(int arg0) throws IOException {
		open();
		file.write(arg0);
		cursorPosition = file.getFilePointer();
		close();
	}

	public void writeLine(String str) throws IOException {
		open();
		this.file.writeChars(str);
		this.file.writeChar('\n');
		cursorPosition = file.getFilePointer();
		close();
	}
	public void close() throws IOException {
		this.file.close();
	}

	public Long getCursorPosition() {
		return cursorPosition;
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
