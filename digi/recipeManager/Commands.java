package digi.recipeManager;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import digi.recipeManager.data.*;

class CmdRecipes implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(!RecipeManager.getPlugin().allowedToCraft(sender))
			return true;
		
		if(args.length <= 0)
		{
			Messages.COMMAND_RMRECIPES_USAGE.print(sender, null, new String[][]
			{
				{
					"{command}",
					label
				}
			});
			
			Messages.COMMAND_RMRECIPES_WORKBENCHRECIPES.print(sender, null, new String[][]
			{
				{
					"{craftrecipes}",
					"" + RecipeManager.getRecipes().getCraftRecipes().size()
				},
				{
					"{combinerecipes}",
					"" + RecipeManager.getRecipes().getCombineRecipes().size()
				}
			});
			
			Messages.COMMAND_RMRECIPES_FURNACERECIPES.print(sender, null, new String[][]
			{
				{
					"{smeltrecipes}",
					"" + RecipeManager.getRecipes().getSmeltRecipes().size()
				},
				{
					"{fuels}",
					"" + RecipeManager.getRecipes().getFuels().size()
				}
			});
			
			return true;
		}
		
		int perPage = 10;
		String itemStr = args[0].toUpperCase();
		int page = 0;
		Item item;
		boolean ingredient;
		List<Recipe> recipes;
		int recipesNum;
		
		String name = null;
		
		if(sender instanceof Player)
			name = ((Player)sender).getName();
		
		boolean next = itemStr.equals("NEXT");
		boolean prev = (next ? false : itemStr.equals("PREV"));
		
		if(next || prev)
		{
			Page data = RecipeManager.getPlugin().playerPage.get(name);
			
			if(data == null)
				return true;
			
			item = data.item;
			ingredient = data.ingredient;
			recipes = RecipeManager.getRecipes().getRecipesForItem(item, ingredient);
			recipesNum = recipes.size();
			
			if((prev && (data.page - 1) < 0) || (next && ((data.page + 1) * perPage) > recipesNum))
				return true;
			
			if(next)
				data.page++;
			else
				data.page--;
			
			page = data.page;
			
			RecipeManager.getPlugin().playerPage.put(name, data);
		}
		else
		{
			if(itemStr.equals("THIS"))
			{
				if(!(sender instanceof Player))
				{
					sender.sendMessage("Only players can use the 'THIS' argument.");
					return true;
				}
				
				ItemStack itemStack = ((Player)sender).getItemInHand();
				
				if(itemStack == null || itemStack.getTypeId() == 0)
				{
					Messages.COMMAND_RMRECIPES_INVALIDHELDITEM.print(sender);
					return true;
				}
				else
				{
					item = new Item(itemStack);
					item.setAmount(0);
					item.getEnchantments().clear();
				}
			}
			else
			{
				item = RecipeManager.getRecipes().processItem(itemStr, 0, true, false, false, false);
				
				if(item == null)
				{
					Messages.COMMAND_RMRECIPES_INVALIDITEM.print(sender, null, new String[][]
					{
						{
							"{item}",
							itemStr.toLowerCase()
						}
					});
					return true;
				}
			}
			
			ingredient = (args.length > 1);
			recipes = RecipeManager.getRecipes().getRecipesForItem(item, ingredient);
			recipesNum = recipes.size();
			
			if(recipesNum == 0)
			{
				if(ingredient)
					Messages.COMMAND_RMRECIPES_NOINGREDIENT.print(sender, null, new String[][]
					{
						{
							"{item}",
							item.printAuto()
						}
					});
				else
					Messages.COMMAND_RMRECIPES_NORESULT.print(sender, null, new String[][]
					{
						{
							"{item}",
							item.printAuto()
						}
					});
				
				return true;
			}
			
			page = 0;
			RecipeManager.getPlugin().playerPage.put(name, new Page(item, ingredient, page));
		}
		
		sender.sendMessage(" "); // empty line
		
		if(ingredient)
		{
			Messages.COMMAND_RMRECIPES_LISTINGREDIENT.print(sender, null, new String[][]
			{
				{
					"{item}",
					item.printAuto()
				},
				{
					"{recipes}",
					recipesNum + " " + (recipesNum == 1 ? Messages.GENERAL_RECIPE.get() : Messages.GENERAL_RECIPES.get())
				}
			});
		}
		else
		{
			Messages.COMMAND_RMRECIPES_LISTRESULT.print(sender, null, new String[][]
			{
				{
					"{item}",
					item.printAuto()
				},
				{
					"{recipes}",
					recipesNum + " " + (recipesNum == 1 ? Messages.GENERAL_RECIPE.get() : Messages.GENERAL_RECIPES.get())
				}
			});
		}
		
		int maxPages = (int)Math.ceil(recipesNum / (0.0 + perPage));
		
		if(recipesNum > perPage)
		{
			Messages.COMMAND_RMRECIPES_PAGEOFPAGES.print(sender, null, new String[][]
			{
				{
					"{page}",
					"" + (page + 1)
				},
				{
					"{pages}",
					"" + maxPages
				}
			});
		}
		
		sender.sendMessage(" "); // empty line.
		
		Recipe r;
		int start = (page * perPage);
		int end = (start + perPage);
		
		for(int i = start; i < end; i++)
		{
			if(i >= recipesNum)
				break;
			
			r = recipes.get(i);
			
			if(r.getPermission() != null && !sender.hasPermission(r.getPermission().getValue()))
				continue;
			
			if(sender instanceof Player && !r.isUsableInWorld(((Player)sender).getWorld().getName()))
				continue;
			
			Messages.printMessage(sender, r.print());
			
			// TODO: finish these
			
			/*
			if(r.minExp > 0 || r.maxExp > 0 || r.minLevel > 0 || r.maxLevel > 0 || r.minMoney > 0 || r.maxMoney > 0)
			{
				Main.printMessage(sender, "Requirements: ???");
			}
			*/
			
			/*
			StringBuffer rewards = new StringBuffer();
			
			if(r.giveExp != 0)
				rewards.append(" " + (r.giveExp > 0 ? "+" : "-") + Math.abs(r.giveExp) + " experience");
			
			if(r.giveLevel != 0)
				rewards.append(" " + (r.giveLevel > 0 ? "+" : "-") + Math.abs(r.giveLevel) + " level");
			
			if(Econ.enabled)
			{
				if(r.giveMoney != 0)
					rewards.append(" " + (r.giveMoney > 0 ? "+" : "-") + Econ.moneyFormat(Math.abs(r.giveMoney)));
			}
			
			if(rewards.length() > 0)
				Main.printMessage(sender, "Rewards and costs: " + rewards);
			
			if(sender.isOp())
			{
				if(r.permission != null)
					sender.sendMessage("Permission: " + r.permission);
				
				if(r.worlds.size() > 0)
				{
					StringBuffer worlds = new StringBuffer("Worlds: ");
					
					for(String worldName : r.worlds)
					{
						worlds.append(worldName + ", ");
					}
					
					sender.sendMessage(worlds.substring(worlds.length() - 2, worlds.length()));
				}
			}
			*/
			
			sender.sendMessage(" ");
		}
		
		if(recipesNum > end)
		{
			Messages.COMMAND_RMRECIPES_NEXTAVAILABLE.print(sender, null, new String[][]
			{
				{
					"{page}",
					"" + (page + 1)
				},
				{
					"{pages}",
					"" + maxPages
				},
				{
					"{command}",
					"/recipes next"
				}
			});
		}
		else if(page > 0)
		{
			Messages.COMMAND_RMRECIPES_PREVAVAILABLE.print(sender, null, new String[][]
			{
				{
					"{page}",
					"" + (page + 1)
				},
				{
					"{pages}",
					"" + maxPages
				},
				{
					"{command}",
					"/recipes prev"
				}
			});
		}
		
		sender.sendMessage(" ");
		
		return true;
	}
}

class CmdCheck implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Messages.COMMAND_RMCHECK_CHECKING.print(sender, null, new String[][]
		{
			{
				"{folder}",
				"recipes"
			}
		});
		
		if(RecipeManager.getRecipes().loadRecipes(true))
			Messages.COMMAND_RMCHECK_VALID.print(sender);
		else if(sender instanceof Player)
			Messages.COMMAND_RMCHECK_ERRORS.print(sender);
		
		return true;
	}
}

class CmdReload implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Messages.COMMAND_RMRELOAD_RELOADING.print(sender);
		
		char lastExistingRecipes = RecipeManager.getSettings().EXISTING_RECIPES;
		
		RecipeManager.getPlugin().loadSettings();
		
		if(lastExistingRecipes != RecipeManager.getSettings().EXISTING_RECIPES && RecipeManager.getSettings().EXISTING_RECIPES == 'n')
		{
			Bukkit.getLogger().info("The 'existing-recipes' was changed to 'nothing', plugin will attempt to restore all recipes but it can't restore recipes from other plugins or mods.");
			Bukkit.getServer().resetRecipes();
		}
		
		if(RecipeManager.getRecipes().loadRecipes(false))
		{
			Messages.COMMAND_RMRELOAD_DONE.print(sender, null, new String[][]
			{
				{
					"{recipes}",
					"" + (RecipeManager.getRecipes().getCraftRecipes().size() + RecipeManager.getRecipes().getCombineRecipes().size() + RecipeManager.getRecipes().getSmeltRecipes().size() + RecipeManager.getRecipes().getFuels().size())
				}
			});
		}
		else if(sender instanceof Player)
			Messages.COMMAND_RMRELOAD_ERRORS.print(sender);
		
		return true;
	}
}