package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.HumanSimulationException;
import vanished.Simulator.Inventory;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Skill.SkillComparator;
import vanished.Simulator.Structure.RunnableRoomDef.WorkerRequirement;

public class RunnableRoom extends Room {

	// �����̑����i�̃��X�g
	Inventory runnableEquipItemInventory = new Inventory();

	// �X�L�����ɘJ���҂̊Ǘ�������B
	TreeMap<Skill, WorkerManager> runnableWorkerManager = new TreeMap<Skill, WorkerManager>(new SkillComparator());

	public RunnableRoom(Building building, RunnableRoomDef roomDef) {
		super(building, roomDef);

		// workerManager������������B
		for (Entry<Skill, WorkerRequirement> e : roomDef.workerRequirement.entrySet()) {
			Skill skill = e.getKey();
			WorkerManager hrm = new WorkerManager();
			this.runnableWorkerManager.put(skill, hrm);
		}
	}

	// �������ғ��\�����ׂ�B
	private boolean IsRunnable(long timeStart, long duration) {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;

		// �ғ��ɕK�v�ȑ����i�͑S�Ă�����Ă��邩�`�F�b�N�B
		if (this.IsEquipmentOK(timeStart, duration) == false) return false;

		// ���݂���J���҂́A�Œ�J���Ґ��������Ă��邩�`�F�b�N�B
		for (Entry<Skill, WorkerManager> e : runnableWorkerManager.entrySet()) {
			Skill skill = e.getKey();
			WorkerManager wm = e.getValue();
			WorkerRequirement wc = roomDef.GetWorkerCondition(skill);
			if (wm.IsRunnable(wc, timeStart, duration) == false) return false;
		}

		return true;
	}

	// �ғ��ɕK�v�ȑ����i�͑S�Ă�����Ă��邩�`�F�b�N�B
	private boolean IsEquipmentOK(long timeStart, long duration) {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;

		// �ғ��ɕK�v�ȑ����i�͑S�Ă�����Ă��邩�`�F�b�N�B
		for (Entry<ItemDef, Double> e : roomDef.equipItemListForRun.entrySet()) {
			ItemDef itemDef = e.getKey();
			double numEquipment = e.getValue();
			double numItem = this.runnableEquipItemInventory.GetNumStock(itemDef);
			if (numItem < numEquipment) return false;
		}

		return true;
	}

	// �������W�������̉��{�ŉғ����Ă��邩���ׂ�B
	public double ComputeEfficiency(long timeStart, long duration) {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;

		double minEfficiency = Double.MAX_VALUE;
		for (Entry<Skill, WorkerManager> e : runnableWorkerManager.entrySet()) {
			Skill skill = e.getKey();
			WorkerManager wm = e.getValue();
			WorkerRequirement wc = roomDef.GetWorkerCondition(skill);
			double efficiency = wm.ComputeEfficiency(wc, timeStart, duration);
			if (efficiency < minEfficiency) {
				minEfficiency = efficiency;
			}
		}
		return minEfficiency;
	}

	public class CallForWorker {
		Skill skill;
		double wage;

		public CallForWorker(Skill skill, double wage) {
			this.skill = skill;
			this.wage = wage;
		}

		public double GetWage() {
			return wage;
		}

		public Skill GetSkill() {
			return skill;
		}
	}

	// �~�����l�ރ��X�g��Ԃ��B
	public ArrayList<CallForWorker> GetDesiredWorker() {
		ArrayList<CallForWorker> ret = new ArrayList<CallForWorker>();

		for (Entry<Skill, WorkerManager> e : runnableWorkerManager.entrySet()) {
			Skill skill = e.getKey();
			WorkerManager wm = e.getValue();
			CallForWorker cfw = new CallForWorker(skill, wm.wage);
			ret.add(cfw);
		}

		return ret;
	}

	// �J�����Ԃ�Ԃ��B
	public long GetDurationForWork(Skill skill) {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;
		WorkerRequirement wc = roomDef.GetWorkerCondition(skill);
		return wc.durationForWork;
	}

	// ������Ԃ��B
	public double GetWage(Skill skill) {
		WorkerManager wm = this.runnableWorkerManager.get(skill);
		return wm.wage;
	}

	// RunnableRoom���ғ��\�����ׂ�BRunnableRoom���g�������l���Ăяo���B
	public void Greeting(long timeStart, long duration, boolean simulation) throws HumanSimulationException {

		// �����ɓ���B
		this.Enter(timeStart, duration, simulation);

		// ���̕������ғ��\�����ׂ�B
		if (IsRunnable(timeStart, duration) == false) throw new HumanSimulationException("");
	}

	public class WorkResult {
		public double gain;
		public long duration;

		public WorkResult(double gain, long duration) {
			this.gain = gain;
			this.duration = duration;
		}
	}

	// Worker�Ƃ��ē����B
	public WorkResult Work(Skill skill, long timeStart, boolean simulation) throws HumanSimulationException {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;

		WorkerManager workerManager = runnableWorkerManager.get(skill);
		WorkerRequirement workerRequirement = roomDef.GetWorkerCondition(skill);

		// �����ɓ���B
		this.Enter(timeStart, workerRequirement.durationForWork, simulation);

		// �d��������B
		workerManager.Work(workerRequirement, timeStart, workerRequirement.durationForWork, simulation);

		if (simulation == false) {
			this.AddMoney(timeStart, -workerManager.wage);
		}

		WorkResult result = new WorkResult(workerManager.wage, workerRequirement.durationForWork);
		return result;
	}
}
