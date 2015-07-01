
import java.io.EOFException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import transactionIO.TransactionalFileInputStream;
import transactionIO.TransactionalFileOutputStream;
import migratableProcess.MigratableProcess;


public class FindHotWords extends MigratableProcess{

	private static final long serialVersionUID = -5113884218671704607L;
	
	private TransactionalFileInputStream input;
	private TransactionalFileOutputStream output;
	private Long threshold;
	private Map<String, Long> wordCount;
	private HashSet<String> stopwords;
	
	public FindHotWords(String[] args) throws Exception {
		super(args);
		if (args.length != 3 || !isaNum(args[2])) {
			System.out.println("usage: FindHotWords <inputfile> <outputfile> <threshold>");
			System.out.println("\tthe final parameter must be a number!");
			throw new Exception("Invalid arguments");
		}
		wordCount = new ConcurrentHashMap<String, Long>();
		StopWords sw = new StopWords();
		stopwords = sw.stopwords;
		input = new TransactionalFileInputStream(args[0], 0L);
		output = new TransactionalFileOutputStream(args[1], 0L);
		threshold = Long.parseLong(args[2]);
		isFinished = false;
		isSuspended = false;
	}

	@Override
	public void run() {
		System.out.println("Process FindHotWords start running...");
		isSuspended = false;
		/*try {
			System.out.println("Opening the source and dest file...");
			input.open();
			output.open();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("Cannont open the source/dest file.");
		}*/
		
		//Running the process
		System.out.println("FindHotWords is now Processing...");
		while (!this.isSuspended && !this.isFinished) {
			String currentLine = null;
			try {
				currentLine = input.readLine();
			} catch (EOFException eof) {
				writeTheWordsToFile();
				System.out.println("Finished the FindHotWords processing");
				isFinished = true;
				break;
			} catch (IOException eio) {
				eio.printStackTrace();
			}
			if(currentLine == null) {
				writeTheWordsToFile();
				System.out.println("Finished the FindHotWords processing");
				isFinished = true;
				break;
			}
			// handling the word count
			currentLine = currentLine.toLowerCase().replaceAll("\\p{Punct}|\\d"," ");
			String[] words = currentLine.split(" ");
			for(String word : words) {
				if(!stopwords.contains(word)) {
					if(wordCount.containsKey(word)) {
						wordCount.put(word, wordCount.get(word) + 1);
					} else {
						wordCount.put(word, 1L);
					}
				} else {
					continue;
				}
			}
		}
		
		/*System.out.println("Closing files...");
		try {
			output.flush();
			input.close();
			output.close();
			System.out.println("File closed.");
		} catch (IOException e1) {
			System.out.println("System failure when disconnecting source/desinate file.");
			e1.printStackTrace();
		}*/
	}
	
	public void writeTheWordsToFile() {
		Iterator<Entry<String, Long>> iter = wordCount.entrySet().iterator();
		int count = 1;
		while (iter.hasNext()){
			Map.Entry<String, Long> entry = (Map.Entry<String, Long>) iter.next();
			String word = entry.getKey();
			Long number = entry.getValue();
			if(number.compareTo(threshold) >= 0) {
				String out = count++ + "  " + word + "  " + number;
				try {
					output.writeLine(out);
				} catch (IOException e) {
					System.out.println("There are unkown errors happened when write to the file!!!");
					e.printStackTrace();
				}
			} else {
				continue;
			}
		}
	}

	@Override
	public void suspend() {
		isSuspended = true;
	}

	@Override
	public String toString() {
		String result = "";
		result = "Source file: " + input.getFilePath() + "\nDesination file: " + output.getFilePath() + "\nThreshold: " + threshold;
		return result;
	}
	
	
	public boolean isaNum (String input) {
		boolean flag = true;
		try {
			Long.parseLong(input);
		} catch (NumberFormatException e) {
			flag = false;
			System.out.println("The input string is not a Num!");
		}
		return flag;
	}
}
