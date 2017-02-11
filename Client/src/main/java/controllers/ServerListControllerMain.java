package controllers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import data.Favourites;
import data.SampServer;
import entities.Player;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import query.SampQuery;
import util.GTA;

public abstract class ServerListControllerMain implements ViewController
{
	@FXML
	protected TableView<SampServer>			tableView;

	@FXML
	private TableColumn<SampServer, String>	columnPassword;

	@FXML
	private TableColumn<SampServer, String>	columnHostname;

	@FXML
	private TableColumn<SampServer, String>	columnPlayers;

	@FXML
	private TableColumn<SampServer, String>	columnPing;

	@FXML
	private TableColumn<SampServer, String>	columnMode;

	@FXML
	private TableColumn<SampServer, String>	columnLanguage;

	@FXML
	private TableView<Player>				playerTable;

	@FXML
	private TableColumn<Player, String>		playerName;

	@FXML
	private TableColumn<Player, String>		playerScore;

	@FXML
	protected Label							playerCount;

	@FXML
	protected Label							slotCount;

	@FXML
	protected Label							serverCount;

	@FXML
	private TextField						serverAddress;

	@FXML
	private Label							serverLagcomp;

	@FXML
	private Label							serverPing;

	@FXML
	private Label							serverPassword;

	private static Thread					threadGetPlayers;

	protected ContextMenu					menu;

	protected MenuItem						addToFavourites;

	protected MenuItem						removeFromFavourites;

	private MenuItem						connectItem;

	private MenuItem						copyIpAddressAndPort;

	@FXML
	private TextField						nameFilter;

	@FXML
	private TextField						modeFilter;

	@FXML
	private TextField						languageFilter;

	@FXML
	private ComboBox<String>				versionFilter;

	protected int							playersPlaying	= 0;

	protected int							maxSlots		= 0;

	// HACK(MSC) This is a little hacky, because it needs 3 lists in order to
	// keep all data, make sorting possible and make filtering possible.
	protected ObservableList<SampServer>	servers			= FXCollections.observableArrayList();

	protected FilteredList<SampServer>		filteredServers	= new FilteredList<>(servers);

	protected ObservableList<SampServer>	sortedServers	= FXCollections.observableArrayList();

	@Override
	public void init()
	{
		// TODO(MSC) Improve by including MaxPlayers as Secondary sorting
		// condition
		columnPlayers.setComparator((o1, o2) ->
		{
			int p1 = Integer.parseInt(o1.replaceAll("[/](.*)", ""));
			int p2 = Integer.parseInt(o2.replaceAll("[/](.*)", ""));
			return p1 < p2 ? -1 : p1 == p2 ? 0 : 1;
		});

		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		tableView.setItems(sortedServers);
	}

	@FXML
	protected void onTableViewMouseReleased(MouseEvent clicked)
	{
		if (Objects.nonNull(menu))
		{
			menu.hide();
		}

		List<SampServer> serverList = tableView.getSelectionModel().getSelectedItems();

		if (Objects.nonNull(serverList))
		{
			if (clicked.getButton().equals(MouseButton.PRIMARY))
			{
				SampServer server = serverList.get(0);
				if (Objects.nonNull(server))
				{
					updateServerInfo(server);
				}
			}
			else if (clicked.getButton().equals(MouseButton.SECONDARY))
			{
				displayMenu(serverList, clicked.getScreenX(), clicked.getScreenY());
			}
		}
	}

	@FXML
	protected void onTableViewKeyReleased(KeyEvent released)
	{
		SampServer server = tableView.getSelectionModel().getSelectedItem();

		if (released.getCode().equals(KeyCode.DOWN) || released.getCode().equals(KeyCode.KP_DOWN) || released.getCode().equals(KeyCode.KP_UP) || released.getCode().equals(KeyCode.UP))
		{
			updateServerInfo(server);
		}
	}

	@FXML
	public void onFilterSettingsChange()
	{
		filteredServers.setPredicate(server ->
		{
			String nameFilterSetting = nameFilter.getText();

			if (!StringUtils.isEmpty(nameFilterSetting))
			{
				if (nameFilterSetting.charAt(0) == '!')
				{
					if (server.getHostname().toLowerCase().contains((nameFilterSetting.replace("!", "").toLowerCase())))
					{
						return false;
					}
				}
				else
				{
					if (!server.getHostname().toLowerCase().contains(nameFilterSetting.toLowerCase()))
					{
						return false;
					}
				}
			}

			String modeFilterSetting = modeFilter.getText();

			if (!StringUtils.isEmpty(modeFilterSetting))
			{
				if (modeFilterSetting.charAt(0) == '!')
				{
					if (server.getMode().toLowerCase().contains((modeFilterSetting.replace("!", "").toLowerCase())))
					{
						return false;
					}
				}
				else
				{
					if (!server.getMode().toLowerCase().contains(modeFilterSetting.toLowerCase()))
					{
						return false;
					}
				}
			}

			String languageFilterSetting = languageFilter.getText();

			if (!StringUtils.isEmpty(languageFilterSetting))
			{
				if (languageFilterSetting.charAt(0) == '!')
				{
					if (server.getLanguage().toLowerCase().contains(languageFilterSetting.replace("!", "").toLowerCase()))
					{
						return false;
					}
				}
				else
				{
					if (!server.getLanguage().toLowerCase().contains(languageFilterSetting.toLowerCase()))
					{
						return false;
					}
				}
			}

			if (!versionFilter.getSelectionModel().isEmpty())
			{
				String versionFilterSetting = versionFilter.getSelectionModel().getSelectedItem().toString();

				if (!server.getVersion().toLowerCase().contains(versionFilterSetting.toLowerCase()))
				{
					return false;
				}
			}

			return true;
		});

		updateTable();
	}

	private void updateTable()
	{
		sortedServers.clear();
		sortedServers.addAll(filteredServers);
		tableView.sort();
	}

	protected void displayMenu(List<SampServer> serverList, double posX, double posY)
	{
		if (Objects.isNull(menu))
		{
			menu = new ContextMenu();
			connectItem = new MenuItem("Connect to Server");
			addToFavourites = new MenuItem("Add to Favourites");
			removeFromFavourites = new MenuItem("Remove from Favourites");
			copyIpAddressAndPort = new MenuItem("Copy IP Address and Port");

			menu.getItems().add(connectItem);
			menu.getItems().add(new SeparatorMenuItem());
			menu.getItems().add(addToFavourites);
			menu.getItems().add(removeFromFavourites);
			menu.getItems().add(copyIpAddressAndPort);
		}

		if (serverList.size() == 1)
		{
			connectItem.setVisible(true);
			copyIpAddressAndPort.setVisible(true);
			menu.getItems().get(1).setVisible(true);
		}
		else
		{
			connectItem.setVisible(false);
			copyIpAddressAndPort.setVisible(false);
			menu.getItems().get(1).setVisible(false);
		}

		menu.setOnAction(action ->
		{
			MenuItem clickedItem = (MenuItem) action.getTarget();

			if (clickedItem == connectItem)
			{
				SampServer server = serverList.get(0);
				SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort()));

				if (query.isConnected())
				{
					String[] serverInfo = query.getBasicServerInfo();

					if (Objects.nonNull(serverInfo))
					{
						if (!serverInfo[0].equals("0"))
						{
							TextInputDialog dialog = new TextInputDialog();
							dialog.setTitle("Connect to Server");
							dialog.setHeaderText("Enter the servers password.");

							Optional<String> result = dialog.showAndWait();
							if (result.isPresent())
							{
								GTA.connectToServerPerShell(server.getAddress() + ":" + server.getPort(), result.get());
							}
						}
						else
						{
							GTA.connectToServerPerShell(server.getAddress() + ":" + server.getPort());
						}
					}
					else
					{
						GTA.connectToServerPerShell(server.getAddress() + ":" + server.getPort());
					}
					query.close();
				}
				else
				{
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Connect to Server");
					alert.setHeaderText("Can't connect to Server");
					alert.setContentText("Server seems to be offline, try again.");

					alert.showAndWait();
				}
			}
			else if (clickedItem == addToFavourites)
			{
				for (SampServer serverItem : serverList)
				{
					Favourites.addServerToFavourites(serverItem);
				}
			}
			else if (clickedItem == removeFromFavourites)
			{
				for (SampServer serverItem : serverList)
				{
					Favourites.removeServerFromFavourites(serverItem);
				}
				servers.removeAll(serverList);
				updateTable();
			}
			else if (clickedItem == copyIpAddressAndPort)
			{
				SampServer server = serverList.get(0);
				StringSelection stringSelection = new StringSelection(server.getAddress() + ":" + server.getPort());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			}
		});

		menu.show(tableView, posX, posY);
	}

	protected void updateServerInfo(SampServer server)
	{
		playerTable.getItems().clear();

		playerTable.setPlaceholder(new Label("Retrieving..."));

		serverAddress.setText(server.getAddress() + ":" + server.getPort());
		serverLagcomp.setText(server.getLagcomp());

		serverPing.setText("Retrieving ...");
		serverPassword.setText("Retrieving ...");

		if (Objects.nonNull(threadGetPlayers))
		{
			threadGetPlayers.interrupt();
		}

		threadGetPlayers = new Thread(() ->
		{
			SampQuery query = new SampQuery(server.getAddress(), Integer.parseInt(server.getPort()));

			if (!query.isConnected())
			{
				Platform.runLater(() ->
				{
					serverPing.setText("Server Offline");
					serverPassword.setText("");
					playerTable.setPlaceholder(new Label("Couldn't retrieve players, server is offline."));
				});
			}
			else
			{
				String[] serverInfo = query.getBasicServerInfo();

				if (Objects.nonNull(serverInfo))
				{
					server.setPlayers(Integer.parseInt(serverInfo[1]));
					server.setMaxPlayers(Integer.parseInt(serverInfo[2]));
				}

				ObservableList<Player> players = FXCollections.observableArrayList();

				String[][] basicPlayers = query.getBasicPlayerInfo();

				if (Objects.nonNull(basicPlayers))
				{
					for (int i = 0; i < basicPlayers.length; i++)
					{
						players.add(new Player(basicPlayers[i][0], basicPlayers[i][1]));
					}
				}

				final String passworded;
				if (Objects.nonNull(serverInfo))
				{
					passworded = serverInfo[0].equals("0") ? "No" : "Yes";
				}
				else
				{
					passworded = "";
				}

				long ping = query.getPing();

				Platform.runLater(() ->
				{
					serverPassword.setText(passworded);
					serverPing.setText("" + ping);

					if (!players.isEmpty())
					{
						playerTable.setItems(players);
					}
					else
					{
						playerTable.setPlaceholder(new Label("Currently, noone is playing on this server."));
					}

					if (playerTable.getItems().isEmpty() && server.getPlayers() >= 100)
					{
						Label label = new Label("Sorry, since this server has more than 100 active players, we are not able to retrieve any player related information.");
						label.setWrapText(true);
						label.setAlignment(Pos.CENTER);
						playerTable.setPlaceholder(label);
					}
				});

				String[][] rules = query.getServersRules();

				if (Objects.nonNull(rules))
				{
					for (int i = 0; rules.length > i; i++)
					{
						if (rules[i][0].equals("lagcomp"))
						{
							String temp = rules[i][1];
							if (Objects.nonNull(temp) && !temp.isEmpty())
							{
								Platform.runLater(() ->
								{
									serverLagcomp.setText(temp);
								});
							}
							break;
						}
					}
				}
				query.close();

				Platform.runLater(() ->
				{
					updateGlobalInfo();
				});
			}
		});

		threadGetPlayers.start();
	}

	private void updateGlobalInfo()
	{
		int playersPlaying = 0;
		int maxSlots = 0;

		for (SampServer server : sortedServers)
		{
			playersPlaying += server.getPlayers();
			maxSlots += server.getMaxPlayers();
		}

		int freeSlots = maxSlots - playersPlaying;

		serverCount.setText(sortedServers.size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText(freeSlots + "");
	}
}
