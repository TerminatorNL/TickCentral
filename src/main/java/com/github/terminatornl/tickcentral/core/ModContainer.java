package com.github.terminatornl.tickcentral.core;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.TickHub;
import com.github.terminatornl.tickcentral.api.TickInterceptor;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.*;

public class ModContainer implements net.minecraftforge.fml.common.ModContainer, TickInterceptor {

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		BlockVerification blockVerification = new BlockVerification();
		TickCentral.LOGGER.info("Testing if transformations have been applied...");

		TickInterceptor origionalExecutor = TickHub.INTERCEPTOR;
		TickHub.INTERCEPTOR = this;
		blockVerification.updateTick(null, null, null, null);
		blockVerification.randomTick(null, null, null, null);
		blockVerification.update();
		blockVerification.KILL_MODE = false;
		blockVerification.ACTIVATED = false;
		try {
			TickHub.trueUpdateTick(blockVerification, null, null, null, null);
		} catch (NoSuchMethodError e) {
			TickCentral.LOGGER.fatal("Normal block updates: TrueTick-NOT OKAY!", e);
			BlockVerification.AbortServer();
		}
		if (blockVerification.ACTIVATED == false) {
			TickCentral.LOGGER.fatal("Normal block updates: TrueTick-NOT FIRED");
			BlockVerification.AbortServer();
		}
		blockVerification.ACTIVATED = false;
		try {
			TickHub.trueRandomTick(blockVerification, null, null, null, null);
		} catch (NoSuchMethodError e) {
			TickCentral.LOGGER.fatal("Random block updates: TrueTick-NOT OKAY!", e);
			BlockVerification.AbortServer();
		}
		if (blockVerification.ACTIVATED == false) {
			TickCentral.LOGGER.fatal("Random block updates: TrueTick-NOT FIRED");
			BlockVerification.AbortServer();
		}
		blockVerification.ACTIVATED = false;
		try {
			TickHub.trueUpdate(blockVerification);
		} catch (NoSuchMethodError e) {
			TickCentral.LOGGER.fatal("ITickable updates: TrueTick-NOT OKAY!", e);
			BlockVerification.AbortServer();
		}
		if (blockVerification.ACTIVATED == false) {
			TickCentral.LOGGER.fatal("ITickable updates: TrueTick-NOT FIRED");
			BlockVerification.AbortServer();
		}
		blockVerification.ACTIVATED = false;
		blockVerification.callInternally();



		EntityVerification entityVerification = new EntityVerification();
		TickHub.INTERCEPTOR = this;
		entityVerification.onUpdate();
		entityVerification.KILL_MODE = false;
		entityVerification.ACTIVATED = false;
		try {
			TickHub.trueOnUpdate(entityVerification);
		} catch (NoSuchMethodError e) {
			TickCentral.LOGGER.fatal("Entity updates: TrueTick-NOT OKAY!", e);
			EntityVerification.AbortServer();
		}
		if (entityVerification.ACTIVATED == false) {
			TickCentral.LOGGER.fatal("Entity updates: TrueTick-NOT FIRED");
			EntityVerification.AbortServer();
		}
		entityVerification.ACTIVATED = false;
		entityVerification.callInternally();

		TickHub.INTERCEPTOR = origionalExecutor;
		TickCentral.LOGGER.info("Success!");
		return true;
	}

	@Override
	public void redirectUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
		TickCentral.LOGGER.info("Normal block updates: Redirection-OK");
	}

	@Override
	public void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
		TickCentral.LOGGER.info("Random block updates: Redirection-OK");
	}

	@Override
	public void redirectUpdate(ITickable tickable) {
		TickCentral.LOGGER.info("ITickable updates: Redirection-OK");
	}

	@Override
	public void redirectOnUpdate(Entity entity) {
		TickCentral.LOGGER.info("Entity updates: Redirection-OK");
	}

	@Override
	public String getModId() {
		return TickCentral.MODID;
	}

	@Override
	public String getName() {
		return TickCentral.NAME;
	}

	@Override
	public String getVersion() {
		return TickCentral.VERSION;
	}

	/**
	 * The location on the file system which this mod came from
	 */
	@Override
	public File getSource() {
		return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
	}

	/**
	 * The metadata for this mod
	 */
	@Override
	public ModMetadata getMetadata() {
		ModMetadata metadata = new ModMetadata();
		metadata.name = TickCentral.NAME;
		metadata.modId = TickCentral.MODID;
		metadata.authorList = new LinkedList<>();
		metadata.authorList.add("Terminator_NL");
		metadata.description = "A coremod which makes transforming classes without mixin easier, regardless if mixin is installed.";
		return metadata;
	}

	/**
	 * Attach this mod to it's metadata from the supplied metadata collection
	 *
	 * @param mc
	 */
	@Override
	public void bindMetadata(MetadataCollection mc) {

	}

	/**
	 * Set the enabled/disabled state of this mod
	 *
	 * @param enabled
	 */
	@Override
	public void setEnabledState(boolean enabled) {

	}

	/**
	 * A list of the modids that this mod requires loaded prior to loading
	 */
	@Override
	public Set<ArtifactVersion> getRequirements() {
		return new HashSet<>();
	}

	/**
	 * A list of modids that should be loaded prior to this one. The special
	 * value <strong>*</strong> indicates to load <em>after</em> any other mod.
	 */
	@Override
	public List<ArtifactVersion> getDependencies() {
		return new LinkedList<>();
	}

	/**
	 * A list of modids that should be loaded <em>after</em> this one. The
	 * special value <strong>*</strong> indicates to load <em>before</em> any
	 * other mod.
	 */
	@Override
	public List<ArtifactVersion> getDependants() {
		return new LinkedList<>();
	}

	/**
	 * A representative string encapsulating the sorting preferences for this
	 * mod
	 */
	@Override
	public String getSortingRules() {
		return "";
	}

	/**
	 * Does this mod match the supplied mod
	 *
	 * @param mod
	 */
	@Override
	public boolean matches(Object mod) {
		return mod == TickCentral.INSTANCE;
	}

	/**
	 * Get the actual mod object
	 */
	@Override
	public Object getMod() {
		return TickCentral.INSTANCE;
	}

	@Override
	public ArtifactVersion getProcessedVersion() {
		return new DefaultArtifactVersion(TickCentral.VERSION);
	}

	@Override
	public boolean isImmutable() {
		return true;
	}

	@Override
	public String getDisplayVersion() {
		return TickCentral.VERSION;
	}

	@Override
	public VersionRange acceptableMinecraftVersionRange() {
		try {
			return VersionRange.createFromVersionSpec("[0,)");
		} catch (InvalidVersionSpecificationException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	@Override
	public Certificate getSigningCertificate() {
		return null;
	}

	@Override
	public Map<String, String> getCustomModProperties() {
		return EMPTY_PROPERTIES;
	}

	@Override
	public Class<?> getCustomResourcePackClass() {
		return null;
	}

	@Override
	public Map<String, String> getSharedModDescriptor() {
		return null;
	}

	@Override
	public Disableable canBeDisabled() {
		return Disableable.NEVER;
	}

	@Override
	public String getGuiClassName() {
		return null;
	}

	@Override
	public List<String> getOwnedPackages() {
		return ImmutableList.of();
	}

	@Override
	public boolean shouldLoadInEnvironment() {
		return true;
	}

	@Override
	public URL getUpdateUrl() {
		return null;
	}

	private int classVersion = 0;

	@Override
	public void setClassVersion(int classVersion) {
		this.classVersion = classVersion;
	}

	@Override
	public int getClassVersion() {
		return classVersion;
	}
}
