package digi.recipeManager.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;

import digi.recipeManager.data.Recipe;

/**
 * Event triggered when RecipeManager's custom recipes' ingredients are placed in the workbench and the result is displayed.<br>
 * Player can return null in certain situations, so be sure to prepare for that situation.<br>
 * Event can't be cancelled but you can set the result to null to prevent player from crafting the recipe.
 * 
 * @author Digi
 */
public class RecipeManagerPreCraftEvent extends Event
{
	private static final HandlerList	handlers	= new HandlerList();
	private ItemStack					result;
	private Recipe						recipe;
	private Player						player;
	
	public RecipeManagerPreCraftEvent(Recipe recipe, ItemStack result, Player player)
	{
		this.recipe = recipe;
		this.result = result;
		this.player = player;
	}
	
	/**
	 * @return player or null if crafted by automated plugins
	 */
	public Player getPlayer()
	{
		return player;
	}
	
	/**
	 * @return recipe or null if it's a repair recipe
	 */
	public Recipe getRecipe()
	{
		return recipe;
	}
	
	/**
	 * Use getRecipe().getResults().get(0) to get the result that would've been displayed.
	 * 
	 * @return result item or null if player doesn't have access to recipe
	 */
	public ItemStack getDisplayResult()
	{
		return result;
	}
	
	/**
	 * Shortcut for: (getRecipe() == null)
	 * 
	 * @return Repair recipe true/false
	 */
	public boolean isRepair()
	{
		return recipe == null;
	}
	
	/**
	 * Sets the display result.<br>
	 * Setting this to null will prevent the player from crafting the recipe!
	 * 
	 * @param result
	 *            ItemStack displayed result or null to 'cancel' event
	 */
	public void setDisplayResult(ItemStack result)
	{
		this.result = result;
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
}
