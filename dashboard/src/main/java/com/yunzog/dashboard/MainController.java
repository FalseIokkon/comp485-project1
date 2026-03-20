package com.yunzog.dashboard;

import com.yunzog.dashboard.dao.DivisionDAO;
import com.yunzog.dashboard.model.DivisionKPI;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MainController {

    @FXML private TableView<DivisionKPI> table;
    @FXML private TableColumn<DivisionKPI, String> divisionCol;
    @FXML private TableColumn<DivisionKPI, String> metricCol;
    @FXML private TableColumn<DivisionKPI, Double> valueCol;
    @FXML private TableColumn<DivisionKPI, String> asOfCol;

    private final DivisionDAO dao = new DivisionDAO();

    @FXML
    private void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        divisionCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().division()));

        metricCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().metric()));

        valueCol.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().value()));

        asOfCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().asOf()));

        valueCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                } else {
                    if (value % 1 == 0) {
                        setText(String.format("%.0f", value));
                    } else {
                        setText(String.format("%.1f", value));
                    }
                }
            }
        });

        loadTable();
    }

    @FXML
    private void onRefresh() {
        loadTable();
    }

    private void loadTable() {
        table.setItems(FXCollections.observableArrayList(dao.getAllKpis()));
    }
}