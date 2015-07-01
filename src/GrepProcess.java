import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import migratableProcess.MigratableProcess;
import transactionIO.TransactionalFileInputStream;
import transactionIO.TransactionalFileOutputStream;

public class GrepProcess extends MigratableProcess
{
	private static final long serialVersionUID = -4962285005365453106L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception
	{
		super(args);
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1],0L);
		outFile = new TransactionalFileOutputStream(args[2], 0L);
	}

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				String line = in.readLine();

				if (line == null) break;
				
				if (line.contains(query)) {
					out.println(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}