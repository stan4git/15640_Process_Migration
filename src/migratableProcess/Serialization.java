package migratableProcess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serialization {

	// method 1: serialization
	public static void serialize(MigratableProcess task) {
		System.out.println(task.toString());
		try {
			System.out.println("Start the serialization Process : " + task.getPid());
			File file = new File(Configuration.DIRECTORY + task.getPid() + ".ms");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			
			FileOutputStream fileOutputStream = new FileOutputStream(Configuration.DIRECTORY + Integer.toString(task.getPid()) + ".ms");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(task);
			objectOutputStream.flush();
			objectOutputStream.close();
			System.out.println("Serialization has done!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// method 2: Deserialization
	public static MigratableProcess deserialize(int pid) {
		try {
			System.out.println("Start the deserialization Process : " + pid);
			FileInputStream file = new FileInputStream(Configuration.DIRECTORY
					+ Integer.toString(pid) + ".ms");
			ObjectInputStream objectInputStream = new ObjectInputStream(file);
			Object object = objectInputStream.readObject();
			MigratableProcess migratableProcess = (MigratableProcess) object;
			objectInputStream.close();
			migratableProcess.toString();
			System.out.println("Deserialization has done!");
			return migratableProcess;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
