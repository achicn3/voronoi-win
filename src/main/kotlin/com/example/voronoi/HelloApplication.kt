package com.example.voronoi

import com.example.voronoi.Point.Companion.sortPoint
import com.example.voronoi.Utils.TYPE_CIRCUMCENTER_POINT
import com.example.voronoi.Utils.TYPE_EDGE
import com.example.voronoi.Utils.TYPE_MID_POINT
import com.example.voronoi.Utils.TYPE_POINT
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.stage.FileChooser
import javafx.stage.Stage

private fun drawPoint(gc: GraphicsContext, x: Double, y: Double, color: Paint) {
    val radius = 5.0
    gc.run {
        fill = color
        fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }
}

private fun drawPoint(gc: GraphicsContext, x: Double, y: Double) {
    drawPoint(gc, x, y, Color.RED)
}

private fun drawLine(gc: GraphicsContext, pointA: Point, pointB: Point, color: Paint) {
    gc.fill = color
    gc.strokeLine(pointA.x, pointA.y, pointB.x, pointB.y)
}

private fun drawText(gc: GraphicsContext, x: Double, y: Double, idx: Int) {
    gc.fill = Color.BLACK
    gc.fillText("#$idx", x + 5.0, y + 5.0)
}


private fun onDrawVDiagram(gc: GraphicsContext, vDiagram: VDiagram, shouldDraw: Boolean): ArrayList<Edge> {
    var idx = 0
    val edge = arrayListOf<Edge>()
    while (idx < vDiagram.voronoiList.size && idx >= 0) {
        if (vDiagram.voronoiList[idx].pointA == vDiagram.voronoiList[idx].pointB) {
            vDiagram.voronoiList.removeAt(idx)
            --idx
            continue
        }
        edge.add(Edge(vDiagram.voronoiList[idx].pointA, vDiagram.voronoiList[idx].pointB))
        if (shouldDraw) drawLine(gc, vDiagram.voronoiList[idx].pointA, vDiagram.voronoiList[idx].pointB, Color.CYAN)
        ++idx
    }
    return edge
}

private fun onDrawConvexHull(gc: GraphicsContext, pointList: ArrayList<Point>, tangent: ArrayList<Edge>? = null, shouldDraw: Boolean): ArrayList<Edge> {
    var isTangent = false
    val edge = arrayListOf<Edge>()
    for (idx in 0 until pointList.size) {
        isTangent = false
        if (tangent != null) {
            for (edge in tangent) {
                if (edge == Edge(pointList[idx], pointList[(idx + 1) % pointList.size])) {
                    isTangent = true
                    break
                }
            }
        }
        if (isTangent) {
            if (shouldDraw) drawLine(gc, pointList[idx], pointList[(idx + 1) % pointList.size], Color.BLACK)
            edge.add(Edge(pointList[idx], pointList[(idx + 1) % pointList.size]))
        } else {
            if (shouldDraw) drawLine(gc, pointList[idx], pointList[(idx + 1) % pointList.size], Color.GREEN)
            edge.add(Edge(pointList[idx], pointList[(idx + 1) % pointList.size]))
        }
        if (shouldDraw) {
            drawPoint(gc, pointList[idx].x, pointList[idx].y)
            drawText(
                    gc,
                    pointList[idx].x,
                    pointList[idx].y,
                    idx
            )
        }
    }
    return edge
}

private fun clearView(gc: GraphicsContext, canvas: Canvas) {
    gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
}

fun drawPoint(gc: GraphicsContext, type: Int, point: Point) {
    val color =
            when (type) {
                TYPE_POINT -> {
                    Color.RED
                }
                TYPE_MID_POINT -> {
                    Color.GREENYELLOW
                }
                TYPE_CIRCUMCENTER_POINT -> {
                    Color.BLUEVIOLET
                }
                else -> Color.BLACK
            }
    drawPoint(gc, point.x, point.y, color)
}

fun firstRun(gc: GraphicsContext, viewModel: VoronoiViewModel) {
    viewModel.onClickRun()
    val edge = onDrawVDiagram(gc, viewModel.vDiagram, false)
    viewModel.step.add(Step(null, edge, TYPE_EDGE, false))
    val vdEdge = onDrawConvexHull(gc, viewModel.vDiagram.pointList, null, false)
    viewModel.vDiagram.voronoiList.addAll(vdEdge)
    viewModel.step.add(Step(null, vdEdge, TYPE_EDGE, false))
    viewModel.isRun = true
}

class HelloApplication : Application() {
    private val viewModel = VoronoiViewModel()
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
        val btnNext = scene.lookup("#btn_next") as Button
        val canvas = scene.lookup("#drawView") as Canvas
        val gc: GraphicsContext = canvas.graphicsContext2D
        btnClear.setOnMouseClicked {
            gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
            viewModel.clearAll()
        }
        btnOpen.setOnMouseClicked {
            gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
            viewModel.clearAll()
            val file = FileChooser().showOpenDialog(stage) ?: return@setOnMouseClicked
            if (file.canRead())
                viewModel.setReader(file.bufferedReader())
            else println("open file failed")

            viewModel.pointList.forEach {
                drawPoint(gc, it.x, it.y)
            }
            if (viewModel.isOpenOutput && viewModel.vDiagram.voronoiList.isNotEmpty()) {
                viewModel.vDiagram.pointList.forEach {
                    drawPoint(gc, it.x, it.y)
                }
                onDrawVDiagram(gc, viewModel.vDiagram, true)
                onDrawConvexHull(gc, viewModel.pointList, null, true)
            }
        }
        btnSave.setOnMouseClicked {
            FileChooser().showSaveDialog(stage)?.let { file->
                val writer = file.printWriter()
                val sortedPoint = viewModel.pointList.sortPoint()
                sortedPoint.forEach { point ->
                    writer.println(point.toString())
                }
                val sortedEdge = Point.sortEdge(viewModel.vDiagram.voronoiList).distinct()
                sortedEdge.forEach { edge ->
                    writer.println(edge.toString())
                }

                viewModel.isWriteFinish= true
                writer.close()
            }

        }

        canvas.setOnMouseClicked { event ->
            drawPoint(gc, event.x, event.y)
            viewModel.pointList.add(Point(event.x,event.y))
        }
        btnRun.setOnMouseClicked {
            if (viewModel.pointList.isEmpty()) return@setOnMouseClicked
            if (!viewModel.isRun) {
                firstRun(gc, viewModel)
            }
            for (step in viewModel.nowStep until viewModel.step.size) {
                viewModel.step[step].pointList?.forEach {
                    drawPoint(gc, viewModel.step[step].type ?: return@forEach, it)
                }
                viewModel.step[step].edgeList?.forEach {
                    drawLine(gc, it.pointA, it.pointB, Color.BLACK)
                }
            }
            viewModel.nowStep = viewModel.step.size
        }

        btnNext.setOnMouseClicked {
            clearView(gc, canvas)
            viewModel.init()
            viewModel.parseNext()
            viewModel.pointList.forEach {
                drawPoint(gc, it.x, it.y)
            }
        }
        btnStep.setOnMouseClicked {
            if (!viewModel.isRun) {
                firstRun(gc, viewModel)
            }
            if (viewModel.nowStep >= viewModel.step.size) {
                clearView(gc, canvas)
                viewModel.pointList.forEach {
                    drawPoint(gc, it.x, it.y)
                }
                viewModel.nowStep = 0
                viewModel.isRun = false
                return@setOnMouseClicked
            }
            for (i in 0 until viewModel.step.size) {
                println("Steps $i ${viewModel.step[i].type} ${viewModel.step[i].pointList} ${viewModel.step[i].edgeList}")
            }

            viewModel.step[viewModel.nowStep].type?.let { type ->
                viewModel.step[viewModel.nowStep].pointList?.forEach {
                    drawPoint(gc, type, it)
                }
            }
            viewModel.step[viewModel.nowStep].edgeList?.forEach {
                drawLine(gc, it.pointA, it.pointB, Color.BLACK)
            }

            ++viewModel.nowStep
        }

    }
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(HelloApplication::class.java)
        }
    }

}