
import java.io.IOException;

import migratableProcess.MigratableProcess;
import transactionIO.TransactionalFileInputStream;
import transactionIO.TransactionalFileOutputStream;

/*
 * This class compute the sum of numbers so far in a column and output it to designated file.
 * Args: <src_file> <des_file>
 */
public class Sum extends MigratableProcess {
	private static final long serialVersionUID = -1621753752860221771L;
	private Long lastComputedResult;
	private TransactionalFileInputStream input;
	private TransactionalFileOutputStream output;
	
	public Sum(String[] args) throws IOException {
		super(args);
		input = new TransactionalFileInputStream(args[0], 0L);
		output = new TransactionalFileOutputStream(args[1], 0L);
		lastComputedResult = 0L;
		this.isFinished = false;
		this.isSuspended = false;
	}

	@Override
	public void run() {
		System.out.println("Process Sum start running...");
		isSuspended = false;
		/*try {
			System.out.println("Opening file...");
			input.open();
			output.open();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Cannont open the source/destinate file.");
		}
		*/
		
		//Running the process
		System.out.println("Sum is now Processing...");
		while (!this.isSuspended && !this.isFinished) {
			try {
				String current = input.readLine();
				if (current != null) {
					int current_num = Integer.MAX_VALUE;
					current = current.replaceAll("^[0-9]", "");
					if (!current.equals("")) {
						current_num = Integer.parseInt(current);
					} else {
						continue;
					}
					lastComputedResult += current_num;
					output.writeLine(lastComputedResult.toString());
				} else {
					System.out.println("Sum Process finished.");
					this.isFinished = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.out.println("Invaild number format.");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Can not open file.");
			}
		}
		
		/*System.out.println("Closing files...");
		try {
			System.out.println(lastComputedResult);
			output.flush();
			input.close();
			output.close();
			System.out.println("File closed.");
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("System failure when disconnecting source/desinate file.");
		}*/
	}

	@Override
	public void suspend() {
		this.isSuspended = true;
	}

	@Override
	public String toString() {
		String result = "";
		result = "Source file: " + input.getFilePath() + "\nDesination file: " + output.getFilePath() + "\nCurrent sum: " + lastComputedResult;
		return result;
	}

}
