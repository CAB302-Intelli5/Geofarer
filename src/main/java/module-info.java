module com.example.geofarer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.geofarer to javafx.fxml;
    exports com.example.geofarer;
}