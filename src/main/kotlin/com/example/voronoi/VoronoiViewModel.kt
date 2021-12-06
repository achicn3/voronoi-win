package com.example.voronoi

import com.example.voronoi.Point.Companion.addVector
import com.example.voronoi.Point.Companion.getCrossProduct
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
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.round

class VoronoiViewModel : Control() {
    val pointList = arrayListOf<Point>()
    private var bufferedReader: BufferedReader? = null
    var isRun = false
    var isWriteFinish = false
    var isReadFinish = false
    var vDiagram = VDiagram()
    val step = arrayListOf<Step>()
    var nowStep: Int = 0
    var isOpenOutput = false
    fun clearAll(){
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
        //step.add(Step(arrayListOf(pointA,pointB),null, TYPE_POINT,false))
        step.add(Step(arrayListOf(midPoint),null, TYPE_MID_POINT,false))
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

    private fun calculatePerpendicular(pointA: Point,pointB: Point) : Edge{
        return kotlin.run {
            val mid = getMidPoint(pointA, pointB)
            //step.add(Step(arrayListOf(pointA,pointB), null, TYPE_POINT,false))
            step.add(Step(arrayListOf(mid), null, TYPE_MID_POINT,false))
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
                add(calculatePerpendicular(a,b))
                add(calculatePerpendicular(b,c))
            }
        } else {
            val circumCenter = Point.getCircumcenter(a, b, c)
            val v = Point.sortVectorByCounterClockwise(vd.pointList)
            vd.pointList.clear()
            vd.pointList.addAll(v)
            step.add(Step(arrayListOf(circumCenter),null, TYPE_CIRCUMCENTER_POINT,false))
            for (idx in vd.pointList.indices) {
                val mid = getMidPoint(vd.pointList[idx], vd.pointList[(idx + 1) % 3])
                //step.add(Step(arrayListOf(vd.pointList[idx], vd.pointList[(idx + 1) % 3]),null, TYPE_POINT,false))
                step.add(Step(arrayListOf(mid),null, TYPE_MID_POINT,false))
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
        val leftCH = arrayListOf<Point>().apply {
            addAll(Point.getConvexHull(left.pointList))
        }
        val rightCH = arrayListOf<Point>().apply {
            addAll(Point.getConvexHull(right.pointList))
        }
        step.add(Step(pointList = leftCH, type = TYPE_CONVEX_HULL))
        step.add(Step(pointList = rightCH, type = TYPE_CONVEX_HULL))
        val tangent = getTangent(leftCH, rightCH)
        val hyperPlaneList = arrayListOf<Edge>()

        var interPoint: Point? = null
        var nearPoint: Point? = null
        vd.pointList.addAll(left.pointList)
        vd.pointList.addAll(right.pointList)
        vd.voronoiList.addAll(left.voronoiList)
        vd.voronoiList.addAll(right.voronoiList)

        val eliminateLine = arrayListOf<Int>()
        val deleteLine = arrayListOf<Int>()
        var scan = Edge(tangent[0].pointA, tangent[0].pointB)
        var lastNearestPoint = Edge.getPerpendicular(scan).pointA
        var hyperPlane: Edge? = null
        var touchEdge: Edge? = null
        var lastEdge: Edge? = null
        while (scan != tangent[1]) {
            hyperPlane = Edge.getPerpendicular(scan)
            nearPoint = null
            for (idx in vd.voronoiList.indices) {
                if (lastEdge != null && lastEdge == vd.voronoiList[idx]) continue

                /**找交點*/
                interPoint = getIntersection(hyperPlane, vd.voronoiList[idx])
                if (interPoint != null && round(lastNearestPoint.y) >= round(interPoint.y)) {
                    if (nearPoint == null) {
                        nearPoint = interPoint
                        touchEdge = vd.voronoiList[idx]
                        continue
                    }
                    if (Point.getDistance(hyperPlane.pointA, interPoint) < Point.getDistance(
                                    hyperPlane.pointA,
                                    nearPoint
                            )
                    ) {
                        nearPoint = interPoint
                        touchEdge = vd.voronoiList[idx]
                    }
                }
            }
            hyperPlane.pointA = lastNearestPoint
            nearPoint?.let {
                hyperPlaneList.add(Edge(hyperPlane.pointA, it, scan.pointA, scan.pointB))
                lastNearestPoint = it
            }
            eliminateLine.add(vd.voronoiList.indexOf(touchEdge))
            lastEdge = touchEdge
            when {
                scan.pointA == touchEdge?.perA -> {
                    scan.pointA = touchEdge.perB!!
                }
                scan.pointA == touchEdge?.perB -> {
                    scan.pointA = touchEdge.perA!!
                }
                scan.pointB == touchEdge?.perA -> {
                    scan.pointB = touchEdge.perB!!
                }
                scan.pointB == touchEdge?.perB -> {
                    scan.pointB = touchEdge.perA!!
                }
            }
        }
        //檢查上下切線共線？
        if (tangent[0] == tangent[1]) {
            hyperPlaneList.add(
                    Edge(
                            Edge.getPerpendicular(tangent[0]).pointA,
                            Edge.getPerpendicular(tangent[0]).pointB,
                            scan.pointA,
                            scan.pointB
                    )
            )
        } else {
            nearPoint?.let {
                hyperPlaneList.add(
                        Edge(
                                it,
                                Edge.getPerpendicular(tangent[1]).pointA,
                                scan.pointA,
                                scan.pointB
                        )
                )
            }
        }
        step.add(Step(hyperPlaneList, type = TYPE_HYPER_LINE))
        //消線
        for (idx in eliminateLine.indices) {
            if (Point.getCrossProduct(
                            hyperPlaneList[idx].pointB,
                            hyperPlaneList[idx + 1].pointB,
                            hyperPlaneList[idx].pointA
                    ) >= 0
            ) {
                if (Point.getCrossProduct(
                                hyperPlaneList[idx].pointB,
                                vd.voronoiList[eliminateLine[idx]].pointA,
                                hyperPlaneList[idx].pointA
                        ) > 0
                ) {
                    for (edge in vd.voronoiList) {
                        if (edge.pointA == vd.voronoiList[eliminateLine[idx]].pointA && edge.pointB != vd.voronoiList[eliminateLine[idx]].pointB) {
                            if (Point.getCrossProduct(
                                            edge.pointA,
                                            edge.pointB,
                                            hyperPlaneList[idx].pointB
                                    ) > 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        } else if (edge.pointB == vd.voronoiList[eliminateLine[idx]].pointA && edge.pointA != vd.voronoiList[eliminateLine[idx]].pointB) {
                            if (Point.getCrossProduct(
                                            edge.pointB,
                                            edge.pointA,
                                            hyperPlaneList[idx].pointB
                                    ) > 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        }
                    }
                } else {
                    for (edge in vd.voronoiList) {
                        if (edge.pointA == vd.voronoiList[eliminateLine[idx]].pointB && edge.pointB != vd.voronoiList[eliminateLine[idx]].pointA) {
                            if (Point.getCrossProduct(
                                            edge.pointA,
                                            edge.pointB,
                                            hyperPlaneList[idx].pointB
                                    ) > 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        } else if (edge.pointB == vd.voronoiList[eliminateLine[idx]].pointB && edge.pointA != vd.voronoiList[eliminateLine[idx]].pointA) {
                            if (Point.getCrossProduct(
                                            edge.pointB,
                                            edge.pointA,
                                            hyperPlaneList[idx].pointB
                                    ) > 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        }
                    }
                }
            } else if (Point.getCrossProduct(
                            hyperPlaneList[idx].pointB,
                            hyperPlaneList[idx + 1].pointB,
                            hyperPlaneList[idx].pointA
                    ) < 0
            ) {
                if (getCrossProduct(
                                hyperPlaneList[idx].pointB,
                                vd.voronoiList[eliminateLine[idx]].pointA,
                                hyperPlaneList[idx].pointA
                        ) < 0
                ) {
                    for (edge in vd.voronoiList) {
                        if (edge.pointA == vd.voronoiList[eliminateLine[idx]].pointA && edge.pointB != vd.voronoiList[eliminateLine[idx]].pointB) {
                            if (getCrossProduct(
                                            edge.pointA,
                                            edge.pointB,
                                            hyperPlaneList[idx].pointB
                                    ) < 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        } else if (edge.pointB == vd.voronoiList[eliminateLine[idx]].pointA && edge.pointA != vd.voronoiList[eliminateLine[idx]].pointB) {
                            if (getCrossProduct(
                                            edge.pointB,
                                            edge.pointA,
                                            hyperPlaneList[idx].pointB
                                    ) < 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        }
                    }
                    vd.voronoiList[eliminateLine[idx]].pointA = hyperPlaneList[idx].pointB
                } else {
                    for (edge in vd.voronoiList) {
                        if (edge.pointA == vd.voronoiList[eliminateLine[idx]].pointB && edge.pointB != vd.voronoiList[eliminateLine[idx]].pointA) {
                            if (getCrossProduct(
                                            edge.pointA,
                                            edge.pointB,
                                            hyperPlaneList[idx].pointB
                                    ) < 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        } else if (edge.pointB == vd.voronoiList[eliminateLine[idx]].pointB && edge.pointA != vd.voronoiList[eliminateLine[idx]].pointA) {
                            if (getCrossProduct(
                                            edge.pointB,
                                            edge.pointA,
                                            hyperPlaneList[idx].pointB
                                    ) < 0
                            ) {
                                deleteLine.add(vd.voronoiList.indexOf(edge))
                            }
                        }
                    }
                    vd.voronoiList[eliminateLine[idx]].pointA = hyperPlaneList[idx].pointB
                }
            }
        }
        deleteLine.sortDescending()
        val temp = deleteLine.distinct()
        for (idx in temp) {
            vd.voronoiList.removeAt(idx)
        }
        //step.add(Step(vd.voronoiList, type = TYPE_VORONOI, true))
        vd.voronoiList.addAll(hyperPlaneList)
        //step.add(Step(vd.voronoiList, type = TYPE_MERGE_VORONOI, true))
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
        val temp = Point.getConvexHull(pointList)
        val tangent = arrayListOf<Edge>()
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
        val list = arrayListOf<Point>().apply {
            addAll(temp)
        }
        step.add(Step(list, tangent, TYPE_CONVEX_HULL, true))

        return tangent
    }


    private fun calculateVDiagram(list: ArrayList<Point>): VDiagram {
        list.let {
            when (it.size) {
                0, 1 -> return VDiagram()
                2, 3 -> {
                    return if (it.size == 3) threePoint(it) else twoPoint(it)
                    //step.add(Step(pointList = tp.pointList, type = TYPE_VORONOI))
                    //return tp
                }
                else -> {
                    val left = arrayListOf<Point>().apply {
                        addAll(Point.getLeftPart(it))
                    }
                    val right = arrayListOf<Point>().apply {
                        addAll(Point.getRightPart(it))
                    }
                    //getTangent(left, right)
                    //return VDiagram()
                    //Log.d("vdiagram","${calculateVDiagram(left)} right:${calculateVDiagram(right)}")
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
        vDiagram = calculateVDiagram(pointList)

    }

    private fun onOpenOutput(firstPoint: String) {
        isOpenOutput = true
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
                println("read point: $point")
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