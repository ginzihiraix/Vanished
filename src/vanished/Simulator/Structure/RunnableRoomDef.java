package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;

import vanished.Simulator.GlobalParameter;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Skill.SkillComparator;

public class RunnableRoomDef extends RoomDef {

	// 稼動に必要な装備品のリスト
	TreeMap<ItemDef, Integer> equipItemListForRun = new TreeMap<ItemDef, Integer>(new ItemDefComparator());

	// 稼動に必要な人材のリスト
	TreeMap<Skill, WorkerRequirement> workerRequirement = new TreeMap<Skill, WorkerRequirement>(new SkillComparator());

	public class WorkerRequirement {
		// 稼動するために必要な平均労働者数
		int minAverageNumWorkerForRun;

		// 稼動するために必要な労働者が働ける最大数
		int workerCapacityForRun;

		// 　標準効率を達成するために必要な労働者数
		double numWorkerForStandardPerformance;

		// 労働者の一回の労働時間
		long durationForWork;

		public WorkerRequirement(String prefix, Properties p) {
			this.minAverageNumWorkerForRun = Integer.parseInt(p.getProperty(prefix + "minAverageNumWorkerForRun"));
			this.durationForWork = Long.parseLong(p.getProperty(prefix + "durationForWork"));
			this.workerCapacityForRun = Integer.parseInt(p.getProperty(prefix + "workerCapacityForRun"));
			this.numWorkerForStandardPerformance = Double.parseDouble(p.getProperty(prefix + "numWorkerForStandardPerformance"));
		}
	}

	public RunnableRoomDef(String name, Properties p) throws Exception {
		super(name, p);

		for (String key : p.stringPropertyNames()) {
			String value = p.getProperty(key);

			String[] parts = key.split("\\.");

			if (parts.length == 3 && parts[0].equals("run") && parts[1].equals("equipment")) {
				String itemName = parts[2];
				ItemDef itemDef = GlobalParameter.dm.GetItemDef(itemName);

				int num = Integer.parseInt(value);

				equipItemListForRun.put(itemDef, num);
			}
		}

		ArrayList<Skill> skills = new ArrayList<Skill>();
		for (String key : p.stringPropertyNames()) {
			String[] parts = key.split("\\.");
			if (parts.length == 4 && parts[0].equals("run") && parts[1].equals("worker") && parts[3].equals("minAverageNumWorkerForRun")) {
				String skillname = parts[2];
				Skill skill = GlobalParameter.dm.GetSkill(skillname);
				skills.add(skill);
			}
		}

		for (Skill skill : skills) {
			WorkerRequirement wc = new WorkerRequirement("run.worker." + skill.GetName() + ".", p);
			this.workerRequirement.put(skill, wc);
		}
	}

	public WorkerRequirement GetWorkerCondition(Skill skill) {
		return this.workerRequirement.get(skill);
	}
}
