package digi.recipeManager.data;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import digi.recipeManager.*;

public class Smelt extends Recipe
{
	private ItemData	ingredient;
	private Item		result;
	private double		minTime;
	private double		maxTime;
	
	public Smelt(ItemData ingredient, Item result, double minTime, double maxTime, Recipe recipeData)
	{
		super(recipeData);
		this.ingredient = ingredient;
		this.result = result;
		this.minTime = minTime;
		this.maxTime = maxTime;
	}
	
	/**
	 * You can use .getItemStack() to get the ItemStack.
	 * 
	 * @return Recipe's ingredient ItemData
	 */
	public ItemData getIngredient()
	{
		return ingredient;
	}
	
	/**
	 * Set the ingredient item for this recipe.
	 * 
	 * @param result
	 *            the ItemData ingredient
	 */
	public void setIngredient(ItemData ingredient)
	{
		this.ingredient = ingredient;
	}
	
	/**
	 * Set the ingredient item for this recipe.
	 * Shortcut for: setIngredient(new ItemData(ingredient_item_stack));
	 * 
	 * @param result
	 *            the ItemData ingredient
	 */
	public void setIngredient(ItemStack ingredient)
	{
		setIngredient(new ItemData(ingredient));
	}
	
	/**
	 * Get the random chance of success for this recipe
	 * 
	 * @return randomly true or false depending on chance
	 */
	public boolean getChanceResult()
	{
		return (result.getChance() > RecipeManager.random.nextInt(100));
	}
	
	/**
	 * You can use .getItemStack() to get the ItemStack.
	 * 
	 * @return Recipe's result Item
	 */
	public Item getResult()
	{
		return result;
	}
	
	/**
	 * Set the result item for this recipe.
	 * 
	 * @param result
	 *            the Item result
	 */
	public void setResult(Item result)
	{
		this.result = result;
	}
	
	/**
	 * Set the result item for this recipe.<br>
	 * Shortcut for: setResult(new Item(result_item_stack));
	 * 
	 * @param result
	 *            the ItemStack result
	 */
	public void setResult(ItemStack result)
	{
		setResult(new Item(result));
	}
	
	/**
	 * @return random time between minTime and maxTime or just minTime if maxTime is not specified
	 */
	public double getTime()
	{
		return (maxTime > minTime ? minTime + RecipeManager.random.nextInt((int)(maxTime - minTime)) : minTime);
	}
	
	public double getMinTime()
	{
		return minTime;
	}
	
	public void setMinTime(double minTime)
	{
		this.minTime = minTime;
	}
	
	public double getMaxTime()
	{
		return maxTime;
	}
	
	public void setMaxTime(double maxTime)
	{
		this.maxTime = maxTime;
	}
	
	@Override
	public String[] print()
	{
		return new String[]
		{
			ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Furnace smelt recipe:",
			ChatColor.BLUE + ingredient.printItemData() + ChatColor.GREEN + " => " + ChatColor.WHITE + result.printAuto() + ChatColor.GRAY + " (" + ChatColor.RED + (minTime == -1 ? "normal speed" : (minTime == 0 ? "instant smelting" : minTime + (maxTime > minTime ? "-" + maxTime : "") + ChatColor.WHITE + " seconds")) + ChatColor.GRAY + ")"
		};
	}
	
	public void sendLog(String playerName, ItemData ingredient, Item result)
	{
		if(log)
			Messages.log("[@log] " + (playerName == null ? "(unknown player)" : playerName) + " smelted " + (ingredient == null ? "nothing" : ingredient.printItemData()) + " to make " + (result == null ? "nothing" : result.printAuto()));
	}
}