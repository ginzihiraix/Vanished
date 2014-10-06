package vanished.Simulator;

import java.util.ArrayList;

import vanished.Simulator.Item.ConsumeDef;
import vanished.Simulator.Item.ItemDef;
import vanished.Simulator.Item.MoveMethod;
import vanished.Simulator.Skill.Skill;
import vanished.Simulator.Structure.Building;
import vanished.Simulator.Structure.DeliverRoom;
import vanished.Simulator.Structure.DeliverRoom.CallForItem;
import vanished.Simulator.Structure.FactoryRoom;
import vanished.Simulator.Structure.FactoryRoom.CallForMaker;
import vanished.Simulator.Structure.FactoryRoom.CallForMakerInKind;
import vanished.Simulator.Structure.Room;
import vanished.Simulator.Structure.ShopRoom;
import vanished.Simulator.Structure.ShopRoom.ItemCatalog;

public class MapManager {
	int worldSize = 16;
	ArrayList<Building> buildingList = new ArrayList<Building>();

	public int GetWorldSize() {
		return worldSize;
	}

	public boolean CreateBuilding(Building building) {
		buildingList.add(building);
		return true;
	}

	public long GetTravelTime(MoveMethod moveMethod, Building b1, Building b2) {
		return 10L;
	}

	public long GetTravelTime(MoveMethod moveMethod, Room r1, Room r2) {
		return GetTravelTime(moveMethod, r1.GetParentBuilding(), r2.GetParentBuilding());
	}

	public ArrayList<Building> GetBuildingList(int left, int right, int bottom, int top) {
		ArrayList<Building> ret = new ArrayList<Building>();
		for (Building building : buildingList) {
			Rect rect = building.GetLocation();
			if (rect.right <= left || rect.left >= right || rect.top <= bottom || rect.bottom >= top) continue;
			ret.add(building);
		}
		return ret;
	}

	public ArrayList<Room> GetRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom) {
		ArrayList<Room> list = new ArrayList<Room>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (moveMethod != null && currentRoom != null) {
					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;
				}
				list.add(room);
			}
		}
		return list;
	}

	public ArrayList<DeliverRoom> GetDeliverableRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, double maxMoney,
			double maxNumPickup, boolean realOnlyFlag) {
		ArrayList<DeliverRoom> list = new ArrayList<DeliverRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (realOnlyFlag == true) {
					if (room.IsReal() == false) continue;
				}

				if (room instanceof DeliverRoom) {
					DeliverRoom deliverableRoom = (DeliverRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					ArrayList<CallForItem> itemDefList = deliverableRoom.GetDesiredItemList(maxMoney, maxNumPickup);
					if (itemDefList.size() == 0) continue;

					list.add(deliverableRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<DeliverRoom> GetDeliverableRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, ItemDef deliverItem,
			double maxMoney, double maxNumPick, boolean realOnlyFlag) {
		ArrayList<DeliverRoom> list = new ArrayList<DeliverRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (realOnlyFlag == true) {
					if (room.IsReal() == false) continue;
				}

				if (room instanceof DeliverRoom) {
					DeliverRoom deliverableRoom = (DeliverRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room.GetParentBuilding(), currentRoom.GetParentBuilding());
					if (travelTime > maxTravelTime) continue;

					if (deliverableRoom.GetDesiredItemWithNewPrice(deliverItem, maxMoney, maxNumPick) == null) continue;

					list.add(deliverableRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<ShopRoom> GetShopRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, ItemDef desiredItem, double maxMoney,
			double maxNumPick, boolean realOnlyFlag) {
		ArrayList<ShopRoom> list = new ArrayList<ShopRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (realOnlyFlag == true) {
					if (room.IsReal() == false) continue;
				}

				if (room instanceof ShopRoom) {
					ShopRoom shopRoom = (ShopRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					ItemCatalog ic = shopRoom.GetProductItemForConsumeWithNewPrice(maxMoney, maxNumPick);
					if (ic == null) continue;
					if (ic.itemDef != desiredItem) continue;

					list.add(shopRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<ShopRoom> GetShopRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, double maxMoney, double maxNumPick,
			boolean realOnlyFlag) {
		ArrayList<ShopRoom> list = new ArrayList<ShopRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (realOnlyFlag == true) {
					if (room.IsReal() == false) continue;
				}

				if (room instanceof ShopRoom) {
					ShopRoom shopRoom = (ShopRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					ItemCatalog ic = shopRoom.GetProductItemForConsumeWithNewPrice(maxMoney, maxNumPick);
					if (ic == null) continue;

					list.add(shopRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<ShopRoom> GetConsumableRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, double maxMoney, double maxNumPick,
			boolean realOnlyFlag) {
		ArrayList<ShopRoom> list = new ArrayList<ShopRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (realOnlyFlag == true) {
					if (room.IsReal() == false) continue;
				}

				if (room instanceof ShopRoom) {
					ShopRoom shopRoom = (ShopRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room.GetParentBuilding(), currentRoom.GetParentBuilding());
					if (travelTime > maxTravelTime) continue;

					ItemCatalog ic = shopRoom.GetProductItemForConsumeWithNewPrice(maxMoney, maxNumPick);
					if (ic == null) continue;
					if (ic.itemDef instanceof ConsumeDef == false) continue;

					list.add(shopRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<FactoryRoom> GetConsumableAndMakableRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, double maxMoney,
			double maxNumPick, boolean realOnlyFlag) {
		ArrayList<FactoryRoom> list = new ArrayList<FactoryRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (realOnlyFlag == true) {
					if (room.IsReal() == false) continue;
				}

				if (room instanceof FactoryRoom) {
					FactoryRoom factoryRoom = (FactoryRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room.GetParentBuilding(), currentRoom.GetParentBuilding());
					if (travelTime > maxTravelTime) continue;

					ItemCatalog ic = factoryRoom.GetProductItemForConsumeWithNewPrice(maxMoney, maxNumPick);
					if (ic == null) continue;
					if (ic.itemDef instanceof ConsumeDef == false) continue;

					CallForMakerInKind cfm = factoryRoom.GetDesiredMakerInKind(maxNumPick);
					if (cfm == null) continue;

					list.add(factoryRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<FactoryRoom> GetFactoryRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, boolean realOnlyFlag) {
		ArrayList<FactoryRoom> list = new ArrayList<FactoryRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof FactoryRoom) {
					if (realOnlyFlag == true) {
						if (room.IsReal() == false) continue;
					}

					FactoryRoom factoryRoom = (FactoryRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					CallForMaker cfm = factoryRoom.GetDesiredMaker(Double.MAX_VALUE);
					if (cfm == null) continue;

					list.add(factoryRoom);
				}
			}
		}
		return list;
	}

	private ArrayList<FactoryRoom> GetFactoryRoomList(Skill skill) {
		ArrayList<FactoryRoom> list = new ArrayList<FactoryRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof FactoryRoom) {
					FactoryRoom factoryRoom = (FactoryRoom) room;

					CallForMaker cfm = factoryRoom.GetDesiredMaker(Double.MAX_VALUE);
					if (cfm == null) continue;

					if (skill.hasAbility(cfm.skill) == false) continue;

					list.add(factoryRoom);
				}
			}
		}
		return list;
	}

}
