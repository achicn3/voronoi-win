package com.example.voronoi
// $LAN=KOTLIN$
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.pow


/**
 * 定義點的XY座標
 * */
private val exp = 10e-20
data class Point(
        var x: Double,
        var y: Double
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Point) return false
        return abs(x- other.x) <=exp && abs(y-other.y)<= exp
    }

    override fun toString(): String {
        return "P $x $y"
    }

    companion object {
        //計算外心
        fun getCircumcenter(a: Point, b: Point, c: Point): Point {
            //共點?
            if (a == b && a == c) return a
            val tempX =
                    (a.x.pow(2.0) + a.y.pow(2.0)) * (b.y - c.y) + (b.x.pow(2.0) + b.y.pow(2.0)) * (c.y - a.y) + (c.x.pow(
                            2.0
                    ) + c.y.pow(2.0)) * (a.y - b.y)
            val tempY =
                    (a.x.pow(2.0) + a.y.pow(2.0)) * (c.x - b.x) + (b.x.pow(2.0) + b.y.pow(2.0)) * (a.x - c.x) + (c.x.pow(
                            2.0
                    ) + c.y.pow(2.0)) * (b.x - a.x)
            val tempDet = 2 * ((b.y - c.y) * a.x + (c.y - a.y) * b.x + (a.y - b.y) * c.x)
            return Point(tempX / tempDet, tempY / tempDet)
        }

        //取得重心點
        fun getCenter(pointList: ArrayList<Point>): Point =
                Point(
                        pointList.sumOf { it.x } / pointList.size,
                        pointList.sumOf { it.y } / pointList.size
                )

        //排序點的X Y座標
        fun ArrayList<Point>.sortPoint(): List<Point> =
                this.sortedWith(compareBy({ it.x }, { it.y }))

        fun sortEdge(edgeList: ArrayList<Edge>): List<Edge> {
            //先排序線的兩個點
            edgeList.forEach {
                if (it.pointA.x > it.pointB.x || (it.pointA.x == it.pointB.x && it.pointA.y > it.pointB.y)) {
                    val temp = it.pointA
                    it.pointA = it.pointB
                    it.pointB = temp
                }
            }
            //排序線
            return edgeList.sortedWith(
                    compareBy(
                            { it.pointA.x },
                            { it.pointB.x },
                            { it.pointA.y },
                            { it.pointB.y })
            )
        }

        fun sortVectorByCounterClockwise(pointList: ArrayList<Point>): ArrayList<Point> {
            val list = ArrayList(pointList)
            if (pointList.size <= 1) return list
            val center = getCenter(list)
            for (i in 0 until list.size - 1) {
                for (j in 0 until list.size - i - 1) {
                    if (getCrossProduct(list[j], list[j + 1], center) > 0) {
                        list[j + 1] = list[j].also {
                            list[j] = list[j + 1]
                        }
                    }
                }
            }
            return list
        }

        fun getNormalVector(a: Point, b: Point): Point =
                getVector(a, b).apply {
                    x = y.also {
                        y = x
                    }
                    x *= -1
                }
        fun extendVector(a: Point, mag: Float): Point =
                Point(a.x * mag, a.y * mag)

        fun extendVector(a: Point, mag: Double): Point =
                Point(a.x * mag, a.y * mag)

        fun addVector(a: Point, b: Point): Point =
                Point(a.x + b.x, a.y + b.y)

        //取得中點
        fun getMidPoint(a: Point, b: Point): Point =
                Point((a.x + b.x) / 2, (a.y + b.y) / 2)

        //By 三角形面積公式(三點共線面積等於0)
        fun isSameLine(a: Point, b: Point, c: Point): Boolean =
                (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) == 0.0

        fun getLeftPart(pointList: ArrayList<Point>): List<Point> =
                pointList.subList(0, (pointList.size + 1) / 2)

        fun getRightPart(pointList: ArrayList<Point>): List<Point> =
                pointList.subList((pointList.size + 1) / 2, pointList.size)

        fun getConvexHull(pointList: ArrayList<Point>): List<Point> {
            //Convex Hull的頂點值
            val chIndex = arrayListOf<Int>()
            //要拜訪的點
            val nextPoint = arrayListOf<Int>()
            var noRotate = false //Grahm scan 用來判斷有沒有轉向
            var sameLine = true
            //從0號開始
            var startPoint = 0
            //CH頂點數目
            var vertexNumber = 1
            //加入起點
            chIndex.add(startPoint)
            pointList.forEachIndexed { index, _ ->
                nextPoint.add(index)
            }
            var nextPointIdx = nextPoint[1]
            while (true) {
                noRotate = false
                var temp = 0
                for (i in nextPoint) {
                    temp = nextPointIdx
                    //考慮向量[AB] 目前走到的點是AB某一點
                    if (i == nextPointIdx || i == chIndex[vertexNumber - 1]) continue
                    val crossProduct = getCrossProduct(
                            pointList[i],
                            pointList[nextPointIdx],
                            pointList[chIndex[vertexNumber - 1]]
                    )
                    when {
                        //順時針
                        crossProduct > 0 -> sameLine = false
                        //逆時針旋轉
                        crossProduct < 0 -> {
                            nextPointIdx = i
                            sameLine = false
                            noRotate = true
                        }
                        //共線，找最近的點，避開原點
                        crossProduct == 0.0 && checkNear(
                                pointList[i],
                                pointList[nextPointIdx],
                                pointList[chIndex[vertexNumber - 1]]
                        ) && i != nextPoint[0] -> {
                            temp = i
                        }
                    }
                }
                //沒有旋轉
                if (!noRotate) {
                    nextPointIdx = temp
                }
                if (nextPointIdx == startPoint) break
                chIndex.add(nextPointIdx)
                nextPoint.remove(nextPointIdx)
                nextPointIdx = if (nextPoint.size > 1) nextPoint[1] else nextPoint[0]
                ++vertexNumber
            }
            //全部共線
            if (sameLine) {
                for (i in chIndex.size - 2 downTo 1) {
                    chIndex.add(chIndex[i])
                }
            }
            return arrayListOf<Point>().apply {
                for (i in chIndex)
                    add(pointList[i])
            }
        }

        //回傳外積的純量
        private fun getCross(a: Point, b: Point): Double =
                a.x * b.y - a.y * b.x

        //取得交點
        fun getIntersection(a1: Point, a2: Point, b1: Point, b2: Point): Point? {
            val a = getVector(a1, a2)
            val b = getVector(b1, b2)
            val c = getVector(a1, b1)
            val cross = arrayListOf(getCross(a, b), getCross(c, b), getCross(c, a))
            if (cross[0] < 0.0) {
                cross[0] *= -1.0
                cross[1] *= -1.0
                cross[2] *= -1.0
            }
            if (cross[0] != 0.0 && cross[1] >= 0.0 && cross[1] <= cross[0] && cross[2] >= 0.0 && cross[2] <= cross[0]) {
                return addVector(
                        a1,
                        extendVector(a, cross[1].toFloat()/cross[0].toFloat())
                )
            }
            return null
        }

        fun getIntersection(edge1: Edge, edge2: Edge): Point? =
                getIntersection(edge1.pointA, edge1.pointB, edge2.pointA, edge2.pointB)

        //計算OAxOB(左上角為(0,0))
        //因左上角為(0,0)，因此若返回值>0 表示 OA到OB為逆時針(i.e. OB->OA為順時針)
        //外積只適用於三維平面，只需將z=0代入即可，因此公式為 x1y2 - y1x2
        fun getCrossProduct(a: Point, b: Point, origin: Point): Double {
            return (a.x - origin.x) * (b.y - origin.y) - (a.y - origin.y) * (b.x - origin.x)
        }
        //取得AB向量
        private fun getVector(a: Point, b: Point): Point =
                Point(b.x - a.x, b.y - a.y)

        //AB距離
        fun getDistance(a: Point, b: Point): Double =
                (a.x - b.x).pow(2) + (a.y - b.y).pow(2)

        //檢查OA有沒有比OB近
        private fun checkNear(a: Point, b: Point, origin: Point): Boolean =
                getDistance(a, origin) < getDistance(b, origin)
    }
}
