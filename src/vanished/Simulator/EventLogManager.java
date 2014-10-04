package vanished.Simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class EventLogManager {

	boolean useFile = true;

	String name;
	ArrayList<EventLog> events = new ArrayList<EventLog>();

	public class EventLog {
		public long time;
		public double value;

		public EventLog(long time, double value) {
			this.time = time;
			this.value = value;
		}
	}

	public EventLogManager(String name) {
		this.name = name;
	}

	public void Put(long timeNow, double value) throws Exception {
		if (useFile == true) {
			File file = new File("data/" + name + ".txt");
			FileWriter fw = new FileWriter(file, true);
			fw.write(String.format("%d, %f\n", timeNow, value));
			fw.flush();
			fw.close();
		} else {
			events.add(new EventLog(timeNow, value));
		}
	}

	public ArrayList<EventLog> Get() throws Exception {
		if (useFile == true) {
			ArrayList<EventLog> ret = new ArrayList<EventLog>();
			File file = new File("data/" + name + ".txt");
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				while (true) {
					String line = br.readLine();
					if (line == null) break;

					String[] part = line.split(",");
					long time = Long.parseLong(part[0]);
					double value = Double.parseDouble(part[1]);
					ret.add(new EventLog(time, value));
				}
				br.close();
			}
			return ret;

		} else {
			return events;
		}
	}

	public ArrayList<EventLog> Get(int numSample) throws Exception {
		if (useFile == true) {
			ArrayList<EventLog> temp = Get();
			int num = temp.size();
			if (num <= numSample) { return temp; }
			double rate = 1.0 * numSample / num;
			ArrayList<EventLog> ret = new ArrayList<EventLog>();
			for (int i = 0; i < num; i++) {
				int index = (int) (i * rate);
				ret.add(temp.get(index));
			}
			return ret;
		} else {
			int num = events.size();
			if (num <= numSample) { return events; }
			double rate = 1.0 * numSample / num;
			ArrayList<EventLog> ret = new ArrayList<EventLog>();
			for (int i = 0; i < num; i++) {
				int index = (int) (i * rate);
				ret.add(events.get(index));
			}
			return ret;
		}
	}

	public void DiscardOldLog(long timeNow) throws Exception {
		if (useFile == true) {
			ArrayList<EventLog> temp = Get();
			File file = new File("data/" + name + ".txt");
			FileWriter fw = new FileWriter(file);
			for (EventLog event : temp) {
				if (event.time < timeNow) continue;
				fw.write(String.format("%d, %f\n", event.time, event.value));
			}
			fw.flush();
			fw.close();
		} else {
			Iterator<EventLog> it = events.iterator();
			while (true) {
				if (it.hasNext() == false) break;
				EventLog event = it.next();
				if (event.time < timeNow) {
					events.remove(it);
				}
			}
		}
	}
}
