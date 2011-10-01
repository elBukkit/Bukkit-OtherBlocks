// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.subject.VehicleTarget;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ContentsDrop extends DropType {
	public ContentsDrop() {
		super(DropCategory.CONTENTS);
	}

	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		// First locate the object; it's a block, storage minecart, or player
		Inventory container = null;
		if(source instanceof BlockTarget) {
			Block block = ((BlockTarget)source).getBlock();
			BlockState state = block.getState();
			if(state instanceof ContainerBlock) {
				container = ((ContainerBlock) state).getInventory();
				// If it's a furnace which is smelting, remove one of what's being smelted.
				// TODO: A way to give the user a choice whether this happens (Zar: I don't think this option is needed, default action of removing what's being smelted is probably expected)
				if(state instanceof Furnace) {
					Furnace oven = (Furnace) state;
					ItemStack cooking = container.getItem(0); // first item is the item being smelted
					if(oven.getCookTime() > 0) cooking.setAmount(cooking.getAmount()-1);
					if(cooking.getAmount() <= 0) container.setItem(0, null);
				}
			} else if(state instanceof Jukebox) { // Drop the currently playing record
				Material mat = ((Jukebox) state).getPlaying();
				if(mat != null) drop(where, new ItemStack(mat, 1), flags.naturally);
				return;
			} else if(state instanceof CreatureSpawner) { // Drop the creature in the spawner
				drop(where, flags.recipient, ((CreatureSpawner) state).getCreatureType(), new CreatureData(0));
				return;
			}
		} else { // It's not a container block, so it must be an entity
			if(source instanceof PlayerSubject) {
				container = ((PlayerSubject)source).getPlayer().getInventory();
			} else if(source instanceof VehicleTarget) {
				Entity vehicle = ((VehicleTarget)source).getVehicle();
				if(vehicle instanceof StorageMinecart) {
					container = ((StorageMinecart)vehicle).getInventory();
				}
			} else if(source instanceof CreatureSubject) {
				// Endermen!
				LivingEntity creature = ((CreatureSubject)source).getAgent();
				if(creature instanceof Enderman) {
					ItemStack stack = ((Enderman)creature).getCarriedMaterial().toItemStack(1);
					drop(where, stack, flags.naturally);
					return;
				}
			}
		}
		if(container == null) return; // Doubt this'll ever happen, but just in case
		// And now pass it on!
		drop(where, container, flags.naturally);
	}
	
	private static void drop(Location where, Inventory container, boolean naturally) {
		for(ItemStack item : container.getContents()) {
			if(item == null) continue;
			drop(where, item, naturally);
		}
	}

	@Override
	public String getName() {
		return "CONTENTS";
	}

	@Override
	public double getAmount() {
		return 1;
	}

	@Override
	public DoubleRange getAmountRange() {
		return new DoubleRange(1.0);
	}
}
