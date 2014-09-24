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
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("���̏�"), new Location(0, 0), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("���̏�"), new Location(1, 1), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("�̐Ώ�"), new Location(2, 2), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("�̐Ώ�"), new Location(3, 3), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("�����ݏ�"), new Location(4, 4), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("�����ݏ�"), new Location(5, 5), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("�ނ��"), new Location(6, 6), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("�ނ��"), new Location(7, 7), 0));
			mapManager.CreateBuilding(new Building(0, GlobalParameter.dm.GetBuilding("�\�[�Z�[�W�H��"), new Location(8, 8), 0));
		}

		humanManager = new HumanManager(mapManager);
	}

	public void run() {
		try {

			System.out.println(System.getProperty("file.encoding"));

			System.out.println("===Start===");

			// XXX:GP��static�̏��������L�b�N����B
			{
				GlobalParameter.dm.GetItemDef("water");
			}

			// XXX:Utility�̃e�X�g
			if (false) {
				UtilityManager um = new UtilityManager();
				System.out.println(um.ComputeUtility(0));
				ItemDef fish = GlobalParameter.dm.GetItemDef("fish");
				um.AddUtility(fish.GetUtilities(), 100, 500);
				System.out.println(um.ComputeUtility(100));
				ItemDef water = GlobalParameter.dm.GetItemDef("water");
				um.AddUtility(water.GetUtilities(), 100, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 100, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 100, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 100, 100);
				System.out.println(um.ComputeUtility(100));
				um.AddUtility(fish.GetUtilities(), 100, 100);
				System.out.println(um.ComputeUtility(100));
			}

			long timeLast7day = 0;
			long timeLast100day = 0;

			for (int frame = 0; frame < 10000000; frame++) {

				// �Ƃ肠�����A�V�~�����[�V����������������Ԓx���l�������āA���s���Ă݂�B
				Human humanOldest = humanManager.GetOldestHuman();
				long timeNow = humanOldest.humanStatus.timeSimulationComplete;

				humanOldest.GenerateAndExecuteAction();

				if (timeNow - timeLast100day > 60 * 24 * 100) {
					timeLast100day = timeNow;

					// ���[�e�B���e�B�̍����l�́A�q���𐶂ށi���􂷂�j
					{
						ArrayList<Human> nh = new ArrayList<Human>();
						for (Human human : humanManager.humans) {
							Human h = human.Birth();
							if (h == null) continue;
							nh.add(h);
						}
						humanManager.humans.addAll(nh);
					}

					// �N���Ƃ��Ă�l�A���[�e�B���e�B�̒Ⴂ�l�́A���ʁB
					{
						ArrayList<Human> nh = new ArrayList<Human>();
						for (Human human : humanManager.humans) {
							boolean flagDeath = human.Death();
							if (flagDeath) continue;
							nh.add(human);
						}
						humanManager.humans = nh;
					}

					// �����Ԃ����Ă���ɂ��ւ�炸�A�����]�����Ă��Ȃ������͉󂷁B
					{
						ArrayList<Building> alive = new ArrayList<Building>();
						for (Building building : mapManager.buildingList) {
							if (building.CheckRecoverInitialCost(timeNow) == false) continue;
							alive.add(building);
						}
						mapManager.buildingList = alive;
					}

					// ���z������ǉ�����B
					{

					}

					// ���z�����ŁA�����Ԃ����Ă�����̂ɔ��f�������B�����]���ł��Ă�����͎̂��̉�����B�ł��Ă��Ȃ����͉̂󂷁B
					{

					}
				}

				if (timeNow - timeLast7day >= 60 * 24 * 7) {
					timeLast7day = timeNow;

					// ���݊������Ă��錚���́A���z�����t���O�𗧂Ă�B
					{
						for (Building building : mapManager.buildingList) {
							building.CheckBuildingCompleted();
						}

					}

					// ���i�𒲐�����B
					{
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

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
