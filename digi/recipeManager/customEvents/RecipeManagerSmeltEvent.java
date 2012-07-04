package digi.recipeManager.customEvents;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;

import digi.recipeManager.data.*;

/**
 * Event triggered when RecipeManager's custom furnace recipes are beeing smelted.<br>
 * Player can return null in certain situations, so be sure to prepare for that situation.<br>
 * Event can be cancelled to prevent the action.
 * 
 * @author Digi
 */
public class RecipeManagerSmeltEvent extends Event implements Cancellable
{
	private static final HandlerList	handlers	= new HandlerList();
	private boolean						cancelled	= false;
	private Smelt						recipe;
	private Fuel						fuelRecipe;
	private Item						result;
	private Block						block;
	private String						smelter;
	private String						fueler;
	
	public RecipeManagerSmeltEvent(Smelt recipe, Fuel fuelRecipe, Item result, Block block, String smelter, String fueler)
	{
		this.recipe = recipe;
		this.fuelRecipe = fuelRecipe;
		this.result = result;
		this.block = block;
		this.smelter = smelter;
		this.fueler = fueler;
	}
	
	/**
	 * @return RecipeManager's Smelt class recipe, never null
	 */
	public Smelt getRecipe()
	{
		return recipe;
	}
	
	/**
	 * Gets the fuel recipe that powered the furnace.
	 * 
	 * @return RecipeManager's Fuel class recipe or null if not found
	 */
	public Fuel getFuelRecipe()
	{
		return fuelRecipe;
	}
	
	/**
	 * @return result item or null if chance of failure occured
	 */
	public Item getResult()
	{
		return result;
	}
	
	/**
	 * Sets the result of the recipe.
	 * 
	 * @param result
	 *            the new result as ItemStack
	 */
	public void setResult(ItemStack result)
	{
		this.result = new Item(result);
	}
	
	/**
	 * Sets the result of the recipe.
	 * 
	 * @param result
	 *            the new result as Item
	 */
	public void setResult(Item result)
	{
		this.result = result;
	}
	
	/**
	 * @return furnace block of the involved event
	 */
	public Block getBlock()
	{
		return block;
	}
	
	/**
	 * Get the player's name that initially placed the ingredient for this recipe.<br>
	 * Can be null in certain situatinos!
	 * 
	 * @return smelter's name or null
	 */
	public String getSmelterName()
	{
		return smelter;
	}
	
	/**
	 * Get the Player object of the player that placed the ingredient.<br>
	 * NOTE: This returns null if player is not online or plugin couldn't get the player's name, use getSmelterName() to get his name only.<br>
	 * Shortcut for: Bukkit.getPlayerExact(event.getSmelterName());
	 * 
	 * @return Player object of the smelter or null
	 */
	public Player getSmelter()
	{
		return (smelter == null ? null : Bukkit.getPlayerExact(smelter));
	}
	
	/**
	 * Get the player's name that placed the fuel powering this recipe.<br>
	 * Can be null in certain situatinos!
	 * 
	 * @return fueler's name or null
	 */
	public String getFuelerName()
	{
		return fueler;
	}
	
	/**
	 * Get the Player object of the player that placed the fuel powering this recipe.<br>
	 * NOTE: This returns null if player is not online or plugin couldn't get the player's name, use getFuelerName() to get his name only.<br>
	 * Shortcut for: Bukkit.getPlayerExact(event.getSmelterName());
	 * 
	 * @return Player object of the fueler or null
	 */
	public Player getFueler()
	{
		return (fueler == null ? null : Bukkit.getPlayerExact(fueler));
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}
	
	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}
}
