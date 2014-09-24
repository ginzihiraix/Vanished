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

	// 部屋の装備品のリスト
	Inventory runnableEquipItemInventory = new Inventory();

	// スキル毎に労働者の管理をする。
	TreeMap<Skill, WorkerManager> runnableWorkerManager = new TreeMap<Skill, WorkerManager>(new SkillComparator());

	public RunnableRoom(Building building, RunnableRoomDef roomDef) {
		super(building, roomDef);

		// workerManagerを初期化する。
		for (Entry<Skill, WorkerRequirement> e : roomDef.workerRequirement.entrySet()) {
			Skill skill = e.getKey();
			WorkerManager hrm = new WorkerManager();
			this.runnableWorkerManager.put(skill, hrm);
		}
	}

	// 部屋が稼動可能か調べる。
	private boolean IsRunnable(long timeStart, long duration) {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;

		// 稼動に必要な装備品は全てそろっているかチェック。
		if (this.IsEquipmentOK(timeStart, duration) == false) return false;

		// 現在いる労働者は、最低労働者数を上回っているかチェック。
		for (Entry<Skill, WorkerManager> e : runnableWorkerManager.entrySet()) {
			Skill skill = e.getKey();
			WorkerManager wm = e.getValue();
			WorkerRequirement wc = roomDef.GetWorkerCondition(skill);
			if (wm.IsRunnable(wc, timeStart, duration) == false) return false;
		}

		return true;
	}

	// 稼動に必要な装備品は全てそろっているかチェック。
	private boolean IsEquipmentOK(long timeStart, long duration) {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;

		// 稼動に必要な装備品は全てそろっているかチェック。
		for (Entry<ItemDef, Double> e : roomDef.equipItemListForRun.entrySet()) {
			ItemDef itemDef = e.getKey();
			double numEquipment = e.getValue();
			double numItem = this.runnableEquipItemInventory.GetNumStock(itemDef);
			if (numItem < numEquipment) return false;
		}

		return true;
	}

	// 部屋が標準効率の何倍で稼動しているか調べる。
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

	// 欲しい人材リストを返す。
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

	// 労働時間を返す。
	public long GetDurationForWork(Skill skill) {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;
		WorkerRequirement wc = roomDef.GetWorkerCondition(skill);
		return wc.durationForWork;
	}

	// 賃金を返す。
	public double GetWage(Skill skill) {
		WorkerManager wm = this.runnableWorkerManager.get(skill);
		return wm.wage;
	}

	// RunnableRoomが稼動可能か調べる。RunnableRoomを使いたい人が呼び出す。
	public void Greeting(long timeStart, long duration, boolean simulation) throws HumanSimulationException {

		// 部屋に入る。
		this.Enter(timeStart, duration, simulation);

		// この部屋が稼動可能か調べる。
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

	// Workerとして働く。
	public WorkResult Work(Skill skill, long timeStart, boolean simulation) throws HumanSimulationException {
		RunnableRoomDef roomDef = (RunnableRoomDef) this.roomDef;

		WorkerManager workerManager = runnableWorkerManager.get(skill);
		WorkerRequirement workerRequirement = roomDef.GetWorkerCondition(skill);

		// 部屋に入る。
		this.Enter(timeStart, workerRequirement.durationForWork, simulation);

		// 仕事をする。
		workerManager.Work(workerRequirement, timeStart, workerRequirement.durationForWork, simulation);

		if (simulation == false) {
			this.AddMoney(timeStart, -workerManager.wage);
		}

		WorkResult result = new WorkResult(workerManager.wage, workerRequirement.durationForWork);
		return result;
	}
}
