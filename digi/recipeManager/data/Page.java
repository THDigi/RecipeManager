package digi.recipeManager.data;

public class Page
{
	public Item		item;
	public boolean	ingredient;
	public int		page;
	
	public Page(Item item, boolean ingredient, int page)
	{
		this.item = item;
		this.ingredient = ingredient;
		this.page = page;
	}
}