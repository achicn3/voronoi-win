package com.example.voronoi

// $LAN=KOTLIN$
data class Step(
    val pointList: ArrayList<Point>? = null,
    val edgeList: ArrayList<Edge>? = null,
    var type: Int? = null,
    var clear: Boolean? = null,
    var enable: Boolean? = true
) {
    constructor(edgeList: ArrayList<Edge>?, type: Int?, clear: Boolean? = false) : this(
        null,
        edgeList = kotlin.run {
            arrayListOf<Edge>().apply {
                edgeList?.forEach { edge ->
                    add(Edge(edge.pointA, edge.pointB, edge.perA, edge.perB))
                }
            }
        },
        type = type,
        clear = clear
    )

    constructor(vDiagram: VDiagram, clear: Boolean? = false) : this(
        pointList = vDiagram.pointList,
        edgeList = vDiagram.voronoiList,
        type = Utils.TYPE_VORONOI,
        clear = clear
    )

    //判斷兩個step是否相等
    override fun equals(other: Any?): Boolean {
        val t: Step = other as Step? ?: return false
        edgeList?.let {
            it.forEachIndexed { index, edge ->
                t.edgeList?.get(index)?.equals(edge)?.let { isEqual ->
                    if (!isEqual) return false
                }
            }
        }
        return type == t.type
    }

    override fun toString(): String {
        return pointList?.fold("", { acc: String, point: Point ->
            acc + "P ${point.x} ${point.y}\n"
        }) ?: ""
    }
}