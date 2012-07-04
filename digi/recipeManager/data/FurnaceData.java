package digi.recipeManager.data;

import java.io.Serializable;

public class FurnaceData implements Serializable
{
	private static final long	serialVersionUID	= -1689042281099016546L;
	private String				smelter;
	private ItemData			smeltItem;
	private String				fueler;
	private ItemData			fuelItem;
	
	public String getSmelter()
	{
		return smelter;
	}
	
	public FurnaceData setSmelter(String playerName)
	{
		smelter = playerName;
		return this;
	}
	
	public ItemData getSmeltItem()
	{
		return smeltItem;
	}
	
	public FurnaceData setSmeltItem(ItemData smeltItem)
	{
		this.smeltItem = smeltItem;
		return this;
	}
	
	public String getFueler()
	{
		return fueler;
	}
	
	public FurnaceData setFueler(String playerName)
	{
		fueler = playerName;
		return this;
	}
	
	public ItemData getFuelItem()
	{
		return fuelItem;
	}
	
	public FurnaceData setFuelItem(ItemData fuelItem)
	{
		this.fuelItem = fuelItem;
		return this;
	}
}