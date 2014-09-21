package vanished.Simulator;

import java.util.ArrayList;

import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Structure.Building;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.Room;

public class Main {

	public static void main(String[] args) {
		try {

			System.out.println(System.getProperty("file.encoding"));

			System.out.println("===Start===");

			GlobalParameter.dm.GetItemDef("fish");

			// Utility�̃e�X�g
			{
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

			MapManager mm = new MapManager();

			{
				Building water = new Building(GlobalParameter.dm.GetBuilding("�����ݏ�"));
				mm.CreateBuilding(water);

				Building fish = new Building(GlobalParameter.dm.GetBuilding("�ނ��"));
				mm.CreateBuilding(fish);

				Building souseiji = new Building(GlobalParameter.dm.GetBuilding("�\�[�Z�[�W�H��"));
				mm.CreateBuilding(souseiji);

				// Building sleep = new Building(GlobalParameter.dm.GetBuilding("�A�p�[�g"));
				// mm.CreateBuilding(sleep);
			}

			HumanManager humanManager = new HumanManager(mm);

			long timeLastManage = 0;
			long timeLast5Year = 0;

			for (int frame = 0; frame < 10000000; frame++) {

				// �Ƃ肠�����A�V�~�����[�V����������������Ԓx���l�������āA���s���Ă݂�B
				Human humanOldest = humanManager.GetOldestHuman();
				long timeNow = humanOldest.humanStatus.timeSimulationComplete;

				humanOldest.GenerateAndExecuteAction();

				if (timeNow - timeLast5Year > 60 * 24 * 100) {
					timeLast5Year = timeNow;

					// ���N�Ɉ�x�A���[�e�B���e�B�̍����l�́A�q���𐶂ށi���􂷂�j
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

					// �����Ԃ����Ă���ɂ��ւ�炸�A�����]�����Ă��Ȃ������͉󂷁B���z�������󂷁B
					{

					}

					// ���z������ǉ�����B
					{

					}

				}

				if (timeNow - timeLastManage >= 60 * 24 * 5) {
					timeLastManage = timeNow;
					for (Building building : mm.buildingList) {
						for (Room room : building.GetRoomList()) {
							if (room instanceof FactoryRoom) {
								FactoryRoom factoryRoom = (FactoryRoom) room;
								factoryRoom.DumpStatus(timeNow);
								factoryRoom.ManagePriceSet(mm, humanManager, timeNow);
							}
						}
					}
					System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
