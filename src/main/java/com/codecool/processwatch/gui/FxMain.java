package com.codecool.processwatch.gui;

import com.codecool.processwatch.os.OsProcessSource;
import com.codecool.processwatch.queries.SelectAll;
import com.codecool.processwatch.queries.SelectByName;
import com.codecool.processwatch.queries.SelectByParentPID;
import com.codecool.processwatch.queries.SelectByUser;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.ListIterator;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * The JavaFX application Window.
 */
public class FxMain extends Application {

    private static final String TITLE = "Process Watch";

    private App app;

    /**
     * Entrypoint for the javafx:run maven task.
     *
     * @param args an array of the command line parameters.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Build the application window and set up event handling.
     *
     * @param primaryStage a stage created by the JavaFX runtime.
     */
    public void start(Stage primaryStage) {
        primaryStage.setTitle(TITLE);
        primaryStage.setMinHeight(160);

        ObservableList<ProcessView> displayList = observableArrayList();
        app = new App(displayList);

        var tableView = new TableView<ProcessView>(displayList);

        var pidColumn = new TableColumn<ProcessView, Long>("Process ID");
        pidColumn.setCellValueFactory(new PropertyValueFactory<ProcessView, Long>("pid"));
        pidColumn.setPrefWidth(128);
        pidColumn.setResizable(false);

        var parentPidColumn = new TableColumn<ProcessView, Long>("Parent Process ID");
        parentPidColumn.setCellValueFactory(new PropertyValueFactory<ProcessView, Long>("parentPid"));
        parentPidColumn.setPrefWidth(128);
        parentPidColumn.setResizable(false);

        var userNameColumn = new TableColumn<ProcessView, String>("User");
        userNameColumn.setCellValueFactory(new PropertyValueFactory<ProcessView, String>("userName"));
        userNameColumn.setPrefWidth(192);
        userNameColumn.setMaxWidth(576);

        var processNameColumn = new TableColumn<ProcessView, String>("Name");
        processNameColumn.setCellValueFactory(new PropertyValueFactory<ProcessView, String>("processName"));
        processNameColumn.setPrefWidth(512);
        processNameColumn.setMaxWidth(1536);

        var argsColumn = new TableColumn<ProcessView, String>("Arguments");
        argsColumn.setCellValueFactory(new PropertyValueFactory<ProcessView, String>("args"));

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getColumns().addAll(pidColumn, parentPidColumn, userNameColumn, processNameColumn, argsColumn);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Menu menu = new Menu("Help");
        MenuItem menuItem1 = new MenuItem("About");
        menuItem1.setOnAction(ignoreEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("Process watch");
            String s = "Version 1.0.0\n\nCreators :\nDeszatta László\nFazekas László\nGörcs Balázs\nGróf András\nPetrényi Zoltán\nStraub Levente";
            alert.setContentText(s);
            alert.show();
        });
        menu.getItems().add(menuItem1);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu);
        Region spacer = new Region();
        spacer.getStyleClass().add("menu-bar");
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        HBox menubars = new HBox(spacer, menuBar);
        menuBar.setFocusTraversable(true);

        var refreshButton = new Button("Refresh");
        Tooltip tooltipRefresh = new Tooltip("Refreshes the process list.");
        refreshButton.setTooltip(tooltipRefresh);
        refreshButton.setOnAction(ignoreEvent -> app.refresh());
        refreshButton.setMinWidth(96);
        refreshButton.setMinHeight(32);
        refreshButton.setTranslateX(8);
        refreshButton.setTranslateY(8);

        var killButton = new Button("Kill");
        Tooltip tooltipKill = new Tooltip("will finish the program's execution.");
        killButton.setTooltip(tooltipKill);
        killButton.setOnAction(ignoreEvent -> {
            ArrayList<ProcessView> products = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
            for (ProcessView model : products) {
                ProcessHandle.of(model.getPid()).ifPresent(ProcessHandle::destroy);
            }
        });
        killButton.setMinWidth(96);
        killButton.setMinHeight(32);
        killButton.setTranslateX(16);
        killButton.setTranslateY(8);

        Label filterLabel = new Label("Filter mode :");
        filterLabel.setMinWidth(72);
        filterLabel.setTranslateX(48);
        filterLabel.setTranslateY(16);

        var filterMode = new ChoiceBox<>();
        Tooltip tooltipSelectFilter = new Tooltip("Available filtering modes.");
        filterMode.setTooltip(tooltipSelectFilter);
        filterMode.getItems().clear();
        filterMode.getItems().add("none");
        filterMode.getItems().add("by Parent Process ID");
        filterMode.getItems().add("by User");
        filterMode.getItems().add("by Name");
        filterMode.getSelectionModel().selectFirst();
        filterMode.setMinWidth(160);
        filterMode.setTranslateX(56);
        filterMode.setTranslateY(12);

        var filterParam = new ChoiceBox<>();
        Tooltip tooltipSelectUser = new Tooltip("Required filter parameter.");
        filterParam.setTooltip(tooltipSelectUser);
        filterParam.setMinWidth(96);
        filterParam.setTranslateX(72);
        filterParam.setTranslateY(12);
        filterParam.setDisable(true);

        filterMode.setOnAction(e -> {
            System.out.println("Filter mode : " + filterMode.getSelectionModel().getSelectedItem());
            filterParam.getItems().clear();
            switch (filterMode.getSelectionModel().getSelectedIndex()) {
                case 1: {
                    filterParam.getItems().addAll(new OsProcessSource().getActualParentPid());
                    break;
                }
                case 2: {
                    filterParam.getItems().addAll(new OsProcessSource().getActualUserNames());
                    break;
                }
                case 3: {
                    filterParam.getItems().addAll(new OsProcessSource().getActualProcessNames());
                    break;
                }
                default: {
                    app.setQuery(new SelectAll());
                    break;
                }
            }
            if (filterMode.getSelectionModel().getSelectedIndex() != 0) {
                filterParam.getItems().sort(null);
                filterParam.getSelectionModel().selectFirst();
            }
            filterParam.setDisable(filterMode.getSelectionModel().getSelectedIndex() == 0);
        });

        filterParam.setOnAction(e -> {
            if (filterParam.getSelectionModel().getSelectedItem() != null) {
                System.out.println("Filter param : " + filterParam.getSelectionModel().getSelectedItem());
                switch (filterMode.getSelectionModel().getSelectedIndex()) {
                    case 1: {
                        SelectByParentPID selectByParentPID = new SelectByParentPID((long) filterParam.getSelectionModel().getSelectedItem());
                        app.setQuery(selectByParentPID);
                        break;
                    }
                    case 2: {
                        SelectByUser selectByUser = new SelectByUser(filterParam.getSelectionModel().getSelectedItem().toString());
                        app.setQuery(selectByUser);
                        break;
                    }
                    case 3: {
                        SelectByName selectByName = new SelectByName(filterParam.getSelectionModel().getSelectedItem().toString());
                        app.setQuery(selectByName);
                        break;
                    }
                    default: {
                        app.setQuery(new SelectAll());
                        break;
                    }
                }
            }
        });

        HBox statusbar = new HBox(refreshButton, killButton, filterLabel, filterMode, filterParam);
        statusbar.setFocusTraversable(false);
        statusbar.setMinHeight(48);

        BorderPane box = new BorderPane();
        box.setTop(menubars);
        box.setCenter(tableView);
        box.setBottom(statusbar);
        box.getStylesheets().add("dark-theme.css");
        var scene = new Scene(box, 1280, 720);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
