package digi.recipeManager.data;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.ChatColor;

import digi.recipeManager.*;

public class Craft extends Recipe
{
	private ItemData[]	ingredients;
	private List<Item>	results;
	
	public Craft(ItemData[] ingredients, List<Item> results, Recipe recipeData)
	{
		super(recipeData);
		this.ingredients = ingredients;
		this.results = results;
	}
	
	public Craft(Recipe recipeData)
	{
		super(recipeData);
	}
	
	/**
	 * Set the ingredients matrix.
	 * 
	 * @param ingredients
	 */
	public void setIngredients(ItemData[] ingredients)
	{
		this.ingredients = ingredients;
	}
	
	/**
	 * Get all ingredients for this recipe
	 * 
	 * @return List of ingredient items
	 */
	public ItemData[] getIngredients()
	{
		return ingredients;
	}
	
	/**
	 * Set all possible results for this recipe.<br>
	 * You can specify only one to be a fixed result.
	 * 
	 * @param results
	 */
	public void setResults(List<Item> results)
	{
		this.results = results;
	}
	
	/**
	 * Get all of the results in a list
	 * You shouldn't use this but it's here if needed
	 * 
	 * @return List of result items
	 */
	public List<Item> getResults()
	{
		return results;
	}
	
	/**
	 * Gets the result of the recipe or gets a random one from the list if there are more
	 * The same method is used by the plugin when crafting, use this if you want to emulate crafting
	 * Returns AIR if chance of failure occured
	 * 
	 * @return The result item
	 */
	public Item getResult()
	{
		if(results.size() == 1)
			return results.get(0);
		
		int rand = RecipeManager.random.nextInt(100);
		int chance = 0;
		Item result = null;
		
		for(Item item : results)
		{
			if((chance += item.getChance()) > rand)
			{
				result = item;
				break;
			}
		}
		
		return result;
	}
	
	// TODO test...
	/*
	public ItemStack getDisplayResult(ItemStack[] matrix)
	{
		return processResult(results.get(0), matrix).getItemStack();
	}
	
	public Item getResult(ItemStack[] matrix)
	{
		return processResult(getResult(), matrix);
	}
	
	private Item processResult(Item result, ItemStack[] matrix)
	{
	//		result.setType(-1); // FIXME
		
		if(result.getType() == -1)
		{
			result.special = "1"; // FIXME
			
			int slot = Integer.valueOf(result.special);
			
			System.out.print("Clone slot " + slot);
			
			if(matrix[slot] != null)
				return new Item(matrix[slot]);
			
			return result;
		}
		
		result.setData((short)-1); // FIXME
	//		result.special = "r-maxdmg - (1-hp + 2-hp)"; // FIXME
		result.special = "r-maxdmg - ((1-hp + 2-hp) + r-maxdmg * 10 / 100)";
		
		if(result.getData() == -1 && result.special != null)
		{
			ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			
			String dataString = new String(result.special);
			
			System.out.print("[debug] special data before = " + dataString);
			
			dataString = dataString.replace("r-hp", "" + Math.max(result.getMaterial().getMaxDurability() - result.getData(), 0));
			dataString = dataString.replace("r-data", "" + result.getData());
			dataString = dataString.replace("r-maxdmg", "" + result.getMaterial().getMaxDurability());
			
			for(int i = 0; i < 9; i++)
			{
				dataString = dataString.replace((i + 1) + "-hp", "" + (matrix[i] != null ? Math.max(matrix[i].getType().getMaxDurability() - matrix[i].getDurability(), 0) : "0"));
				dataString = dataString.replace((i + 1) + "-data", "" + (matrix[i] != null ? matrix[i].getDurability() : "0"));
				dataString = dataString.replace((i + 1) + "-maxdmg", "" + (matrix[i] != null ? matrix[i].getType().getMaxDurability() : "0"));
			}
			
			System.out.print("[debug] special data after = " + dataString);
			
			short data = result.getData();
			
			try
			{
				data = ((Double)scriptEngine.eval(dataString)).shortValue();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			System.out.print("[debug] new data value = " + data);
			
			result.setData((short)Math.min(Math.max(data, 0), 65535));
		}
		
		return result;
	}
	*/
	
	@Override
	public String[] print()
	{
		String[] slots = new String[9];
		List<String> str = new ArrayList<String>();
		HashMap<String, Character> charItems = new HashMap<String, Character>();
		char charIndex = 'a';
		Character charStr;
		
		for(int i = 0; i < 9; i++)
		{
			if(ingredients[i] == null)
				slots[i] = ChatColor.GRAY + "[" + ChatColor.BLACK + "_" + ChatColor.GRAY + "]";
			else
			{
				charStr = charItems.get(ingredients[i].printItemData());
				
				if(charStr == null)
				{
					charItems.put(ingredients[i].printItemData(), charIndex);
					charStr = charIndex;
					charIndex++;
				}
				
				slots[i] = ChatColor.GRAY + "[" + ChatColor.BLUE + charStr + ChatColor.GRAY + "]";
			}
		}
		
		str.add(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Workbench shaped recipe:");
		str.add(slots[0] + slots[1] + slots[2]);
		str.add(slots[3] + slots[4] + slots[5]);
		str.add(slots[6] + slots[7] + slots[8]);
		
		for(Entry<String, Character> entry : charItems.entrySet())
		{
			str.add("  " + ChatColor.BLUE + entry.getValue() + ChatColor.GRAY + " = " + ChatColor.WHITE + entry.getKey());
		}
		
		int offset = str.size();
		String[] display = new String[offset + results.size()];
		
		for(int i = 0; i < offset; i++)
		{
			display[i] = str.get(i);
		}
		
		for(int i = 0; i < results.size(); i++)
		{
			display[i + offset] = ChatColor.GREEN + " => " + ChatColor.WHITE + results.get(i).printAuto();
		}
		
		return display;
	}
	
	public void sendLog(String playerName, Item result)
	{
		if(log)
		{
			StringBuffer string = new StringBuffer();
			
			for(ItemData item : ingredients)
			{
				if(item != null)
					string.append("+" + item.printItemData());
			}
			
			Messages.log("[@log] " + (playerName == null ? "(unknown player)" : playerName) + " crafted " + string.deleteCharAt(0).toString() + " to make " + (result == null ? "nothing" : result.printAuto()));
		}
	}
}