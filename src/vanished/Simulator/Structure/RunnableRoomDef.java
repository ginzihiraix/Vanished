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

	// �ғ��ɕK�v�ȑ����i�̃��X�g
	TreeMap<ItemDef, Integer> equipItemListForRun = new TreeMap<ItemDef, Integer>(new ItemDefComparator());

	// �ғ��ɕK�v�Ȑl�ނ̃��X�g
	TreeMap<Skill, WorkerRequirement> workerRequirement = new TreeMap<Skill, WorkerRequirement>(new SkillComparator());

	public class WorkerRequirement {
		// �ғ����邽�߂ɕK�v�ȕ��ϘJ���Ґ�
		int minAverageNumWorkerForRun;

		// �ғ����邽�߂ɕK�v�ȘJ���҂�������ő吔
		int workerCapacityForRun;

		// �@�W��������B�����邽�߂ɕK�v�ȘJ���Ґ�
		double numWorkerForStandardPerformance;

		// �J���҂̈��̘J������
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
