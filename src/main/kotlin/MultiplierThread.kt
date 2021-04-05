internal class MultiplierThread(
    private val firstMatrix: Array<IntArray>,
    private val secondMatrix: Array<IntArray>,
    private val resultMatrix: Array<IntArray>,
    private val firstIndex: Int,
    private val lastIndex: Int
) : Thread() {
    private val sumLength: Int = secondMatrix.size
    private fun calcValue(row: Int, col: Int) {
        var sum = 0
        for (i in 0 until sumLength) sum += firstMatrix[row][i] * secondMatrix[i][col]
        resultMatrix[row][col] = sum
    }

    override fun run() {
        val colCount: Int = secondMatrix[0].size
        for (index in firstIndex until lastIndex) calcValue(index / colCount, index % colCount)
    }

}