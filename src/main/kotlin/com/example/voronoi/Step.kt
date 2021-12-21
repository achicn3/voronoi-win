package com.example.voronoi

import com.example.voronoi.Utils.TYPE_CONVEX_HULL
import com.example.voronoi.Utils.TYPE_HYPER_LINE
import com.example.voronoi.Utils.TYPE_MERGE_VORONOI
import com.example.voronoi.Utils.TYPE_POINT
import com.example.voronoi.Utils.TYPE_TANGENT
import com.example.voronoi.Utils.TYPE_VORONOI

// $LAN=KOTLIN$
data class Step(
        val pointList: ArrayList<Point>? = null,
        val edgeList: ArrayList<Edge>? = null,
        var type: Int? = null,
        var clear: Boolean? = false,
        var enable: Boolean? = true
) {

    constructor(pointList: ArrayList<Point>, type: Int?, clear: Boolean? = false) : this(
            pointList = kotlin.run {
                arrayListOf<Point>().apply {
                    addAll(pointList)
                }
            },
            edgeList = null,
            type = type,
            clear = clear,
            enable = true
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
        val p = pointList?.fold("") { acc: String, point: Point ->
            acc + point
        }
        val e = edgeList?.fold(""){acc, edge ->
            acc+edge.toString()
        }
        val type = when(type){
            TYPE_CONVEX_HULL-> "CONVEX HULL"
            TYPE_HYPER_LINE->"HYPER PLANE"
            TYPE_VORONOI->"VORONOI"
            TYPE_TANGENT->"TANGENT"
            TYPE_MERGE_VORONOI->"MERGE VORONOI"
            TYPE_POINT->"POINT"
            else->"not set"
        }

        return "----edge:$e\npoint:$p\ntype:$type\nenable:$enable clear:$clear----\n\n"
    }
}