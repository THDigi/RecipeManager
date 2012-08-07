package digi.recipeManager;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Permissions
{
	private Permission	permissions	= null;
	
	public Permissions()
	{
		if(Bukkit.getPluginManager().getPlugin("Vault") instanceof Vault)
		{
			RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			
			if(permissionProvider != null)
			{
				permissions = permissionProvider.getProvider();
				
				if(permissions.isEnabled())
					Bukkit.getLogger().fine("Vault has made group-permission available for this plugin.");
				else
				{
					permissions = null;
					Bukkit.getLogger().fine("Vault doesn't have a group-permission plugin connected.");
				}
			}
		}
	}
	
	protected void clearData()
	{
		permissions = null;
	}
	
	public boolean isEnabled()
	{
		return permissions != null;
	}
	
	public boolean playerInGroup(Player player, String group)
	{
		return (permissions == null ? false : permissions.playerInGroup(player, group));
	}
}
