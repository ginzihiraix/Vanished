package vanished.Simulator.Skill;

import java.util.Properties;

import vanished.Simulator.GlobalParameter;

public class Skill {
	String name;

	String preName = null;

	public Skill(String name, Properties p) {
		this.name = name;
		preName = p.getProperty("preSkill");
	}

	public String GetName() {
		return name;
	}

	public boolean hasAbility(Skill a) {
		Skill now = this;
		while (true) {
			if (now.name.equals(a.name)) return true;

			if (now.preName == null) break;

			now = GlobalParameter.dm.GetSkill(now.preName);
		}
		return false;
	}
}
