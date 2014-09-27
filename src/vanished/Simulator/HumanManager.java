package vanished.Simulator;

import java.util.ArrayList;

import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.Room;

public class HumanManager {

	ArrayList<Human> humans = new ArrayList<Human>();

	public HumanManager(MapManager mm) throws Exception {
		for (int i = 0; i < 50; i++) {
			ArrayList<Room> list = mm.GetRoomList(null, Long.MAX_VALUE, null);
			Room currentRoom = list.get(OtherUtility.rand.nextInt(list.size()));
			Human human = new Human(mm, 0, currentRoom);
			humans.add(human);
		}
	}

	public Human GetOldestHuman() {
		long timeOld = Long.MAX_VALUE;
		Human humanOld = null;
		for (Human human : humans) {
			long timeNow = human.humanStatus.timeSimulationComplete;
			if (timeNow < timeOld) {
				timeOld = timeNow;
				humanOld = human;
			}
		}
		return humanOld;
	}

	public ArrayList<Human> GetHumanList(Skill skill) {
		ArrayList<Human> ret = new ArrayList<Human>();
		for (Human human : humans) {
			if (human.humanStatus.myskill.hasAbility(skill) == true) {
				ret.add(human);
			}
		}
		return ret;
	}

}
