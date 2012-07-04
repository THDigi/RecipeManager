package digi.recipeManager;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.*;

import digi.recipeManager.data.*;

public class FurnacesTask implements Runnable
{
	private int		ticks;
	private Recipes	recipes;
	
	public FurnacesTask(int ticks)
	{
		this.ticks = (10 / ticks);
		
		recipes = RecipeManager.getRecipes();
	}
	
	@Override
	public void run()
	{
		Iterator<Entry<String, Double>> iterator = recipes.furnaceSmelting.entrySet().iterator();
		
		Entry<String, Double> entry;
		Furnace furnace;
		FurnaceInventory inventory;
		ItemStack smelt;
		ItemStack result;
		Item recipeResult;
		Smelt recipe;
		double time;
		
		while(iterator.hasNext())
		{
			entry = iterator.next();
			
			if((furnace = Recipes.stringToFurnace(entry.getKey())) == null)
			{
				iterator.remove();
				continue;
			}
			
			inventory = furnace.getInventory();
			smelt = inventory.getSmelting();
			
			// if there's nothing to smelt, remove furnace
			if(smelt == null || smelt.getType() == Material.AIR)
			{
				entry.setValue(0.0);
				continue;
			}
			
			recipe = recipes.getSmeltRecipe(smelt);
			
			// No custom recipe for item or it has default time
			if(recipe == null || recipe.getMinTime() <= -1.0)
			{
				entry.setValue(0.0);
				continue;
			}
			
			recipeResult = recipe.getResult();
			result = inventory.getResult();
			
			// If the result already in the furnace is something else than the recipe OR result is already max stacks, skip furnace
			if(result != null && (!recipeResult.compareItemStack(inventory.getResult()) || result.getAmount() >= result.getType().getMaxStackSize()))
			{
				entry.setValue(0.0);
				continue;
			}
			
			if(recipe.getMinTime() <= 0.0)
			{
				furnace.setCookTime((short)200);
			}
			else
			{
				time = entry.getValue();
				
				if(time >= 200 || furnace.getCookTime() == 0 || furnace.getCookTime() >= 200)
				{
					entry.setValue(0.0);
				}
				else
				{
					time = time + (ticks / recipe.getTime());
					furnace.setCookTime((short)Math.max(time, 1));
					entry.setValue(time);
				}
			}
		}
		
		iterator = null;
	}
}
