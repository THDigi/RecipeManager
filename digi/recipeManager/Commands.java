package digi.recipeManager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import digi.recipeManager.data.*;
import digi.recipeManager.data.Recipe;

class CmdRecipes implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(!RecipeManager.plugin.allowedToCraft(sender))
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
					"" + RecipeManager.recipes.craftRecipes.size()
				},
				{
					"{combinerecipes}",
					"" + RecipeManager.recipes.combineRecipes.size()
				}
			});
			
			Messages.COMMAND_RMRECIPES_FURNACERECIPES.print(sender, null, new String[][]
			{
				{
					"{smeltrecipes}",
					"" + RecipeManager.recipes.smeltRecipes.size()
				},
				{
					"{fuels}",
					"" + RecipeManager.recipes.fuels.size()
				}
			});
			
			return true;
		}
		
		String itemStr = args[0].toUpperCase();
		int perPage = 10;
		int page = 0;
		int recipesNum;
		boolean ingredient;
		List<Recipe> recipes;
		Item item;
		String name = null;
		
		if(sender instanceof Player)
			name = ((Player)sender).getName();
		
		boolean next = itemStr.equals("NEXT");
		boolean prev = (next ? false : itemStr.equals("PREV"));
		
		if(next || prev)
		{
			Page data = RecipeManager.plugin.playerPage.get(name);
			
			if(data == null)
				return true;
			
			item = data.item;
			ingredient = data.ingredient;
			recipes = RecipeManager.recipes.getRecipesForItem(item, ingredient);
			recipesNum = recipes.size();
			
			if((prev && (data.page - 1) < 0) || (next && ((data.page + 1) * perPage) > recipesNum))
				return true;
			
			if(next)
				data.page++;
			else
				data.page--;
			
			page = data.page;
			
			RecipeManager.plugin.playerPage.put(name, data);
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
					item.setEnchantments(null);
				}
			}
			else
			{
				item = RecipeManager.recipes.processItem(itemStr, 0, true, false, false, false);
				
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
			recipes = RecipeManager.recipes.getRecipesForItem(item, ingredient);
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
			RecipeManager.plugin.playerPage.put(name, new Page(item, ingredient, page));
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
		
		if(RecipeManager.recipes.loadRecipes(true))
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
		
		char lastExistingRecipes = RecipeManager.settings.EXISTING_RECIPES;
		
		RecipeManager.plugin.loadSettings();
		
		if(lastExistingRecipes != RecipeManager.settings.EXISTING_RECIPES && RecipeManager.settings.EXISTING_RECIPES == 'n')
		{
			Bukkit.getLogger().info("The 'existing-recipes' was changed to 'nothing', plugin will attempt to restore all recipes but it can't restore recipes from other plugins or mods.");
			Bukkit.getServer().resetRecipes();
		}
		
		if(RecipeManager.recipes.loadRecipes(false))
		{
			Messages.COMMAND_RMRELOAD_DONE.print(sender, null, new String[][]
			{
				{
					"{recipes}",
					"" + (RecipeManager.recipes.craftRecipes.size() + RecipeManager.recipes.combineRecipes.size() + RecipeManager.recipes.smeltRecipes.size() + RecipeManager.recipes.fuels.size())
				}
			});
		}
		else if(sender instanceof Player)
			Messages.COMMAND_RMRELOAD_ERRORS.print(sender);
		
		RecipeManager.events.registerEvents();
		
		return true;
	}
}

class CmdExtract implements CommandExecutor
{
	private String	NL	= System.getProperty("line.separator");
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		File file = new File(RecipeManager.plugin.getDataFolder() + File.separator + "recipes" + File.separator + "disabled" + File.separator + "extracted recipes (" + new SimpleDateFormat("yyyy-MM-dd HH-mm").format(new Date()) + ").txt");
		
		if(file.exists())
		{
			Messages.printMessage(sender, "<red>You should wait at least a minute before using this command again to allow another file to be generated.");
			return true;
		}
		
		try
		{
			List<String> parsedCraftRecipes = new ArrayList<String>();
			List<String> parsedCombineRecipes = new ArrayList<String>();
			List<String> parsedSmeltRecipes = new ArrayList<String>();
			
			Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.getServer().recipeIterator();
			org.bukkit.inventory.Recipe r;
			
			int recipesNum = 0;
			
			while(recipes.hasNext())
			{
				r = recipes.next();
				
				if(r == null || RecipeManager.recipes.isCustomRecipe(r))
					continue;
				
				if(r instanceof ShapedRecipe)
				{
					ShapedRecipe recipe = (ShapedRecipe)r;
					StringBuilder recipeString = new StringBuilder("CRAFT" + NL);
					Map<Character, ItemStack> items = recipe.getIngredientMap();
					String[] shape = recipe.getShape();
					char[] cols;
					ItemStack item;
					
					for(String element : shape)
					{
						cols = element.toCharArray();
						
						for(int c = 0; c < cols.length; c++)
						{
							item = items.get(cols[c]);
							
							recipeString.append(parseIngredient(item));
							
							if((c + 1) < cols.length)
								recipeString.append(" + ");
						}
						
						recipeString.append(NL);
					}
					
					parseResult(recipe.getResult(), recipeString);
					
					parsedCraftRecipes.add(recipeString.toString());
				}
				else if(r instanceof ShapelessRecipe)
				{
					ShapelessRecipe recipe = (ShapelessRecipe)r;
					StringBuilder recipeString = new StringBuilder("COMBINE" + NL);
					List<ItemStack> ingredients = recipe.getIngredientList();
					int size = ingredients.size();
					
					for(int i = 0; i < size; i++)
					{
						recipeString.append(parseIngredient(ingredients.get(i)));
						
						if((i + 1) < size)
							recipeString.append(" + ");
					}
					
					recipeString.append(NL);
					parseResult(recipe.getResult(), recipeString);
					
					parsedCombineRecipes.add(recipeString.toString());
				}
				else if(r instanceof FurnaceRecipe)
				{
					FurnaceRecipe recipe = (FurnaceRecipe)r;
					StringBuilder recipeString = new StringBuilder("SMELT" + NL);
					
					recipeString.append(parseIngredient(recipe.getInput()));
					recipeString.append(NL);
					parseResult(recipe.getResult(), recipeString);
					
					parsedSmeltRecipes.add(recipeString.toString());
				}
				
				recipesNum++;
			}
			
			if(recipesNum == 0)
				sender.sendMessage("No recipes to extract, all recipes are handled by this plugin.");
			
			else
			{
				sender.sendMessage("Extracting existing unhandled recipes to '" + file.getPath() + "'...");
				
				file.createNewFile();
				BufferedWriter stream = new BufferedWriter(new FileWriter(file));
				
				stream.write("//---------------------------------------------------" + NL + "// Craft recipes" + NL + NL);
				
				for(String str : parsedCraftRecipes)
				{
					stream.write(str);
				}
				
				stream.write("//---------------------------------------------------" + NL + "// Combine recipes" + NL + NL);
				
				for(String str : parsedCombineRecipes)
				{
					stream.write(str);
				}
				
				stream.write("//---------------------------------------------------" + NL + "// Smelt recipes" + NL + NL);
				
				for(String str : parsedSmeltRecipes)
				{
					stream.write(str);
				}
				
				stream.close();
				
				sender.sendMessage("Done. Now you can add @override to individual recipes to override the original ones or set 'existing-recipes: clear' in config.yml to easily override all of them.");
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	private String parseIngredient(ItemStack item)
	{
		return (item == null ? "AIR" : item.getType() + (item.getAmount() > 1 || item.getDurability() != -1 ? ":" + item.getDurability() + (item.getAmount() > 1 ? ":" + item.getAmount() : "") : ""));
	}
	
	private void parseResult(ItemStack result, StringBuilder recipeString)
	{
		recipeString.append("= " + result.getType() + (result.getAmount() > 1 || result.getDurability() != 0 ? ":" + result.getDurability() + (result.getAmount() > 1 ? ":" + result.getAmount() : "") : ""));
		
		int enchantments = result.getEnchantments().size();
		
		if(enchantments > 0)
		{
			recipeString.append(" | ");
			int i = 0;
			
			for(Entry<Enchantment, Integer> entry : result.getEnchantments().entrySet())
			{
				recipeString.append(entry.getKey() + ":" + entry.getValue());
				
				if(++i < enchantments)
					recipeString.append(", ");
			}
		}
		
		recipeString.append(NL + NL);
	}
}