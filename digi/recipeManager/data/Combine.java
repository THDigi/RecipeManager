package digi.recipeManager.data;

import java.util.List;

import org.bukkit.ChatColor;

import digi.recipeManager.*;

public class Combine extends Recipe
{
	private List<Item>	ingredients;
	private List<Item>	results;
	
	public Combine(List<Item> ingredients, List<Item> results, Recipe recipeData)
	{
		super(recipeData);
		this.ingredients = ingredients;
		this.results = results;
	}
	
	/**
	 * Get all ingredients for this recipe
	 * 
	 * @return List of ingredient items
	 */
	public List<Item> getIngredients()
	{
		return ingredients;
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
		
		for(Item item : results)
		{
			if((chance += item.getChance()) > rand)
				return item;
		}
		
		return new Item(0);
	}
	
	/**
	 * Gets the recipe in print format with colors and stuff
	 * Returns an array of strings, each index is a line.
	 * 
	 * @return recipe print lines
	 */
	@Override
	public String[] print()
	{
		StringBuilder items = new StringBuilder();
		boolean first = true;
		
		for(Item i : ingredients)
		{
			items.append((first ? "" : ChatColor.GRAY + " + ") + ChatColor.BLUE + i.printItemData());
			first = false;
		}
		
		String[] display = new String[results.size() + 2];
		
		display[0] = ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Workbench shapeless recipe:";
		display[1] = items.toString();
		
		for(int i = 0; i < results.size(); i++)
			display[i + 2] = ChatColor.GREEN + " => " + ChatColor.WHITE + results.get(i).printAuto();
		
		return display;
	}
	
	public void sendLog(String playerName, Item result)
	{
		if(log)
		{
			StringBuffer string = new StringBuffer();
			
			for(ItemData item : ingredients)
			{
				string.append("+" + item.printItemData());
			}
			
			Messages.log("[@log] " + (playerName == null ? "(unknown player)" : playerName) + " combined " + string.deleteCharAt(0).toString() + " to make " + (result == null ? "nothing" : result.printAuto()));
		}
	}
}