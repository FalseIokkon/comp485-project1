package com.yunzog.dashboard;

import com.yunzog.dashboard.dao.DivisionDAO;
import com.yunzog.dashboard.model.DivisionKPI;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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
        divisionCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().division()));
        metricCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().metric()));
        valueCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().value()));
        asOfCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().asOf()));

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