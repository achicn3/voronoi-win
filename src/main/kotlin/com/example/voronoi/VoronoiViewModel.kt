package com.example.voronoi

import com.example.voronoi.Point.Companion.addVector
import com.example.voronoi.Point.Companion.getConvexHull
import com.example.voronoi.Point.Companion.getCrossProduct
import com.example.voronoi.Point.Companion.getDistance
import com.example.voronoi.Point.Companion.getIntersection
import com.example.voronoi.Point.Companion.getMidPoint
import com.example.voronoi.Point.Companion.sortPoint
import com.example.voronoi.Utils.TYPE_CIRCUMCENTER_POINT
import com.example.voronoi.Utils.TYPE_CONVEX_HULL
import com.example.voronoi.Utils.TYPE_HYPER_LINE
import com.example.voronoi.Utils.TYPE_MERGE_VORONOI
import com.example.voronoi.Utils.TYPE_MID_POINT
import com.example.voronoi.Utils.TYPE_POINT
import com.example.voronoi.Utils.TYPE_VORONOI
import javafx.collections.FXCollections
import javafx.scene.control.Control
import javafx.scene.paint.Color
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

class VoronoiViewModel : Control() {
    val pointList = arrayListOf<Point>()
    private var bufferedReader: BufferedReader? = null
    val randomColors = arrayListOf<Color>().apply {
        for(i in 1..100){
            add(Color.color(Math.random(),Math.random(),Math.random()))
        }
    }
    var isRun = false
    var isWriteFinish = false
    var isReadFinish = false
    var vDiagram = VDiagram()
    val step = arrayListOf<Step>()
    var nowStep: Int = 0
    var isOpenOutput = false
    fun clearAll() {
        pointList.clear()
        bufferedReader = null
        init()
    }

    fun init() {
        isRun = false
        isWriteFinish = false
        isReadFinish = false
        vDiagram = VDiagram()
        step.clear()
        nowStep = 0
        isOpenOutput = false
        randomColors.apply {
            clear()
            for(i in 1..5000){
                add(Color.color(Math.random(),Math.random(),Math.random()))
            }
        }
    }

    fun onClickSave(fileName: String) {
        File(fileName).printWriter().use { out ->
            vDiagram.run {
                val sortedPoint = pointList.sortPoint()
                sortedPoint.forEach { point ->
                    out.println(point.toString())
                }
                val sortedEdge = Point.sortEdge(voronoiList)
                sortedEdge.forEach { edge ->
                    out.println(edge.toString())
                }
                isWriteFinish = true
            }
            out.close()
        }
    }

    private fun twoPoint(pointList: ArrayList<Point>): VDiagram {
        if (pointList.size < 2) return VDiagram()
        return twoPoint(pointList[0], pointList[1])
    }

    /**
     * 兩點的VD，直接計算兩點中點，並以中點為起點法向量兩邊方向延伸一千倍
     * */
    private fun twoPoint(pointA: Point, pointB: Point): VDiagram {
        val midPoint = Point.getMidPoint(pointA, pointB)
        val vd = VDiagram()
        vd.pointList.add(pointA)
        vd.pointList.add(pointB)
        val extendPoint1 = Point.addVector(
                midPoint,
                Point.extendVector(Point.getNormalVector(pointA, pointB), 1000.0)
        )
        val extendPoint2 = Point.addVector(
                midPoint,
                Point.extendVector(Point.getNormalVector(pointB, pointA), 1000.0)
        )
        vd.voronoiList.add(
                Edge(
                        extendPoint1, extendPoint2, pointA, pointB
                )
        )

        return vd
    }

    private fun threePoint(pointList: ArrayList<Point>): VDiagram {
        if (pointList.size < 3) return twoPoint(pointList)
        return threePoint(pointList[0], pointList[1], pointList[2])
    }

    private fun calculatePerpendicular(pointA: Point, pointB: Point): Edge {
        return kotlin.run {
            val mid = getMidPoint(pointA, pointB)
            val edge = Edge(
                    addVector(
                            mid,
                            Point.extendVector(Point.getNormalVector(pointA, pointB), 1000.0)
                    ),
                    addVector(
                            mid,
                            Point.extendVector(Point.getNormalVector(pointB, pointA), 1000.0)
                    ),
                    pointA, pointB
            )
            edge
        }
    }

    /**
     * 三個點的VD，先依序將點a,b,c加入到變數vd當中
     * 再判斷三點是否共線？其中共線判斷方法可以利用三點圍成的三角形面積為0來判斷
     * 若三點共線：計算(a,b)、(b,c)的中點、往上跟往下的法向量，並延伸此法向量的長度一千倍，以中點為基準點做線段(中垂線)
     * 若三點無共線： 利用Bubble sort，將點依據逆時針方向進行排序，再計算三點的外心circumCenter，之後分別計算(a,b),(b,c),(c,a)的中點
     * 並以中點為基礎往法向量方向延伸一千倍為此線段的終點，外心為線段的起點，就可以做出三點的VD
     * */
    private fun threePoint(a: Point, b: Point, c: Point): VDiagram {
        val vd = VDiagram().apply {
            pointList.add(a)
            pointList.add(b)
            pointList.add(c)
        }
        if (Point.isSameLine(a, b, c)) {
            val sortedPoint = vd.pointList.sortPoint()
            sortedPoint.let {
                vd.pointList.clear()
                vd.pointList.addAll(it)
            }
            vd.voronoiList.apply {
                add(calculatePerpendicular(a, b))
                add(calculatePerpendicular(b, c))
            }
        } else {
            val circumCenter = Point.getCircumcenter(a, b, c)
            val v = Point.sortVectorByCounterClockwise(vd.pointList)
            vd.pointList.clear()
            vd.pointList.addAll(v)
            step.add(Step(arrayListOf(circumCenter), null, TYPE_CIRCUMCENTER_POINT, false))
            for (idx in vd.pointList.indices) {
                val mid = getMidPoint(vd.pointList[idx], vd.pointList[(idx + 1) % 3])
                //從中點往法向量方向延伸當終點，外心之後會當起點
                val extendMid = addVector(
                        mid,
                        Point.extendVector(
                                Point.getNormalVector(
                                        vd.pointList[idx],
                                        vd.pointList[(idx + 1) % 3]
                                ), 1000.0
                        )
                )
                //step.add(Step(arrayListOf(extendMid),null, TYPE_MID_POINT,false))
                vd.voronoiList.add(
                        Edge(
                                //外心當起點
                                circumCenter,
                                extendMid,
                                vd.pointList[idx],
                                vd.pointList[(idx + 1) % 3]
                        )
                )
            }
        }
        return vd
    }

    private fun mergeVoronoi(left: VDiagram, right: VDiagram): VDiagram {
        val vd = VDiagram()
        val lower: ArrayList<Point> = ArrayList(getConvexHull(left.pointList))
        val upper = ArrayList<Point>(getConvexHull(right.pointList))
        step.add(Step(pointList = lower, type = TYPE_CONVEX_HULL))
        step.add(Step(pointList = upper, type = TYPE_CONVEX_HULL))
        val tangentLine = getTangent(lower, upper)
        //找Hyper plane
        val hyperPlaneList = ArrayList<Edge>()
        var intersectPoint: Point
        var candidate = Edge()
        var lastEdge: Edge? = null
        val eliminate = ArrayList<Int>()
        val delete = ArrayList<Int>()
        left.pointList.forEach {
            vd.pointList.add(Point(it.x, it.y))
        }
        right.pointList.forEach {
            vd.pointList.add(Point(it.x, it.y))
        }
        left.voronoiList.forEach {
            vd.voronoiList.add(Edge(it.pointA, it.pointB, it.perA, it.perB))
        }
        right.voronoiList.forEach {
            vd.voronoiList.add(Edge(it.pointA, it.pointB, it.perA, it.perB))
        }
        var hyperPlaneEdge: Edge? = null
        var nearPoint: Point? = null
        var scan = Edge(tangentLine[0].pointA, tangentLine[0].pointB)
        var lastNearPoint: Point = Edge.getPerpendicular(scan).pointA
        var t = 0
        while (scan != tangentLine[1]) {
            ++t
            hyperPlaneEdge = Edge.getPerpendicular(scan)
            nearPoint = null
            for (i in vd.voronoiList.indices) {
                if (lastEdge != null && lastEdge == vd.voronoiList[i]) continue
                val point = getIntersection(hyperPlaneEdge, vd.voronoiList[i])
                if (point != null && round(lastNearPoint.y) >= point.y) {
                    if (nearPoint == null) {
                        nearPoint = point
                        candidate = vd.voronoiList[i]
                        continue
                    }
                    if (getDistance(hyperPlaneEdge.pointA, point) < getDistance(hyperPlaneEdge.pointA, nearPoint)) {
                        nearPoint = point
                        candidate = vd.voronoiList[i]
                    }
                }
            }
            hyperPlaneEdge.pointA = lastNearPoint
            nearPoint?.let { point ->
                hyperPlaneList.add(Edge(hyperPlaneEdge.pointA, point, scan.pointA, scan.pointB))
                lastNearPoint = point
            }

            if(!eliminate.contains(vd.voronoiList.indexOf(candidate)))
            eliminate.add(vd.voronoiList.indexOf(candidate))
            lastEdge = candidate
            when {
                scan.pointA == candidate.perA -> scan.pointA = candidate.perB!!
                scan.pointA == candidate.perB -> scan.pointA = candidate.perA!!
                scan.pointB == candidate.perA -> scan.pointB = candidate.perB!!
                scan.pointB == candidate.perB -> scan.pointB = candidate.perA!!
            }
        }
        if (tangentLine[0] == tangentLine[1]) {
            hyperPlaneList.add(Edge(Edge.getPerpendicular(tangentLine[0]).pointA,
                    Edge.getPerpendicular(tangentLine[0]).pointB,
                    scan.pointA,
                    scan.pointB))
        }
        else{
            hyperPlaneList.add(Edge(nearPoint!!, Edge.getPerpendicular(tangentLine[1]).pointA, scan.pointA, scan.pointB))

        }
        step.add(Step(edgeList = hyperPlaneList, type = TYPE_HYPER_LINE))
        for (i in 0 until eliminate.size) {
            if (getCrossProduct(hyperPlaneList[i].pointB, hyperPlaneList[i + 1].pointB, hyperPlaneList[i].pointA) >= 0) {
                if (getCrossProduct(hyperPlaneList[i].pointB, vd.voronoiList[eliminate[i]].pointA, hyperPlaneList[i].pointA) > 0) {
                    for (j in vd.voronoiList) {
                        if (j.pointA == vd.voronoiList[eliminate[i]].pointA && j.pointB != vd.voronoiList[eliminate[i]].pointB) {
                            if (getCrossProduct(j.pointA, j.pointB, hyperPlaneList[i].pointB) > 0) delete.add(vd.voronoiList.indexOf(j))
                        } else if (j.pointB == vd.voronoiList[eliminate[i]].pointA && j.pointA != vd.voronoiList[eliminate[i]].pointB) {
                            if (getCrossProduct(j.pointB, j.pointA, hyperPlaneList[i].pointB) > 0) {
                                delete.add(vd.voronoiList.indexOf(j))
                            }
                        }
                    }
                    vd.voronoiList[eliminate[i]].pointA = hyperPlaneList[i].pointB
                } else {
                    for (j in vd.voronoiList) {
                        if (j.pointA == vd.voronoiList[eliminate[i]].pointB && j.pointB != vd.voronoiList[eliminate[i]].pointA) {
                            if (getCrossProduct(j.pointA, j.pointB, hyperPlaneList[i].pointB) > 0) delete.add(vd.voronoiList.indexOf(j))
                        } else if (j.pointB == vd.voronoiList[eliminate[i]].pointB && j.pointA != vd.voronoiList[eliminate[i]].pointA) {
                            if (getCrossProduct(j.pointB, j.pointA, hyperPlaneList[i].pointB) > 0) {
                                delete.add(vd.voronoiList.indexOf(j))
                            }
                        }
                    }
                    vd.voronoiList[eliminate[i]].pointB = hyperPlaneList[i].pointB
                }
            } else {
                if (getCrossProduct(hyperPlaneList[i].pointB, vd.voronoiList[eliminate[i]].pointA, hyperPlaneList[i].pointA) < 0) {
                    for (j in vd.voronoiList) {
                        if (j.pointA == vd.voronoiList[eliminate[i]].pointA && j.pointB != vd.voronoiList[eliminate[i]].pointB) {
                            if (getCrossProduct(j.pointA, j.pointB, hyperPlaneList[i].pointB) < 0) delete.add(vd.voronoiList.indexOf(j))
                        } else if (j.pointB == vd.voronoiList[eliminate[i]].pointA && j.pointA != vd.voronoiList[eliminate[i]].pointB) {
                            if (getCrossProduct(j.pointB, j.pointA, hyperPlaneList[i].pointB) < 0) {
                                delete.add(vd.voronoiList.indexOf(j))
                            }
                        }
                    }
                    vd.voronoiList[eliminate[i]].pointA = hyperPlaneList[i].pointB
                } else {
                    for (j in vd.voronoiList) {
                        if (j.pointA == vd.voronoiList[eliminate[i]].pointB && j.pointB != vd.voronoiList[eliminate[i]].pointA) {
                            if (getCrossProduct(j.pointA, j.pointB, hyperPlaneList[i].pointB) < 0) delete.add(vd.voronoiList.indexOf(j))
                        } else if (j.pointB == vd.voronoiList[eliminate[i]].pointB && j.pointA != vd.voronoiList[eliminate[i]].pointA) {
                            if (getCrossProduct(j.pointB, j.pointA, hyperPlaneList[i].pointB) < 0) {
                                delete.add(vd.voronoiList.indexOf(j))
                            }
                        }
                    }
                    vd.voronoiList[eliminate[i]].pointB = hyperPlaneList[i].pointB
                }
            }

        }
        delete.sortDescending()
        val distinctDelete = delete.distinct()
        for (i in distinctDelete){
            vd.voronoiList.removeAt(i)
        }
        step.add(Step(null,vd.voronoiList, TYPE_VORONOI,true))
        for (i in hyperPlaneList) {
            vd.voronoiList.add(Edge(i.pointA, i.pointB, i.perA, i.perB))
        }
        step.add(Step(null,edgeList = vd.voronoiList, type = TYPE_MERGE_VORONOI, true))
        return vd
    }

    private fun getTangent(left: ArrayList<Point>, right: ArrayList<Point>): ArrayList<Edge> {
        val pointList = arrayListOf<Point>().apply {
            addAll(left)
            addAll(right)
        }
        val sortedPoint = pointList.sortPoint()
        sortedPoint.let {
            pointList.clear()
            pointList.addAll(it)
        }
        val temp = getConvexHull(pointList)
        val tangent = ArrayList<Edge>()
        for (idx in temp.indices) {
            if ((left.contains(temp[idx]) && right.contains(temp[(idx + 1) % temp.size])) ||
                    (right.contains(temp[idx]) && left.contains(temp[(idx + 1) % temp.size]))
            ) {
                tangent.add(
                        Edge(
                                temp[idx],
                                temp[(idx + 1) % temp.size]
                        )
                )
            }
        }
        step.add(Step(ArrayList(temp), tangent, TYPE_CONVEX_HULL, clear = true, enable = true))

        return tangent
    }


    fun calculateVDiagram(list: ArrayList<Point>): VDiagram {
        list.let {
            when (it.size) {
                0, 1 -> return VDiagram()
                2, 3 -> {
                    val vd =  if (it.size == 3) threePoint(it) else twoPoint(it)
                    step.add(Step(null,vd.voronoiList, type = TYPE_VORONOI))
                    return vd
                }
                else -> {
                    val left = arrayListOf<Point>().apply {
                        addAll(Point.getLeftPart(it))
                    }
                    val right = arrayListOf<Point>().apply {
                        addAll(Point.getRightPart(it))
                    }
                    return mergeVoronoi(calculateVDiagram(left), calculateVDiagram(right))
                }
            }
        }
    }

    fun onClickRun() {
        vDiagram.pointList.clear()
        vDiagram.voronoiList.clear()
        step.clear()
        nowStep = 0
        val sortedPoint = pointList.sortPoint()
        sortedPoint.let {
            pointList.clear()
            pointList.addAll(it)
        }


    }

    private fun onOpenOutput(firstPoint: String) {
        isOpenOutput = true
        println("here open ouput...")
        val vd = VDiagram().apply {
            var point = firstPoint.split(" ")
            pointList.add(Point(point[1].toDouble(), point[2].toDouble()))

            val bufferedReader = bufferedReader ?: return
            while (bufferedReader.ready()) {
                val line = bufferedReader.readLine()
                if (line.isNotEmpty() && line[0] == 'P') {
                    point = line.split(" ")
                    pointList.add(Point(point[1].toDouble(), point[2].toDouble()))
                }
                if (line.isNotEmpty() && line[0] == 'E') {
                    val edge = line.split(" ")
                    voronoiList.add(
                            Edge(
                                    edge[1].toDouble(),
                                    edge[2].toDouble(),
                                    edge[3].toDouble(),
                                    edge[4].toDouble()
                            )
                    )
                }
            }
        }
        vDiagram = vd
    }

    fun parseNext() {
        pointList.clear()
        val bufferedReader = bufferedReader ?: return
        while (bufferedReader.ready()) {
            val line = bufferedReader.readLine()
            if (line.isNotEmpty() && line[0] == 'P') {
                onOpenOutput(line)
                return
            }
            if (line.isEmpty() || line[0] == '#') {
                continue
            }
            if (line[0] == '0') {
                this.bufferedReader = null
                isReadFinish = true
                return
            }
            val n = line.toInt()
            for (i in 0 until n) {
                val point = bufferedReader.readLine().split(" ")
                pointList.add(Point(point[0].toDouble(), point[1].toDouble()))
            }
            break
        }
    }

    fun setReader(bufferedReader: BufferedReader) {
        this.bufferedReader = bufferedReader
        parseNext()
    }

    fun addPoint(x: Float, y: Float) {
        pointList.add(Point(x.toDouble(), y.toDouble()))
    }
}