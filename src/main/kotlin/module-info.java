module com.example.voronoi {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens com.example.voronoi to javafx.fxml;
    exports com.example.voronoi;
}