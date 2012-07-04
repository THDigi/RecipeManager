package digi.recipeManager;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings
{
	public char		EXISTING_RECIPES			= 'r';
	public boolean	REPAIR_RECIPES				= true;
	public boolean	REPAIR_ENCHANTED			= false;
	public boolean	COLOR_CONSOLE				= true;
	public boolean	RETURN_BUCKETS				= true;
	public boolean	RETURN_POTIONS				= true;
	public boolean	RETURN_BOWL					= true;
	public boolean	FUEL_RETURN_BUCKETS			= true;
	public char		FURNACE_SHIFT_CLICK			= 'f';
	public int		FURNACE_TICKS				= 1;
	public boolean	COMPATIBILITY_CHUNKEVENTS	= true;
	public boolean	METRICS						= true;
	
	public void load(FileConfiguration cfg)
	{
		EXISTING_RECIPES = cfg.getString("existing-recipes", "" + EXISTING_RECIPES).charAt(0);
		REPAIR_RECIPES = cfg.getBoolean("repair-recipes", REPAIR_RECIPES);
		REPAIR_ENCHANTED = cfg.getBoolean("repair-enchanted", REPAIR_ENCHANTED);
		COLOR_CONSOLE = cfg.getBoolean("color-console", COLOR_CONSOLE);
		RETURN_BUCKETS = cfg.getBoolean("return-empty.buckets", RETURN_BUCKETS);
		RETURN_POTIONS = cfg.getBoolean("return-empty.potions", RETURN_POTIONS);
		RETURN_BOWL = cfg.getBoolean("return-empty.bowl", RETURN_BOWL);
		FUEL_RETURN_BUCKETS = cfg.getBoolean("fuel-return-buckets", FUEL_RETURN_BUCKETS);
		FURNACE_SHIFT_CLICK = cfg.getString("furnace-shift-click", "" + FURNACE_SHIFT_CLICK).charAt(0);
		FURNACE_TICKS = cfg.getInt("furnace-ticks", FURNACE_TICKS);
		COMPATIBILITY_CHUNKEVENTS = cfg.getBoolean("compatibility.chunk-events", COMPATIBILITY_CHUNKEVENTS);
		METRICS = cfg.getBoolean("metrics", METRICS);
		
		Logger log = Bukkit.getLogger();
		
		log.fine("config.yml settings:");
		log.fine("    existing-recipes: " + EXISTING_RECIPES);
		log.fine("    repair-recipes: " + REPAIR_RECIPES);
		log.fine("    repair-enchanted: " + REPAIR_ENCHANTED);
		log.fine("    color-console: " + COLOR_CONSOLE);
		log.fine("    return-empty.buckets: " + RETURN_BUCKETS);
		log.fine("    return-empty.potions: " + RETURN_POTIONS);
		log.fine("    return-empty.bowl: " + RETURN_BOWL);
		log.fine("    fuel-return-buckets: " + FUEL_RETURN_BUCKETS);
		log.fine("    furnace-shift-click: " + FURNACE_SHIFT_CLICK);
		log.fine("    furnace-ticks: " + FURNACE_TICKS);
		log.fine("    compatibility.chunk-events: " + COMPATIBILITY_CHUNKEVENTS);
		log.fine("    metrics: " + METRICS);
	}
}