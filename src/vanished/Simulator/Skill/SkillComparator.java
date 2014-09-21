package vanished.Simulator.Skill;

import java.util.Comparator;

public class SkillComparator implements Comparator<Skill> {

	@Override
	public int compare(Skill o1, Skill o2) {
		return o1.name.compareTo(o2.name);
	}

}
