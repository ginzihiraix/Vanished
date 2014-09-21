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
import vanished.Simulator.Structure.Room;
import vanished.Simulator.Structure.RunnableRoom;
import vanished.Simulator.Structure.ShopRoom;
import vanished.Simulator.Structure.ShopRoom.ItemCatalog;

public class MapManager {
	ArrayList<Building> buildingList = new ArrayList<Building>();

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

	public ArrayList<ShopRoom> GetConsumableRoomList(MoveMethod moveMethod, long maxTravelTime, double maxMoney, long timeStart, Room currentRoom) {
		ArrayList<ShopRoom> list = new ArrayList<ShopRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof ShopRoom) {
					ShopRoom shopRoom = (ShopRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room.GetParentBuilding(), currentRoom.GetParentBuilding());
					if (travelTime > maxTravelTime) continue;

					ItemCatalog ic = shopRoom.GetProductItem(maxMoney, true);
					if (ic == null) continue;
					if (ic.itemDef instanceof ConsumeDef == false) continue;

					list.add(shopRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<RunnableRoom> GetRunnableRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom) {
		ArrayList<RunnableRoom> list = new ArrayList<RunnableRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof RunnableRoom) {
					RunnableRoom runnableRoom = (RunnableRoom) room;

					if (moveMethod != null) {
						long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
						if (travelTime > maxTravelTime) continue;
					}

					list.add(runnableRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<DeliverRoom> GetDeliverableRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom) {
		ArrayList<DeliverRoom> list = new ArrayList<DeliverRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof DeliverRoom) {
					DeliverRoom deliverableRoom = (DeliverRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					ArrayList<CallForItem> itemDefList = deliverableRoom.GetDesiredItemList();
					if (itemDefList.size() == 0) continue;

					list.add(deliverableRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<DeliverRoom> GetDeliverableRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom, ItemDef deliverItem) {
		ArrayList<DeliverRoom> list = new ArrayList<DeliverRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof DeliverRoom) {
					DeliverRoom deliverableRoom = (DeliverRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room.GetParentBuilding(), currentRoom.GetParentBuilding());
					if (travelTime > maxTravelTime) continue;

					if (deliverableRoom.GetDesiredItem(deliverItem) == null) continue;

					list.add(deliverableRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<ShopRoom> GetShopRoomList(MoveMethod moveMethod, long maxTravelTime, double maxMoney, Room currentRoom) {
		ArrayList<ShopRoom> list = new ArrayList<ShopRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof ShopRoom) {
					ShopRoom shopRoom = (ShopRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					ItemCatalog ic = shopRoom.GetProductItem(maxMoney, true);
					if (ic == null) continue;

					list.add(shopRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<ShopRoom> GetShopRoomList(MoveMethod moveMethod, long maxTravelTime, double maxMoney, Room currentRoom, ItemDef desiredItem) {
		ArrayList<ShopRoom> list = new ArrayList<ShopRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof ShopRoom) {
					ShopRoom shopRoom = (ShopRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					if (shopRoom.GetProductItem(maxMoney, true, desiredItem) == null) continue;

					list.add(shopRoom);
				}
			}
		}
		return list;
	}

	public double FindMinPrice(ItemDef desiredItem, boolean checkStock) {
		double min = Double.MAX_VALUE;

		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof ShopRoom) {
					ShopRoom shopRoom = (ShopRoom) room;
					ItemCatalog catalog = shopRoom.GetProductItem(Double.MAX_VALUE, checkStock, desiredItem);
					if (catalog == null) continue;

					if (catalog.price < min) {
						min = catalog.price;
					}
				}
			}
		}

		return min;
	}

	public ArrayList<FactoryRoom> GetFactoryRoomList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom) {
		ArrayList<FactoryRoom> list = new ArrayList<FactoryRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof FactoryRoom) {
					FactoryRoom factoryRoom = (FactoryRoom) room;

					long travelTime = this.GetTravelTime(moveMethod, room, currentRoom);
					if (travelTime > maxTravelTime) continue;

					CallForMaker cfm = factoryRoom.GetDesiredMaker();
					if (cfm == null) continue;

					list.add(factoryRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<FactoryRoom> GetFactoryRoomList(Skill skill) {
		ArrayList<FactoryRoom> list = new ArrayList<FactoryRoom>();
		for (Building building : buildingList) {
			for (Room room : building.GetRoomList()) {
				if (room instanceof FactoryRoom) {
					FactoryRoom factoryRoom = (FactoryRoom) room;

					CallForMaker cfm = factoryRoom.GetDesiredMaker();
					if (cfm == null) continue;

					if (skill.hasAbility(cfm.skill) == false) continue;

					list.add(factoryRoom);
				}
			}
		}
		return list;
	}

	public ArrayList<Building> GetNotCompletedBuildingList(MoveMethod moveMethod, long maxTravelTime, Room currentRoom) {
		ArrayList<Building> list = new ArrayList<Building>();
		for (Building building : buildingList) {
			long travelTime = this.GetTravelTime(moveMethod, building, currentRoom.GetParentBuilding());
			if (travelTime > maxTravelTime) continue;
			if (building.IsBuildCompleted() == true) continue;
			list.add(building);
		}
		return list;
	}
}
