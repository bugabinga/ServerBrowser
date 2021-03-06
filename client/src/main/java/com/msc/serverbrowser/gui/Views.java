package com.msc.serverbrowser.gui;

import java.util.HashMap;
import java.util.Map;

import com.msc.serverbrowser.constants.Paths;
import com.msc.serverbrowser.gui.controllers.implementations.ServerListAllController;
import com.msc.serverbrowser.gui.controllers.implementations.ServerListFavController;
import com.msc.serverbrowser.gui.controllers.implementations.SettingsController;
import com.msc.serverbrowser.gui.controllers.implementations.UsernameController;
import com.msc.serverbrowser.gui.controllers.implementations.VersionChangeController;
import com.msc.serverbrowser.gui.controllers.interfaces.ViewController;

/**
 * Holds all the important information related to every single view.
 *
 * @author Marcel
 */
public enum Views
{
	@SuppressWarnings("javadoc")
	SERVERS_FAV(1, "Servers | Favourites", ServerListFavController.class, Paths.VIEW_PATH + "ServerList.fxml", Paths.STYLESHEET_PATH + "serverListStyle.css"),
	@SuppressWarnings("javadoc")
	SERVERS_ALL(2, "Servers | All", ServerListAllController.class, Paths.VIEW_PATH + "ServerList.fxml", Paths.STYLESHEET_PATH + "serverListStyle.css"),
	@SuppressWarnings("javadoc")
	USERNAME_CHANGER(3, "Username Changer", UsernameController.class, Paths.VIEW_PATH + "Username.fxml", Paths.STYLESHEET_PATH + "usernameStyle.css"),
	@SuppressWarnings("javadoc")
	VERSION_CHANGER(4, "Version Changer", VersionChangeController.class, Paths.VIEW_PATH + "Version.fxml", Paths.STYLESHEET_PATH + "versionStyle.css"),
	@SuppressWarnings("javadoc")
	SETTINGS(5, "Settings", SettingsController.class, Paths.VIEW_PATH + "Settings.fxml", Paths.STYLESHEET_PATH + "settingsStyle.css");

	private final int id;

	private final String title;

	private Class<? extends ViewController> necessaryController;

	private String stylesheetPath;

	private String fxmlPath;

	private final static Map<Integer, Views> idMapping = new HashMap<>();

	// Create Mapping in order to be able to find an enum value by simply providing its id.
	static
	{
		for (final Views view : Views.values())
		{
			Views.idMapping.put(view.getId(), view);
		}
	}

	private Views(final int id, final String title, final Class<? extends ViewController> necessaryController, final String fxmlPath,
			final String stylesheetPathCss)
	{
		this.id = id;
		this.title = title;
		this.necessaryController = necessaryController;
		this.stylesheetPath = stylesheetPathCss;
		this.fxmlPath = fxmlPath;
	}

	/**
	 * @return ID of the View
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * @return The Views title that will be used in the titleBar of the application
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @return the {@link ViewController} Type to be used for this View
	 */
	public Class<? extends ViewController> getControllerType()
	{
		return necessaryController;
	}

	/**
	 * @return the path to the css stylesheet of this View
	 */
	public String getStylesheetPath()
	{
		return stylesheetPath;
	}

	/**
	 * @return the path to the FMXL file of this View
	 */
	public String getFXMLPath()
	{
		return fxmlPath;
	}

	/**
	 * Returns the Enum Value that has the given id.
	 *
	 * @param idToGet
	 *            id to check against
	 * @return the found Enum Value
	 */
	public static Views valueOf(final int idToGet)
	{
		return Views.idMapping.get(idToGet);
	}
}
