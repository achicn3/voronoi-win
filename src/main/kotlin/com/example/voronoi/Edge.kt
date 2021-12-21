package com.example.voronoi

// $LAN=KOTLIN$
data class Edge(
        var pointA: Point,
        var pointB: Point,
        var perA: Point?, //中垂線的點A
        var perB: Point?
) {
    constructor(x1: Double, y1: Double, x2: Double, y2: Double) : this(
            Point(x1, y1),
            Point(x2, y2),
            null,
            null
    )

    constructor(pointA: Point, pointB: Point) : this(pointA, pointB, null, null)
    constructor() : this(
            Point(0.0,0.0),
            Point(0.0,0.0),
            null, null
    )

    override fun toString(): String {
        return "E ${pointA.x} ${pointA.y} ${pointB.x} ${pointB.y}"
    }

    companion object {
        //先取得 線段的中點
        //並計算法向量
        fun getPerpendicular(edge: Edge): Edge =
                Point.getMidPoint(edge.pointA, edge.pointB).let { mid ->
                    return Edge(
                            Point.addVector(
                                    mid,
                                    Point.extendVector(Point.getNormalVector(edge.pointA, edge.pointB), 600.0)
                            ),
                            Point.addVector(
                                    mid,
                                    Point.extendVector(Point.getNormalVector(edge.pointB, edge.pointA), 600.0)
                            ),
                            edge.pointA,
                            edge.pointB
                    )
                }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Edge) return false
        return (pointA == other.pointA && pointB == other.pointB) || (pointA == other.pointB && pointB == other.pointA)

    }
}
