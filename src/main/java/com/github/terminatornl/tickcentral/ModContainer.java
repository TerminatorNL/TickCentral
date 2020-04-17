package com.github.terminatornl.tickcentral;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.InvalidVersionSpecificationException;
import net.minecraftforge.fml.common.versioning.VersionRange;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.*;

public class ModContainer implements net.minecraftforge.fml.common.ModContainer {

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		Verification.EarlyCheck();
		return true;
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
		return null;
	}

	/**
	 * The metadata for this mod
	 */
	@Override
	public ModMetadata getMetadata() {
		return null;
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
