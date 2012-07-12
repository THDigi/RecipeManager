package digi.recipeManager;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.*;

import net.minecraft.server.*;

import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.*;

import digi.recipeManager.data.*;
import digi.recipeManager.data.Item;
import digi.recipeManager.data.Recipe;

public class Recipes
{
	private ItemStack						placeholderItem		= new ItemStack(Material.LONG_GRASS, 0, (short)1337);
	private List<Craft>						craftRecipes		= new ArrayList<Craft>();
	private List<Combine>					combineRecipes		= new ArrayList<Combine>();
	private HashMap<String, Smelt>			smeltRecipes		= new HashMap<String, Smelt>();
	private HashMap<String, Fuel>			fuels				= new HashMap<String, Fuel>();
	
	private List<String>					recipeErrors		= new ArrayList<String>();
	private String							currentFile			= null;
	private int								currentFileLine		= 0;
	
	private int								craftNum			= 0;
	private int								combineNum			= 0;
	private boolean							smeltCustomRecipes	= false;
	
	protected HashMap<String, FurnaceData>	furnaceData			= new HashMap<String, FurnaceData>();
	protected HashMap<String, Double>		furnaceSmelting		= new HashMap<String, Double>();
	private int								furnaceTaskId		= 0;
	
	private Logger							log;
	
	public Recipes()
	{
		log = Bukkit.getLogger();
	}
	
	/**
	 * Gets all CRAFT recipes (workbench shaped)
	 * 
	 * @return
	 */
	public List<Craft> getCraftRecipes()
	{
		return craftRecipes;
	}
	
	/**
	 * Gets Craft recipe class from the placeholder result so you can get the real result of the recipe
	 * 
	 * @param placeholderResult
	 *            the ItemStack result placeholder
	 * @return Can be null if not found
	 */
	public Craft getCraftRecipe(ItemStack placeholderResult)
	{
		return (!isCustomRecipe(placeholderResult) || craftRecipes.size() >= placeholderResult.getAmount() ? null : craftRecipes.get(placeholderResult.getAmount()));
	}
	
	/**
	 * Gets all COMBINE recipes (workbench shapeless)
	 * 
	 * @return
	 */
	public List<Combine> getCombineRecipes()
	{
		return combineRecipes;
	}
	
	/**
	 * Gets Combine recipe class from the placeholder result so you can get the real result of the recipe
	 * 
	 * @param placeholderResult
	 *            the ItemStack result placeholder
	 * @return Can be null if not found
	 */
	public Combine getCombineRecipe(ItemStack placeholderResult)
	{
		return (!isCustomRecipe(placeholderResult) || combineRecipes.size() >= placeholderResult.getAmount() ? null : combineRecipes.get(placeholderResult.getAmount()));
	}
	
	/**
	 * Gets all SMELT recipes (furnace recipes)
	 * The string in the hashmap is the ID of the recipe
	 * 
	 * @return
	 */
	public HashMap<String, Smelt> getSmeltRecipes()
	{
		return smeltRecipes;
	}
	
	/**
	 * Gets Smelt recipe class from the ingredient
	 * 
	 * @param ingredient
	 *            the ItemStack ingredient
	 * @return Can be null if not found
	 */
	public Smelt getSmeltRecipe(ItemStack ingredient)
	{
		ItemData item = new ItemData(ingredient);
		Smelt recipe = smeltRecipes.get(item.getType() + "");
		
		if(recipe == null)
			return smeltRecipes.get(item.getType() + ":" + item.getData());
		
		return recipe;
	}
	
	/**
	 * Gets all FUEL recipes (furnace fuels)
	 * 
	 * @return
	 */
	public HashMap<String, Fuel> getFuels()
	{
		return fuels;
	}
	
	/**
	 * Gets a Fuel recipe class recipe from an ingredient (the fuel itself)
	 * 
	 * @param ingredient
	 *            the ItemStack ingredient
	 * @return
	 */
	public Fuel getFuelRecipe(ItemStack ingredient)
	{
		if(ingredient == null)
			return null;
		
		return getFuelRecipe(new ItemData(ingredient));
	}
	
	/**
	 * Gets a Fuel recipe class recipe from an ingredient (the fuel itself)
	 * 
	 * @param ingredient
	 *            the ItemData ingredient
	 * @return
	 */
	public Fuel getFuelRecipe(ItemData ingredient)
	{
		if(ingredient == null)
			return null;
		
		Fuel recipe = fuels.get(ingredient.getType() + "");
		
		if(recipe == null)
			return fuels.get(ingredient.getType() + ":" + ingredient.getData());
		
		return recipe;
	}
	
	/**
	 * Returns a FurnaceData class which has some new serval furnace specific methods.
	 * If furnace data entry does not exist it will be created and returned, if you don't want this to happen just use the other method with the 2nd argument as false.
	 * 
	 * @param location
	 *            location of the furnace
	 * @return FurnaceData object
	 */
	public FurnaceData getFurnaceData(Location location)
	{
		return getFurnaceData(location, true);
	}
	
	/**
	 * Returns a FurnaceData class which has serval furnace specific methods.<br>
	 * If 2nd argument is true and the FurnaceData object doesn't exist, it will be created and returned, using false will return null if object doesn't exist.
	 * 
	 * @param location
	 *            location of the furnace
	 * @return FurnaceData object or null if create is false and object doesn't exist
	 */
	public FurnaceData getFurnaceData(Location location, boolean create)
	{
		if(create)
		{
			String locStr = locationToString(location);
			
			if(!furnaceData.containsKey(locStr))
				furnaceData.put(locStr, new FurnaceData());
		}
		
		return furnaceData.get(locationToString(location));
	}
	
	/**
	 * Gets the placeholder item that RecipeManager uses for recipe results to track them.
	 * Only ID and Data values are appliable, the amount is used as the recipe's ID.
	 * 
	 * @return The item as ItemStack object
	 */
	public ItemStack getPlaceholderItem()
	{
		return placeholderItem;
	}
	
	/**
	 * Gets the list of the recipe loading errors
	 * 
	 * @return list of strings, never null but can be empty if no errors occured.
	 */
	public List<String> getRecipeErrors()
	{
		return recipeErrors;
	}
	
	/**
	 * Does result fit player's inventory on shift+click or player's cursor on click ?
	 * 
	 * @param player
	 *            player for inventory
	 * @param result
	 *            result in question
	 * @param cursor
	 *            cursor item if not shift+click
	 * @param shiftClick
	 *            is shift+click ?
	 * @return false if inventory or cursor is full, otherwise true.
	 */
	public boolean isResultTakeable(Player player, Item result, ItemStack cursor, boolean shiftClick)
	{
		if(shiftClick)
		{
			for(ItemStack item : player.getInventory().getContents())
			{
				if(item == null || item.getTypeId() == 0 || (result.compareItemStack(item) && item.getAmount() < item.getType().getMaxStackSize()))
					return true;
			}
		}
		else
		{
			if(cursor == null || cursor.getTypeId() == 0 || (result.compareItemStack(cursor) && cursor.getAmount() < cursor.getType().getMaxStackSize()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Check if item is the placeholder item used by RecipeManager to track its recipes
	 * Works only for WORKBENCH recipes because only those use placeholders.
	 * 
	 * @param result
	 * @return
	 */
	public boolean isCustomRecipe(ItemStack result)
	{
		return (result != null && result.getTypeId() == placeholderItem.getTypeId() && result.getDurability() == placeholderItem.getDurability());
	}
	
	/**
	 * Checks if recipe is a custom RecipeManager recipe.
	 * Works for workbench and furnace recipes as well.
	 * 
	 * @param recipe
	 * @return
	 */
	public boolean isCustomRecipe(org.bukkit.inventory.Recipe recipe)
	{
		if(recipe instanceof FurnaceRecipe)
		{
			Smelt smelt = getSmeltRecipe(((FurnaceRecipe)recipe).getInput());
			
			return (smelt != null);
		}
		
		return isCustomRecipe(recipe.getResult());
	}
	
	/**
	 * Gets all recipes for a single item
	 * Useful for printing recipes
	 * 
	 * @param item
	 *            item as ItemStack
	 * @param ingredient
	 *            item is ingredient (true) or result (false) ?
	 * @return list of recipes
	 */
	public List<Recipe> getRecipesForItem(ItemStack item, boolean ingredient)
	{
		return getRecipesForItem(new Item(item), ingredient);
	}
	
	/**
	 * Gets all recipes for a single item
	 * Useful for printing recipes
	 * 
	 * @param item
	 *            item as Item
	 * @param ingredient
	 *            item is ingredient (true) or result (false) ?
	 * @return list of recipes
	 */
	public List<Recipe> getRecipesForItem(Item item, boolean ingredient)
	{
		List<Recipe> recipes = new ArrayList<Recipe>();
		
		for(Craft r : craftRecipes)
		{
			if(ingredient)
			{
				for(ItemData i : r.getIngredients())
				{
					if(item.compareItemData(i))
					{
						recipes.add(r);
						break;
					}
				}
			}
			else
			{
				for(Item i : r.getResults())
				{
					if(item.compareItemData(i))
					{
						recipes.add(r);
						break;
					}
				}
			}
		}
		
		for(Combine r : combineRecipes)
		{
			for(ItemData i : (ingredient ? r.getIngredients() : r.getResults()))
			{
				if(item.compareItemData(i))
				{
					recipes.add(r);
					break;
				}
			}
		}
		
		for(Smelt r : smeltRecipes.values())
		{
			if(item.compareItemData(ingredient ? r.getIngredient() : r.getResult()))
				recipes.add(r);
		}
		
		if(ingredient)
		{
			for(Fuel r : fuels.values())
			{
				if(item.compareItemData(r.getFuel()))
					recipes.add(r);
			}
		}
		
		return recipes;
	}
	
	/* ====================================================================================== */
	
	/**
	 * Removes all RecipeManager's custom recipes from the server.
	 */
	public void removeCustomRecipes()
	{
		Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.recipeIterator();
		org.bukkit.inventory.Recipe recipe;
		
		while(recipes.hasNext())
		{
			if((recipe = recipes.next()) != null && isCustomRecipe(recipe))
				recipes.remove();
		}
	}
	
	/**
	 * Removes all vanilla Minecraft recipes.<br>
	 * Note: prints "(num) recipes" in console, triggered by the game, nothing to worry about tough.
	 */
	@SuppressWarnings(
	{
		"unchecked",
		"rawtypes"
	})
	public void removeDefaultRecipes() // removes only vanilla Minecraft recipes
	{
		// remove furnace recipes, easy.
		FurnaceRecipes.getInstance().recipies.keySet().removeAll(new FurnaceRecipes().recipies.keySet());
		
		Logger logger = Logger.getLogger("Minecraft");
		Filter oldFilter = logger.getFilter();
		
		logger.setFilter(new Filter()
		{
			@Override
			public boolean isLoggable(LogRecord log)
			{
				return !log.getMessage().endsWith(" recipes");
			}
		});
		
		// remove workbench recipes, not so easy...
		List workbenchRaw = new CraftingManager().recipies;
		List<String> workbench = new ArrayList<String>();
		
		logger.setFilter(oldFilter);
		
		for(Object raw : workbenchRaw)
		{
			if(raw instanceof ShapedRecipes)
				workbench.add(shapedToString(((ShapedRecipes)raw).toBukkitRecipe()));
			
			else if(raw instanceof ShapelessRecipes)
				workbench.add(shapelessToString(((ShapelessRecipes)raw).toBukkitRecipe()));
		}
		
		Iterator recipes = CraftingManager.getInstance().getRecipies().iterator();
		Object obj;
		
		while(recipes.hasNext())
		{
			obj = recipes.next();
			
			if(obj instanceof ShapedRecipes)
			{
				if(workbench.contains(shapedToString(((ShapedRecipes)obj).toBukkitRecipe())))
					recipes.remove();
			}
			else if(obj instanceof ShapelessRecipes)
			{
				if(workbench.contains(shapelessToString(((ShapelessRecipes)obj).toBukkitRecipe())))
					recipes.remove();
			}
		}
	}
	
	/**
	 * Re-adds vanilla Minecraft recipes to the game, preserving current recipes.<br>
	 * NOTE: This is diferent from getServer().resetRecipes(), that removes everything else and leaves only the vanilla recipes.
	 */
	@SuppressWarnings("unchecked")
	public void restoreDefaultRecipes()
	{
		Logger logger = Logger.getLogger("Minecraft");
		Filter oldFilter = logger.getFilter();
		
		logger.setFilter(new Filter()
		{
			@Override
			public boolean isLoggable(LogRecord log)
			{
				return !log.getMessage().endsWith(" recipes");
			}
		});
		
		FurnaceRecipes.getInstance().recipies.putAll(new FurnaceRecipes().recipies);
		CraftingManager.getInstance().recipies.addAll(new CraftingManager().recipies);
		
		logger.setFilter(oldFilter);
	}
	
	protected boolean loadRecipes(boolean simulation)
	{
		if(!simulation)
		{
			reset();
			
			switch(RecipeManager.getSettings().EXISTING_RECIPES)
			{
				case 'r':
					removeDefaultRecipes();
					break;
				
				case 'c':
					Bukkit.clearRecipes();
					break;
			}
		}
		
		File dir = new File(RecipeManager.getPlugin().getDataFolder() + File.separator + "recipes");
		
		if(!dir.exists())
			dir.mkdirs();
		
		recipeErrors.clear();
		
		loadDirectory(dir, simulation);
		
		if(recipeErrors.size() > 0)
		{
			StringBuilder errors = new StringBuilder();
			
			for(String error : recipeErrors)
			{
				errors.append(error + "\r\n");
			}
			
			Messages.log(ChatColor.RED + "There were errors processing the files: \r\n\r\n" + ChatColor.YELLOW + errors + "\r\n\r\n");
		}
		
		currentFile = null;
		currentFileLine = 0;
		
		if(!simulation)
		{
			log.fine("Total loaded:");
			log.fine("  " + craftRecipes.size() + " crafting recipes");
			log.fine("  " + combineRecipes.size() + " combining recipes");
			log.fine("  " + smeltRecipes.size() + " smelting recipes");
			log.fine("  " + fuels.size() + " fuels");
			
			// restart furnace task if any custom time smelt recipes exist
			
			boolean cancelTask = !RecipeManager.getSettings().COMPATIBILITY_CHUNKEVENTS;
			
			if(!cancelTask)
			{
				if(smeltCustomRecipes)
				{
					if(furnaceSmelting == null)
						furnaceSmelting = new HashMap<String, Double>();
					
					if(furnaceTaskId <= 0)
						furnaceTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(RecipeManager.getPlugin(), new FurnacesTask(RecipeManager.getSettings().FURNACE_TICKS), 0, RecipeManager.getSettings().FURNACE_TICKS);
				}
				else
				{
					cancelTask = true;
					furnaceSmelting = null;
				}
			}
			
			if(cancelTask && furnaceTaskId > 0)
			{
				Bukkit.getScheduler().cancelTask(furnaceTaskId);
				furnaceTaskId = -1;
			}
		}
		
		return (recipeErrors.size() == 0);
	}
	
	private void loadDirectory(File dir, boolean simulation)
	{
		log.fine("Loading '" + dir.getName() + "' folder...");
		
		String fileName;
		
		for(File file : dir.listFiles())
		{
			fileName = file.getName();
			
			if(file.isDirectory())
			{
				if(fileName.equalsIgnoreCase("disabled"))
				{
					log.fine("Skipped the 'disabled' folder");
					continue;
				}
				
				if(RecipeManager.getSettings().EXISTING_RECIPES == 'n' && fileName.equalsIgnoreCase("default"))
				{
					log.fine("Skipped the 'default' folder (due to config's 'existing-recipes' set to 'nothing')");
					continue;
				}
				
				loadDirectory(file, simulation);
			}
			else if(fileName.endsWith(".txt"))
			{
				try
				{
					loadDataFile(file, simulation);
				}
				catch(Exception e)
				{
					Messages.log(ChatColor.RED + "Failed to load '" + file.toString() + "', error:");
					e.printStackTrace();
				}
			}
		}
	}
	
	private void loadDataFile(File file, boolean simulation) throws Exception
	{
		String fileName = file.getName(); //.getPath().replace("plugins" + File.separator + "RecipeManager" + File.separator + "recipes" + File.separator, "");
		
		log.fine("Loading '" + fileName + "' file...");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
		Recipe recipeData = new Recipe();
		String error;
		String line;
		currentFile = fileName;
		currentFileLine = 0;
		
		while((line = readLine(reader)) != null)
		{
			if((line = processLine(line, false)) == null)
				continue;
			
			try
			{
				if((line = processRecipeData(line, reader, recipeData)) == null)
					continue;
				
				error = null;
				
				if(line.equalsIgnoreCase("CRAFT"))
					error = craftRecipe(line, reader, new Recipe(recipeData), simulation);
				else if(line.equalsIgnoreCase("COMBINE"))
					error = combineRecipe(line, reader, new Recipe(recipeData), simulation);
				else if(line.equalsIgnoreCase("SMELT"))
					error = smeltRecipe(line, reader, new Recipe(recipeData), simulation);
				else if(line.equalsIgnoreCase("FUEL"))
					error = fuelRecipe(line, reader, new Recipe(recipeData), simulation);
				else
					error = "<yellow>Line " + currentFileLine + " was skipped: \"" + line + "\"";
				
				if(error != null)
					recipeError(error);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		if(currentFileLine == 0)
			Messages.log("<yellow>Recipe file '" + fileName + "' is empty.");
		
		reader.close();
	}
	
	private String readLine(BufferedReader reader) throws Exception
	{
		currentFileLine++; // to easily keep track of lines
		
		return reader.readLine();
	}
	
	private String processLine(String string, boolean upperCase)
	{
		if(string == null || string.isEmpty())
			return null;
		
		String comments[] =
		{
			"//",
			"#"
		};
		
		int index;
		string = (upperCase ? string.trim().toUpperCase() : string.trim());
		
		for(String comm : comments)
		{
			index = string.indexOf(comm);
			
			if(index == 0)
				return null;
			
			if(index > -1)
				return string.substring(0, index).trim();
		}
		
		return string;
	}
	
	private void recipeError(String error)
	{
		recipeErrors.add("(" + currentFile + ":" + currentFileLine + ") " + error);
	}
	
	protected ItemData processItemData(String string, int defaultData, boolean allowData, boolean printRecipeErrors)
	{
		return new ItemData(processItem(string, defaultData, allowData, false, false, printRecipeErrors));
	}
	
	protected Item processItem(String string, int defaultData, boolean allowData, boolean allowAmount, boolean allowEnchantments, boolean printRecipeErrors)
	{
		string = string.trim();
		
		if(string.length() == 0)
			return null;
		
		String itemString[] = string.split("\\|");
		String stringArray[] = itemString[0].trim().split(":");
		
		if(stringArray.length <= 0 || stringArray[0].isEmpty())
			return new Item(0);
		
		stringArray[0] = stringArray[0].trim();
		String alias = RecipeManager.getPlugin().getAliases().get(stringArray[0]);
		
		if(alias != null)
			return processItem(string.replace(stringArray[0], alias), defaultData, allowData, allowAmount, allowEnchantments, printRecipeErrors);
		
		Material mat = Material.matchMaterial(stringArray[0]);
		
		if(mat == null)
		{
			if(printRecipeErrors)
				recipeError("Item '" + stringArray[0] + "' does not exist! (TIP: name could be different, look in readme.txt for links)");
			
			return null;
		}
		
		int type = mat.getId();
		
		if(type <= 0)
			return new Item(0);
		
		int data = defaultData;
		
		if(stringArray.length > 1)
		{
			if(allowData)
			{
				try
				{
					stringArray[1] = stringArray[1].trim();
					
					if(stringArray[1].charAt(0) != '*')
						data = Math.max(Integer.valueOf(stringArray[1]), data);
				}
				catch(Exception e)
				{
					if(printRecipeErrors)
						recipeError("Item '" + mat + " has data value that is not a number: '" + stringArray[1] + "', defaulting to " + defaultData);
				}
			}
			else if(printRecipeErrors)
				recipeError("Item '" + mat + "' can't have data value defined in this recipe's slot, data value ignored.");
		}
		
		int amount = 1;
		
		if(stringArray.length > 2)
		{
			if(allowAmount)
			{
				try
				{
					amount = Math.max(Integer.valueOf(stringArray[2].trim()), 1);
				}
				catch(Exception e)
				{
					if(printRecipeErrors)
						recipeError("Item '" + mat + "' has amount value that is not a number: " + stringArray[2] + ", defaulting to 1");
				}
			}
			else if(printRecipeErrors)
				recipeError("Item '" + mat + "' can't have amount defined in this recipe's slot, amount ignored.");
		}
		
		Item item = new Item(type, amount, (short)data);
		
		if(itemString.length > 1)
		{
			if(allowEnchantments)
			{
				if(item.getAmount() > 1)
				{
					if(printRecipeErrors)
						recipeError("Item '" + mat + "' has enchantments and more than 1 amount, it can't have both, amount set to 1.");
					
					item.setAmount(1);
				}
				
				String[] enchants = itemString[1].split(",");
				String[] enchData;
				Enchantment ench;
				int level;
				
				for(String enchant : enchants)
				{
					enchant = enchant.trim();
					enchData = enchant.split(":");
					
					if(enchData.length != 2)
					{
						if(printRecipeErrors)
							recipeError("Enchantments have to be 'ENCHANTMENT:LEVEL' format. (TIP: see readme.txt for enchantment list link)");
						
						continue;
					}
					
					ench = Enchantment.getByName(enchData[0]);
					
					if(ench == null)
					{
						try
						{
							ench = Enchantment.getById(Integer.valueOf(enchData[0]));
						}
						catch(Exception e)
						{
							ench = null;
						}
						
						if(ench == null)
						{
							if(printRecipeErrors)
								recipeError("Enchantment '" + enchData[0] + "' does not exist! (TIP: name or id could be different, see readme.txt about enchantments list links)");
							
							continue;
						}
					}
					
					if(enchData[1].equals("MAX"))
						level = ench.getMaxLevel();
					
					else
					{
						try
						{
							level = Integer.valueOf(enchData[1]);
						}
						catch(Exception e)
						{
							if(printRecipeErrors)
								recipeError("Invalid enchantment level: '" + enchData[1] + "' must be a valid number, positive, zero or negative.");
							
							continue;
						}
					}
					
					item.getEnchantments().put(ench, level);
				}
			}
			else if(printRecipeErrors)
				recipeError("Item '" + mat + "' can't use enchantments in this recipe slot!");
		}
		
		return item;
	}
	
	private String craftRecipe(String line, BufferedReader reader, Recipe recipeData, boolean simulation) throws Exception
	{
		line = processLine(readLine(reader), false); // skip recipe header, read next line
		line = processRecipeData(line, reader, recipeData); // check for @flags
		
		if(line == null)
			return "Recipe has no ingredients !";
		
		HashMap<ItemData, Character> itemChars = new HashMap<ItemData, Character>();
		List<String> recipeShape = new ArrayList<String>();
		ItemData[] ingredients = new ItemData[9];
//		Item[] leftovers = new Item[9];
		
		String split[];
//		String itemSplit[];
		String resultRaw = null;
		ItemData itemData;
		Character c;
		int charIndex = 97;
		int itemIndex = 0;
		int rows = 0;
		int largest = 0;
		boolean errors = false;
		
		while(rows < 3)
		{
			if(rows > 0)
				line = processLine(readLine(reader), true);
			
			if(line == null)
				continue;
			
			// if we bump into the result prematurely (smaller recipes)
			
			if(line.charAt(0) == '=')
			{
				resultRaw = line;
				break;
			}
			
			split = line.split("\\+");
			
			int len = Math.min(split.length, 3); // max 3 items per row !
			StringBuilder recipeChars = new StringBuilder();
			
			itemIndex = rows * 3;
			largest = Math.max(len, largest);
			
			for(int i = 0; i < len; i++)
			{
				// splitting line by ">" char to get leftover item (if any)
//				itemSplit = split[i].split(">");
				itemData = processItemData(split[i] /*itemSplit[0]*/, -1, true, true);
				
				if(itemData == null)
				{
					errors = true;
					continue;
				}
				
				if(itemData.getType() > 0)
				{
					Character ic = itemChars.get(itemData);
					
					if(ic == null)
					{
						c = (char)charIndex++;
						
						itemChars.put(itemData, c);
					}
					else
						c = ic;
					
					ingredients[itemIndex] = itemData; // add non-AIR item
				}
				else
					c = ' ';
				
				recipeChars.append(c);
				
				// get leftover item (if any)
				/*
				if(itemData.type != 0 && itemSplit.length > 1)
					leftovers[itemIndex] = Main.processItem(itemSplit[1].trim(), 0, true, true);
				*/
				
				itemIndex++;
			}
			
			recipeShape.add(recipeChars.toString());
			rows++;
		}
		
		// If result wasn't found yet, it's got to be the 4th line
		
		if(resultRaw == null)
			resultRaw = processLine(readLine(reader), true);
		
		List<Item> results = getResults(resultRaw, reader, false, false);
		
		if(results == null)
			return "Invalid result item(s)! (TIP: forgot the '=' ?)";
		
		if(errors)
			return "Recipe has some invalid ingredients, fix them!";
		
		// starting the recipe and setting it's result
		
		ShapedRecipe recipe = new ShapedRecipe(new ItemStack(placeholderItem.getTypeId(), craftNum, placeholderItem.getDurability()));
		String[] chars = new String[recipeShape.size()];
		int i = 0;
		
		for(String shapeRow : recipeShape)
		{
			chars[i++] = (shapeRow.length() < largest ? (shapeRow + "    ").substring(0, largest) : shapeRow);
		}
		
		recipe.shape(chars);
		
		for(Entry<ItemData, Character> entry : itemChars.entrySet())
		{
			recipe.setIngredient(entry.getValue(), entry.getKey().getMaterial(), entry.getKey().getData());
		}
		
		if(simulation)
			return null;
		
		for(Craft r : craftRecipes)
		{
			if(r.getIngredients() == ingredients)
				return "Another recipe with the same ingredients already exists!";
		}
		
		// finally add the recipe to the server!
		
		if(!Bukkit.addRecipe(recipe))
			return "Couldn't add recipe, unknown error";
		
		craftRecipes.add(craftNum, new Craft(ingredients, results, recipeData));
		craftNum++;
		
		return null;
	}
	
	private String combineRecipe(String line, BufferedReader reader, Recipe recipeData, boolean simulation) throws Exception
	{
		line = processLine(readLine(reader), false); // skip recipe header, read next line
		line = processRecipeData(line, reader, recipeData); // check for @flags
		
		if(line == null)
			return "Recipe has no ingredients !";
		
		String[] ingredientsRaw = line.split("\\+");
		
		List<Item> results = getResults(processLine(readLine(reader), true), reader, false, false);
		
		if(results == null)
			return "Invalid result item(s)! (TIP: forgot the '=' ?)";
		
		ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(placeholderItem.getTypeId(), combineNum, placeholderItem.getDurability()));
		List<Item> ingredients = new ArrayList<Item>();
		Item item;
		int items = 0;
		
		for(String itemRaw : ingredientsRaw)
		{
			item = processItem(itemRaw, -1, true, true, false, true);
			
			if(item == null)
				return "Combine recipes can't have AIR ingredients !";
			
			if((items += item.getAmount()) > 9)
				return "Combine recipes can't have more than 9 ingredients !";
			
			ingredients.add(item);
			recipe.addIngredient(item.getAmount(), item.getMaterial(), item.getData());
		}
		
		if(simulation)
			return null;
		
		for(Combine r : combineRecipes)
		{
			if(r.getIngredients() == ingredients)
				return "Another recipe with the same ingredients already exists!";
		}
		
		if(!Bukkit.addRecipe(recipe))
			return "Couldn't add recipe, unknown error";
		
		combineRecipes.add(combineNum, new Combine(ingredients, results, recipeData));
		combineNum++;
		
		return null;
	}
	
	private String smeltRecipe(String line, BufferedReader reader, Recipe recipeData, boolean simulation) throws Exception
	{
		line = processLine(readLine(reader), false); // skip recipe header, read next line
		line = processRecipeData(line, reader, recipeData); // check for @flags
		
		if(line == null)
			return "Recipe has no ingredient !";
		
		String[] split = line.split("%");
		
		if(split.length == 0)
			return "No ingredient!";
		
		List<Item> results = getResults(processLine(readLine(reader), true), reader, true, true);
		
		if(results == null)
			return "Invalid result item! (TIP: forgot the '=' ?)";
		
		if(results.size() > 1)
			recipeError("Can't have more than 1 result in this recipe, the rest were ignored.");
		
		Item result = results.get(0);
		
		// TODO: revert allowData to true when data values for furnaces have been fixed
		ItemData ingredient = processItemData(split[0], -1, false, true);
		double minTime = -1.0;
		double maxTime = -1.0;
		
		if(split.length >= 2)
		{
			String[] timeSplit = split[1].trim().split("-");
			
			if(!timeSplit[0].equals("INSTANT"))
			{
				minTime = Double.valueOf(timeSplit[0]);
				
				if(timeSplit.length >= 2)
					maxTime = Double.valueOf(timeSplit[1]);
			}
			else
				minTime = 0.0;
			
			if(maxTime > -1.0 && minTime >= maxTime)
				return "Smelting recipe has minimum time less or equal to maximum time! (TIP: Use a single number if you want a fixed value)";
		}
		
		// the recipe
		
		if(simulation)
			return null;
		
		if(smeltRecipes.containsKey(ingredient.getType() + (ingredient.getData() == -1 ? "" : ":" + ingredient.getData())))
			return "Recipe for smelting " + ingredient.printItemData() + " already exists! (TIP: if it's a MC recipe, edit it in the 'default' folder)";
		
		if(!Bukkit.addRecipe(new FurnaceRecipe((result.getType() == 0 ? new ItemStack(placeholderItem.getTypeId()) : result.getItemStack()), ingredient.getMaterial(), ingredient.getData())))
			return "Couldn't add recipe, unknown error";
		
		smeltRecipes.put(getIngredientString(ingredient), new Smelt(ingredient, result, minTime, maxTime, recipeData));
		
		if(!smeltCustomRecipes && minTime >= 0.0)
			smeltCustomRecipes = true;
		
		return null;
	}
	
	private String fuelRecipe(String line, BufferedReader reader, Recipe recipeData, boolean simulation) throws Exception
	{
		line = processLine(readLine(reader), false); // skip recipe header, read next line
		line = processRecipeData(line, reader, recipeData); // check for @flags
		
		if(line == null)
			return "Recipe has no ingredient !";
		
		String[] split = line.split("%");
		
		if(split[1] == null)
			return "No burn time set!";
		
		String[] timeSplit = split[1].trim().split("-");
		int minTime = Math.max(Integer.valueOf(timeSplit[0]), 1);
		int maxTime = -1;
		
		if(timeSplit.length >= 2)
			maxTime = Math.max(Integer.valueOf(timeSplit[1]), maxTime);
		
		ItemData ingredient = (ItemData)processItem(split[0], -1, true, false, false, true);
		
		if(ingredient == null || ingredient.getType() == 0)
			return "Invalid item: '" + ingredient + "'";
		
		if(minTime <= 0)
			return "Fuel " + ingredient.getMaterial() + ":" + ingredient.getData() + " can't burn for negative or zero seconds!";
		
		if(maxTime > -1 && minTime >= maxTime)
			return "Fuel " + ingredient.getMaterial() + ":" + ingredient.getData() + " has minimum time less or equal to maximum time! (TIP: Use a single number if you want a fixed value)";
		
		if(simulation)
			return null;
		
		if(fuels.containsKey(ingredient))
			return "Fuel " + ingredient.getMaterial() + ":" + ingredient.getData() + " already exists!";
		
		fuels.put(getIngredientString(ingredient), new Fuel(ingredient, minTime, maxTime, recipeData));
		
		return null;
	}
	
	private List<Item> getResults(String line, BufferedReader reader, boolean allowAir, boolean oneResult) throws Exception
	{
		List<Item> results = new ArrayList<Item>();
		Item noPercentItem = null;
		String[] resultRawSplit;
		Item item;
		int totalpercentage = 0;
		
		while(line != null && !line.isEmpty() && line.charAt(0) == '=')
		{
			resultRawSplit = line.substring(1).trim().split("%"); // remove the = character and see if it's got percentage
			
			if(resultRawSplit.length >= 2)
			{
				item = processItem(resultRawSplit[1].trim(), 0, true, true, true, true);
				
				if(item == null || (!allowAir && item.getType() == 0))
				{
					recipeError("Invalid result !");
					return null;
				}
				
				try
				{
					item.setChance(Math.min(Math.max(Integer.valueOf(resultRawSplit[0].trim()), 0), 100));
				}
				catch(Exception e)
				{
					recipeError("Invalid percentage number: " + resultRawSplit[0]);
					return null;
				}
				
				if((totalpercentage += item.getChance()) > 100)
				{
					recipeError("Total result items' chance exceeds 100% ! (TIP: not defining percentage for 1 item will make it's chance fit with the rest until 100%)");
					return null;
				}
				
				results.add(item);
			}
			else
			{
				if(resultRawSplit[0] == null)
					return null;
				
				if(noPercentItem != null)
				{
					recipeError("Can't have more than 1 item without procentage to fill the rest!");
					return null;
				}
				
				noPercentItem = processItem(resultRawSplit[0].trim(), 0, true, true, true, true);
				
				if(noPercentItem == null || (!allowAir && noPercentItem.getType() == 0))
				{
					recipeError("Invalid result !");
					return null;
				}
			}
			
			line = processLine(readLine(reader), true);
		}
		
		if(noPercentItem != null)
		{
			noPercentItem.setChance(100 - totalpercentage);
			results.add(noPercentItem);
		}
		else if(results.isEmpty())
			return null;
		else if(!oneResult && totalpercentage < 100)
			results.add(new Item(0, 0, (short)0, (100 - totalpercentage)));
		
		return results;
	}
	
	private String getIngredientString(ItemData ingredient)
	{
		return ingredient.getType() + (ingredient.getData() == -1 ? "" : ":" + ingredient.getData());
	}
	
	protected void reset()
	{
		removeCustomRecipes();
		
		craftRecipes.clear();
		combineRecipes.clear();
		smeltRecipes.clear();
		fuels.clear();
		
		craftNum = 0;
		combineNum = 0;
		smeltCustomRecipes = false;
	}
	
	private String processRecipeData(String line, BufferedReader reader, Recipe recipeData) throws Exception
	{
		while(line != null && line.charAt(0) == '@')
		{
			recipeFlags(line.substring(1).trim(), recipeData); // remove the @ and process flags
			line = processLine(readLine(reader), false);
		}
		
		return (line == null ? null : line.toUpperCase());
	}
	
	private void recipeFlags(String line, Recipe recipe)
	{
		String[] split = line.split(":", 2);
		String flag = split[0].trim().toLowerCase();
		
		if(split.length < 2)
		{
			recipeError("@" + flag + " doesn't have any values!");
			return;
		}
		
		String value = split[1].trim();
		
		if(flag.equalsIgnoreCase("failmessage"))
		{
			recipe.setFailMessage(value);
			return;
		}
		
		if(flag.equalsIgnoreCase("permission"))
		{
			if(value.equalsIgnoreCase("false"))
			{
				recipe.setPermission(null);
				return;
			}
			
			split = value.split("\\|");
			value = split[0].trim();
			String failMessage = null;
			
			if(split.length > 1)
				failMessage = split[1];
			
			split = value.split("=");
			String perm = split[0].trim();
			
			if(perm.length() < 3 || !perm.matches("[a-zA-Z0-9.]+"))
			{
				recipeError("@permission has invalid value: '" + perm + "', minimum 3 chars, numbers, letters and dots only!");
				return;
			}
			
			Permission p = Bukkit.getPluginManager().getPermission(perm);
			boolean exists = (p != null);
			
			if(!exists)
			{
				p = new Permission(perm);
				p.setDescription("RecipeManager's recipes");
			}
			
			if(split.length > 1)
			{
				String defValue = split[1].trim();
				
				switch(defValue.charAt(0))
				{
					case 'T':
					case 't':
					{
						p.setDefault(PermissionDefault.TRUE);
						break;
					}
					
					case 'F':
					case 'f':
					{
						p.setDefault(PermissionDefault.FALSE);
						break;
					}
					
					case 'O':
					case 'o':
					{
						p.setDefault(PermissionDefault.OP);
						break;
					}
					
					case 'N':
					case 'n':
					{
						p.setDefault(PermissionDefault.NOT_OP);
						break;
					}
					
					default:
					{
						recipeError("Invalid default value '" + defValue + "' for permission node '" + perm + "' (TIP: use only true/false/op/non-op)");
						return;
					}
				}
				
				log.fine("Set permission " + perm + "'s default value to " + p.getDefault().toString());
			}
			
			recipe.setPermission(new Flag<String>(p.getName(), failMessage));
			
			if(!exists)
				Bukkit.getPluginManager().addPermission(p);
			
			return;
		}
		
		if(flag.equalsIgnoreCase("groups"))
		{
			if(!RecipeManager.getPermissions().isEnabled())
				return;
			
			if(value.equalsIgnoreCase("false"))
			{
				recipe.setGroups(null);
				return;
			}
			
			split = value.split("\\|");
			value = split[0].trim();
			String failMessage = null;
			
			if(split.length > 1)
				failMessage = split[1];
			
			recipe.setGroups(new Flag<Set<String>>(new HashSet<String>(), failMessage));
			split = value.split(",");
			
			for(String group : split)
			{
				group = group.trim();
				
				if(group.isEmpty())
				{
					recipeError("@" + flag + " has a group that's invalid: '" + group + "'");
					continue;
				}
				
				recipe.getGroups().getValue().add(group);
			}
			
			return;
		}
		
		if(flag.equalsIgnoreCase("worlds"))
		{
			if(value.equalsIgnoreCase("false"))
			{
				recipe.setWorlds(null);
				return;
			}
			
			split = value.split("\\|");
			value = split[0].trim();
			String failMessage = null;
			
			if(split.length > 1)
				failMessage = split[1];
			
			recipe.setWorlds(new Flag<Set<String>>(new HashSet<String>(), failMessage));
			split = value.split(",");
			
			for(String world : split)
			{
				world = world.trim();
				
				if(world.isEmpty() || Bukkit.getWorld(world) == null)
				{
					recipeError("@" + flag + " has a world that's invalid: '" + world + "'");
					continue;
				}
				
				recipe.getWorlds().getValue().add(world);
			}
			
			return;
		}
		
		if(flag.equalsIgnoreCase("proximity"))
		{
			if(value.equalsIgnoreCase("false"))
			{
				recipe.setWorlds(null);
				return;
			}
			
			split = value.split("\\|");
			value = split[0].trim();
			String failMessage = null;
			String successMessage = null;
			
			if(split.length > 1)
			{
				failMessage = split[1].trim();
				
				if(split.length > 2)
					successMessage = split[2].trim();
			}
			
			try
			{
				recipe.setProximity(new Flag<Integer>((value.equalsIgnoreCase("online") ? -1 : Integer.valueOf(value)), failMessage, successMessage));
			}
			catch(Exception e)
			{
				recipeError("@" + flag + " has invalid number value!");
				return;
			}
			
			return;
		}
		
		if(flag.equalsIgnoreCase("command"))
		{
			if(!value.equalsIgnoreCase("false"))
			{
				if(recipe.getCommands() == null)
					recipe.setCommands(new HashSet<String>());
				
				recipe.getCommands().add(value);
			}
			else
				recipe.setCommands(null);
			
			return;
		}
		
		if(flag.equalsIgnoreCase("message"))
		{
			if(!value.equalsIgnoreCase("false"))
			{
				if(recipe.getMessages() == null)
					recipe.setMessages(new HashSet<String>());
				
				recipe.getMessages().add(value);
			}
			else
				recipe.setMessages(null);
			
			return;
		}
		
		if(flag.equalsIgnoreCase("log"))
		{
			try
			{
				recipe.setLog(Boolean.valueOf(value));
			}
			catch(Exception e)
			{
				recipeError("@" + flag + " only accepts true or false! Set to true.");
				recipe.setLog(true);
			}
			
			return;
		}
		
		if(flag.equalsIgnoreCase("givexp") || flag.equalsIgnoreCase("giveexp") || flag.equalsIgnoreCase("givelevel") || flag.equalsIgnoreCase("givemoney"))
		{
			split = value.split("\\|");
			value = split[0].trim();
			String failMessage = null;
			String successMessage = null;
			
			Double val = null;
			char type = line.charAt(4);
			
			if(!value.equalsIgnoreCase("false"))
			{
				if(split.length > 1)
				{
					failMessage = split[1].trim();
					
					if(split.length > 2)
						successMessage = split[2].trim();
				}
				
				try
				{
					val = (type == 'm' ? Double.valueOf(value) : Integer.valueOf(value));
				}
				catch(Exception e)
				{
					recipeError("@" + flag + " has invalid number value!");
					return;
				}
				
				if(val == 0)
					val = null;
			}
			
			switch(type)
			{
				case 'e': // give[e]xp
				case 'x': // give[x]p
				{
					recipe.setGiveExp(val == null ? null : new Flag<Integer>(val.intValue(), failMessage, successMessage));
					break;
				}
				
				case 'l': // give[l]evel
				{
					recipe.setGiveLevel(val == null ? null : new Flag<Integer>(val.intValue(), failMessage, successMessage));
					break;
				}
				
				case 'm': // give[m]oney
				{
					if(!RecipeManager.getEconomy().isEnabled())
						return;
					
					recipe.setGiveMoney(val == null ? null : new Flag<Double>(val, failMessage, successMessage));
					break;
				}
			}
			
			return;
		}
		
		if(flag.equalsIgnoreCase("minxp") || flag.equalsIgnoreCase("minexp") || flag.equalsIgnoreCase("maxxp") || flag.equalsIgnoreCase("maxexp") || flag.equalsIgnoreCase("minlevel") || flag.equalsIgnoreCase("maxlevel") || flag.equalsIgnoreCase("minmoney") || flag.equalsIgnoreCase("maxmoney"))
		{
			split = value.split("\\|");
			value = split[0].trim();
			String failMessage = null;
			
			Double val = null;
			char type = line.charAt(3);
			
			if(!value.equalsIgnoreCase("false"))
			{
				if(split.length > 1)
					failMessage = split[1].trim();
				
				try
				{
					val = (type == 'm' ? Double.valueOf(value) : Integer.valueOf(value));
				}
				catch(Exception e)
				{
					recipeError("@" + flag + " has invalid number value!");
					return;
				}
				
				if(val <= 0)
					val = null;
			}
			
			switch(type)
			{
				case 'e': // min/max[e]xp
				case 'x': // min/max[x]p
				{
					switch(line.charAt(2))
					{
						case 'n':
							recipe.setMinExp(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
							break;
						case 'x':
							recipe.setMaxExp(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
							break;
					}
					
					break;
				}
				
				case 'l': // min/max[l]evel
				{
					switch(line.charAt(2))
					{
						case 'n':
							recipe.setMinLevel(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
							break;
						case 'x':
							recipe.setMaxLevel(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
							break;
					}
					
					break;
				}
				
				case 'm': // min/max[m]oney
				{
					if(!RecipeManager.getEconomy().isEnabled())
						return;
					
					switch(line.charAt(2))
					{
						case 'n':
							recipe.setMinMoney(val == null ? null : new Flag<Double>(val, failMessage));
							break;
						case 'x':
							recipe.setMaxMoney(val == null ? null : new Flag<Double>(val, failMessage));
							break;
					}
					
					break;
				}
			}
			
			return;
		}
		
		log.warning("Unknown flag: '" + line + "' !");
	}
	
	protected static String locationToString(Location location)
	{
		return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
	}
	
	protected static Furnace stringToFurnace(String string)
	{
		String[] split = string.split(":");
		World world = Bukkit.getWorld(split[0]);
		
		if(world == null) // world doesn't exist
			return null;
		
		Block block = world.getBlockAt(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
		
		if(block.getType() != Material.BURNING_FURNACE) // not a running furnace
			return null;
		
		BlockState blockState = block.getState();
		
		if(!(blockState instanceof Furnace)) // not really a furnace
			return null;
		
		Furnace furnace = (Furnace)blockState;
		
		if(furnace.getBurnTime() <= 0) // furnace is not really running
			return null;
		
		return furnace;
	}
	
	private String shapedToString(ShapedRecipe recipe)
	{
		StringBuilder str = new StringBuilder();
		
		for(Entry<Character, ItemStack> entry : recipe.getIngredientMap().entrySet())
		{
			if(entry.getKey() != null && entry.getValue() != null)
				str.append(entry.getKey() + "=" + entry.getValue().getType() + ":" + entry.getValue().getDurability() + ";");
		}
		
		for(String row : recipe.getShape())
		{
			str.append(row + ";");
		}
		
		return str.toString();
	}
	
	private String shapelessToString(ShapelessRecipe recipe)
	{
		StringBuilder str = new StringBuilder();
		
		for(ItemStack ingredient : recipe.getIngredientList())
		{
			if(ingredient == null)
				continue;
			
			str.append(ingredient.getType() + ":" + ingredient.getDurability() + ";");
		}
		
		return str.toString();
	}
}