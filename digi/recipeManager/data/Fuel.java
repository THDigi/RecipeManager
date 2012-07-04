package digi.recipeManager.data;

import java.util.Random;

import org.bukkit.ChatColor;

import digi.recipeManager.Messages;

public class Fuel extends Recipe
{
	private ItemData	fuel;
	private int			minTime;
	private int			maxTime;
	
	/**
	 * The fuel recipe constructor
	 * 
	 * @param fuel
	 *            the ItemData fuel
	 * @param minTime
	 *            minimum time for random or fixed
	 * @param maxTime
	 *            maximum time for random (do not set to allow fixed value)
	 * @param recipeData
	 *            the recipe flags
	 */
	public Fuel(ItemData fuel, int minTime, int maxTime, Recipe recipeData)
	{
		super(recipeData);
		setFuel(fuel);
		setMinTime(minTime);
		setMaxTime(maxTime);
	}
	
	/**
	 * Sets the item that will bun as fuel
	 * 
	 * @param fuel
	 *            the ItemData fuel
	 */
	public void setFuel(ItemData fuel)
	{
		this.fuel = fuel;
	}
	
	/**
	 * @return the fuel
	 */
	public ItemData getFuel()
	{
		return fuel;
	}
	
	/**
	 * Set the minimum time fuel can burn
	 */
	public void setMinTime(int minTime)
	{
		this.minTime = minTime;
	}
	
	/**
	 * @return minimum time fuel can burn
	 */
	public int getMinTime()
	{
		return minTime;
	}
	
	/**
	 * Set the maximum time fuel can burn
	 */
	public void setMaxTime(int maxTime)
	{
		this.maxTime = maxTime;
	}
	
	/**
	 * @return maximum time fuel can burn
	 */
	public int getMaxTime()
	{
		return maxTime;
	}
	
	/**
	 * Gets the random time between minTime and maxTime or only minTime if maxTime isn't set
	 * 
	 * @return the fuel burn time
	 */
	public int getTime()
	{
		return (maxTime > minTime ? minTime + new Random().nextInt(maxTime - minTime) : minTime);
	}
	
	@Override
	public String[] print()
	{
		return new String[]
		{
			ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Furnace fuel:",
			ChatColor.BLUE + fuel.printItemData() + ChatColor.GRAY + " (" + ChatColor.WHITE + "burns " + ChatColor.RED + minTime + (maxTime > minTime ? "-" + maxTime : "") + ChatColor.WHITE + " seconds" + ChatColor.GRAY + ")"
		};
	}
	
	public void sendLog(String playerName, ItemData ingredient)
	{
		if(log)
			Messages.log("[@log] " + (playerName == null ? "(unknown player)" : playerName) + " fueled furnace with " + (ingredient == null ? "nothing" : ingredient.printItemData()));
	}
}