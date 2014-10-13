package vanished.Simulator;

import java.util.ArrayList;

import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Structure.Building;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.Room;

public class SimulationMain extends Thread {
	MapManager mapManager;
	HumanManager humanManager;

	public SimulationMain() throws Exception {
		mapManager = new MapManager();

		{
			// mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("伐採場"), new Location(0, 0), 0, true));
			// mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("伐採場"), new Location(1, 1), 0, true));
			// mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("採石場"), new Location(2, 2), 0, true));
			// mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("採石場"), new Location(3, 3), 0, true));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("水汲み場"), new Location(4, 4), 0, true));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("水汲み場"), new Location(5, 5), 0, true));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("釣り場"), new Location(6, 6), 0, true));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("釣り場"), new Location(7, 7), 0, true));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("ソーセージ工場"), new Location(8, 8), 0, true));
		}

		humanManager = new HumanManager(mapManager);
	}

	public void run() {
		try {
			System.out.println(System.getProperty("file.encoding"));

			System.out.println("===Start===");

			// XXX:GPのstaticの初期化をキックする。
			{
				GlobalParameter.dm.GetItemDef("water");
			}

			// XXX:Utilityのテスト
			if (false) {
				UtilityManager um = new UtilityManager();
				System.out.println(um.ComputeUtility(0));
				ItemDef fish = GlobalParameter.dm.GetItemDef("fish");
				um.AddUtility(fish.GetUtilities(), 1, 100);
				System.out.println(um.ComputeUtility(100));
				ItemDef water = GlobalParameter.dm.GetItemDef("water");
				um.AddUtility(water.GetUtilities(), 1, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 1, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 1, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 1, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 1, 100);
				System.out.println(um.ComputeUtility(100));
			}

			long timeLast1day = 0;
			long timeLast7day = 0;
			long timeLast100day = 0;

			for (int frame = 0; frame < 10000000; frame++) {

				// とりあえず、シミュレーション完了時刻が一番遅い人を見つけて、実行してみる。
				Human humanOldest = humanManager.GetOldestHuman();
				long timeNow = humanOldest.humanStatus.timeSimulationComplete;

				humanOldest.GenerateAndExecuteAction();

				if (timeNow - timeLast1day >= 60 * 24) {
					timeLast1day = timeNow;

					// 一日の情報を記録する。
					for (Building building : mapManager.buildingList) {
						building.WriteLog(timeNow);
					}

					// 古いログは廃棄する。
					for (Building building : mapManager.buildingList) {
						building.DiscardOldLog(timeNow);
					}
				}

				if (timeNow - timeLast7day >= 60 * 24 * 1) {
					timeLast7day = timeNow;

					// 建設完了している建物は、建築完了フラグを立てる。
					{
						for (Building building : mapManager.buildingList) {
							building.ChangeBuildingStatus();
						}
					}

					// 価格を調整する。
					{
						if (false) {
							ArrayList<FactoryRoom> list = new ArrayList<FactoryRoom>();
							for (Building building : mapManager.buildingList) {
								for (Room room : building.GetRoomList()) {
									if (room instanceof FactoryRoom) {
										FactoryRoom factoryRoom = (FactoryRoom) room;
										list.add(factoryRoom);
									}
								}
							}
							int index = OtherUtility.rand.nextInt(list.size());
							FactoryRoom factoryRoom = list.get(index);
							factoryRoom.DumpStatus(timeNow);
							factoryRoom.ManagePriceSet(mapManager, humanManager, timeNow);
							System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
						}

						if (true) {
							for (Building building : mapManager.buildingList) {
								for (Room room : building.GetRoomList()) {
									if (room instanceof FactoryRoom) {
										FactoryRoom factoryRoom = (FactoryRoom) room;
										factoryRoom.DumpStatus(timeNow);
										factoryRoom.ManagePriceSet(mapManager, humanManager, timeNow);
									}
								}
							}
							System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
						}
					}
				}
				if (timeNow - timeLast100day > 60 * 24 * 100) {
					timeLast100day = timeNow;

					// ユーティリティの高い人は、子供を生む（分裂する）
					if (false) {
						ArrayList<Human> nh = new ArrayList<Human>();
						for (Human human : humanManager.humans) {
							Human h = human.Birth();
							if (h == null) continue;
							nh.add(h);
						}
						humanManager.humans.addAll(nh);
					}

					// 年をとってる人、ユーティリティの低い人は、死ぬ。
					if (false) {
						ArrayList<Human> nh = new ArrayList<Human>();
						for (Human human : humanManager.humans) {
							boolean flagDeath = human.Death();
							if (flagDeath) continue;
							nh.add(human);
						}
						humanManager.humans = nh;
					}

					// 一定期間たっているにも関わらず、黒字転換していない建物は壊す。
					if (false) {
						ArrayList<Building> alive = new ArrayList<Building>();
						for (Building building : mapManager.buildingList) {
							if (building.IsInitialCostRecovered(timeNow) == false) continue;
							alive.add(building);
						}
						mapManager.buildingList = alive;
					}

					// 仮想建物を追加する。
					{

					}

					// 仮想建物で、一定期間たっているものに判断を下す。黒字転換できているものは実体化する。できていないものは壊す。
					{

					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
