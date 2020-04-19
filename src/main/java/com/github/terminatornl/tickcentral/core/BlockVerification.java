package com.github.terminatornl.tickcentral.core;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.TickInterceptor;
import com.github.terminatornl.tickcentral.api.TickHub;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Random;

public class BlockVerification extends Block implements ITickableProxyInterface, TickInterceptor {

	public boolean KILL_MODE = true;
	public boolean ACTIVATED = false;
	private String UPDATE_TYPE = "EXTERNAL";

	public BlockVerification() {
		super(Material.CAKE);
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		ACTIVATED = true;
		if(KILL_MODE){
			AbortServer();
		}else{
			TickCentral.LOGGER.info("Random block updates: " + UPDATE_TYPE + "-TrueTick-OK");
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		ACTIVATED = true;
		if(KILL_MODE){
			AbortServer();
		}else{
			TickCentral.LOGGER.info("Normal block updates: " + UPDATE_TYPE + "-TrueTick-OK");
		}
	}

	@Override
	public void update() {
		ACTIVATED = true;
		if(KILL_MODE){
			AbortServer();
		}else {
			TickCentral.LOGGER.info("ITickable updates: " + UPDATE_TYPE + "-TrueTick-OK");
		}
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
		updateTick(null, null, null, null);
		if(ACTIVATED == false){
			TickCentral.LOGGER.fatal("Normal block updates: " + UPDATE_TYPE + "-TrueTick-NOT OKAY!");
			BlockVerification.AbortServer();
		}
		ACTIVATED = false;
		randomTick(null, null, null, null);
		if(ACTIVATED == false){
			TickCentral.LOGGER.fatal("Random block updates: " + UPDATE_TYPE + "-TrueTick-NOT OKAY!");
			BlockVerification.AbortServer();
		}
		ACTIVATED = false;
		update();
		if(ACTIVATED == false){
			TickCentral.LOGGER.fatal("ITickable updates: " + UPDATE_TYPE + "-TrueTick-NOT OKAY!");
			BlockVerification.AbortServer();
		}
		TickHub.INTERCEPTOR = origionalInterceptor;
	}
}
