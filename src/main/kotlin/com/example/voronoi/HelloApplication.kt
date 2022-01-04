package com.example.voronoi

import com.example.voronoi.Point.Companion.sortPoint
import com.example.voronoi.Utils.TYPE_CIRCUMCENTER_POINT
import com.example.voronoi.Utils.TYPE_CONVEX_HULL
import com.example.voronoi.Utils.TYPE_EDGE
import com.example.voronoi.Utils.TYPE_HYPER_LINE
import com.example.voronoi.Utils.TYPE_MERGE_VORONOI
import com.example.voronoi.Utils.TYPE_MID_POINT
import com.example.voronoi.Utils.TYPE_POINT
import com.example.voronoi.Utils.TYPE_VORONOI
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
    val temp = gc.stroke
    gc.stroke = color
    gc.strokeLine(pointA.x, pointA.y, pointB.x, pointB.y)
    gc.stroke = temp
}

private fun drawLine(gc:GraphicsContext,edge: Edge,color: Paint){
    val temp = gc.stroke
    gc.stroke = color
    drawLine(gc,edge.pointA,edge.pointB,color)
    gc.stroke = temp
}
private fun drawText(gc: GraphicsContext, x: Double, y: Double, idx: Int) {
    val temp = gc.fill
    gc.fill = Color.BLACK
    gc.fillText("#$idx", x + 5.0, y + 5.0)
    gc.fill = temp
}


private fun onDrawVDiagram(gc: GraphicsContext, edgeList: ArrayList<Edge>, color: Paint): ArrayList<Edge> {
    var idx = 0
    val edge = arrayListOf<Edge>()
    var i = -1
    while(i<edgeList.size){
        ++i
        if(i<edgeList.size && edgeList[i].pointA == edgeList[i].pointB){
            edgeList.removeAt(i--)
            continue;
        }
        if(i<edgeList.size){
            drawLine(gc,edgeList[i],color)
        }
    }
    return edge
}

private fun onDrawConvexHull(gc: GraphicsContext, pointList: ArrayList<Point>?, tangent: ArrayList<Edge>? = null,color: Color) {
    var isTangent = false
    if(pointList!=null)
    for (idx in 0 until pointList.size) {
        isTangent = false
        if (tangent != null) {
            for (e in tangent) {
                if (e == Edge(pointList[idx], pointList[(idx + 1) % pointList.size])) {
                    isTangent = true
                    break
                }
            }
        }
        if (isTangent) {
            drawLine(gc, pointList[idx], pointList[(idx + 1) % pointList.size], color)
        } else {
            drawLine(gc, pointList[idx], pointList[(idx + 1) % pointList.size], color)
        }
        drawText(
                gc,
                pointList[idx].x,
                pointList[idx].y,
                idx
        )

    }
}

private fun clearView(gc: GraphicsContext, canvas: Canvas) {
    gc.clearRect(0.0, 0.0, canvas.width, canvas.height)
}

fun drawPoint(gc: GraphicsContext,point: Point,color: Paint){
    drawPoint(gc,point.x,point.y,color)
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
    viewModel.vDiagram = viewModel.calculateVDiagram(viewModel.pointList)
    viewModel.isRun = true
}
private fun checkType(viewModel: VoronoiViewModel){
    when(viewModel.step[viewModel.nowStep].type ){
        TYPE_VORONOI->{
            viewModel.step[viewModel.nowStep].clear?.let { clear->
                if(clear){
                    viewModel.step[viewModel.nowStep-2].enable = false
                    var count = 0
                    var i = viewModel.nowStep
                    while(count<2 && i-1 >=0){
                        --i
                        viewModel.step[i].enable?.let { enable->
                            if((viewModel.step[i].type == TYPE_MERGE_VORONOI || viewModel.step[i].type == TYPE_VORONOI) && enable){
                                viewModel.step[i].enable = false
                                ++count
                            }
                        }
                    }
                }
            }
        }
        TYPE_CONVEX_HULL,TYPE_MERGE_VORONOI->{
            if(viewModel.step[viewModel.nowStep].clear == true){
                viewModel.step[viewModel.nowStep-1].enable = false
                viewModel.step[viewModel.nowStep-2].enable = false
            }
        }
    }
}
class HelloApplication : Application() {
    private val viewModel = VoronoiViewModel()
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("hello-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 1024.0, 768.0)

        stage.title = "Voronoi Diagram"
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
                onDrawVDiagram(gc, viewModel.vDiagram.voronoiList, Color.DARKCYAN)
                onDrawConvexHull(gc, viewModel.pointList, null,Color.RED)
            }
        }
        btnSave.setOnMouseClicked {
            FileChooser().showSaveDialog(stage)?.let { file->
                val writer = file.printWriter()
                val sortedPoint = viewModel.pointList.sortPoint()
                sortedPoint.forEach { point ->
                    writer.println(point.toString())
                }
                if(viewModel.step.isNotEmpty()) {
                    val edge = viewModel.step.last().edgeList ?: arrayListOf()
                    val sortedEdge = Point.sortEdge(edge)
                    sortedEdge.forEach { e ->
                        writer.println(e.toString())
                    }
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
            clearView(gc,canvas)
            firstRun(gc, viewModel)
            for (step in viewModel.nowStep until viewModel.step.size) {
                clearView(gc,canvas)
                viewModel.pointList.forEach {
                    drawPoint(gc,it,Color.RED)
                }
                checkType(viewModel)
                if(viewModel.step[step].enable == true)
                    drawRecord(gc,viewModel.step[step],viewModel.randomColors[step])
            }
            viewModel.nowStep = viewModel.step.size
            //viewModel.init()
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
            if (viewModel.pointList.isEmpty()) return@setOnMouseClicked
            clearView(gc, canvas)
            if (!viewModel.isRun) {
                firstRun(gc, viewModel)
            }
            viewModel.pointList.forEach {
                drawPoint(gc,it,Color.RED)
            }
            if (viewModel.nowStep >= viewModel.step.size) {
                clearView(gc,canvas)
                viewModel.init()
                viewModel.isRun = false
                return@setOnMouseClicked
            }
            println("now step: \n ${viewModel.step[viewModel.nowStep]}")
            checkType(viewModel)
            for(i in 0..viewModel.nowStep) {
                if(viewModel.step[i].enable == true){
                    drawRecord(gc,viewModel.step[i],viewModel.randomColors[i])
                }
            }
            ++viewModel.nowStep
        }

    }

    private fun drawRecord(gc: GraphicsContext, step: Step,color: Color) {
        when(step.type){
            TYPE_VORONOI->{
                val color = if(step.clear == true){
                    Color.POWDERBLUE
                }else{
                    val idx = viewModel.step.indexOf(step)
                    if(idx %2 ==0) Color.DARKBLUE
                    else Color.BLACK
                }
                step.edgeList?.let {
                    onDrawVDiagram(gc,it,color)
                }
            }
            TYPE_CONVEX_HULL->{
                onDrawConvexHull(gc,step.pointList,step.edgeList,color)
            }
            TYPE_HYPER_LINE->{
                step.edgeList?.forEach {edge->
                    drawLine(gc,edge,Color.CYAN)
                }
            }
            TYPE_MERGE_VORONOI->{
                step.edgeList?.forEach { edge ->
                    drawLine(gc,edge,Color.NAVY)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(HelloApplication::class.java)
        }
    }

}