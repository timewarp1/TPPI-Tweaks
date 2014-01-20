package tppitweaks.event;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import tppitweaks.client.gui.GuiHelper;
import tppitweaks.client.gui.UpdateGui;
import tppitweaks.config.ConfigurationHandler;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TPPIEventHandler
{

	private boolean shouldLoadGUI = true;

	public static boolean NBTValOnDeath;

	@SideOnly(Side.CLIENT)
	@ForgeSubscribe
	public void onGui(GuiOpenEvent event)
	{
		if (shouldLoadGUI && ConfigurationHandler.showDownloadGUI && event.gui instanceof GuiMainMenu)
		{
			event.gui = new UpdateGui(event.gui, true);
			GuiHelper.updateGui = (UpdateGui) event.gui;
			shouldLoadGUI = false;

			ConfigurationHandler.stopShowingGUI();
		}
		else
		{
			Field f = null;
			try
			{
				f = FMLCommonHandler.class.getDeclaredField("brandings");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			f.setAccessible(true);
			try
			{
				if (f.get(FMLCommonHandler.instance()) == null)
				{
					FMLCommonHandler.instance().computeBranding();
					doThisAgain();
				}
				else
				{
					addStuff(f);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void doThisAgain()
	{
		Field f = null;
		try
		{
			f = FMLCommonHandler.class.getDeclaredField("brandings");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		addStuff(f);
	}
	
	@SuppressWarnings("unchecked")
	private void addStuff(Field f)
	{
		f.setAccessible(true);
		try
		{
			ImmutableList<String> list = (ImmutableList<String>) f.get(FMLCommonHandler.instance());
			List<String> newList = new ArrayList<String>();
			boolean replaced = false;
			
			for (String s : list)
			{
				if (s.contains("Feed"))
				{
					newList.add("Test Pack Please Ignore");
					replaced = true;
				}
				else if (s.equals("Test Pack Please Ignore"))
				{
					// Do nothing
				}
				else if (s.contains("Forge") && !s.contains(":"))
				{
					String[] sa = s.split(" ");
					String firstLine = sa[0] + " " + sa[1];
					String secondLine = sa[2];
					newList.add(firstLine + ":");
					newList.add("    " + secondLine);
				}
				else
					newList.add(s);
			}
			
			if (!replaced)
				newList.add("Test Pack Please Ignore");
				
			f.set(FMLCommonHandler.instance(), ImmutableList.copyOf(newList));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@ForgeSubscribe
	public void onLivingDeath(LivingDeathEvent event)
	{
		EntityPlayer entityPlayer;
		if (event.entityLiving instanceof EntityPlayer)
		{
			System.out.println("getting NBT");
			entityPlayer = (EntityPlayer) event.entityLiving;

			NBTValOnDeath = entityPlayer.getEntityData().getCompoundTag("TPPI").getBoolean("hasBook");
		}
	}

	public NBTTagCompound getTag(EntityPlayer entity, boolean useClassVal)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("hasBook", useClassVal ? NBTValOnDeath : true);

		return tag;
	}

	@ForgeSubscribe
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if (event.itemStack.getItem().itemID == mods.immibis.chunkloader.DimensionalAnchors.instance.block.blockID)
		{
			event.toolTip.add("\u00A7oA chunk loader");
		}
	}
}
