package minesweeper

import java.util.*
import kotlin.math.sqrt

fun main() {
    print("How many mines do you want on the field? ")
    val numberOfMines = readln().toInt()
    val fieldSize = 81
    val field = Field(fieldSize, numberOfMines)
    field.printField()
    var isGameFinished = false
    var gameStatus = false
    while (!isGameFinished) {
        print("Set/unset mines marks or claim a cell as free: ")
        val input = readln().split(" ")
        val x = input[0].toInt()
        val y = input[1].toInt()
        val operation = input[2]
        try {

            field.operate(operation, y - 1, x - 1)
            field.printField()
            isGameFinished = field.checkIfGameFinished()
            if(isGameFinished) gameStatus = true
        }catch (e: Exception) {
            field.printField()
            println(e.message)
            isGameFinished = true
        }

    }
    if (gameStatus) println("Congratulations! You found all the mines!")



}


class Field(val fieldSize: Int, val numberOfMines: Int){
    val field: MutableList<MutableList<Cell>>
    init {
        field = buildField(fieldSize, numberOfMines)
    }


    fun buildField(fieldSize: Int, numberOfMines: Int): MutableList<MutableList<Cell>> {
        var field = initializeField(fieldSize)
        field = placeMines(field, numberOfMines)
        field = placeHints(field)
        return field
    }
    fun placeMines(field : MutableList<MutableList<Cell>>, numberOfMines: Int): MutableList<MutableList<Cell>> {
        val randomGenerator = Random()
        var placedMines = 0
        while (placedMines < numberOfMines) {
            val rowIndex = randomGenerator.nextInt(field.size)
            val columnIndex = randomGenerator.nextInt(field.size)
            if (field[rowIndex][columnIndex].type == CellType.NORMAL){
                field[rowIndex][columnIndex].type = CellType.MINE
                placedMines++
            }
        }
        return field
    }
    fun initializeField(fieldSize: Int): MutableList<MutableList<Cell>> {
        val rowSize = sqrt(fieldSize.toDouble()).toInt()
        val field = mutableListOf<MutableList<Cell>>()
        repeat(rowSize) {
            val temp = mutableListOf<Cell>()
            repeat(rowSize) {
                temp.add(Cell(CellType.NORMAL))
            }
            field.add(temp)
        }
        return field
    }
    fun placeHints(field : MutableList<MutableList<Cell>>): MutableList<MutableList<Cell>> {
        for (i in field.indices) {
            for (j in field[i].indices) {
                if(field[i][j].type == CellType.MINE) continue
                val minesNearby = calculateNearbyMines(field, i, j)
                if (minesNearby > 0){
                    field[i][j].type = CellType.NUMBER
                    field[i][j].number = minesNearby
                }
            }
        }
        return field
    }
    fun calculateNearbyMines(field: MutableList<MutableList<Cell>>, i: Int, j: Int): Int {
        var count = 0
        count += if (checkMinePoint(field,i,j-1)) 1 else 0
        count += if (checkMinePoint(field,i-1,j-1)) 1 else 0
        count += if (checkMinePoint(field,i+1,j-1)) 1 else 0
        count += if (checkMinePoint(field,i-1,j)) 1 else 0
        count += if (checkMinePoint(field,i+1,j)) 1 else 0
        count += if (checkMinePoint(field,i,j+1)) 1 else 0
        count += if (checkMinePoint(field,i-1,j+1)) 1 else 0
        count += if (checkMinePoint(field,i+1,j+1)) 1 else 0
        return count
    }
    fun checkMinePoint(field: MutableList<MutableList<Cell>>, i: Int, j: Int): Boolean{
        return if (i >= 0 && i < field.size && j >= 0 && j < field.size) field[i][j].type == CellType.MINE else false
    }
    fun printField() {
        println(" │123456789│")
        println("—│—————————│")
        for (i in 0..<field.size) {
            println("${i + 1}│${field[i].joinToString("")}│")
        }
        println("—│—————————│")
    }
    fun checkPoint(field: MutableList<MutableList<Cell>>, i: Int, j: Int): Boolean{
        return field[i][j].type == CellType.MINE || field[i][j].type == CellType.NORMAL
    }
    fun checkIfAllNonMinesOpened(): Boolean{
        for (i in field.indices) {
            for (j in field[i].indices) {
                if ( (field[i][j].type != CellType.MINE && !field[i][j].isOpened)) return false
            }
        }
        return true
    }
    fun checkIfAllMinesMarked(): Boolean{
        for (i in field.indices) {
            for (j in field[i].indices) {
                if ( (field[i][j].type == CellType.MINE && !field[i][j].isMarked) || (field[i][j].type != CellType.MINE && field[i][j].isMarked) ) return false
            }
        }
        return true
    }
    fun checkIfGameFinished(): Boolean {
        return checkIfAllNonMinesOpened() || checkIfAllMinesMarked()
    }
    fun markPoint(i: Int, j: Int){
        field[i][j].isMarked = !field[i][j].isMarked
    }
    fun openPoint(i: Int, j: Int){
        if ( !(i >= 0 && i < field.size && j >= 0 && j < field.size) ) return
        if (field[i][j].isOpened) return
        field[i][j].isOpened = true
        if (field[i][j].type == CellType.NORMAL){
            openPoint(i, j - 1)
            openPoint(i-1, j - 1)
            openPoint(i + 1, j - 1)
            openPoint(i - 1, j)
            openPoint(i + 1, j)
            openPoint(i, j + 1)
            openPoint(i - 1, j + 1)
            openPoint(i + 1, j + 1)
        }
    }
    fun operate(operation: String, i: Int, j: Int){
        when(operation){
            "free" -> openPoint(i, j)
            "mine" -> markPoint(i, j)
        }
    }
}



class Cell(var type: CellType){
    var isMarked: Boolean = false
        set(value) {
            field = value
        }
    var number: Int = 0
        set(value) {
            field = value
        }
    var isOpened: Boolean = false
        set(value) {
            field = value
            if (type == CellType.MINE){throw IllegalStateException("You stepped on a mine and failed!")}
        }

    override fun toString() : String{
        return when(type){
            CellType.MINE -> if (isOpened) "X" else {if(isMarked) "*" else "."}
            CellType.NUMBER -> if (isOpened) "$number" else {if(isMarked) "*" else "."}
            CellType.NORMAL -> if (isOpened) "/" else {if(isMarked) "*" else "."}
        }
    }
}

enum class CellType{
    NORMAL,
    MINE,
    NUMBER;
}