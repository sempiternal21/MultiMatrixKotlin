import java.io.FileWriter
import java.io.IOException
import java.util.*

object Main {
    private fun randomMatrix(matrix: Array<IntArray>) {
        val random = Random()
        for (row in matrix.indices) for (col in matrix[row].indices) matrix[row][col] = random.nextInt(100)
    }

    @Throws(IOException::class)
    private fun printMatrix(
        fileWriter: FileWriter,
        matrix: Array<IntArray>
    ) {
        var hasNegative = false
        var maxValue = 0
        for (row in matrix) {
            for (element in row) {
                var temp = element
                if (element < 0) {
                    hasNegative = true
                    temp = -temp
                }
                if (temp > maxValue) maxValue = temp
            }
        }
        var len = maxValue.toString().length + 1
        if (hasNegative) ++len
        val formatString = "%" + len + "d"
        for (row in matrix) {
            for (element in row)
                fileWriter.write(String.format(formatString, element))
            fileWriter.write("\n")
        }
    }

    private fun printAllMatrix(
        fileName: String,
        firstMatrix: Array<IntArray>,
        secondMatrix: Array<IntArray>,
        resultMatrix: Array<IntArray>
    ) {
        try {
            FileWriter(fileName, false).use { fileWriter ->
                fileWriter.write("First matrix:\n")
                printMatrix(fileWriter, firstMatrix)
                fileWriter.write("\nSecond matrix:\n")
                printMatrix(fileWriter, secondMatrix)
                fileWriter.write("\nResult matrix:\n")
                printMatrix(fileWriter, resultMatrix)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun multiplyMatrix(
        firstMatrix: Array<IntArray>,
        secondMatrix: Array<IntArray>
    ): Array<IntArray> {
        val rowCount = firstMatrix.size
        val colCount: Int = secondMatrix[0].size
        val sumLength = secondMatrix.size
        val result = Array(rowCount) {
            IntArray(
                colCount
            )
        }
        for (row in 0 until rowCount) {
            for (col in 0 until colCount) {
                var sum = 0
                for (i in 0 until sumLength) sum += firstMatrix[row][i] * secondMatrix[i][col]
                result[row][col] = sum
            }
        }
        return result
    }

    private fun multiplyMatrixMT(
        firstMatrix: Array<IntArray>,
        secondMatrix: Array<IntArray>,
        threadCount: Int
    ): Array<IntArray> {
        assert(threadCount > 0)
        val rowCount = firstMatrix.size
        val colCount: Int = secondMatrix[0].size
        val result = Array(rowCount) {
            IntArray(
                colCount
            )
        }
        val cellsForThread = rowCount * colCount / threadCount
        var firstIndex = 0
        val multiplierThreads = arrayOfNulls<MultiplierThread>(threadCount) // Массив потоков.

        for (threadIndex in threadCount - 1 downTo 0) {
            var lastIndex = firstIndex + cellsForThread
            if (threadIndex == 0) {
                lastIndex = rowCount * colCount
            }
            multiplierThreads[threadIndex] = MultiplierThread(firstMatrix, secondMatrix, result, firstIndex, lastIndex)
            multiplierThreads[threadIndex]!!.start()
            firstIndex = lastIndex
        }

        try {
            for (multiplierThread in multiplierThreads) multiplierThread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return result
    }

    private const val FIRST_MATRIX_ROWS = 4
    private const val FIRST_MATRIX_COLS = 4
    private const val SECOND_MATRIX_ROWS = FIRST_MATRIX_COLS
    private const val SECOND_MATRIX_COLS = 4
    @JvmStatic
    fun main(args: Array<String>) {
        val firstMatrix = Array(FIRST_MATRIX_ROWS) {
            IntArray(
                FIRST_MATRIX_COLS
            )
        }
        val secondMatrix = Array(SECOND_MATRIX_ROWS) {
            IntArray(
                SECOND_MATRIX_COLS
            )
        }
        randomMatrix(firstMatrix)
        randomMatrix(secondMatrix)
        val resultMatrixMT = multiplyMatrixMT(firstMatrix, secondMatrix, Runtime.getRuntime().availableProcessors())

        val resultMatrix = multiplyMatrix(firstMatrix, secondMatrix)
        for (row in 0 until FIRST_MATRIX_ROWS) {
            for (col in 0 until SECOND_MATRIX_COLS) {
                if (resultMatrixMT[row][col] != resultMatrix[row][col]) {
                    println("Error in multithreaded calculation!")
                    return
                }
            }
        }
        printAllMatrix("Matrix.txt", firstMatrix, secondMatrix, resultMatrixMT)
    }
}