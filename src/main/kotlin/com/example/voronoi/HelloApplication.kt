package com.example.voronoi

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.stage.Stage

private fun drawCircle(x: Double,y:Double,gc: GraphicsContext){
    val radius = 5.0
    gc.fillOval(x-radius, y-radius, radius * 2, radius * 2);
}
class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 1024.0, 768.0)

        stage.title = "M093040072 Voronoi Diagram"
        stage.scene = scene

        stage.show()
        val btnRun = scene.lookup("#btn_run") as Button
        val btnClear = scene.lookup("#btn_clear") as Button
        val btnOpen = scene.lookup("#btn_open") as Button
        val btnSave = scene.lookup("#btn_save") as Button
        val btnStep = scene.lookup("#btn_step") as Button
        val canvas = scene.lookup("#drawView") as Canvas
        val gc: GraphicsContext = canvas.graphicsContext2D
        btnRun.setOnMouseClicked { event->
            val radius = 5.0
            gc.fillOval(300.0-radius, 150.0-radius, radius * 2, radius * 2);
        }
        btnClear.setOnMouseClicked {

        }
        canvas.setOnMouseClicked { event->
            drawCircle(event.x,event.y,gc)
        }
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}