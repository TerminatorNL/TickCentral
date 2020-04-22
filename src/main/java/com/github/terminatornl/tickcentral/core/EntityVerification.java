package com.github.terminatornl.tickcentral.core;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.TickHub;
import com.github.terminatornl.tickcentral.api.TickInterceptor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Random;

public class EntityVerification extends Entity implements TickInterceptor {

	public boolean KILL_MODE = true;
	public boolean ACTIVATED = false;
	private String UPDATE_TYPE = "EXTERNAL";

	public EntityVerification() {
		super(null);
	}


	public static void AbortServer(){
		TickCentral.LOGGER.info("Unable to transform the code properly. Something is interfering with this mod.");
		FMLCommonHandler.instance().exitJava(1, false);
	}

	@Override
	public void redirectUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){

	}

	@Override
	public void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){

	}

	@Override
	public void redirectUpdate(ITickable tickable){

	}

	@Override
	public void redirectOnUpdate(Entity entity){

	}

	public void callInternally(){
		TickInterceptor origionalInterceptor = TickHub.INTERCEPTOR;
		TickHub.INTERCEPTOR = this;
		UPDATE_TYPE = "INTERNAL";
		ACTIVATED = false;
		onUpdate();
		if(ACTIVATED == false){
			TickCentral.LOGGER.fatal("Entity updates: " + UPDATE_TYPE + "-TrueTick-NOT OKAY!");
			EntityVerification.AbortServer();
		}
		TickHub.INTERCEPTOR = origionalInterceptor;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		ACTIVATED = true;
		if(KILL_MODE){
			AbortServer();
		}else {
			TickCentral.LOGGER.info("Entity updates: " + UPDATE_TYPE + "-TrueTick-OK");
		}
	}

	@Override
	protected void entityInit() {

	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 *
	 * @param compound
	 */
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {

	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 *
	 * @param compound
	 */
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {

	}
}
