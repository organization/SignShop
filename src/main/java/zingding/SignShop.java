package zingding;
/*
*SignShop,  Buy & Sell By Touching Sign! [ Nukkit ]
*Copyright (C) 2016  ZINGDING <tjgmltn07@gmail.com>
*
*This program is free software: you can redistribute it and/or modify
*it under the terms of the GNU Affero General Public License as published
*by the Free Software Foundation, either version 3 of the License, or
*(at your option) any later version.
*
*This program is distributed in the hope that it will be useful,
*but WITHOUT ANY WARRANTY; without even the implied warranty of
*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*GNU Affero General Public License for more details.
*
*You should have received a copy of the GNU Affero General Public License
*along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import me.onebone.economyapi.EconomyAPI;

public class SignShop extends PluginBase implements Listener {
	public String[] T = new String[] { "§f", "§b", "§c" };
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap<Player, Item> register = new HashMap();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap<Player, Item> buy = new HashMap();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap<String, Double[]> json = new HashMap();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerCommand(String name, String description, String usage, String permission, String[] aliases) {
		SimpleCommandMap map = this.getServer().getCommandMap();
		PluginCommand cmd = new PluginCommand(name, this);
		cmd.setDescription(description);
		cmd.setUsage(usage);
		cmd.setPermission(permission);
		cmd.setAliases(aliases);
		map.register(name, cmd);
	}

	public static boolean isNumber(String str) {
		boolean result = false;

		try {
			Double.parseDouble(str);
			result = true;
		} catch (Exception arg2) {
			;
		}

		return result;
	}

	public void save() {
		File data = new File(this.getDataFolder().getAbsolutePath() + "/price.txt");
		@SuppressWarnings("rawtypes")
		Iterator item = Item.getCreativeItems().iterator();

		while (item.hasNext()) {
			this.json.put(((Item) item.next()).getName(), new Double[] { Double.valueOf(0.0), Double.valueOf(0.0) });
		}

		try {
			BufferedWriter e = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(data, true), "UTF-8"));
			@SuppressWarnings("rawtypes")
			Iterator arg4 = this.json.entrySet().iterator();

			while (arg4.hasNext()) {
				@SuppressWarnings("rawtypes")
				Entry entry = (Entry) arg4.next();
				String key = (String) entry.getKey();
				e.write(key + ":0.0,0.0#\n");
			}

			e.close();
		} catch (UnsupportedEncodingException arg6) {
			arg6.printStackTrace();
		} catch (FileNotFoundException arg7) {
			arg7.printStackTrace();
		} catch (IOException arg8) {
			arg8.printStackTrace();
		}

	}

	public void load() {
		this.json.clear();
		File data = new File(this.getDataFolder().getAbsolutePath() + "/price.txt");
		String readData = "";
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(data));
		} catch (FileNotFoundException arg12) {
			arg12.printStackTrace();
		}

		String st;
		try {
			while ((st = in.readLine()) != null && st != "") {
				readData = readData + st;
			}
		} catch (IOException arg13) {
			arg13.printStackTrace();
		}

		try {
			in.close();
		} catch (IOException arg11) {
			arg11.printStackTrace();
		}

		String[] arg7;
		int arg6 = (arg7 = readData.split("#")).length;

		for (int arg5 = 0; arg5 < arg6; ++arg5) {
			String s = arg7[arg5];
			if (!s.equals("")) {
				String[] s2 = s.split(":");
				String[] s3 = s2[1].split(",");
				Double[] money = new Double[] { Double.valueOf(Double.parseDouble(s3[0])),
						Double.valueOf(Double.parseDouble(s3[1])) };
				this.json.put(s2[0], money);
			}
		}

	}

	public Integer getItemCount(Player player, Item item) {
		@SuppressWarnings("rawtypes")
		Iterator iter = player.getInventory().all(item).values().iterator();
		Integer count = Integer.valueOf(0);
		Integer id = Integer.valueOf(item.getId());
		Integer damage = Integer.valueOf(item.getDamage());

		while (iter.hasNext()) {
			Item next = (Item) iter.next();
			if (next.getId() == id.intValue() && next.getDamage() == damage.intValue()) {
				count = Integer.valueOf(count.intValue() + next.getCount());
			}
		}

		return count;
	}

	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		this.registerCommand("purchase", "Purchase items.", "/ Purchase [quantity]","SignShop.commands.buy", new String[] { "buy" });
		this.registerCommand("Sales", "Sell items.", "/ Sales [quantity]", "SignShop.commands.sell", new String[] { "sell" });
		this.registerCommand("shop", "Manage your store.", "/shop", "SignShop.commands.shop", new String[] { "shop" });
		this.getDataFolder().mkdirs();
		File data = new File(this.getDataFolder().getAbsolutePath() + "/price.txt");
		if (data.exists()) {
			this.load();
		} else {
			this.save();
		}

	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName();
		Player player = this.getServer().getPlayer(sender.getName());
		Integer count;
		Item item1;
		Double[] b1;
		if (cmd.equals("purchase")) {
			if (args.length < 1) {
				sender.sendMessage(this.T[2] + "Please write down the number of purchases.");
				return true;
			}

			if (!this.buy.containsKey(player)) {
				sender.sendMessage(this.T[2] + "[Store] Please select an item in the store  .");
				return true;
			}

			item1 = (Item) this.buy.get(player);
			b1 = (Double[]) this.json.get(item1.getName());
			if (b1[0].doubleValue() <= 0.0) {
				sender.sendMessage(this.T[2] + "[Store] This item can not be purchased.");
				return true;
			}

			count = null;
			if (isNumber(args[0])) {
				count = Integer.valueOf(Integer.parseInt(args[0]));
			} else {
				if (!args[0].equals("all")) {
					sender.sendMessage(this.T[2] + "[Store] Enter an integer or all.");
					return true;
				}

				count = Integer
						.valueOf((int) Math.floor(EconomyAPI.getInstance().myMoney(player) / b1[0].doubleValue()));
			}

			if (EconomyAPI.getInstance().myMoney(player) < (double) count.intValue() * b1[0].doubleValue()) {
				sender.sendMessage(this.T[2] + "[Store] There is not enough money.");
				return true;
			}

			EconomyAPI.getInstance().reduceMoney(player, (double) count.intValue() * b1[0].doubleValue());
			item1.setCount(count.intValue());
			player.getInventory().addItem(new Item[] { item1 });
			player.sendMessage(this.T[0] + "I've completed my purchase!");
		} else if (cmd.equals("Sales")) {
			if (args.length < 1) {
				sender.sendMessage(this.T[2] + "Please write down the number of purchases.");
				return true;
			}

			if (!this.buy.containsKey(player)) {
				item1 = player.getInventory().getItemInHand();
				b1 = (Double[]) this.json.get(item1.getName());
				if (b1[1].doubleValue() <= 0.0) {
					sender.sendMessage(this.T[2] + "[Store] This item is not available for sale.");
					return true;
				}

				count = null;
				if (isNumber(args[0])) {
					count = Integer.valueOf(Integer.parseInt(args[0]));
				} else {
					if (!args[0].equals("all")) {
						sender.sendMessage(this.T[2] + "[Store] Enter an integer or all.");
						return true;
					}

					count = this.getItemCount(player, item1);
				}

				if (this.getItemCount(player, item1).intValue() < count.intValue()) {
					sender.sendMessage(this.T[2] + "[Store] Item is not enough.");
					return true;
				}

				EconomyAPI.getInstance().addMoney(player, (double) count.intValue() * b1[1].doubleValue());
				item1.setCount(count.intValue());
				player.getInventory().removeItem(new Item[] { item1 });
				player.sendMessage(this.T[0] + "I have completed the sale!");
				return true;
			}

			item1 = (Item) this.buy.get(player);
			b1 = (Double[]) this.json.get(item1.getName());
			count = null;
			if (b1[1].doubleValue() <= 0.0) {
				sender.sendMessage(this.T[2] + "[Store] This item is not available for sale.");
				return true;
			}

			if (isNumber(args[0])) {
				count = Integer.valueOf(Integer.parseInt(args[0]));
			} else {
				if (!args[0].equals("all")) {
					sender.sendMessage(this.T[2] + "[Store] Please enter an integer or all.");
					return true;
				}

				count = this.getItemCount(player, item1);
			}

			if (this.getItemCount(player, item1).intValue() < count.intValue()) {
				sender.sendMessage(this.T[2] + "[Store] Item is not enough.");
				return true;
			}

			EconomyAPI.getInstance().addMoney(player, (double) count.intValue() * b1[1].doubleValue());
			item1.setCount(count.intValue());
			player.getInventory().removeItem(new Item[] { item1 });
			player.sendMessage(this.T[0] + "I have completed the sale!");
		} else if (cmd.equals("shop")) {
			if (args.length < 1) {
				sender.sendMessage(
				 this.T[0] + "1. " + this.T[1] + "/Create Store [id](:[damage])" + this.T[0] + "- Create a store.");
				sender.sendMessage(this.T[0] + "2. " + this.T[1] + "/Remove Store" + this.T[0] + "-Remove the store.");
				sender.sendMessage(this.T[0] + "3. " + this.T[1] + "/Cancel Store" + this.T[0] + "-Cancel the store operation.");
				return true;
			}

			if (args[0].equals("produce")) {
				if (this.register.containsKey(player)) {
					sender.sendMessage(this.T[2] + "[Store] Already creating the store.");
					return true;
				}

				Integer[] item = new Integer[] { null, Integer.valueOf(0) };
				if (args[1].indexOf(":") == -1) {
					if (!isNumber(args[1])) {
						if (Item.fromString(args[1]) == null) {
							sender.sendMessage(this.T[2] + "[Store] No such item found.");
							return true;
						}

						item[0] = Integer.valueOf(Item.fromString(args[1]).getId());
						item[1] = Integer.valueOf(Item.fromString(args[1]).getDamage());
					} else {
						item[0] = Integer.valueOf(Integer.parseInt(args[1]));
					}
				} else if (args[1].indexOf(":") != -1) {
					String[] b = args[1].split(":");
					if (!isNumber(b[0]) || !isNumber(b[1])) {
						sender.sendMessage(this.T[2] + "[Store] Item code and damage must be an integer.");
						return true;
					}

					item[0] = Integer.valueOf(Integer.parseInt(b[0]));
					item[1] = Integer.valueOf(Integer.parseInt(b[1]));
				}

				this.register.put(player, Item.get(item[0].intValue(), item[1]));
				sender.sendMessage(this.T[0] + "Touch the [Store] sign to create a store.");
			} else if (args[0].equals("remove")) {
				if (this.register.containsKey(player)) {
					sender.sendMessage(this.T[2] + "[Store] You are already removing the store..");
					return true;
				}

				this.register.put(player, (Item) null);
				sender.sendMessage(this.T[2] + "[Store] Touch to remove the store.");
			} else {
				if (!args[0].equals("cancel")) {
					sender.sendMessage(
					this.T[0] + "1. " + this.T[1] + "/Create Store [id](:[damage])" + this.T[0] + "-Create a store.");
					sender.sendMessage(this.T[0] + "2. " + this.T[1] + "/Remove a store" + this.T[0] + "-Remove a store.");
					sender.sendMessage(this.T[0] + "3. " + this.T[1] + "/Cancel Store" + this.T[0] + "-Cancel Store.");
					return true;
				}

				if (!this.register.containsKey(player)) {
					sender.sendMessage(this.T[2] + "[Store] No action to cancel.");
					return true;
				}

				this.register.remove(player);
				sender.sendMessage(this.T[0] + "Canceled [store] operation.");
				this.register.put(player, (Item) null);
			}
		}

		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (event.getAction() == 1 && (block.getId() == 63 || block.getId() == 68)) {
			BlockEntitySign Sign = (BlockEntitySign) player.getLevel()
					.getBlockEntity(new Vector3(block.getX(), block.getY(), block.getZ()));
			String[] text = Sign.getText();
			Item item;
			if (this.register.containsKey(player)) {
				item = (Item) this.register.get(player);
				if (item == null) {
					Sign.setText("", "", "", "");
					player.sendMessage(this.T[0] + "[Store] Successfully uninstalled the store!");
				}

				Double[] money = (Double[]) this.json.get(item.getName());
				String[] moneyText = new String[2];
				if (money[0].doubleValue() <= 0.0) {
					moneyText[0] = "Can not buy";
				} else {
					moneyText[0] = Double.toString(money[0].doubleValue()) + this.T[0]
							+ EconomyAPI.getInstance().getMonetaryUnit();
				}

				if (money[1].doubleValue() <= 0.0) {
					moneyText[1] = "Not Available";
				} else {
					moneyText[1] = Double.toString(money[1].doubleValue()) + this.T[0]
							+ EconomyAPI.getInstance().getMonetaryUnit();
				}

				Sign.setText(this.T[1] + "[ shop ]", this.T[0] + item.getName(), this.T[0] + "purchase : " + this.T[1]
						+ moneyText[0] + this.T[0] + " Sales : " + this.T[1] + moneyText[1]);
				this.register.remove(player);
				player.sendMessage(this.T[0] + "[Store] Successfully created a store!");
			} else {
				if (text[1].length() < 2) {
					return;
				}

				item = Item.fromString(text[1].substring(2, text[1].length()));
				this.getLogger().info(item.getName());
				if (item != null && text[0].equals(this.T[1] + "[ shop ]")) {
					this.buy.put(player, item);
					player.sendMessage(this.T[0] + "=====[ shop ]=====");
					player.sendMessage(this.T[1] + text[1] + this.T[0] + "You have selected.");
					player.sendMessage(text[2] + this.T[0] + " Number of possession: " + this.T[1] + this.getItemCount(player, item));
					player.sendMessage(this.T[1] + "/Buy | Sales [quantity | all ]" + this.T[0] + "Deal with.");
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (block.getId() == 63 || block.getId() == 68) {
			BlockEntitySign Sign = (BlockEntitySign) player.getLevel()
					.getBlockEntity(new Vector3(block.getX(), block.getY(), block.getZ()));
			String[] text = Sign.getText();
			if (!player.hasPermission("SignShop.break") && text[0].equals(this.T[1] + "[ 상점 ]")) {
				event.setCancelled(true);
			}
		}

	}
}
