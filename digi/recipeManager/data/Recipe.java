package digi.recipeManager.data;

import java.util.Set;

import org.bukkit.*;
import org.bukkit.entity.Player;

import digi.recipeManager.*;

public class Recipe
{
	private String				failMessage	= null;
	private Flag<String>		permission	= null;
	private Flag<Set<String>>	groups		= null;
	private Flag<Set<String>>	worlds		= null;
	private Flag<Integer>		proximity	= null;
	private Set<String>			commands	= null;
	private Set<String>			messages	= null;
	private Flag<Integer>		giveExp		= null;
	private Flag<Integer>		minExp		= null;
	private Flag<Integer>		maxExp		= null;
	private Flag<Integer>		giveLevel	= null;
	private Flag<Integer>		minLevel	= null;
	private Flag<Integer>		maxLevel	= null;
	private Flag<Double>		giveMoney	= null;
	private Flag<Double>		minMoney	= null;
	private Flag<Double>		maxMoney	= null;
	protected boolean			log			= false;
	
	public Recipe()
	{
	}
	
	public Recipe(Recipe recipe)
	{
		failMessage = recipe.failMessage;
		permission = recipe.permission;
		groups = recipe.groups;
		worlds = recipe.worlds;
		proximity = recipe.proximity;
		commands = recipe.commands;
		messages = recipe.messages;
		giveExp = recipe.giveExp;
		minExp = recipe.minExp;
		maxExp = recipe.maxExp;
		giveLevel = recipe.giveLevel;
		minLevel = recipe.minLevel;
		maxLevel = recipe.maxLevel;
		giveMoney = recipe.giveMoney;
		minMoney = recipe.minMoney;
		maxMoney = recipe.maxMoney;
		log = recipe.log;
	}
	
	/**
	 * This sets the failure by chance message (Default: "Recipe failed! ({chance} chance)")<br>
	 * Set to "false" to disable from printing or set to null to allow default to take over.
	 * 
	 * @param failMessage
	 *            the message or "false" or null
	 */
	public void setFailMessage(String failMessage)
	{
		this.failMessage = failMessage;
	}
	
	/**
	 * This is the failure by chance message (Default: "Recipe failed! ({chance} chance)")
	 * 
	 * @return the custom message, may be "false" if disabled or null if default should be used/not set
	 */
	public String getFailMessage()
	{
		return failMessage;
	}
	
	public void setPermission(Flag<String> permission)
	{
		this.permission = permission;
	}
	
	public Flag<String> getPermission()
	{
		return permission;
	}
	
	public void setGroups(Flag<Set<String>> groups)
	{
		this.groups = groups;
	}
	
	public Flag<Set<String>> getGroups()
	{
		return groups;
	}
	
	public void setWorlds(Flag<Set<String>> worlds)
	{
		this.worlds = worlds;
	}
	
	public Flag<Set<String>> getWorlds()
	{
		return worlds;
	}
	
	public void setProximity(Flag<Integer> proximity)
	{
		this.proximity = proximity;
	}
	
	public Flag<Integer> getProximity()
	{
		return proximity;
	}
	
	public void setCommands(Set<String> commands)
	{
		this.commands = commands;
	}
	
	public Set<String> getCommands()
	{
		return commands;
	}
	
	public void setMessages(Set<String> messages)
	{
		this.messages = messages;
	}
	
	public Set<String> getMessages()
	{
		return messages;
	}
	
	public Flag<Integer> getGiveExp()
	{
		return giveExp;
	}
	
	public void setGiveExp(Flag<Integer> flag)
	{
		giveExp = flag;
	}
	
	public Flag<Integer> getMinExp()
	{
		return minExp;
	}
	
	public void setMinExp(Flag<Integer> flag)
	{
		minExp = flag;
	}
	
	public Flag<Integer> getMaxExp()
	{
		return maxExp;
	}
	
	public void setMaxExp(Flag<Integer> flag)
	{
		maxExp = flag;
	}
	
	public Flag<Integer> getGiveLevel()
	{
		return giveLevel;
	}
	
	public void setGiveLevel(Flag<Integer> flag)
	{
		giveLevel = flag;
	}
	
	public Flag<Integer> getMinLevel()
	{
		return minLevel;
	}
	
	public void setMinLevel(Flag<Integer> flag)
	{
		minLevel = flag;
	}
	
	public Flag<Integer> getMaxLevel()
	{
		return maxLevel;
	}
	
	public void setMaxLevel(Flag<Integer> flag)
	{
		maxLevel = flag;
	}
	
	public Flag<Double> getGiveMoney()
	{
		return giveMoney;
	}
	
	public void setGiveMoney(Flag<Double> flag)
	{
		giveMoney = flag;
	}
	
	public Flag<Double> getMinMoney()
	{
		return minMoney;
	}
	
	public void setMinMoney(Flag<Double> flag)
	{
		minMoney = flag;
	}
	
	public Flag<Double> getMaxMoney()
	{
		return maxMoney;
	}
	
	public void setMaxMoney(Flag<Double> flag)
	{
		maxMoney = flag;
	}
	
	public boolean getLog()
	{
		return log;
	}
	
	public void setLog(boolean log)
	{
		this.log = log;
	}
	
	public boolean isUsablePermissions(Player player, boolean printMessages)
	{
		if(permission != null && permission.getValue() != null && !player.hasPermission(permission.getValue()))
		{
			if(printMessages)
			{
				Messages.CRAFT_NOPERMISSION.print(player, permission.getFailMessage(), new String[][]
				{
					{
						"{permission}",
						permission.getValue()
					}
				});
			}
			
			return false;
		}
		
		return true;
	}
	
	public boolean isUsableInWorld(String worldName)
	{
		return (worlds != null && worlds.getValue().size() > 0 ? worlds.getValue().contains(worldName.toLowerCase()) : true);
	}
	
	public boolean isUsableWorlds(Player player, boolean printMessages)
	{
		if(worlds != null && !player.hasPermission("recipemanager.noworld") && !isUsableInWorld(player.getWorld().getName()))
		{
			if(printMessages)
			{
				StringBuffer worldsList = new StringBuffer();
				
				for(String world : worlds.getValue())
				{
					worldsList.append(", " + world);
				}
				
				Messages.CRAFT_NOWORLD.print(player, worlds.getFailMessage(), new String[][]
				{
					{
						"{worlds}",
						worldsList.deleteCharAt(0).deleteCharAt(0).toString()
					}
				});
			}
			
			return false;
		}
		
		return true;
	}
	
	public boolean isUsableGroups(Player player, boolean printMessages)
	{
		if(groups != null)
		{
			for(String group : groups.getValue())
			{
				if(!RecipeManager.getPermissions().playerInGroup(player, group))
				{
					if(printMessages)
					{
						StringBuffer groupList = new StringBuffer();
						
						for(String str : groups.getValue())
						{
							groupList.append(", " + str);
						}
						
						Messages.CRAFT_NOGROUP.print(player, groups.getFailMessage(), new String[][]
						{
							{
								"{groups}",
								groupList.deleteCharAt(0).deleteCharAt(0).toString()
							}
						});
					}
					
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean isUsableExp(Player player, boolean printMessages)
	{
		int playerExp = new ExperienceManager(player).getCurrentExp();
//		int playerExp = player.getTotalExperience();
		
		boolean usable = true;
		
		if(minExp != null && minExp.getValue() > 0 && minExp.getValue() > playerExp)
		{
			if(printMessages)
			{
				Messages.CRAFT_MINEXP.print(player, minExp.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						"" + Math.abs(minExp.getValue())
					}
				});
			}
			
			usable = false;
		}
		
		if(maxExp != null && maxExp.getValue() > 0 && maxExp.getValue() < playerExp)
		{
			if(printMessages)
			{
				Messages.CRAFT_MAXEXP.print(player, maxExp.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						"" + Math.abs(maxExp.getValue())
					}
				});
			}
			
			usable = false;
		}
		
		if(giveExp != null && giveExp.getValue() < 0 && (playerExp + giveExp.getValue()) < 0)
		{
			if(printMessages)
			{
				Messages.CRAFT_COSTEXP.print(player, giveExp.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						"" + Math.abs(giveExp.getValue())
					}
				});
			}
			
			usable = false;
		}
		
		return usable;
	}
	
	public boolean isUsableLevel(Player player, boolean printMessages)
	{
		int playerLevel = player.getLevel();
		
		boolean usable = true;
		
		if(minLevel != null && minLevel.getValue() > 0 && minLevel.getValue() > playerLevel)
		{
			if(printMessages)
			{
				Messages.CRAFT_MINLEVEL.print(player, minLevel.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						"" + Math.abs(minLevel.getValue())
					}
				});
			}
			
			usable = false;
		}
		
		if(maxLevel != null && maxLevel.getValue() > 0 && maxLevel.getValue() < playerLevel)
		{
			if(printMessages)
			{
				Messages.CRAFT_MAXLEVEL.print(player, maxLevel.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						"" + Math.abs(maxLevel.getValue())
					}
				});
			}
			
			usable = false;
		}
		
		if(giveLevel != null && giveLevel.getValue() < 0 && (playerLevel + giveLevel.getValue()) < 0)
		{
			if(printMessages)
			{
				Messages.CRAFT_COSTLEVEL.print(player, giveLevel.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						"" + Math.abs(giveLevel.getValue())
					}
				});
			}
			
			usable = false;
		}
		
		return usable;
	}
	
	public boolean isUsableMoney(Player player, boolean printMessages)
	{
		if(!RecipeManager.getEconomy().isEnabled())
			return true;
		
		double playerMoney = RecipeManager.getEconomy().getMoney(player.getName());
		
		boolean usable = true;
		
		if(minMoney != null && minMoney.getValue() > 0 && minMoney.getValue() > playerMoney)
		{
			if(printMessages)
			{
				Messages.CRAFT_MINMONEY.print(player, minMoney.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						minMoney.getValue().toString()
					},
					{
						"{money}",
						RecipeManager.getEconomy().getFormat(Math.abs(minMoney.getValue()))
					}
				});
			}
			
			usable = false;
		}
		
		if(maxMoney != null && maxMoney.getValue() > 0 && maxMoney.getValue() < playerMoney)
		{
			if(printMessages)
			{
				Messages.CRAFT_MAXMONEY.print(player, maxMoney.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						maxMoney.getValue().toString()
					},
					{
						"{money}",
						RecipeManager.getEconomy().getFormat(Math.abs(maxMoney.getValue()))
					}
				});
			}
			
			usable = false;
		}
		
		if(giveMoney != null && giveMoney.getValue() < 0 && (playerMoney + giveMoney.getValue()) < 0)
		{
			if(printMessages)
			{
				Messages.CRAFT_COSTMONEY.print(player, giveMoney.getFailMessage(), new String[][]
				{
					{
						"{amount}",
						giveMoney.getValue().toString()
					},
					{
						"{money}",
						RecipeManager.getEconomy().getFormat(Math.abs(giveMoney.getValue()))
					}
				});
			}
			
			usable = false;
		}
		
		return usable;
	}
	
	public boolean isUsableProximity(String playerName, Location location, boolean printMessage)
	{
		if(proximity == null) // not set
			return true;
		
		if(proximity.getValue() > 0) // set to: distance blocks
		{
			Player player = Bukkit.getPlayerExact(playerName);
			
			if(player == null)
				return false; // player offline
				
			if(location.distance(player.getLocation()) > proximity.getValue())
			{
				if(printMessage)
				{
					Messages.CRAFT_NOPROXIMITY.print(player, proximity.getFailMessage(), new String[][]
					{
						{
							"{location}",
							location.getWorld().getName() + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")"
						},
						{
							"{distance}",
							"" + proximity.getValue()
						}
					});
				}
				
				return false;
			}
			else
				return true;
		}
		else
			return (Bukkit.getPlayerExact(playerName) != null); // set to: online
	}
	
	/**
	 * Checks if a specific player can use this recipe, permission, world, requirements, everything!
	 * 
	 * @param player
	 *            the player
	 * @param printMessages
	 *            send requirement messages to player ?
	 * @return
	 */
	@Deprecated
	public boolean usableBy(Player player, boolean printMessages)
	{
		return isUsableBy(player, printMessages);
	}
	
	/**
	 * Checks if a specific player can use this recipe, permission, world, requirements, everything!
	 * 
	 * @param player
	 *            the player
	 * @param printMessages
	 *            send requirement messages to player ?
	 * @return
	 */
	public boolean isUsableBy(Player player, boolean printMessages)
	{
		return (isUsablePermissions(player, printMessages) && isUsableGroups(player, printMessages) && isUsableWorlds(player, printMessages) && isUsableExp(player, printMessages) && isUsableLevel(player, printMessages) && isUsableMoney(player, printMessages));
	}
	
	/**
	 * @return true if recipe has any reward/cost/command, false otherwise
	 */
	public boolean isRewarding()
	{
		return (commands != null || giveExp != null || giveLevel != null || (RecipeManager.getEconomy().isEnabled() && giveMoney != null));
	}
	
	/**
	 * Printing method for recipes
	 * 
	 * @return
	 */
	public String[] print()
	{
		return new String[]
		{
			""
		};
	}
	
	/**
	 * Applies the "@giveexp" flag of the recipe to the player if the flag is present
	 * 
	 * @param player
	 */
	public void affectExp(Player player)
	{
		if(player == null || giveExp == null || giveExp.getValue() == 0)
			return;
		
		new ExperienceManager(player).changeExp(giveExp.getValue());
		
		/* inaccurate
		if(giveExp.getValue() < 0)
		{
			int exp = player.getTotalExperience();
			player.setTotalExperience(0);
			player.setLevel(0);
			player.setExp(0);
			player.giveExp(Math.max(exp + giveExp.getValue(), 0));
		}
		else
			player.giveExp(giveExp.getValue());
		*/
		
		(giveExp.getValue() > 0 ? Messages.CRAFT_GIVEEXP : Messages.CRAFT_TAKEEXP).print(player, giveExp.getSuccessMessage(), new String[][]
		{
			{
				"{amount}",
				"" + Math.abs(giveExp.getValue())
			}
		});
	}
	
	/**
	 * Applies the "@givelevel" flag of the recipe to the player if the flag is present
	 * 
	 * @param player
	 */
	public void affectLevel(Player player)
	{
		if(player == null || giveLevel == null || giveLevel.getValue() == 0)
			return;
		
		player.setLevel(player.getLevel() + giveLevel.getValue());
		
		(giveLevel.getValue() > 0 ? Messages.CRAFT_GIVELEVEL : Messages.CRAFT_TAKELEVEL).print(player, giveLevel.getSuccessMessage(), new String[][]
		{
			{
				"{amount}",
				"" + Math.abs(giveLevel.getValue())
			}
		});
	}
	
	/**
	 * Applies the "@givemoney" flag of the recipe to the player if the flag is present
	 * 
	 * @param player
	 */
	public void affectMoney(Player player)
	{
		if(player == null || giveMoney == null || giveMoney.getValue() == 0 || !RecipeManager.getEconomy().isEnabled())
			return;
		
		RecipeManager.getEconomy().giveMoney(player.getName(), giveMoney.getValue());
		
		(giveMoney.getValue() > 0 ? Messages.CRAFT_GIVEMONEY : Messages.CRAFT_TAKEMONEY).print(player, giveMoney.getSuccessMessage(), new String[][]
		{
			{
				"{amount}",
				giveMoney.getValue().toString()
			},
			{
				"{money}",
				RecipeManager.getEconomy().getFormat(Math.abs(giveMoney.getValue()))
			}
		});
	}
	
	private String replaceVariables(String data, Player player, ItemData ingredient, Item result)
	{
		data = data.replaceAll("\\{player\\}", (player == null ? "(nobody)" : player.getName()));
		data = data.replaceAll("\\{ingredient\\}", (ingredient == null ? "nothing" : result.printItemData()));
		data = data.replaceAll("\\{result\\}", (result == null ? "nothing" : result.printItemData()));
		
		return data;
	}
	
	/**
	 * Applies the "@commands" flag of the recipe to the player and server if the flag is present
	 * 
	 * @param player
	 * @param result
	 *            result item of the recipe, to replace the {result} variable
	 */
	public void sendCommands(Player player, ItemData ingredient, Item result)
	{
		if(player == null || commands == null || commands.size() == 0)
			return;
		
		for(String command : commands)
		{
			command = replaceVariables(command, player, ingredient, result);
			
			if(command.charAt(0) == '/')
			{
				if(player != null)
					player.chat(command);
			}
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		}
	}
	
	/**
	 * Applies the "@messages" flag of the recipe, sending the messages to the crafter
	 * 
	 * @param player
	 * @param result
	 *            result item of the recipe, to replace the {result} variable
	 */
	public void sendMessages(Player player, ItemData ingredient, Item result)
	{
		if(player == null || messages == null || messages.size() == 0)
			return;
		
		for(String message : messages)
		{
			Messages.printMessage(player, replaceVariables(message, player, ingredient, result));
		}
	}
}
