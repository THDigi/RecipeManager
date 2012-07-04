package digi.recipeManager.data;

public class Flag<T>
{
	private T		value			= null;
	private String	failMessage		= null;
	private String	successMessage	= null;
	
	public Flag(T value)
	{
		this.value = value;
	}
	
	public Flag(T value, String failMessage)
	{
		this.value = value;
		setFailMessage(failMessage);
	}
	
	public Flag(T value, String failMessage, String successMessage)
	{
		this.value = value;
		setFailMessage(failMessage);
		setSuccessMessage(successMessage);
	}
	
	public void setFailMessage(String failMessage)
	{
		this.failMessage = failMessage;
	}
	
	public String getFailMessage()
	{
		return failMessage;
	}
	
	public void setSuccessMessage(String successMessage)
	{
		this.successMessage = successMessage;
	}
	
	public String getSuccessMessage()
	{
		return successMessage;
	}
	
	public T getValue()
	{
		return value;
	}
	
	public void setValue(T value)
	{
		this.value = value;
	}
}