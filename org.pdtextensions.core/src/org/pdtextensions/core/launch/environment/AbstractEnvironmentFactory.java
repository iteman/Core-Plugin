package org.pdtextensions.core.launch.environment;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.php.internal.ui.preferences.util.PreferencesSupport;

@SuppressWarnings("restriction")
public abstract class AbstractEnvironmentFactory implements EnvironmentFactory {

	@Override
	public Environment getEnvironment(IProject project) {

		IPreferenceStore store = getPreferenceStore();
		PreferencesSupport prefSupport = new PreferencesSupport(getPluginId(), store);
		String executable = prefSupport.getPreferencesValue(getExecutableKey(), null, project);
		String useProjectPhar = prefSupport.getPreferencesValue(getUseProjectKey(), null, project);
		String systemPhar = prefSupport.getPreferencesValue(getScriptKey(), null, project);
		
		if (executable != null && executable.length() > 0) {
			if (useProjectPhar != null && "true".equals(useProjectPhar) || (systemPhar == null || systemPhar.length() == 0) ) {
				return getProjectEnvironment(executable);
			}
			
			return new SysPhpSysPhar(executable, systemPhar);
		}
		
		return null;		
	}

	/**
	 * Get the preference store of the plugin using the launcher.
	 * @return
	 */
	protected abstract IPreferenceStore getPreferenceStore();
	
	/**
	 * Get the Plugin ID of the plugin using the launcher.
	 * @return
	 */
	protected abstract String getPluginId();
	
	/**
	 * 
	 * Get the {@link Environment} which uses a script inside the project for execution.
	 * @param executable
	 * @return
	 */
	protected abstract PrjPharEnvironment getProjectEnvironment(String executable);
	
	/**
	 * Get the key for the php executable.
	 * @return
	 */
	protected abstract String getExecutableKey();
	
	/**
	 * Get the key for the preference setting if the launcher should use a script per project or a global script for launching.
	 * @return
	 */
	protected abstract String getUseProjectKey();
	
	/**
	 * Get the key for the preference setting for the location of the php script to execute (if the user selected a global script for every launcher) 
	 * @return
	 */
	protected abstract String getScriptKey();

}
