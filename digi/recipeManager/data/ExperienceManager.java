package digi.recipeManager.data;

import java.util.Arrays;

import org.bukkit.entity.Player;

/**
 * @author desht
 * 
 *         Adapted from ExperienceUtils code originally in ScrollingMenuSign.
 * 
 *         Credit to nisovin (http://forums.bukkit.org/threads/experienceutils-make-giving-taking-exp-a-bit-more-intuitive.54450/#post-1067480)
 *         for an implementation that avoids the problems of getTotalExperience(), which doesn't work properly after a player has enchanted something.
 * 
 *         Edited for the needs of this plugin (RecipeManager)
 */
public class ExperienceManager
{
	private static int		hardMaxLevel	= 65535;
	private static int		xpForNextLevel[];
	private static int		xpForLevel[];
	private final Player	player;
	
	static
	{
		lookupTables(10);
	}
	
	/**
	 * Create a new ExperienceManager for the given player.
	 * 
	 * @param player
	 *            The player for this ExperienceManager object
	 */
	public ExperienceManager(Player player)
	{
		this.player = player;
	}
	
	private static void lookupTables(int maxLevel)
	{
		xpForNextLevel = new int[maxLevel];
		xpForLevel = new int[maxLevel];
		xpForLevel[0] = 0;
		int incr = 17;
		
		for(int l = 1; l < xpForLevel.length; l++)
		{
			xpForNextLevel[l - 1] = incr;
			xpForLevel[l] = xpForLevel[l - 1] + incr;
			
			incr += (l >= 16 ? (l >= 30 ? 7 : 3) : 0);
		}
		
		xpForNextLevel[xpForNextLevel.length - 1] = incr;
	}
	
	public void addExp(int amount)
	{
		int xp = getCurrentExp() + amount;
		
		if(xp < 0)
			xp = 0;
		
		int curLvl = player.getLevel();
		int newLvl = (curLvl < 0 ? 0 : getLevelForExp(xp));
		
		if(curLvl != newLvl)
			player.setLevel(newLvl);
		
		player.setExp((float)(xp - getXpForLevel(newLvl)) / (float)xpForNextLevel[newLvl]);
	}
	
	public int getCurrentExp()
	{
		int lvl = player.getLevel();
		
		return (lvl < 0 ? 0 : getXpForLevel(lvl) + Math.round(xpForNextLevel[lvl] * player.getExp()));
	}
	
	private int getLevelForExp(int exp)
	{
		if(exp <= 0)
			return 0;
		
		if(exp > xpForLevel[xpForLevel.length - 1])
		{
			int newMax = calculateLevelForExp(exp) * 2;
			
			if(newMax > hardMaxLevel)
				throw new IllegalArgumentException("Level for exp " + exp + " > hard max level " + hardMaxLevel);
			
			lookupTables(newMax);
		}
		
		int pos = Arrays.binarySearch(xpForLevel, exp);
		
		return (pos < 0 ? -pos - 2 : pos);
	}
	
	private static int calculateLevelForExp(int exp)
	{
		int level = 0;
		int curExp = 7;
		int incr = 10;
		
		while(curExp <= exp)
		{
			curExp += incr;
			level++;
			incr += (level % 2 == 0) ? 3 : 4;
		}
		
		return level;
	}
	
	private int getXpForLevel(int level)
	{
		if(level > hardMaxLevel)
			throw new IllegalArgumentException("Level " + level + " > hard max level " + hardMaxLevel);
		
		if(level >= xpForLevel.length)
			lookupTables(level * 2);
		
		return xpForLevel[level];
	}
}