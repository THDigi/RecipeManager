package digi.recipeManager.data;

import java.io.Serializable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemData implements Serializable
{
	private static final long	serialVersionUID	= -8496727107474925826L;
	protected int				type				= 0;
	protected short				data				= 0;
	
	public ItemData(int type)
	{
		this.type = type;
	}
	
	public ItemData(int type, short data)
	{
		this.type = type;
		this.data = data;
	}
	
	public ItemData(Item item)
	{
		type = item.getType();
		data = item.getData();
	}
	
	public ItemData(ItemStack item)
	{
		type = item.getTypeId();
		data = item.getDurability();
	}
	
	public void setType(Material type)
	{
		this.type = type.getId();
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setData(short data)
	{
		this.data = data;
	}
	
	public short getData()
	{
		return data;
	}
	
	public Material getMaterial()
	{
		return Material.getMaterial(type);
	}
	
	public ItemStack getItemStack()
	{
		return new ItemStack(type, 1, data);
	}
	
	public boolean compareItemStack(ItemStack item)
	{
		if(item == null)
			return false;
		
		int dur = item.getDurability();
		
		return (item.getTypeId() == type && (dur == -1 || data == -1 || dur == data));
	}
	
	public boolean compareItemData(ItemData item)
	{
		return (item != null && item.type == type && (item.data == -1 || item.data == data));
	}
	
	public String convertString()
	{
		return type + (data >= 0 ? ":" + data : "");
	}
	
	public String printItemData()
	{
		return getMaterial().toString().toLowerCase() + (data >= 0 ? ":" + data : "");
	}
	
	@Override
	public String toString()
	{
		return printItemData();
	}
}