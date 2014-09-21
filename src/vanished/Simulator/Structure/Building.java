package vanished.Simulator.Structure;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.ItemDefComparator;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Skill.SkillComparator;
import vanished.Simulator.Structure.BuildingDef.MaterialInvestDef;
import vanished.Simulator.Structure.BuildingDef.Recipe;

public class Building {
	BuildingDef buildingDef;

	// このBuildingの建設が完了しているか否かのフラグ
	boolean buildCompletedFlag = false;

	// ビル建設のために投入したリソースを一時格納するための場所
	InvestBox investProgress = new InvestBox();

	// 建築者のリスト
	HumanExistRecordManager builderExistRecordManager = new HumanExistRecordManager();

	// 所属するRoomの一覧
	ArrayList<Room> roomList = new ArrayList<Room>();

	class InvestRecord {
		long time;
		int amount;

		public InvestRecord(long time, int amount) {
			this.time = time;
			this.amount = amount;
		}
	}

	public class MaterialInvestManager {
		// 資材をどう保つかを記述したクラス。
		MaterialInvestDef materialInvestDef;

		// 価格
		double price = 1;

		// 資材の総投下量
		double amount = 0;

		// 資材投下の履歴
		ArrayList<InvestRecord> inHistory = new ArrayList<InvestRecord>();

		public MaterialInvestManager(MaterialInvestDef materialInvestDef) {
			this.materialInvestDef = materialInvestDef;
		}
	}

	public class InvestBox {
		// 投資したアイテム名と数量のマップ。
		TreeMap<ItemDef, Integer> material = new TreeMap<ItemDef, Integer>(new ItemDefComparator());

		// 投資した労働力と時間のマップ。
		TreeMap<Skill, Long> workload = new TreeMap<Skill, Long>(new SkillComparator());

		public void InvestMaterial(ItemDef itemDef, int num) {
			Integer m = material.get(itemDef);
			if (m == null) {
				m = 0;
			}
			m += num;
			material.put(itemDef, m);
		}

		public void InvestWorkload(Skill skill, long duration) {
			Long w = workload.get(skill);
			if (w == null) {
				w = 0L;
			}
			w += duration;
			workload.put(skill, w);
		}

		public int GetProgressMaterial(ItemDef itemDef) {
			Integer ret = material.get(itemDef);
			if (ret == null) ret = 0;
			return ret;
		}

		public double GetProgressWorkload(Skill skill) {
			Long ret = workload.get(skill);
			if (ret == null) ret = 0L;
			return ret;
		}

		public boolean IsComplete(Recipe r) {
			for (Entry<ItemDef, Integer> e : material.entrySet()) {
				ItemDef itemDef = e.getKey();
				int numStock = e.getValue();
				int numRequired = r.material.get(itemDef);
				if (numStock < numRequired) return false;
			}

			for (Entry<Skill, Long> e : workload.entrySet()) {
				Skill skill = e.getKey();
				long numStock = e.getValue();
				long numRequired = r.workload.get(skill);
				if (numStock < numRequired) return false;
			}

			return true;
		}
	}

	public Building(BuildingDef buildingDef) throws Exception {
		this.buildingDef = buildingDef;

		for (RoomDef roomDef : buildingDef.roomdefList) {
			Room room;
			if (roomDef instanceof FactoryRoomDef) {
				room = new FactoryRoom(this, (FactoryRoomDef) roomDef);
			} else {
				throw new Exception("faefaefawfwae");
			}
			roomList.add(room);
		}
	}

	public boolean IsBuildCompleted() {
		return buildCompletedFlag;
	}

	public ArrayList<Room> GetRoomList() {
		return roomList;
	}

	// 建築に必要な人員リストを返す。
	public class CallForBuilder {
		Skill skill;
		double wage;
		long duration;

		public CallForBuilder(Skill skill, double wage, long duration) {
			this.skill = skill;
			this.wage = wage;
			this.duration = duration;
		}
	}

	public ArrayList<CallForBuilder> GetDesiredBuilderList() {
		ArrayList<CallForBuilder> ret = new ArrayList<CallForBuilder>();

		for (Skill skill : this.buildingDef.recipe.workload.keySet()) {

		}

		return ret;
	}

	// 建築作業をする。
	public void Build() {

	}

}
