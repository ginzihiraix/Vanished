package vanished.Simulator;

import java.util.ArrayList;
import java.util.Iterator;

public class EventLogManager {

	ArrayList<EventLog> events = new ArrayList<EventLog>();

	public class EventLog {
		public long time;
		public double value;

		public EventLog(long time, double value) {
			this.time = time;
			this.value = value;
		}
	}

	public void Put(long timeNow, double value) {
		events.add(new EventLog(timeNow, value));
	}

	public void DiscardOldLog(long timeNow) {
		Iterator<EventLog> it = events.iterator();
		while (true) {
			if (it.hasNext() == false) break;
			EventLog event = it.next();
			if (event.time < timeNow) {
				events.remove(it);
			}
		}
	}

	public ArrayList<EventLog> Get() {
		return events;
	}

	public ArrayList<EventLog> Get(int numSample) {
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
