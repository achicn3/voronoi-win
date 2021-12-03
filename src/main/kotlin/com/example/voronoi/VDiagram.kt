package com.example.voronoi

// $LAN=KOTLIN$
data class VDiagram(
        val pointList: ArrayList<Point> = arrayListOf(),
        val hyperPlaneList: ArrayList<Edge> = arrayListOf(),
        val voronoiList: ArrayList<Edge> = arrayListOf()
)