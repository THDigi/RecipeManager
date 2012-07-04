package digi.recipeManager;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.iCo6.iConomy;
import com.iCo6.system.Accounts;

public class Econ
{
	protected Economy	vaultEcon	= null;
	protected Accounts	iConomyEcon	= null;
	private boolean		enabled		= false;
	
	protected Econ()
	{
		if(Bukkit.getPluginManager().getPlugin("Vault") instanceof Vault)
		{
			RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			
			if(service != null)
			{
				vaultEcon = service.getProvider();
				
				if(vaultEcon != null)
				{
					if(vaultEcon.isEnabled())
						Bukkit.getLogger().fine("Vault detected and connected, economy available.");
					else
					{
						vaultEcon = null;
						Bukkit.getLogger().fine("Vault detected but it's disabled (no economy plugin?), economy not available.");
					}
				}
			}
		}
		else if(Bukkit.getPluginManager().getPlugin("iConomy") instanceof iConomy)
		{
			iConomyEcon = new Accounts();
			
			if(iConomyEcon != null)
				Bukkit.getLogger().fine("iConomy detected and connected, economy available.");
		}
		
		enabled = (vaultEcon != null || iConomyEcon != null);
	}
	
	/**
	 * Checks if you can use economy methods.
	 * 
	 * @return true if economy plugin detected, false otherwise
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * Gets the format of the money, defined by the economy plugin used.<br>
	 * If economy is not enabled this method will return null.
	 * 
	 * @param amount
	 *            money amount to format
	 * @return String with formatted money
	 */
	public String getFormat(double amount)
	{
		if(!enabled)
			return null;
		
		if(vaultEcon != null)
			return vaultEcon.format(amount);
		else if(iConomyEcon != null)
			return iConomy.format(amount);
		
		return null;
	}
	
	/**
	 * Gets how much money a player has.<br>
	 * If economy is not enabled this method will return 0.
	 * 
	 * @param playerName
	 *            player's name
	 * @return money player has, 0 if no economy plugin was found
	 */
	public double getMoney(String playerName)
	{
		if(!enabled)
			return 0;
		
		if(vaultEcon != null)
			return vaultEcon.getBalance(playerName);
		else if(iConomyEcon != null)
			return iConomyEcon.get(playerName).getHoldings().getBalance();
		
		return 0;
	}
	
	/**
	 * Give money to player.<br>
	 * Use negative values to take money.<br>
	 * If economy is not enabled or amount is 0, this method won't do anything
	 * 
	 * @param playerName
	 *            player's name
	 * @param amount
	 *            amount to give
	 */
	public void giveMoney(String playerName, double amount)
	{
		if(!enabled || amount == 0)
			return;
		
		if(vaultEcon != null)
			vaultEcon.depositPlayer(playerName, amount);
		else if(iConomyEcon != null)
			iConomyEcon.get(playerName).getHoldings().add(amount);
	}
}
